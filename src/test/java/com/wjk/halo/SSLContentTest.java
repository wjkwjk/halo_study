package com.wjk.halo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class SSLContentTest {

    /**
     * 使用安全套接字层，可以在客户端和服务器之间建立安全连接。它有助于保护敏感信息，如信用卡号，用户名，密码，别针等。
     * 可以使用HttpClient库创建自己的SSL上下文，从而使连接更安全。
     * 按照下面给出的步骤使用HttpClient库自定义SSLContext
     */

    @Test
    public void SSLContentTest() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        /**
         创建SSLContextBuilder对象SSLContextBuilder是SSLContext对象的构建器。
            使用SSLContexts类的custom()方法创建对象。
         */
        SSLContextBuilder SSLBuilder = SSLContexts.custom();

        /**
         加载密钥库
            在路径Java_home_directory/jre/lib/security/中，可以找到名为cacerts的文件。将其保存为密钥库文件(扩展名为.jks)。
            使用SSLContextBuilder类的loadTrustMaterial()方法加载密钥库文件及其密码(默认为changeit)。
         */
        //加载密钥库文件
        File file = new File("mykeystore.jks");
        SSLBuilder = SSLBuilder.loadTrustMaterial(file, "changeit".toCharArray());

        /**
         构建SSLContext对象SSLContext对象表示安全套接字协议实现。使用build()方法构建SSLContext。
         */
        //构建SSLContext
        SSLContext sslcontext = SSLBuilder.build();

        /**
         创建SSLConnectionSocketFactory对象
            SSLConnectionSocketFactory是用于TSL和SSL连接的分层套接字工厂。使用此方法，可以使用受信任证书列表验证https服务器并验证给定的https服务器。
            可以通过多种方式创建它。根据创建SSLConnectionSocketFactory对象的方式可允许所有主机，仅允许自签名证书，仅允许特定协议等。

            要仅允许特定协议，请通过传递SSLContext对象，表示需要支持的协议的字符串数组，表示需要支持的密码套件的字符串数组以及表示其构造函数的HostnameVerifier对象来创建SSLConnectionSocketFactory对象。
                new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            要允许所有主机，请通过传递SSLContext对象和NoopHostnameVerifier对象来创建SSLConnectionSocketFactory对象。
                SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
         */
        //创建SSLConnectionSocketFactory对象
        SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());

        /**
         创建一个HttpClientBuilder对象
            使用HttpClients类的custom()方法创建HttpClientBuilder对象。
         */
        //创建HttpClientBuilder对象
        HttpClientBuilder clientbuilder = HttpClients.custom();

        /**
         设置SSLConnectionSocketFactory对象
            使用setSSLSocketFactory()方法将SSLConnectionSocketFactory对象设置为HttpClientBuilder
         */
        //设置clientbuilder的SSLSocketFactory
        clientbuilder = clientbuilder.setSSLSocketFactory(sslConSocFactory);

        /**
         构建CloseableHttpClient对象
            通过调用build()方法构建CloseableHttpClient对象。
         */
        //构建可关闭的HttpClient
        CloseableHttpClient httpclient = clientbuilder.build();

        /**
         创建一个Get请求
         */
        HttpGet httpget = new HttpGet("https://yiibai.com/");

        /**
         执行请求
         */
        HttpResponse httpresponse = httpclient.execute(httpget);

        /**
         打印请求状态
         */
        System.out.println(httpresponse.getStatusLine());

        //Retrieving the HttpEntity and displaying the no.of bytes read
        HttpEntity entity = httpresponse.getEntity();
        if (entity != null) {
            System.out.println(EntityUtils.toByteArray(entity).length);
        }


    }

}
