package com.yrf.dilraj.services.downloader;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * {@link Fetcher} facilitates the process of configuration and instantiation of {@link HttpClient} that is to be used
 * for downloading web pages for completing {@link CrawlJob}
 *
 * @author dilraj45
 */
public class Fetcher {

    private static final String TEXT_MIME_TYPES[] = {
            "text/html",
            "application/x-asp",
            "application/xhtml+xml",
            "application/vnd.wap.xhtml+xml"
    };

    private static final String USER_AGENT = "User-Agent";

    private HttpClient client;
    private UserAgent userAgent;
    private int connectTimeoutTime;
    private int readTimeoutTime;

    public Fetcher() {
        this(new UserAgent.Builder().build());
    }

    public Fetcher(UserAgent userAgent) {
        this(userAgent, HttpClientBuilder.create().
                disableCookieManagement().useSystemProperties().build());
        }

    public Fetcher(UserAgent userAgent, HttpClient client) {
        this(userAgent, client, 30000, 30000);
    }

    public Fetcher(UserAgent userAgent, HttpClient client, int connectTimeoutTime, int readTimeoutTime) {
        this.userAgent = userAgent;
        this.client = client;
        this.connectTimeoutTime = connectTimeoutTime;
        this.readTimeoutTime = readTimeoutTime;
    }

    /**
     * This method can be invoked to download web page for a given URL passed as an argument to this method
     *
     * @param url of type {@link URL}
     * @return {@link HttpResponse}
     * @throws IOException
     */
    public HttpResponse get(URL url) throws IOException {
        return this.get(url, new HashMap<>());
    }

    /**
     * This method can be invoked to download web page for a given URL passed as an argument to this method, along
     * with additional request headers
     *
     * @param url {@link URL}
     * @param additionalHeaders {@link Map} with {@link String} key and {@link String} value pairs
     * @return {@link HttpResponse}
     * @throws IOException
     */
    public HttpResponse get(URL url, Map<String, String> additionalHeaders) throws IOException {
        String protocol = url.getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https")) {
            throw new RuntimeException("Bad Protocol");
        }
        HttpGet request = new HttpGet(url.toString());
        request.addHeader(USER_AGENT, this.userAgent.getUserAgentString());
        for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        return client.execute(request);
    }

    public HttpClient getHttpClient() {
        return this.client;
    }

    public void setUserAgent(UserAgent userAgent) {
        this.userAgent = userAgent;
    }

    public UserAgent getUserAgent(UserAgent userAgent) {
        return this.userAgent;
    }
}