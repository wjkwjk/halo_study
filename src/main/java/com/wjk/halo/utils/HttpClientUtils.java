package com.wjk.halo.utils;

import cn.hutool.core.lang.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.lang.NonNull;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 用户创建客户端请求工具HttpClient
 *      创建HttpClient，并且设置参数，包括：
 *          SSL
 *          代理
 *          用户凭证
 */

public class HttpClientUtils {

    private final static int TIMEOUT = 5000;
    private HttpClientUtils(){}

    /**
     * 创建Http客户端请求工具HttpClient
     * @param timeout
     * @return  返回CloseableHttpClient，可以关闭的客户端请求工具HttpClient
     */

    @NonNull
    public static CloseableHttpClient createHttpsClient(int timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException{
        //自定义SSL套接字
        SSLContext sslContext = new SSLContextBuilder() //生成SSLContent生成器
                .loadTrustMaterial(null, (certificate, authType) -> true)   //加载服务端信息，自定义了信任策略，不对服务端证书进行校验
                .build();
        return resolveProxySetting(HttpClients.custom())    //获得了设置凭证注册器以及代理的HttpClinetBuilder
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultRequestConfig(getRequestConfig(timeout))
                .build();
    }

    /**
     * HttpClientBuilder：HttpClient构造器，用于构造HttpClient客户端请求工具
     * 这个函数功能是获取代理主机的信息，包括ip，port，用户名，密码。然后根据这4个值建立用户凭证，
     *  并且在HttpClientBuilder中设置凭证构造器信息以及代理信息
     *  最后返回该HttpClientBuilder
     * @param httpClientBuilder
     * @return
     */
    private static HttpClientBuilder resolveProxySetting(final HttpClientBuilder httpClientBuilder){
        //获取关于代理的环境变量
        final String httpProxyEnv = System.getenv("http_proxy");
        if (StringUtils.isNotBlank(httpProxyEnv)){
            //获取的代理的主机地址，用户名，密码
            final Tuple httpProxy = resolveHttpProxy(httpProxyEnv);
            //创建一个请求代理主机地址的post请求
            final HttpHost httpHost = HttpHost.create(httpProxy.get(0));
            //httpClientBuilder适用于构造客户端请求工具HttpClinet，设置客户端请求工具的代理地址为上述地址
            httpClientBuilder.setProxy(httpHost);
            if (httpProxy.getMembers().length == 3){
                //建立凭证注册器，用于存储凭证
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                //设置凭证
                //new AuthScope(httpHost.getHostName(), httpHost.getPort())：发出请求的地址
                //new UsernamePasswordCredentials(httpProxy.get(1), httpProxy.get(2))：用户名，密码
                //因此总的凭证包含：发出请求的ip，发出请求的port，用户名，密码
                credentialsProvider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
                        new UsernamePasswordCredentials(httpProxy.get(1), httpProxy.get(2)));
                //设置httpClinet构造器的凭证注册器
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        return httpClientBuilder;
    }

    /**
     * 解析从环境变量中获取的代理信息
     * @param httpProxy：代理信息
     * @return 主机地址，用户名，密码
     */
    private static Tuple resolveHttpProxy(final String httpProxy){
        //创建一个请求地址，相当于一个网络地址
        final URI proxyUri = URI.create(httpProxy);
        int port = proxyUri.getPort();
        if (port == -1){
            //判断使用的协议，如果使用了http协议，则默认使用80端口，如果使用了https协议，则默认使用443端口
            if (Objects.equals("http", proxyUri.getScheme())){
                port = 80;
            }
            if (Objects.equals("https", proxyUri.getScheme())){
                port = 443;
            }
        }
        //proxyUri.getScheme() ：返回使用的协议，一般是http或者https
        //获得代理的主机地址
        final String hostUrl = proxyUri.getScheme() + "://" + proxyUri.getHost() + ":" + port;
        final String usernamePassword = proxyUri.getUserInfo();
        if (StringUtils.isNotBlank(usernamePassword)){
            final String username;
            final String password;
            final int atColon = usernamePassword.indexOf(':');
            if (atColon >= 0){
                username = usernamePassword.substring(0, atColon);
                password = usernamePassword.substring(atColon+1);
            }else {
                username = usernamePassword;
                password = null;
            }
            return new Tuple(hostUrl, username, password);
        }else {
            return new Tuple(hostUrl);
        }
    }

    private static RequestConfig getRequestConfig(int timeout){
        return RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }
}
