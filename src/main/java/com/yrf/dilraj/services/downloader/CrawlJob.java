package com.yrf.dilraj.services.downloader;

import com.yrf.dilraj.crawler.CrawlURL;
import com.yrf.dilraj.kafkatut.KafkaUtils;
import org.apache.http.HttpResponse;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class CrawlJob implements Runnable {

    private final Fetcher fetcher;
    private final URL url;
    private final KafkaProducer kafkaProducerClient;

    public static Logger LOGGER = LoggerFactory.getLogger(CrawlJob.class);


    public CrawlJob(URL url) {
        this(url, KafkaUtils.KafkaProducerUtils.createKafkaProducerClient(), new Fetcher());
    }

    public CrawlJob(URL url, KafkaProducer kafkaProducerClient) {
        this(url, kafkaProducerClient, new Fetcher());
    }

    public CrawlJob(URL url, Fetcher fetcher) {
        this(url, KafkaUtils.KafkaProducerUtils.createKafkaProducerClient(), fetcher);
    }

    public CrawlJob(URL url, KafkaProducer kafkaProducerClient, Fetcher fetcher) {
        this.url = url;
        this.kafkaProducerClient = kafkaProducerClient;
        this.fetcher = fetcher;
    }

    @Override
    public void run() {
        // todo: configurations
        try {
            HttpResponse response = this.fetcher.get(url);

            // extracting links from the html response
            Document doc = Jsoup.parse(response.getEntity().getContent(),
                    null, url.toString());
            LOGGER.info("Starting job");
            Elements links = doc.select("a");
            List<CrawlURL> extractedLinks = new LinkedList<>();
            for(Element link : links) {
                URL url = new URL (link.attr("abs:href"));
                extractedLinks.add(new CrawlURL(url));
            }

            // todo: curently adding all the url to queued URls, intial checks can be added
            // todo: change message value type to CrawlURL
            // writing extracted links to kafka stream
            for (CrawlURL crawlUrl: extractedLinks) {
                KafkaUtils.KafkaProducerUtils producer = new KafkaUtils.
                        KafkaProducerUtils<String, String>(this.kafkaProducerClient);
                // todo: move topics to config
                producer.sendMessage("QueuedURLs", crawlUrl.getUrl().getHost(), crawlUrl.getUrl().toString());
            }

            System.out.println(response.getEntity());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void extractLink() throws Exception {
        HttpResponse response = this.fetcher.get(new URL("http://google.com"));

    }
}
