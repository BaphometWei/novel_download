package com.pcdd.sonovel.parse;

import cn.hutool.core.util.URLUtil;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.StringEx;
import lombok.SneakyThrows;
import org.htmlunit.*;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 69书吧书源特殊处理
 * @author Bap
 * Created at 2024/12/11
 */
public class CatalogParser4 extends Source {

    private static final int TIMEOUT_MILLS = 30_000;

    public CatalogParser4(int sourceId) {
        super(sourceId);
    }

    /**
     * 解析全章
     */
    public List<Chapter> parse(String url) {
        return parse(url, 1, Integer.MAX_VALUE);
    }

    /**
     * 解析指定范围章节
     */
    @SneakyThrows
    public List<Chapter> parse(String url, int start, int end) {
        Rule.Book book = this.rule.getBook();
        // 正数表示忽略前 offset 章，负数表示忽略后 offset 章
        int offset = Optional.ofNullable(book.getCatalogOffset()).orElse(0);

        // 构造一个webClient 模拟Chrome 浏览器
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        //支持JavaScript
        webClient.getOptions().setJavaScriptEnabled(true);//启用js
        webClient.getOptions().setCssEnabled(false);//不展示，所以不启用
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setTimeout(10000);

        //设置代理
        ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
        proxyConfig.setProxyHost("127.0.0.1");
        proxyConfig.setProxyPort(10809);
//        proxyConfig.setProxyUsername("proxy_username");//(可选)设置代理服务器的用户名。
//        proxyConfig.setProxyPassword("proxy_password");
        webClient.getOptions().setProxyConfig(proxyConfig);

        URL newUrl = new URL(url);
        WebRequest requestSettings = new WebRequest(newUrl, HttpMethod.GET);
        HtmlPage catalogPage = webClient.getPage(requestSettings);

        DomElement element = catalogPage.getElementById("loadmore");
        catalogPage = element.click();
        Document document = Jsoup.parse(catalogPage.asXml());

        List<Element> elements = document.select(book.getCatalog());
        if (offset != 0) {
            if (offset > 0) elements = elements.subList(offset, elements.size());
            if (offset < 0) elements = elements.subList(0, elements.size() + offset);
        }

        List<Chapter> catalog = new ArrayList<>();
        for (int i = start - 1; i < end && i < elements.size(); i++) {
            String chapterUrl = elements.get(i).childNode(1).attr("href");
            Chapter build = Chapter.builder()
                    .title(elements.get(i).text())
                    .url(CrawlUtils.normalizeUrl(chapterUrl, this.rule.getUrl()))
                    .chapterNo(i + 1)
                    .build();
            catalog.add(build);
        }

        return catalog;
    }

}