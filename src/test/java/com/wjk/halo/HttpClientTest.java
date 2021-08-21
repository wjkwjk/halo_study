package com.wjk.halo;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpClientTest {

    /**
     * 使用HttpClinet发送get请求
     */

    /**
     * 无参Get
     */
    @Test
    public void testGet(){
        //可关闭的httpClient对象，相当于打开一个浏览器
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        String urlStr = "https://www.baidu.com";
        //构造HttpGet请求对象
        HttpGet httpGet = new HttpGet(urlStr);
        //可关闭的相应
        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpGet);
            //获取相应结果
            HttpEntity entity = response.getEntity();
            String toStringResult = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            System.out.println(toStringResult);
            //确保柳关闭
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null){
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 有参get
     */
    @Test
    public void testGet2() throws UnsupportedEncodingException {
        //可关闭的httpClient对象，相当于打开一个浏览器
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

        //密码加密
        String password = "133";
        String passwordEncode = URLEncoder.encode(password, StandardCharsets.UTF_8.name());

        String urlStr = "https://www.baidu.com" + "?" + "password=" + passwordEncode;
        //构造HttpGet请求对象
        HttpGet httpGet = new HttpGet(urlStr);

        //可关闭的相应
        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpGet);
            //获取相应结果
            HttpEntity entity = response.getEntity();
            String toStringResult = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            System.out.println(toStringResult);
            //确保柳关闭
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null){
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 获取响应
     */
    @Test
    public void testGet3(){
        //可关闭的httpClient对象，相当于打开一个浏览器
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        String urlStr = "https://www.baidu.com";
        //构造HttpGet请求对象
        HttpGet httpGet = new HttpGet(urlStr);
        //可关闭的相应
        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpGet);
            //代表本次请求的成功或失败
            StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_OK == statusLine.getStatusCode()){
                System.out.println("响应成功");

                Header[] allHeaders = response.getAllHeaders();
                for (Header header : allHeaders) {
                    System.out.println(header.getName() + " " + header.getValue());
                }

                //获取响应结果
                //HttpEntity不仅可以作为结果，也可以作为请求的参数实体
                HttpEntity entity = response.getEntity();
                String toStringResult = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                System.out.println(toStringResult);
                //确保柳关闭
                EntityUtils.consume(entity);
            }else {
                System.out.println("响应失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null){
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    /**
     * 设置访问代理
     */
    @Test
    public void testGet4(){
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        String urlStr = "https://www.baidu.com";
        HttpGet httpGet = new HttpGet(urlStr);
        //创建代理
        String ip = "";
        int port = 10000;
        HttpHost httpHost = new HttpHost(ip, port);
        //对每一个请求进行配置，会覆盖全局的默认请求配置
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setProxy(httpHost)     //设置代理
                .setConnectTimeout(2000)    //设置连接超时时间，TCP三次握手上限
                .setSocketTimeout(1000)     //设置读取超时时间，表示从请求的网址处获得响应数据的时间间隔
                .setConnectionRequestTimeout(1000)      //设置从连接池中获取connection的超时时间
                .build();
        httpGet.setConfig(requestConfig);

        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String toStringResult = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            System.out.println(toStringResult);
            EntityUtils.consume(entity);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null){
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
