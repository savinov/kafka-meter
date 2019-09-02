
package com.novomatic.jmeter.kafka.sampler;

import com.novomatic.jmeter.kafka.ProducerConstants;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaSampler extends AbstractSampler implements ThreadListener, Interruptible {

    private static final ThreadLocal<KafkaProducer<String, Object>> threadLocal = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(KafkaSampler.class);

    private transient volatile KafkaProducer<String, Object> currentProducer; // used for handling interrupt

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setSampleLabel(String.format("%s #%d", getName(), getThreadId()));
        sampleResult.sampleStart();

        try {
            Optional<String> messageKey = getMessageKey();
            String message = getMessage();
            sampleResult.setSamplerData(message);
            String topic = getTopic();
            ProducerRecord producerRecord = messageKey
                    .map(s -> new ProducerRecord<>(topic, s, message))
                    .orElseGet(() -> new ProducerRecord<>(topic, message));
            KafkaProducer<String, Object> kafkaProducer = getKafkaProducer();
            currentProducer = kafkaProducer;
            Future<RecordMetadata> response = kafkaProducer.send(producerRecord);
            RecordMetadata recordMetadata = response.get();
            sampleResult.setResponseData(recordMetadata.toString(), StandardCharsets.UTF_8.name());
            sampleResult.setResponseCodeOK();
            sampleResult.setSuccessful(true);
            sampleResult.sampleEnd();
        } catch (Exception e) {
            log.error("Failed to send message", e);
            sampleResult.setResponseData(e.getMessage() == null ? e.toString() : e.getMessage(), StandardCharsets.UTF_8.name());
            sampleResult.setSuccessful(false);
            sampleResult.sampleEnd();
        } finally {
            currentProducer = null;
        }

        return sampleResult;
    }

    @Override
    public void threadStarted() {
    }

    @Override
    public boolean interrupt() {
        Optional<KafkaProducer> producer = Optional.ofNullable(currentProducer); // fetch in case gets nulled later
        if (producer.isPresent()) {
            producer.get().close();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void threadFinished() {
        KafkaProducer<String, Object> kafkaProducer = threadLocal.get();
        if (kafkaProducer != null) {
            log.info("Closing kafka producer.");
            threadLocal.remove();
            kafkaProducer.close();
        }
    }

    public Properties getProducerProperties() {
        Properties props = new Properties();
        Arguments arguments = (Arguments) getProperty(ProducerConstants.PROPERTIES).getObjectValue();
        arguments.getArgumentsAsMap().forEach(props::put);
        return props;
    }

    public String getTopic() {
        return getPropertyAsString(ProducerConstants.TOPIC);
    }

    public String getMessage() {
        return getPropertyAsString(ProducerConstants.MESSAGE);
    }

    public Optional<String> getMessageKey() {
        return Optional.ofNullable(getPropertyAsString(ProducerConstants.KEY));
    }

    private KafkaProducer<String, Object> getKafkaProducer() {
        KafkaProducer<String, Object> kafkaProducer = threadLocal.get();
        if (kafkaProducer == null) {
            log.info("Creating kafka producer.");
            kafkaProducer = createKafkaProducer();
            threadLocal.set(kafkaProducer);
        }
        return kafkaProducer;
    }

    private int getThreadId() {
        return getThreadContext().getThreadNum();
    }

    private KafkaProducer<String, Object> createKafkaProducer() {
        Properties producerProperties = getProducerProperties();
        return new KafkaProducer<>(producerProperties);
    }
}
