import org.apache.commons.validator.routines.EmailValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

public class Crawler implements Callable<Object> {

    private final String baseDomain;
    private final String url;
    private final Browser browser;
    private final EmailValidator emailValidator;
    private final Set<String> visitedUrls;
    private final Set<String> emailsFound;

    public Crawler(String url, String baseDomain, Set<String> visitedUrls, Set<String> emailsFound) {
        this.url = url;
        this.baseDomain = baseDomain;
        this.browser = new Browser();
        this.emailValidator = EmailValidator.getInstance();
        this.visitedUrls = visitedUrls;
        this.emailsFound = emailsFound;
    }

    @Override
    public Object call() throws Exception {
        crawl(url);
        return null;
    }

    private void crawl(String url) {
        String html;
        try {
            html = browser.fetchGeneratedHTML(url);
        } catch (IOException e) {
            return;
        }

        findEmail(Jsoup.parse(html).text().split("\\s+|\\n+"));
        Elements elements = Jsoup.parse(html).getAllElements();

        for (Element element : elements) {
            if (!visitedUrls.contains(url) && element.attr("href").contains(baseDomain)) {
                visitedUrls.add(url);
                crawl(url);
            } else if (element.attr("href").startsWith("/")) {
                String generatedUrl = baseDomain + element.attr("href");
                if (!visitedUrls.contains(generatedUrl)) {
                    visitedUrls.add(generatedUrl);
                    crawl(generatedUrl);
                }
            }
        }
    }

    private void findEmail(String[] text) {
        for (String possibleEmail : text) {
            if (emailValidator.isValid(possibleEmail) && !emailsFound.contains(possibleEmail)) {
                emailsFound.add(possibleEmail);
                System.out.println(possibleEmail);
            }
        }
    }


}
