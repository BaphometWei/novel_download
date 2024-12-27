package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.RandomUA;
import com.pcdd.sonovel.util.StringEx;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

//import static org.jline.jansi.AnsiRenderer.render;
import static org.fusesource.jansi.AnsiRenderer.render;
/**
 * @author pcdd
 * Created at 2024/3/17
 */
public class BookParser extends Source {

    private static final int TIMEOUT_MILLS = 30_000;

    public BookParser(int sourceId) {
        super(sourceId);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = this.rule.getBook();
        Document document = Jsoup.connect(url)
                .proxy("127.0.0.1", 10809)//设置代理
                .timeout(TIMEOUT_MILLS)
                .header(Header.USER_AGENT.getValue(), RandomUA.generate())
                .get();
        Elements bookElements = document.select(r.getBookName());
        Elements authorElements = document.select(r.getAuthor());
        Elements introElements = document.select(r.getIntro());
        Elements coverUrlElements = document.select(r.getCoverUrl());

        String bookName = bookElements.text();
        String author = CollUtil.isNotEmpty(authorElements)?authorElements.text():"无";
        String intro = CollUtil.isNotEmpty(introElements)?introElements.text():"无";
        String coverUrl = CollUtil.isNotEmpty(coverUrlElements)?coverUrlElements.attr("src"):"无";

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(HanLP.convertToSimplifiedChinese(StringEx.sNull(bookName)));
        book.setAuthor(HanLP.convertToSimplifiedChinese(StringEx.sNull(author)));
        book.setIntro(HanLP.convertToSimplifiedChinese(StringEx.sNull(intro)));
        book.setCoverUrl(CrawlUtils.normalizeUrl(coverUrl, this.rule.getUrl()));
        //封面替换为起点最新封面
//        book.setCoverUrl(replaceCover(book));
        //根据isCatalog判断详情页是否有完整目录，如果不是，则存入完整目录页url
        book.setCatalogUrl(url);
        if(!this.rule.getBook().getIsCatalog()){
            Elements catalogUrlElements = document.select(r.getCatalogUrl());
            String catalogUrl = catalogUrlElements.attr("href");
            book.setCatalogUrl(CrawlUtils.normalizeUrl(catalogUrl, this.rule.getUrl()));
        }
//        book.setCatalogUrl("https://69shux.co/book/50268/index.html");
        return book;
    }

    /**
     * 封面替换为起点最新封面
     */
    public static String replaceCover(Book book) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(Header.USER_AGENT.getValue(), RandomUA.generate());
        headers.put(Header.COOKIE.getValue(), "w_tsfp=ltvgWVEE2utBvS0Q6KvtkkmvETw7Z2R7xFw0D+M9Os09AacnUJyD145+vdfldCyCt5Mxutrd9MVxYnGAUtAnfxcSTciYb5tH1VPHx8NlntdKRQJtA5qJW1Qbd7J2umNBLW5YI0blj2ovIoFAybBoiVtZuyJ137ZlCa8hbMFbixsAqOPFm/97DxvSliPXAHGHM3wLc+6C6rgv8LlSgXyD8FmNOVlxdr9X0kCb1T0dC3FW9BO+AexINxmkKtutXZxDuDH2tz/iaJWl0QMh5FlBpRw4d9Lh2zC7JmNGJXkaewD23+I2Z7z6ZLh6+2xIAL5FW1kVqQ8ZteI5+URPDSi9YHWPBfp6tQAARvJZ/82seSvFxIb+c1AMu4Zt0AYlsYAN6DEjYTimKd8JSWTLNnUGfotRbsq+NHlkAkBbX2RE5Qdb;");
        HttpResponse resp = HttpRequest.get(StrUtil.format("https://www.qidian.com/so/{}.html", book.getBookName()))
                .headerMap(headers, true)
                .execute();

        Document document = Jsoup.parse(resp.body());
        resp.close();
        Elements elements = document.select(".res-book-item");

        try {
            for (Element e : elements) {
                String name = e.select(".book-mid-info > .book-info-title > a").text();
                // 起点作者
                String author1 = e.select(".book-mid-info > .author > .name").text();
                // 非起点作者
                String author2 = e.select(".book-mid-info > .author > i").text();
                String author = author1.isEmpty() ? author2 : author1;

                if (book.getBookName().equals(name) && book.getAuthor().equals(author)) {
                    String coverUrl = e.select(".book-img-box > a > img").attr("src");
                    return URLUtil.normalize(coverUrl).replaceAll("/150(\\.webp)?", "");
                }
            }
        } catch (Exception e) {
            Console.error(render("最新封面获取失败：{}", e.getMessage()));
            return book.getCoverUrl();
        }

        return book.getCoverUrl();
    }

}
