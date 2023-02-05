package com.xiaozhu.etl.function;

import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.bean.TableProcess;
import com.xiaozhu.etl.common.Config;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HbaseTableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {
    // 声明维度侧输出流标签
    private OutputTag<JSONObject> dimTag;
    // 声明广播状态描述器
    private MapStateDescriptor<String, TableProcess> mapStateDescriptor;
    // 声明连接对象
    private Connection conn;

    @Override
    public void open(Configuration parameters) throws Exception {
        // 注册驱动
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        // 获取连接
        conn = DriverManager.getConnection(Config.PHOENIX_SERVER);
    }

    public HbaseTableProcessFunction(OutputTag<JSONObject> dimTag, MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.dimTag = dimTag;
        this.mapStateDescriptor = mapStateDescriptor;
    }

    // 处理业务流中的数据，maxwell从业务数据库中采集到的数据
    @Override
    public void processElement(JSONObject jsonObj, ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
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

            // 在向下游传递数据之前，将不需要的字段过滤掉
            // 过滤思路：从配置表中读取保留字段，根据保留字段对data中的属性进行过滤
            JSONObject dataJsonObj = jsonObj.getJSONObject("data");
            filterColumns(dataJsonObj,tableProcess.getSinkColumns());

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

    // 过滤字段
    private void filterColumns(JSONObject dataJsonObj, String sinkColumns) {
        // dataJsonObj : {"tm_name":"aaa","logo_url":"aaa","id":12}
        // sinkColumns : id,tm_name
        String[] columnArr = sinkColumns.split(",");
        // 将数组转换成集合，以便下面和entrySet进行比较，数组中没有”包含“方法
        List<String> columnList = Arrays.asList(columnArr);

        // 获取json中的每一个名值对(KV)
        Set<Map.Entry<String, Object>> entrySet = dataJsonObj.entrySet();
//        // 获取迭代器
//        Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
//        // 遍历，如果不包含则删除
//        for (;it.hasNext();) {
//            if(!columnList.contains(it.next().getKey())){
//                it.remove();
//            }
//        }
        entrySet.removeIf(entry -> !columnList.contains(entry.getKey()));
    }

    // 处理广播流中的数据，FlinkCDC从MySQL中读取配置信息
    @Override
    public void processBroadcastElement(String jsonStr, Context ctx, Collector<JSONObject> out) throws Exception {
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

        // 如果读取到的配置信息是维度数据，提前创建维度表
        if (sinkType.equals(TableProcess.SINK_TYPE_HBASE) && "insert".equals(operateType)){
            checkTable(sinkTable,sinkPk,sinkColumns,sinkExtend);
        }

        // 将配置信息放到状态中
        // 拼接key
        String key = sourceTable + ":" + operateType;
        broadcastState.put(key,tableProcess);
    }

    // 在处理配置数据时，提前建立维度表
    // create table if not exists 表空间.表名（字段名 数据类型,字段名 数据类型）
    private void checkTable(String tableName, String pk, String fields, String ext) throws SQLException {
        // 对主键进行空值处理
        if (pk == null){
            pk = "id";
        }
        // 对建表扩展进行空值处理
        if (ext == null){
            ext = "";
        }
        StringBuilder createSql = new StringBuilder("create table if not exists "+
                Config.HBASE_SCHEMA + "." + tableName +"(");

        String[] fieldArr = fields.split(",");
        for (int i = 0; i < fieldArr.length; i++) {
            String field = fieldArr[i];
            // 判断是否为主键
            if (field.equals(pk)){
                createSql.append(field + " varchar primary key ");
            }else {
                createSql.append(field + " varchar ");
            }
            if(i < fieldArr.length - 1){
                createSql.append(",");
            }
        }
        createSql.append(")" + ext);
        System.out.println("phoenix中的建表语句：" + createSql);


        // 创建数据库操作对象
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(createSql.toString());
            // 执行sql语句
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("在phoenix中建表失败");
        } finally {
            // 释放资源
            if(ps != null){
                ps.close();
            }
        }
    }

}
