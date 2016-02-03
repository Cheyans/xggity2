import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class Browser {

    WebClient webClient = new WebClient(BrowserVersion.CHROME);

    public Browser() {
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    public String fetchGeneratedHTML(String url) throws IOException {

        Page page = webClient.getPage(url);
        if(page instanceof HtmlPage) {
            webClient.waitForBackgroundJavaScript(5 * 1000);
            return ((HtmlPage)page).asXml();
        }
        return "";
    }
}
