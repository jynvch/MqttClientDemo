package com.mj.demo.config;

import lombok.Data;
import lombok.Getter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mqtt配置类
 */
@Getter
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.clientId:spring-boot-client}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.cleanSession:true}")
    private boolean cleanSession;

    @Value("${mqtt.connectionTimeout:30}")
    private int connectionTimeout;

    @Value("${mqtt.keepAliveInterval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.defaultTopic}")
    private String defaultTopic;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(true);  // 启用自动重连
        return options;
    }

    @Bean
    public MqttClient mqttClient(MqttConnectOptions options) throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        client.connect(options);
        return client;
    }
}