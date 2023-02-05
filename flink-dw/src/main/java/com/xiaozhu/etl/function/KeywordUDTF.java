package com.xiaozhu.etl.function;

import com.xiaozhu.etl.utils.KeywordUtil;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

import java.util.List;

/**
 * 自定义UDTF函数
 */
// 注解表示Row（一行）中有几列，列名是什么
@FunctionHint(output = @DataTypeHint("ROW<word STRING>"))
public class KeywordUDTF extends TableFunction<Row> {

    public void eval(String text) {
        List<String> keywordList = KeywordUtil.analyze(text);
        for (String keyword : keywordList) {
            // use collect(...) to emit a row
            collect(Row.of(keyword));
        }
    }
}

