package com.novomatic.jmeter.kafka.config.gui;

import com.novomatic.jmeter.kafka.ProducerConstants;
import com.novomatic.jmeter.kafka.config.KafkaProducerConfig;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.swing.*;
import java.awt.*;

public class KafkaProducerConfigGui extends AbstractConfigGui {

    private final boolean displayName;

    private final Arguments defaultArgs = new Arguments();

    private ArgumentsPanel propPanel;

    private JTextField topicTextField;

    public KafkaProducerConfigGui(){
        this(true);
    }

    public KafkaProducerConfigGui(boolean displayName) {
        this.displayName = displayName;
        defaultArgs.addArgument(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        defaultArgs.addArgument(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        defaultArgs.addArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        init();
    }

    @Override
    public String getStaticLabel() {
        return "Kafka Producer Config";
    }

    @Override
    public String getLabelResource() {
        return "kafka_producer_config_title";
    }

    @Override
    public TestElement createTestElement() {
        KafkaProducerConfig config = new KafkaProducerConfig();
        modifyTestElement(config);
        return config;
    }

    @Override
    public void configure(TestElement config) {
        super.configure(config);
        propPanel.configure((Arguments) config.getProperty(ProducerConstants.PROPERTIES).getObjectValue());
        topicTextField.setText(config.getProperty(ProducerConstants.TOPIC).getStringValue());
    }

    @Override
    public void modifyTestElement(TestElement config) {
        configureTestElement(config);
        KafkaProducerConfig kafkaProducerConfig = ((KafkaProducerConfig) config);
        kafkaProducerConfig.setProperties((Arguments) propPanel.createTestElement());
        kafkaProducerConfig.setTopic(topicTextField.getText());
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createTopicNamePanel());
        mainPanel.add(createPropertiesPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTopicNamePanel() {
        JLabel label = new JLabel("Topic Name");
        topicTextField = new JTextField();
        label.setLabelFor(topicTextField);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(topicTextField, BorderLayout.CENTER);
        return panel;
    }

    private ArgumentsPanel createPropertiesPanel() {
        propPanel = new ArgumentsPanel("Kafka Producer Properties",null, true, false);
        propPanel.configure(defaultArgs);
        return propPanel;
    }
}
