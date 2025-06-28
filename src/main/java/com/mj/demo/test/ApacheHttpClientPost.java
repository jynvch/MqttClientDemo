package com.mj.demo.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自动化报工时
 *
 * @author RK-L
 */
@Slf4j
public class ApacheHttpClientPost {

    static String cookieStr = "Hm_lvt_a4471dcb99dc909bee559e7792f51c7f=1748227382; ZDEDebuggerPresent=php,phtml,php3; PHPSESSID=3pt4qu4ofek792umdn4ft3hpj0; powercms_auth=28f1jUG7v5yG2fjTT6aUqrNTrmFUq8u0HqcihV%2Fq%2BSFkXIPgGQNCNlER4lAwDiPzUOMdwKkVSO%2BAgErhOyBIEZBTk6jQkDme2OXhM4YskGAwXvl5suHZLB88ea55%2BIi8e2vSmT3YPJcfqlQsVH3o; acc_auth=0";

    static Map<Integer, String> projectMap = Maps.newHashMap();

    static {
        List<String> configProjectList = Lists.newArrayList("1:30:WRD037-P24027");
        for (String str : configProjectList) {
            String[] split = str.split(":");
            Integer start = Integer.valueOf(split[0]);
            Integer end = Integer.valueOf(split[1]);
            String projectName = split[2];
            if (start > end) {
                log.error("----配置有误");
                break;
            }
            for (int i = start; i <= end; i++) {
                projectMap.put(i, projectName);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        LocalDate startDate = LocalDate.of(2025, 6, 1);
//        LocalDate endDate = LocalDate.of(2025, 6, 15);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        int monthValue = startDate.getMonthValue();
        int year = startDate.getYear();
        //用户id
        String uid = "1524";
        //项目名称
//        String project = "WRD037-P24027";
        while (startDate.compareTo(endDate) <= 0) {
            DayOfWeek dayOfWeek = startDate.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                log.info("-----------------周末跳过:{}", startDate);
                startDate = startDate.plusDays(1);
                //周六日直接跳过
                continue;
            }
            int dayOfMonth = startDate.getDayOfMonth();
            String project = getProject(dayOfMonth);
            if (project == null) {
                log.error("---------------------日期内没有配置项目，跳过。日期:{}", startDate);
                continue;
            }
            reportEveryDay(uid, project, year, monthValue, dayOfMonth);
            startDate = startDate.plusDays(1);
        }
        log.info("-------------------------任务执行完成--------------------------");

    }

    static String getProject(int day) {
        if (projectMap.containsKey(day)) {
            return projectMap.get(day);
        }
        return null;
    }

    public static void reportEveryDay(String uid, String project, int year, int month, int day) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建POST请求
            HttpPost httpPost = new HttpPost("https://crm.raykol.com/oa/hour/edit?shman=" + uid + "&syear=" + year + "&smonth=" + month + "&sday=" + day);

            // 设置表单参数
            List<NameValuePair> formParams = new ArrayList<>();

            //正常班次事件
            LocalTime startTime = LocalTime.of(8, 30);
            LocalTime endTime = LocalTime.of(17, 0);
            //项目名称
//            String project = "WRD037-P24027";
            List<String> filterHour = Lists.newArrayList("12:00", "12:30");

            while (startTime.compareTo(endTime) <= 0) {
                DateTimeFormatter HHss = DateTimeFormatter.ofPattern("HH:mm");
                String HHssStr = startTime.format(HHss);
                if (filterHour.contains(HHssStr)) {
                    formParams.add(new BasicNameValuePair("hourtime[]", HHssStr));
                    formParams.add(new BasicNameValuePair("acheck[]", "0"));
                    formParams.add(new BasicNameValuePair("hxmhid[]", ""));
                } else {
                    formParams.add(new BasicNameValuePair("hourtime[]", HHssStr));
                    formParams.add(new BasicNameValuePair("acheck[]", "1"));
                    formParams.add(new BasicNameValuePair("hxmhid[]", project));
                }
                startTime = startTime.plusMinutes(30);
            }

            //测试数据
//            formParams.add(new BasicNameValuePair("hourtime[]", "08:30"));
//            formParams.add(new BasicNameValuePair("acheck[]", "1"));
//            formParams.add(new BasicNameValuePair("hxmhid[]", "WRD037-P24027"));
//            formParams.add(new BasicNameValuePair("hourtime[]", "09:00"));
//            formParams.add(new BasicNameValuePair("acheck[]", "1"));
//            formParams.add(new BasicNameValuePair("hxmhid[]", ""));

            // 构建表单实体
            httpPost.setEntity(new UrlEncodedFormEntity(formParams));

            // 设置请求头
            httpPost.setHeader("User-Agent", "Java HttpClient");
            httpPost.setHeader("origin", "https://crm.raykol.com");
            httpPost.setHeader("referer", "https://crm.raykol.com/oa/hour/edit?shman=1524&syear=2025&smonth=6&sday=15");
            httpPost.setHeader("cookie", cookieStr);

            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println("Status Code: " + response.getStatusLine().getStatusCode());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("Response Body: " + result);
                }
            }
        }
    }
}