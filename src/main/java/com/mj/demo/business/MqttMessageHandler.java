package com.mj.demo.business;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MqttMessageHandler implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
    private final BusinessNCMDHandler businessNCMDHandler;

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("MQTT连接丢失，正在尝试重连...", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        logger.info("收到消息 [主题: {}] [QoS: {}]: {}", topic, message.getQos(), payload);
//        try {
//            //NDATA的解析
//            Map map = JSON.parseObject(JSON.parseObject(payload).getJSONArray("services").toList(Map.class).get(0).get("eventParams").toString(), Map.class);
//            String strMethod = (String) map.get("strMethod");
//            System.out.printf("NDATA的解析-----------strMethod:" + strMethod);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            //NCMD解析
//            Map msg = JSON.parseObject(payload, Map.class);
//            logger.info("NCMD解析-------------strMethod:{},strID:{}", msg.get("strMethod"), msg.get("strID"));
//
//            String publishMsg ="{\"services\":[{\"eventTime\":\"20250319T121212Z\",\"eventParams\":{\"strMethod\":\"Place\",\"" +
//                    "strID\":\""+msg.get("strID")+"\",\"body\":{\"state\":0}},\"eventType\":\"reply\"}]}";
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        // 在这里添加你的业务处理逻辑
        businessNCMDHandler.processMessage(topic, payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("消息投递完成: {}", token.getMessageId());
    }

}