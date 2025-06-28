package com.mj.demo.business;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttService {

    private final MqttClient mqttClient;
    private final MqttMessageHandler messageHandler;

    public MqttService(MqttClient mqttClient, MqttMessageHandler messageHandler) {
        this.mqttClient = mqttClient;
        this.messageHandler = messageHandler;
        mqttClient.setCallback(messageHandler);
    }

    /**
     * 发布消息
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      服务质量 (0,1,2)
     * @param retained 是否保留消息
     */
    public void publish(String topic, String payload, int qos, boolean retained) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        message.setRetained(retained);
        mqttClient.publish(topic, message);
    }

    public void publish(String topic, String payload) throws MqttException {
        log.info("publish 上报给服务器-------topic:{},payload:{}", topic, payload);
        publish(topic, payload, 2, false);
    }

    /**
     * 订阅主题
     *
     * @param topic 主题 (支持通配符 #/+)
     * @param qos   服务质量
     */
    public void subscribe(String topic, int qos) throws MqttException {
        mqttClient.subscribe(topic, qos);
        System.out.println("已订阅主题: " + topic);
    }

    /**
     * 断开连接
     */
    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

    /**
     * 重新连接
     */
    public void reconnect() throws MqttException {
        if (!mqttClient.isConnected()) {
            mqttClient.reconnect();
        }
    }
}