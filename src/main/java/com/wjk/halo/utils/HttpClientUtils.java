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

public class HttpClientUtils {

    private final static int TIMEOUT = 5000;
    private HttpClientUtils(){}

    @NonNull
    public static CloseableHttpClient createHttpsClient(int timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException{
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (certificate, authType) -> true)
                .build();
        return resolveProxySetting(HttpClients.custom())
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultRequestConfig(getRequestConfig(timeout))
                .build();
    }

    private static HttpClientBuilder resolveProxySetting(final HttpClientBuilder httpClientBuilder){
        final String httpProxyEnv = System.getenv("http_proxy");
        if (StringUtils.isNotBlank(httpProxyEnv)){
            final Tuple httpProxy = resolveHttpProxy(httpProxyEnv);
            final HttpHost httpHost = HttpHost.create(httpProxy.get(0));
            httpClientBuilder.setProxy(httpHost);
            if (httpProxy.getMembers().length == 3){
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
                        new UsernamePasswordCredentials(httpProxy.get(1), httpProxy.get(2)));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        return httpClientBuilder;
    }

    private static Tuple resolveHttpProxy(final String httpProxy){
        final URI proxyUri = URI.create(httpProxy);
        int port = proxyUri.getPort();
        if (port == -1){
            if (Objects.equals("http", proxyUri.getScheme())){
                port = 80;
            }
            if (Objects.equals("https", proxyUri.getScheme())){
                port = 443;
            }
        }
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
