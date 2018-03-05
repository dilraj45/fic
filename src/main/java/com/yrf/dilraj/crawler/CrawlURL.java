package com.yrf.dilraj.crawler;

import java.io.Serializable;
import java.net.URL;


// todo: provide support for URI, support for link context, support for URL type, support for status
// todo: implement kafka serializer and desearializer

/**
 * {@link CrawlURL} facilitates the process of maintaining the metrics required to compute the relevance and other required
 * attribute specific to a URL so as to perform a priority ordered crawling. Machine learning based paradigms or some heuiristics can be
 * the basis for computing the relevance of URLs.
 *
 * @author dilraj45
 */
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

    public URL getUrl() {
        return this.url;
    }
}
