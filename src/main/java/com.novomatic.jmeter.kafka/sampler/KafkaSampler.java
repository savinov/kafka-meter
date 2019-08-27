
package com.novomatic.jmeter.kafka.sampler;

import com.novomatic.jmeter.kafka.ProducerConstants;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
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


public class KafkaSampler extends AbstractSampler implements ThreadListener {

    private static final ThreadLocal<KafkaProducer<String, Object>> threadLocal = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(KafkaSampler.class);

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setSampleLabel(getName());
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
            Future<RecordMetadata> sendFuture = kafkaProducer.send(producerRecord);
            RecordMetadata result = sendFuture.get();
            sampleResult.setResponseData(result.toString(), StandardCharsets.UTF_8.name());
            sampleResult.setResponseCodeOK();
            sampleResult.setSuccessful(true);
            sampleResult.sampleEnd();

        } catch (Exception e) {
            log.error("Failed to send message", e);
            sampleResult.setResponseData(e.getMessage(), StandardCharsets.UTF_8.name());
            sampleResult.setSuccessful(false);
            sampleResult.sampleEnd();
        }

        return sampleResult;
    }

    @Override
    public void threadStarted() {
    }

    @Override
    public void threadFinished() {
        KafkaProducer<String, Object> kafkaProducer = threadLocal.get();
        if (kafkaProducer != null) {
            log.info("Closing kafka producer. Thread: " + Thread.currentThread().getId());
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
            log.info("Creating kafka producer. Thread: " + Thread.currentThread().getId());
            kafkaProducer = createKafkaProducer();
            threadLocal.set(kafkaProducer);
        }
        return kafkaProducer;
    }

    private KafkaProducer<String, Object> createKafkaProducer() {
        Properties producerProperties = getProducerProperties();
        return new KafkaProducer<>(producerProperties);
    }
}
