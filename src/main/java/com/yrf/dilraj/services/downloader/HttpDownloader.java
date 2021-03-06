package com.yrf.dilraj.services.downloader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yrf.dilraj.utils.KafkaUtils;
import javafx.util.Pair;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * {@link HttpDownloader} is wrapper that provide us with functionality of maitaining a thread pool of toe threads
 * that can be used to download web pages. Additionally, this is the entry point for downloader service
 *
 *  @author dilraj45
 */
public class HttpDownloader implements Closeable {

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    public static Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);

    private final ExecutorService downloadThreadPool;
    private final LinkedBlockingQueue<Runnable> downloadQueue;
    private static final AtomicInteger numberOfDownloads = new AtomicInteger(0);
    private static final AtomicInteger queuedRequests = new AtomicInteger(0);
    private static final AtomicInteger runningHandlers = new AtomicInteger(0);


    public HttpDownloader(@Nullable HttpDownloaderConfig config) {

        ThreadFactory downloadThreadFactory =
                new ThreadFactoryBuilder().setNameFormat("downloader-%d").build();

        this.downloadQueue = new LinkedBlockingQueue<>();
        int threadPoolSize = 100;
        this.downloadThreadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L,
                TimeUnit.MILLISECONDS, this.downloadQueue, downloadThreadFactory) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                HttpDownloader.queuedRequests.decrementAndGet();
                HttpDownloader.runningHandlers.incrementAndGet();
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                HttpDownloader.runningHandlers.decrementAndGet();
                HttpDownloader.numberOfDownloads.incrementAndGet();
            }
        };

    }

    /**
     * This method fetches seed URLs from kafka stream and submits them to thread pool of toe thread for downloading
     */
    public void initDownload() {
        Properties props = KafkaUtils.getDefaultKafkaUtilsConfig();
        KafkaUtils.KafkaConsumerUtils<String, String> consumerUtils = new KafkaUtils.KafkaConsumerUtils<>(props);
        consumerUtils.subscribe(Collections.singletonList("QueuedURLs"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer commonProducer = KafkaUtils.KafkaProducerUtils.createKafkaProducerClient(props);
        while (true) {
            LOGGER.info("TOTAL URLS DOWNLOAD, dilraj {}", numberOfDownloads);
            // todo: fetch topic list from config
            // todo: change the value type to CrawlURL
            // todo: move the logic for robust fetching results from stream to downloader service
            List<Pair<String, String>> records = consumerUtils.poll(100);
            LOGGER.info("Total records received from {} stream: {}", "QueuedURLs", records.size());

            if (records.size() > 0 ) {
                LOGGER.info("Total records received from {} stream: {}", "QueuedURLs", records.size());
                LOGGER.info("Running requests: {}", runningHandlers.toString());
                LOGGER.info("Queued Requests: {}", queuedRequests.toString());
            }
            for (Pair<String, String> record : records) {
                try {
                    URL url = new URL(record.getValue());
                    CrawlJob job = new CrawlJob(url, commonProducer);
                    queuedRequests.incrementAndGet();
                    downloadThreadPool.execute(job);
                } catch (MalformedURLException exception) {
                    LOGGER.error("Malformed URLs received from stream: %s", record.getValue());
                }
            }
        }
    }

    @Override
    public void close() {
        downloadThreadPool.shutdownNow();
        try {
            downloadThreadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to shutdown downloader threads.", e);
        }
    }

    public static void main(String[] args) {
        HttpDownloader downloader = new HttpDownloader(null);
        LOGGER.info("Starting downloading service");
        downloader.initDownload();
    }
}
