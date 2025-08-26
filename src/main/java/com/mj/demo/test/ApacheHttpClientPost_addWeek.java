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
public class ApacheHttpClientPost_addWeek {

    //用户的cookie
    static String cookieStr = "lang=zh-cn; device=desktop; theme=default; keepLogin=on; za=RK1770; Hm_lvt_a4471dcb99dc909bee559e7792f51c7f=1748227382; goback=%7B%22my%22%3A%22http%3A%5C%2F%5C%2Fchandao.raykol.com%3A8099%5C%2Frb-browse.html%22%7D; preExecutionID=504; zentaosid=qdrmmag1jii99cjdoghnjbu2mt; selfClose=1; zp=4dc1ffccdc6b9a0e144d681be766bc184c1d6ee3; windowWidth=1824; windowHeight=871; tab=my";
    static String uid = "68a58a3e9d25d";

    public static void main(String[] args) throws Exception {

        addWeekGo(uid);
    }

    public static void addWeekGo(String uid) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建POST请求
            HttpPost httpPost = new HttpPost("http://chandao.raykol.com:8099/rb-create-save--0.html");
            // 设置表单参数
            List<NameValuePair> formParams = new ArrayList<>();

            //测试数据
            formParams.add(new BasicNameValuePair("result", "12"));
            formParams.add(new BasicNameValuePair("plan", "13"));
            formParams.add(new BasicNameValuePair("program", ""));
            formParams.add(new BasicNameValuePair("share", ""));
            formParams.add(new BasicNameValuePair("cong", "2025-08-11"));
            formParams.add(new BasicNameValuePair("dao", "2025-08-17"));
            formParams.add(new BasicNameValuePair("yzm", ""));
            formParams.add(new BasicNameValuePair("uid", uid));

            // 构建表单实体
            httpPost.setEntity(new UrlEncodedFormEntity(formParams));

            // 设置请求头
//            httpPost.setHeader("User-Agent", "Java HttpClient");
            httpPost.setHeader("origin", "http://chandao.raykol.com:8099");
            httpPost.setHeader("referer", "http://chandao.raykol.com:8099/rb-create.html");
            httpPost.setHeader("cookie", cookieStr);
            httpPost.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
            httpPost.setHeader("x-requested-with", "XMLHttpRequest");
            httpPost.setHeader("content-type", "form-action 'self';connect-src 'self';");
            httpPost.setHeader("accept", "application/json, text/javascript, */*; q=0.01");

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