package com.novomatic.jmeter.kafka.config;

import com.novomatic.jmeter.kafka.ProducerConstants;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.Serializable;

public class KafkaProducerConfig extends ConfigTestElement implements Serializable {

    public KafkaProducerConfig() {
        setProperties(new Arguments());
    }

    public void setProperties(Arguments properties) {
        setProperty(new TestElementProperty(ProducerConstants.PROPERTIES, properties));
    }

    public void setTopic(String topic) {
        if(!topic.isEmpty()){
            setProperty(ProducerConstants.TOPIC, topic);
        }
    }
}
