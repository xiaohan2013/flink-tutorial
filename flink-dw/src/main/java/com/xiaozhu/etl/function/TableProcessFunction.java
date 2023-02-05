package com.xiaozhu.etl.function;

import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.bean.TableProcess;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * 实现动态分流
 * 目前流中有两条流中的数据，使用以下两个方法分别进行处理
 */
public class TableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {
    // 声明维度侧输出流标签
    private OutputTag<JSONObject> dimTag;
    // 声明广播状态描述器
    private MapStateDescriptor<String, TableProcess> mapStateDescriptor;

    public TableProcessFunction(OutputTag<JSONObject> dimTag, MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.dimTag = dimTag;
        this.mapStateDescriptor = mapStateDescriptor;
    }

    // 处理业务流中的数据，maxwell从业务数据库中采集到的数据
    @Override
    public void processElement(JSONObject jsonObj, BroadcastProcessFunction<JSONObject, String, JSONObject>.ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
        String table = jsonObj.getString("table");
        String type = jsonObj.getString("type");

        // 在使用maxwell处理历史数据的时候，类型是bootstrap-insert，修复为insert
        if (type.equals("bootstrap-insert")){
            type = "insert";
            jsonObj.put("type",type);
        }

        // 拼接key
        String key = table  + ":" + type;
        // 获取状态
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        // 从状态中获取配置信息
        TableProcess tableProcess = broadcastState.get(key);

        if (tableProcess != null){
            // 在配置表中找到了该操作对应的配置
            // 判断是事实数据还是维度数据
            String sinkTable = tableProcess.getSinkTable();
            jsonObj.put("sink_table",sinkTable);

            String sinkType = tableProcess.getSinkType();
            if (sinkType.equals(TableProcess.SINK_TYPE_HBASE)){
                // 维度数据，放到维度侧输出流中
                ctx.output(dimTag,jsonObj);
            }else if (sinkType.equals(TableProcess.SINK_TYPE_KAFKA)){
                // 事实数据，放到主流中
                out.collect(jsonObj);
            }
        }else {
            // 在配置表中没有该操作对应的配置
            System.out.println("No This Key In TableProcess:" + key);
        }
    }

    // // 处理广播流中的数据，FlinkCDC从MySQL中读取配置信息
    @Override
    public void processBroadcastElement(String jsonStr, BroadcastProcessFunction<JSONObject, String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
        // 获取状态
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        // 将json格式字符串转换为JSON对象
        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        // 获取配置表中的一条配置信息
        // parseObject：将json格式字符串转化为json格式对象
        // 第二个参数为将json字符串转化为何种格式的对象
        TableProcess tableProcess = JSONObject.parseObject(jsonObj.getString("data"), TableProcess.class);
        // 业务数据库表名
        String sourceTable = tableProcess.getSourceTable();
        // 操作类型
        String operateType = tableProcess.getOperateType();
        // 数据类型 hbase -- 维度数据    kafka -- 事实数据
        String sinkType = tableProcess.getSinkType();
        // 指定输出目的地
        String sinkTable = tableProcess.getSinkTable();
        // 主键
        String sinkPk = tableProcess.getSinkPk();
        // 指定保留字段（列）
        String sinkColumns = tableProcess.getSinkColumns();
        // 指定建表扩展语句
        String sinkExtend = tableProcess.getSinkExtend();

        // 将配置信息放到状态中
        // 拼接key
        String key = sourceTable + ":" + operateType;
        broadcastState.put(key,tableProcess);
    }
}
