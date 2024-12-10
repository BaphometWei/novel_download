package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.util.StringEx;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author pcdd
 * Created at 2024/3/17
 */
@AllArgsConstructor
public class ChapterConverter {

    private final ConfigBean config;
    private final TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH));

    public Chapter convert(Chapter chapter, String extName) {
        String filter = new ChapterFilter(config.getSourceId()).filter(chapter.getContent().replaceAll(chapter.getTitle(),""));
        String content = new ChapterFormatter(config).format(HanLP.convertToSimplifiedChinese(StringEx.sNull(filter)));
        chapter.setTitle(HanLP.convertToSimplifiedChinese(StringEx.sNull(chapter.getTitle())));
        if ("txt".equals(extName)) {
            // 全角空格，用于首行缩进
            String ident = "\u3000".repeat(2);
//            StringBuilder result = new StringBuilder();

            Matcher pMatcher = Pattern.compile("<p.*?>(.*?)</p>").matcher(content);
            while (pMatcher.find()) {
                String p = pMatcher.group(1);
                content = content.replaceAll(p, ident + p.replaceAll("<p.*?>","").replaceAll("</p>","") + "\n");
//                result.append(ident).append(pmatcher.group(1)).append("\n");
            }

            Matcher divMatcher = Pattern.compile("<[\\s]*?div[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?div[\\s]*?>").matcher(content);
            while (divMatcher.find()) {
                content = divMatcher.replaceAll("");
                break;
//                String div = divMatcher.group(1);
//                content = content.replaceAll(div, "");
            }

            Matcher brMatcher = Pattern.compile("<br>\\n<br>").matcher(content);
            while (brMatcher.find()) {
                content = brMatcher.replaceAll("");
                break;
//                String br = brMatcher.group(1);
//                content = content.replaceAll(br, "\n");
            }

            content = chapter.getTitle() + "\n".repeat(1) + content + "\n".repeat(3);
        }
        if ("epub".equals(extName) || "html".equals(extName)) {
            chapter.setContent(content);
            content = templateRender(chapter, extName);
        }

        chapter.setContent(content);
        return chapter;
    }

    /**
     * 根据扩展名渲染对应模板
     */
    private String templateRender(Chapter chapter, String extName) {
        // 符合 epub 标准的模板
        Template template = engine.getTemplate(StrUtil.format("chapter_{}.flt", extName));
        Map<String, String> map = new HashMap<>();
        map.put("title", chapter.getTitle());
        map.put("content", chapter.getContent());

        return template.render(map);
    }

}
