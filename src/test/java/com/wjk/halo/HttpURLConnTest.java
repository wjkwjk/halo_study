package com.wjk.halo;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class HttpURLConnTest {

    /**
     * 使用jdk原生的api请求网页
     */
    @Test
    public void test() throws Exception{
        String urlStr = "https://www.baidu.com";
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

        try (
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
        ){
            String line;
            while ((line = br.readLine()) != null){
                System.out.println(line);
            }
        }
    }
}
