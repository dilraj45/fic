package com.yrf.dilraj.services.downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class Fetcher {

    private static final String TEXT_MIME_TYPES[] = {
            "text/html",
            "application/x-asp",
            "application/xhtml+xml",
            "application/vnd.wap.xhtml+xml"
    };

    private static final String USER_AGENT = "User-Agent";

    private HttpClient client;
    @Getter @Setter private UserAgent userAgent;
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

    public HttpResponse get(URL url) throws IOException {
        return this.get(url, new HashMap<>());
    }

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

    public static void main(String[] args) throws Exception {
        Fetcher fetcher = new Fetcher();
        HttpClient client = fetcher.getHttpClient();
        fetcher.client = client;
        URL url = new URL("http:/google.com");
        HttpResponse response = fetcher.get(url, new HashMap<String, String>());
        System.out.println(response.toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (response.getEntity().getContent(), Charset.defaultCharset()));
        String line = "";
        int length = 0;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            length += line.length();
        }
        System.out.println(length);
        System.out.println(response.getEntity().getContent());
    }
}