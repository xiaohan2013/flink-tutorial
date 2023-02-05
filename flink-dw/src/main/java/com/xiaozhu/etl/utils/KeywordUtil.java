package com.xiaozhu.etl.utils;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用IK分子器进行分词
 */
public class KeywordUtil {
    // 分词方法
    public static List<String> analyze(String text){
        StringReader reader = new StringReader(text);
        IKSegmenter ikSegmenter = new IKSegmenter(reader,true);
        List<String> resList = new ArrayList<>();
        try {
            Lexeme lexeme = null;
            while ( (lexeme = ikSegmenter.next()) != null){
                resList.add(lexeme.getLexemeText());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resList;
    }

    public static void main(String[] args) {
        String text = "荣耀Play6T Pro 天玑810 40W超级快充 6nm疾速芯 4800万超清双摄 全网通 5G手机 8GB+256GB 钛空银";
        System.out.println(KeywordUtil.analyze(text));
    }
}

