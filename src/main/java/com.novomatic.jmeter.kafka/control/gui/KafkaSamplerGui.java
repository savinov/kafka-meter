package com.novomatic.jmeter.kafka.control.gui;

import com.novomatic.jmeter.kafka.config.KafkaProducerConfig;
import com.novomatic.jmeter.kafka.config.gui.KafkaProducerConfigGui;
import com.novomatic.jmeter.kafka.sampler.KafkaSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.*;

public class KafkaSamplerGui extends AbstractSamplerGui {

    private KafkaProducerConfigGui kafkaSamplerPanel;

    public KafkaSamplerGui() {
        super();
        init();
    }

    @Override
    public String getStaticLabel() {
        return "Kafka Sampler";
    }

    @Override
    public String getLabelResource() {
        return "kafka_sampler_title";
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        kafkaSamplerPanel = new KafkaProducerConfigGui(false);

        add(kafkaSamplerPanel, BorderLayout.CENTER);
    }

    @Override
    public TestElement createTestElement() {
        KafkaSampler kafkaSampler = new KafkaSampler();
        modifyTestElement(kafkaSampler);
        return kafkaSampler;
    }

    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        KafkaProducerConfig config = (KafkaProducerConfig) kafkaSamplerPanel.createTestElement();
        configureTestElement(sampler);
        sampler.addTestElement(config);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        kafkaSamplerPanel.configure(el);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        kafkaSamplerPanel.clearGui();
    }
}
