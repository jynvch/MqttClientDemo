package com.mj.demo;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.mj.demo.business.MqttService;
import com.mj.demo.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class MqttClientDemoApplication implements CommandLineRunner {
    @Autowired
    private MqttService mqttService;

    @Autowired
    private MqttConfig mqttConfig;

    public static void main(String[] args) {
        SpringApplication.run(MqttClientDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 启动时订阅默认主题
        subscribeDefaultTopic();
        // 示例：发送欢迎消息
        sendWelcomeMessage();
    }

    private void subscribeDefaultTopic() throws MqttException {
        String defaultTopic = mqttConfig.getDefaultTopic(); // 可从配置读取
        mqttService.subscribe(defaultTopic, 1);
    }

    private void sendWelcomeMessage() throws MqttException {
        String topic = "spBv1.0/alcohol/NDATA/STN66_01";
        Map params = Maps.newHashMap();
        params.put("message", "你的大哥已经上线了。(MQTT客户端已启动!)");
        String message = JSON.toJSONString(params);
        mqttService.publish(topic, message, 1, false);
    }
}
