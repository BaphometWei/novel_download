package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.RandomUA;
import com.pcdd.sonovel.util.StringEx;
import lombok.SneakyThrows;
import org.htmlunit.util.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author pcdd
 * Created at 2024/3/23
 */
public class SearchResultParser extends Source {

    private static final int TIMEOUT_MILLS = 15_000;

    //精确搜索，如果匹配成功，直接不展示目录，展示书籍详情页面
    private static boolean isBook = false;

    public SearchResultParser(int sourceId) {
        super(sourceId);
    }

    public List<SearchResult> parse(String keyword) {
        Rule.Search search = this.rule.getSearch();
        boolean isPaging = search.getPagination();
        isBook = false;
        // 模拟搜索请求
        Document document;
        try {
            String userAgent = RandomUA.generate();
            String url = search.getUrl();
            Connection.Response resp = Jsoup.connect(url)
                    .proxy("127.0.0.1", 10809)//设置代理
                    .method(CrawlUtils.buildMethod(this.rule.getSearch().getMethod()))
                    .timeout(TIMEOUT_MILLS)
                    .header("User-Agent", userAgent)
                    .data(CrawlUtils.buildParams(this.rule.getSearch().getBody(), keyword))
                    .cookies(CrawlUtils.buildCookies(this.rule.getSearch().getCookies()))
                    .execute();
            document = resp.parse();
        } catch (Exception e) {
            Console.error(e.getMessage());
            return Collections.emptyList();
        }
        List<SearchResult> firstPageResults = getSearchResults(null, document);

        if (isBook) return firstPageResults;
        if (!isPaging) return firstPageResults;

        Set<String> urls = new LinkedHashSet<>();
        for (Element e : document.select(search.getNextPage()))
            urls.add(CrawlUtils.normalizeUrl(URLDecoder.decode(e.attr("href"), StandardCharsets.UTF_8), this.rule.getUrl()));

        // 使用并行流处理分页 URL
        List<SearchResult> additionalResults = urls.parallelStream()
                .flatMap(url -> getSearchResults(url, null).stream())
                .toList();

        // 合并，不去重（去重用 union）
        return CollUtil.unionAll(firstPageResults, additionalResults);
    }

    @SneakyThrows
    private List<SearchResult> getSearchResults(String url, Document document) {
        Rule.Search rule = this.rule.getSearch();
        // 搜索结果页 DOM
        if (document == null)
            document = Jsoup.connect(url).timeout(TIMEOUT_MILLS).get();

        List<SearchResult> list = new ArrayList<>();
        //如果搜索结果是书籍
        Elements bookboxElements = document.select(rule.getIsBook());
        if(CollUtil.isNotEmpty(bookboxElements)){
            isBook = true;

            Elements bookElements = document.select(this.rule.getBook().getBookName());
            Elements authorElements = document.select(this.rule.getBook().getAuthor());
            Elements bookLatestChapter = document.select(this.rule.getBook().getLatestChapter());

            String bookName = bookElements.text();
            String author = CollUtil.isNotEmpty(authorElements)?authorElements.text():"无";


            SearchResult build = SearchResult.builder()
                    .url(CrawlUtils.normalizeUrl(document.select(this.rule.getBook().getUrl()).attr(rule.getBookUrl()), this.rule.getUrl()))
                    .bookName(StrUtil.isEmptyIfStr(bookName)?"": HanLP.convertToSimplifiedChinese(bookName))//繁体转简体
                    .latestChapter(HanLP.convertToSimplifiedChinese(StringEx.sNull(bookLatestChapter.text())))
                    .author(StrUtil.isEmptyIfStr(bookName)?"": HanLP.convertToSimplifiedChinese(author))
                    .latestUpdate("")
                    .build();

            list.add(build);
            return list;
        }

        Elements elements = document.select(rule.getResult());
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String href = element.select(rule.getBookName()).attr("href");
            String bookName = element.select(rule.getBookName()).text();
            String latestChapter = element.select(rule.getLatestChapter()).text();
            String author = element.select(rule.getAuthor()).text();
            String update = element.select(rule.getUpdate()).text();

            // 针对书源 1：排除第一个 tr 表头
            if (bookName.isEmpty()) continue;

            SearchResult build = SearchResult.builder()
                    .url(CrawlUtils.normalizeUrl(href, this.rule.getUrl()))
                    .bookName(StrUtil.isEmptyIfStr(bookName)?"": HanLP.convertToSimplifiedChinese(bookName))//繁体转简体
                    .latestChapter(StrUtil.isEmptyIfStr(bookName)?"": HanLP.convertToSimplifiedChinese(latestChapter))
                    .author(StrUtil.isEmptyIfStr(bookName)?"": HanLP.convertToSimplifiedChinese(author))
                    .latestUpdate(StrUtil.isEmptyIfStr(bookName)?"": HanLP.convertToSimplifiedChinese(update))
                    .build();

            list.add(build);
        }

        return list;
    }

}