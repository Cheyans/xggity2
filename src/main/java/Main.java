import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if(args.length == 0) {
            System.out.println("Please provide a url parameter");
            System.exit(0);
        }

        String formattedUrl;
        if (!args[0].startsWith("http://") || !args[0].startsWith("https://")) {
            formattedUrl = "http://" + args[0];
        } else {
            formattedUrl = args[0];
        }

        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        String html = null;
        String domain = null;
        try {
            Browser browser = new Browser();
            URL url = new URL(formattedUrl);
            domain = url.getHost();
            html = browser.fetchGeneratedHTML(formattedUrl);
        } catch (IOException e) {
            System.out.println("Unreachable URL: " + formattedUrl);
            System.exit(0);
        }

        int threadCount = (int)(Runtime.getRuntime().availableProcessors() * 1.5);
        Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
        Set<String> emailsFound = Collections.synchronizedSet(new HashSet<>());
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Callable<Object>> crawlers = new ArrayList<>();

        Elements elements = Jsoup.parse(html).getAllElements();
        for (Element element : elements) {
            if (element.attr("href").contains(domain)) {
                crawlers.add(new Crawler(element.attr("abs:href"), domain, visitedUrls, emailsFound));
            } else if(element.attr("href").startsWith("/")) {
                crawlers.add(new Crawler(formattedUrl + element.attr("href"), domain, visitedUrls, emailsFound));
            }
        }
        int finishedCrawlers = 0;
        List<Future<Object>> futures = executorService.invokeAll(crawlers);
        for(Future future: futures) {
            if(future.isDone()) {
                finishedCrawlers++;
                System.out.println(finishedCrawlers + "/" + crawlers.size() + " crawlers finished");
                if(finishedCrawlers == crawlers.size()) {
                    System.exit(0);
                }
            }
        }
    }


}
