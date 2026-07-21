package com.mj.demo.business;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.mj.demo.util.SpringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessNCMDHandler {
    //默认分配的原始样品盘架和位置关联信息
    //原始样品 溶样瓶
    List<RackAreaInfo> pyp10OriginalList = Lists.newArrayList();
    //耗材盘 溶样瓶
    List<RackAreaInfo> pyp10List = Lists.newArrayList();
    //耗材盘 色谱瓶
    List<RackAreaInfo> pspList = Lists.newArrayList();
    //耗材盘 色谱瓶盖
    List<RackAreaInfo> ppgList = Lists.newArrayList();


    //默认其实标识
    AtomicInteger pyp10Start1 = new AtomicInteger(0);
    AtomicInteger pyp10Start2 = new AtomicInteger(0);

    AtomicInteger pspStart = new AtomicInteger(0);
    AtomicInteger ppgStart = new AtomicInteger(0);

    {
        pyp10OriginalList.add(RackAreaInfo.builder().rackCode("PYP10N032S0001").area("A101").build());
        pyp10OriginalList.add(RackAreaInfo.builder().rackCode("PYP10N032S0002").area("A102").build());
        pyp10OriginalList.add(RackAreaInfo.builder().rackCode("PYP10N032S0003").area("A103").build());
//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0004").area("A104").build());
//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0005").area("A105").build());


//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0010").area("A101").build());
//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0011").area("A102").build());
//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0012").area("A103").build());
//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0013").area("A104").build());
//        pwjRackAreaInfoList.add(RackAreaInfo.builder().rackCode("PWJ02N008S0014").area("A105").build());

        //默认4个
        pyp10List.add(RackAreaInfo.builder().rackCode("PYP10N032S0010").area("A301").build());
        pyp10List.add(RackAreaInfo.builder().rackCode("PYP10N032S0011").area("A302").build());
        pyp10List.add(RackAreaInfo.builder().rackCode("PYP10N032S0012").area("A303").build());
        pyp10List.add(RackAreaInfo.builder().rackCode("PYP10N032S0013").area("A304").build());
        pyp10List.add(RackAreaInfo.builder().rackCode("PYP10N032S0014").area("A305").build());
        pyp10List.add(RackAreaInfo.builder().rackCode("PYP10N032S0015").area("A306").build());

        ppgList.add(RackAreaInfo.builder().rackCode("PPG01N032S0001").area("A201").build());
        ppgList.add(RackAreaInfo.builder().rackCode("PPG01N032S0002").area("A202").build());
        ppgList.add(RackAreaInfo.builder().rackCode("PPG01N032S0003").area("A203").build());
        ppgList.add(RackAreaInfo.builder().rackCode("PPG01N032S0004").area("A204").build());
        ppgList.add(RackAreaInfo.builder().rackCode("PPG01N032S0005").area("A205").build());

        pspList.add(RackAreaInfo.builder().rackCode("PSP01N032S0001").area("A104").build());
        pspList.add(RackAreaInfo.builder().rackCode("PSP01N032S0002").area("A105").build());
        pspList.add(RackAreaInfo.builder().rackCode("PSP01N032S0003").area("A106").build());
        pspList.add(RackAreaInfo.builder().rackCode("PSP01N032S0004").area("A107").build());
//        pspList.add(RackAreaInfo.builder().rackCode("PSP01N032S0005").area("B205").build());
    }

    /**
     * @param rackType
     * @param isConsume 是否耗材
     * @return
     */
    public RackAreaInfo getByRackType(String rackType, int isConsume) {
        RackAreaInfo result = null;
        if (rackType == null) {
            return null;
        }
        //非耗材
        if (rackType.equals("PYP10") && isConsume == 0) {
            //原始样盘
            result = getOne(pyp10OriginalList, pyp10Start1);
        } else if (rackType.equals("PYP10") && isConsume == 1) {
            //耗材盘
            result = getOne(pyp10List, pyp10Start2);
        } else if (rackType.equals("PSP01") && isConsume == 1) {
            //耗材盘 色谱瓶
            result = getOne(pspList, pspStart);
        } else if (rackType.equals("PPG01") && isConsume == 1) {
            //耗材盘 色谱瓶盖
            result = getOne(ppgList, ppgStart);
        }
        return result;
    }

    public RackAreaInfo getOne(List<RackAreaInfo> list, AtomicInteger startValue) {
        if (startValue.get() < list.size() - 1) {
            int start = startValue.getAndIncrement();
            return list.get(start);
        } else {
            //循环获取
            startValue.set(0);
            return list.get(0);
        }
    }

    public void processMessage(String topic, String payload) {
        MqttService mqttService = SpringUtils.getBean(MqttService.class);

        List<Integer> dealSequenceList = Lists.newArrayList(454, 455, 456);
        try {
            //NCMD解析
            Map msg = JSON.parseObject(payload, Map.class);
            String strMethod = msg.get("strMethod").toString();
            String strID = msg.get("strID") + "";
            log.info("NCMD解析-------------strMethod:{},strID:{}", strMethod, strID);
            Map body = JSON.parseObject(msg.get("body").toString(), Map.class);

            //转为上报的topic
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
                log.info("------action:{}", action);
                //1:申请入库，2:申请出库
                if (action == 2 || action == 1) {
                    Integer sequenceId = (Integer) body.get("sequenceId");
                    String rackCode = (String) body.get("rackCode");
                    RackAreaInfo byRackType = null;
                    if (rackCode != null) {
                        //原始样品申请库位
                        log.info("------------原始样品申请库位 rackCode:{}", rackCode);
//                        byRackType = getByRackType(rackCode.substring(0, 5));
//                        byRackType = pyp10OriginalList.stream().filter(v -> v.getRackCode().equals(rackCode)).collect(Collectors.toList()).get(0);
                        if ("PPG01".equals(rackCode.substring(0, 5))) {
                            byRackType = ppgList.stream().filter(v -> v.getRackCode().equals(rackCode)).collect(Collectors.toList()).get(0);
                        } else if ("PSP01".equals(rackCode.substring(0, 5))) {
                            byRackType = pspList.stream().filter(v -> v.getRackCode().equals(rackCode)).collect(Collectors.toList()).get(0);
                        } else if ("PYP10".equals(rackCode.substring(0, 5))) {
                            List<RackAreaInfo> newPyp10List = Lists.newArrayList();
                            newPyp10List.addAll(pyp10OriginalList);
                            newPyp10List.addAll(pyp10List);
                            byRackType = newPyp10List.stream().filter(v -> v.getRackCode().equals(rackCode)).collect(Collectors.toList()).get(0);
                        }
                    } else {
                        //耗材盘架申请库位
                        String rackType = (String) body.get("rackType");
                        log.info("------------耗材盘架申请库位 rackType:{}", rackType);
                        if (rackType != null) {
                            byRackType = getByRackType(rackType, 1);
                        }
                    }

                    //对需要处理的序列进行上报，其他序列不处理
                    if (dealSequenceList.contains(sequenceId) || 1 == 1) {
                        if (byRackType == null) {
                            log.error("----------------不对库位询问进行上报-没有找到此盘架类型,序列:{}", sequenceId);
                            return;
                        }
                        String allocationAreaMsg = "{\"services\":[{\"eventTime\":\"20250611T091212Z\",\"eventParams\":{\"strID\":\"" + strID + "\"," +
                                "\"strMethod\":\"AllocationArea2\",\"body\":{\"state\":0,\"rackCode\":\"" + byRackType.getRackCode() + "\"," +
                                "\"pointArea\":\"" + byRackType.getArea() + "\",\"targetArea\":\"" + byRackType.getArea() + "\"}},\"eventType\":\"reply\"}]}";
                        mqttService.publish(ndataTopic, allocationAreaMsg);
                    }
                }
            } else if ("RackMove".equals(strMethod)) {
                Integer action = (Integer) body.get("action");
                if (action == 2 || action == 1) {
                    String rackCode = (String) body.get("rackCode");
                    String pointArea = (String) body.get("pointArea");
                    String rackMoveMsg = "{\"services\":[{\"eventType\":\"reply\",\"eventTime\":\"20250627T121212Z\",\"eventParams\":" +
                            "{\"strID\":\"" + strID + "\",\"strMethod\":\"RackMove\",\"body\":{\"actionType\":4,\"state\":0," +
                            "\"rackCode\":\"" + rackCode + "\",\"fromAreaCode\":\"" + pointArea + "\",\"targetAreaCode\":\"" + pointArea + "\"}}}]}";
                    mqttService.publish(ndataTopic, rackMoveMsg);
                }
            } else if ("SendConsumableRacks".equals(strMethod)) {
                //收到下发补充耗材信息后回复200
                String msg2 = "{ \n" +
                        "    \"services\": [{\n" +
                        "        \"eventType\": \"reply\", \n" +
                        "        \"eventTime\": \"20240319T121212Z\",\n" +
                        "        \"eventParams\": {\n" +
                        "              \"strID\": \"" + strID + "\",\n" +
                        "              \"strMethod\": \"SendConsumableRacks\",\n" +
                        "              \"strCode\": 200\n" +
                        "        }\n" +
                        "    }] \n" +
                        "}";
                mqttService.publish(ndataTopic, msg2);
                log.info("收到下发补充耗材信息后恢复200:{}", msg2);
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
