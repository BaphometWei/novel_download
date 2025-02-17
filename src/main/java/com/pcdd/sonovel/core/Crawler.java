package com.pcdd.sonovel.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.*;
import com.pcdd.sonovel.util.StringEx;
import lombok.SneakyThrows;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2021/6/10
 */
public class Crawler {

    private final ConfigBean config;
    private String bookDir;

    public Crawler(ConfigBean config) {
        this.config = config;
    }

    /**
     * 搜索小说
     *
     * @param keyword 关键字
     * @return 匹配的小说列表
     */
    public List<SearchResult> search(String keyword) {
        Console.log("<== 正在搜索...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        SearchResultParser searchResultParser = new SearchResultParser(config.getSourceId());
        List<SearchResult> searchResults = searchResultParser.parse(keyword);

        stopWatch.stop();
        Console.log("<== 搜索到 {} 条记录，耗时 {} s\n", searchResults.size(), NumberUtil.round(stopWatch.getTotalTimeSeconds(), 2));

        return searchResults;
    }

    /**
     * 爬取小说
     *
     * @param sr    待下载小说
     * @param start 从第几章下载
     * @param end   下载到第几章
     */
    @SneakyThrows
    public double crawl(SearchResult sr, int start, int end) {
        // 小说详情页url
        String url = sr.getUrl();
        String bookName = sr.getBookName();
        String author = sr.getAuthor();
        Book book = new BookParser(config.getSourceId()).parse(url);

        // 小说目录名格式：书名(作者)
        bookDir = String.format("%s (%s)", HanLP.convertToSimplifiedChinese(StringEx.sNull(bookName)), author);
        // 必须 new File()，否则无法使用 . 和 ..
        File dir = FileUtil.mkdir(new File(config.getDownloadPath() + File.separator + bookDir));
        if (!dir.exists()) {
            // C:\Program Files 下创建需要管理员权限
            Console.log(render("@|red 创建下载目录失败\n1. 检查下载路径是否合法\n2. 尝试以管理员身份运行（C 盘部分目录需要管理员权限）|@"));
            return 0;
        }

        Console.log("<== 正在获取章节目录", HanLP.convertToSimplifiedChinese(StringEx.sNull(bookName)));
        // 获取小说目录
        List<Chapter> catalog = new ArrayList<>();
        int catalogSize = 0;
        try {
            //解决对应不同书源书籍目录跟详情页不在同一个页面的解析方式
            Class<?> clazz = Class.forName("com.pcdd.sonovel.parse.CatalogParser" + config.getSourceId());
            Object instance = clazz.getDeclaredConstructor(int.class).newInstance(config.getSourceId());
            Method method = clazz.getMethod("parse", String.class, int.class, int.class);
            catalog = (List<Chapter>) method.invoke(instance, book.getCatalogUrl(), start, end);
//            catalogSize = catalog.parse(url).size();
        } catch (ClassNotFoundException e) {
            CatalogParser catalogParser = new CatalogParser(config.getSourceId());
            catalog = catalogParser.parse(book.getCatalogUrl(), start, end);
            catalogSize = catalogParser.parse(url).size();
        }
        // 防止 start、end 超出范围
        if (CollUtil.isEmpty(catalog)) {
            Console.log(render(StrUtil.format("@|yellow 超出章节范围，该小说共 {} 章|@", catalogSize)));
            return 0;
        }

        int autoThreads = Runtime.getRuntime().availableProcessors() * 2;
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(config.getThreads() == -1 ? autoThreads : config.getThreads());
        // 阻塞主线程，用于计时
        CountDownLatch latch = new CountDownLatch(catalog.size());

        Console.log("<== 开始下载《{}》（{}） 共计 {} 章 | 线程数：{}", bookName, author, catalog.size(), autoThreads);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ChapterParser chapterParser = new ChapterParser(config);
        // 爬取章节并下载
        catalog.forEach(item -> executor.execute(() -> {
            createChapterFile(chapterParser.parse(item, latch, sr));
            Console.log("<== 待下载章节数：{}", latch.getCount());
        }));

        // 阻塞主线程，等待章节全部下载完毕
        latch.await();
        executor.shutdown();
        new CrawlerPostHandler(config).handle(book, dir);
        stopWatch.stop();

        return stopWatch.getTotalTimeSeconds();
    }

    /**
     * 保存章节
     */
    private void createChapterFile(Chapter chapter) {
        if (chapter == null) return;

        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(generatePath(chapter)))) {
            fos.write(chapter.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Console.error(e, e.getMessage());
        }
    }

    private String generatePath(Chapter chapter) {
        // epub 格式转换前的格式为 html
        String extName = Objects.equals("epub", config.getExtName()) ? "html" : config.getExtName();
        String parentPath = config.getDownloadPath() + File.separator + bookDir + File.separator;
        return switch (config.getExtName()) {
            case "html" -> parentPath + chapter.getChapterNo() + "_." + extName;
            case "epub", "txt" -> parentPath + chapter.getChapterNo()
                    // Windows 文件名非法字符替换
                    + "_" + chapter.getTitle().replaceAll("[\\\\/:*?<>]", "") + "." + extName;
            default -> throw new IllegalStateException("暂不支持的下载格式: " + config.getExtName());
        };
    }

}
