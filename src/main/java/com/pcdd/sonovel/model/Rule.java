package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/3/10
 */
@Data
public class Rule {

    private int id;
    private String url;
    private String name;
    private String comment;
    private String type;

    private Search search;
    private Book book;
    private Chapter chapter;

    @Data
    public static class Search {
        private String url;
        private String method;
        private String param;
        private String body;
        private String cookies;
        // 搜索结果是否有分页
        private Boolean pagination;
        private String nextPage;
        // 以下字段不同书源可能不同
        private String result;
        private String bookName;
        private String latestChapter;
        private String author;
        private String update;
        //判断搜索结果是否是书籍详情页
        private String isBook;
        //书籍url
        private String bookUrl;
    }

    @Data
    public static class Book {
        private String url;
        private String bookName;
        private String author;
        private String intro;
        private String category;
        private String coverUrl;
        private String latestChapter;
        private String latestUpdate;
        private String isEnd;
        private String catalog;
        private Integer catalogOffset;
        //判断书籍详情页和目录是否同一页
        private Boolean isCatalog;
        //完整目录页url
        private String catalogUrl;
    }

    @Data
    public static class Chapter {
        private String url;
        // 章节是否有分页
        private Boolean pagination;
        private String nextPage;
        private Integer chapterNo;
        private String title;
        private String content;
        private Boolean paragraphTagClosed;
        private String paragraphTag;
        private String filterTxt;
        private String filterTag;
    }

}
