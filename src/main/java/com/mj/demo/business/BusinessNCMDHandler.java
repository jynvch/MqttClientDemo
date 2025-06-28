package com.mj.demo.business;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.mj.demo.util.SpringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessNCMDHandler {
    //默认分配的原始样品盘架和位置关联信息
    List<RackAreaInfo> pwjRackAreaInfoList = Lists.newArrayList();
    //耗材盘
    List<RackAreaInfo> psbRackAreaInfoList = Lists.newArrayList();
    //默认其实标识
    AtomicInteger pwjStart = new AtomicInteger(0);
    AtomicInteger psbStart = new AtomicInteger(0);

    {
        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0010").area("A101").build());
        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0011").area("A102").build());
        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0011").area("A103").build());
        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0011").area("A104").build());
        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0011").area("A105").build());

        //默认4个
        psbRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PSB02N008S0001").area("B101").build());
        psbRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PSB02N008S0002").area("B102").build());
        psbRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PSB02N008S0003").area("B103").build());
        psbRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PSB02N008S0004").area("B104").build());
        psbRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PSB02N008S0004").area("B105").build());
        psbRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PSB02N008S0004").area("B106").build());

    }

    public RackAreaInfo getByRackType(String rackType) {
        RackAreaInfo result = null;
        if (rackType == null) {
            return null;
        }
        if (rackType.equals("PWJ02")) {
            //原始样盘
            result = getOne(pwjRackAreaInfoList, pwjStart);
        } else if (rackType.equals("PSB02")) {
            //耗材盘
            result = getOne(psbRackAreaInfoList, psbStart);
        }
        return result;
    }

    public RackAreaInfo getOne(List<RackAreaInfo> list, AtomicInteger startValue) {
        if (startValue.get() < list.size() - 1) {
            int start = startValue.incrementAndGet();
            return list.get(start);
        } else {
            //循环获取
            startValue.set(0);
            return list.get(0);
        }
    }

    public void processMessage(String topic, String payload) {
        MqttService mqttService = SpringUtils.getBean(MqttService.class);

        List<Integer> dealSequenceList = Lists.newArrayList(305, 306);
        try {
            //NCMD解析
            Map msg = JSON.parseObject(payload, Map.class);
            String strMethod = msg.get("strMethod").toString();
            String strID = msg.get("strID") + "";
            log.info("NCMD解析-------------strMethod:{},strID:{}", strMethod, strID);
            Map body = JSON.parseObject(msg.get("body").toString(), Map.class);

            String ndataTopic = topic.replace("NCMD", "NDATA");
            if ("Place".equals(strMethod)) {
                //iray下发询问位置的时候返回
                String publishMsg = "{\"services\":[{\"eventTime\":\"20250319T121212Z\",\"eventParams\":{\"strMethod\":\"Place\",\"" +
                        "strID\":\"" + strID + "\",\"body\":{\"state\":0}},\"eventType\":\"reply\"}]}";

                //1秒后回复
                Thread.sleep(1000);
                mqttService.publish(ndataTopic, publishMsg);
            } else if ("AllocationArea2".equals(strMethod)) {
                Integer action = (Integer) body.get("action");
                if (action == 2) {
                    Integer sequenceId = (Integer) body.get("sequenceId");
                    String rackCode = (String) body.get("rackCode");
                    RackAreaInfo byRackType = null;
                    if (rackCode != null) {
                        //原始样品申请库位
                        log.info("------------原始样品申请库位 rackCode:{}", rackCode);
                        byRackType = getByRackType(rackCode.substring(0, 5));
                    } else {
                        //耗材盘架申请库位
                        String rackType = (String) body.get("rackType");
                        log.info("------------耗材盘架申请库位 rackType:{}", rackType);
                        if (rackType != null) {
                            byRackType = getByRackType(rackType);
                        }
                    }

                    //对需要处理的序列进行上报，其他序列不处理
                    if (dealSequenceList.contains(sequenceId)) {
                        if (byRackType == null) {
                            log.error("----------------不对库位询问进行上报-没有找到此盘架类型,序列:{}", sequenceId);
                        }
                        String allocationAreaMsg = "{\"services\":[{\"eventTime\":\"20250611T091212Z\",\"eventParams\":{\"strID\":\"" + strID + "\"," +
                                "\"strMethod\":\"AllocationArea2\",\"body\":{\"state\":0,\"rackCode\":\"" + byRackType.getRackCode() + "\"," +
                                "\"pointArea\":\"" + byRackType.getArea() + "\",\"targetArea\":\"" + byRackType.getArea() + "\"}},\"eventType\":\"reply\"}]}";
                        mqttService.publish(ndataTopic, allocationAreaMsg);
                    }
                }
            } else if ("RackMove".equals(strMethod)) {
                Integer action = (Integer) body.get("action");
                if (action == 2) {
                    String rackCode = (String) body.get("rackCode");
                    String pointArea = (String) body.get("pointArea");
                    String rackMoveMsg = "{\"services\":[{\"eventType\":\"reply\",\"eventTime\":\"20250627T121212Z\",\"eventParams\":" +
                            "{\"strID\":\"" + strID + "\",\"strMethod\":\"RackMove\",\"body\":{\"actionType\":4,\"state\":0," +
                            "\"rackCode\":\"" + rackCode + "\",\"fromAreaCode\":\"" + pointArea + "\",\"targetAreaCode\":\"" + pointArea + "\"}}}]}";
                    mqttService.publish(ndataTopic, rackMoveMsg);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 内部静态类
     */
    @Data
    @Builder
    @AllArgsConstructor
    static class RackAreaInfo {
        private String rackCode;
        private String area;
    }
}
