package com.xiaozhu.etl.app;

import com.xiaozhu.etl.bean.KeywordStats;
import com.xiaozhu.etl.common.Constant;
import com.xiaozhu.etl.function.KeywordUDTF;
import com.xiaozhu.etl.utils.ClickhouseUtil;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class DWSKeywordStatsApp {
    public static void main(String[] args) throws Exception {
        // TODO 1 环境准备
        // 1.1 流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 1.2 表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 1.3 设置并行度
        env.setParallelism(4);

        // TODO 2 检查点相关设置（略）

        // TODO 3 从指定的数据源（kafka）读取数据，转换为动态表
        String topic = "dwd_page_log";
        String groupId = "keyword_stats_app_group";
        // 表字段要和JSON的属性一一对应
        tableEnv.executeSql("CREATE TABLE page_view (" +
                " common MAP<STRING,STRING>," +
                " page MAP<STRING,STRING>," +
                " ts BIGINT," +
                " rowtime AS TO_TIMESTAMP(FROM_UNIXTIME(ts/1000, 'yyyy-MM-dd HH:mm:ss'))," +
                " WATERMARK FOR rowtime AS rowtime - INTERVAL '3' SECOND" +
                " ) WITH (" + KafkaUtils.getKafkaDDL(topic,groupId) + ")");


        // TODO 4 将动态表中表示搜索行为的记录过滤出来
        Table fullwordTable = tableEnv.sqlQuery("select " +
                "  page['item'] fullword,rowtime " +
                " from " +
                "  page_view " +
                " where " +
                "  page['page_id']='good_list' and page['item'] is not null");

        // TODO 2 注册自定义UDTF函数
        tableEnv.createTemporarySystemFunction("ik_analyze", KeywordUDTF.class);

        // TODO 5 使用自定义UDTF函数对搜索关键词进行拆分
        Table keywordTable = tableEnv.sqlQuery("SELECT rowtime, keyword FROM "+ fullwordTable +", LATERAL TABLE(ik_analyze(fullword)) AS T(keyword)");

        // TODO 6 分组、开窗、聚合计算
        Table resTable = tableEnv.sqlQuery("select " +
                "  DATE_FORMAT(TUMBLE_START(rowtime, INTERVAL '10' SECOND),'yyyy-MM-dd HH:mm:ss') as stt, " +
                "  DATE_FORMAT(TUMBLE_END(rowtime, INTERVAL '10' SECOND),'yyyy-MM-dd HH:mm:ss') as edt, " +
                "  keyword, " +
                "  count(*) ct," +
                "  '"+ Constant.KEYWORD_SEARCH +"' source," +
                "  UNIX_TIMESTAMP() * 1000 as ts" +
                " from " +
                "  "+ keywordTable +" " +
                " group by " +
                "  TUMBLE(rowtime, INTERVAL '10' SECOND),keyword ");

        // TODO 7 将表转换为流
        DataStream<KeywordStats> keywordStatsDS = tableEnv.toAppendStream(resTable, KeywordStats.class);
        keywordStatsDS.print(">>>");

        // TODO 8 将流中数据写入ck中
        keywordStatsDS.addSink(ClickhouseUtil.getJdbcSink(
                // 字段顺序与实体类中属性顺序要一致
                "insert into keyword_stats_2022(keyword,ct,source,stt,edt,ts) values(?,?,?,?,?,?) "
        ));


        env.execute();
    }
}

