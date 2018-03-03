package com.yrf.dilraj.crawler;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.URL;


// todo: provide support for URI, support for link context, support for URL type, support for status
// todo: implement kafka serializer and desearializer
@Getter
@Setter
public class CrawlURL implements Serializable {

    private URL url;
    private double relevance = 0;

    public CrawlURL(URL url) {
        this.url = url;
    }

    public CrawlURL(URL url, double relevance) {
        this.url = url;
        this.relevance = relevance;
    }
}
