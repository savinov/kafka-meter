package com.novomatic.jmeter.kafka.config.gui;

import com.novomatic.jmeter.kafka.ProducerConstants;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class KafkaMessageGui extends AbstractConfigGui {

    private JSyntaxTextArea jsonTextArea;

    private JTextField messageKeyTextField;

    public KafkaMessageGui() {
        init();
    }

    @Override
    public String getStaticLabel() {
        return "Kafka Message";
    }

    @Override
    public String getLabelResource() {
        return "kafka_message_title";
    }

    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        jsonTextArea.setText(element.getProperty(ProducerConstants.MESSAGE).getStringValue());
        messageKeyTextField.setText(element.getProperty(ProducerConstants.KEY).getStringValue());
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (!jsonTextArea.getText().isEmpty()) {
            element.setProperty(ProducerConstants.MESSAGE, jsonTextArea.getText().trim());
        }
        if (!messageKeyTextField.getText().isEmpty()) {
            element.setProperty(ProducerConstants.KEY, messageKeyTextField.getText().trim());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        jsonTextArea.setText("");
        messageKeyTextField.setText("");
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));

        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createMessageKeyPanel());
        mainPanel.add(createJsonPanel());
        add(mainPanel);
    }

    private JPanel createMessageKeyPanel() {
        JLabel label = new JLabel("Message Key");
        messageKeyTextField = new JTextField();
        label.setLabelFor(messageKeyTextField);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(messageKeyTextField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createJsonPanel() {
        jsonTextArea = JSyntaxTextArea.getInstance(20, 20);
        JLabel label = new JLabel("Message");
        label.setLabelFor(jsonTextArea);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(JTextScrollPane.getInstance(jsonTextArea), BorderLayout.CENTER);
        return panel;
    }
}
