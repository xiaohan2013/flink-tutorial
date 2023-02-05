package com.xiaozhu.etl.utils;

import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.common.Config;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 从phoenix表中查询数据
 */

public class PhoenixUtil {
    private static Connection conn;

    private static void initConn() {
        try {
            // 注册驱动
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            // 获取连接
            conn = DriverManager.getConnection(Config.PHOENIX_SERVER);
            // 设置操作的表空间
            conn.setSchema(Config.HBASE_SCHEMA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 执行查询sql，将查询结果封装为T类型对象，放到List中
    public static <T> List<T> queryList(String sql, Class<T> clz){
        if (conn == null){
            initConn();
        }
        List<T> resList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            // 创建数据库操作对象
            ps = conn.prepareStatement(sql);
            // 执行SQL语句
            rs = ps.executeQuery();
            // 获取查询结果集的元数据信息
            ResultSetMetaData metaData = rs.getMetaData();

            // 处理结果集
            // ID| TM_NAME
            // 1 | zhangsan
            while (rs.next()){
                // 通过反射创建要封装的对象
                T obj = clz.newInstance();
                // 对所有列进行遍历，以获取列的名称
                // jdbc操作时列从1开始
                for (int i = 1; i <= metaData.getColumnCount(); i++){
                    String columnName = metaData.getColumnName(i);
                    // 通过BeanUtils工具类给对象的属性赋值
                    BeanUtils.setProperty(obj,columnName,rs.getObject(i));
                }
                // 将封装的对象放到List集合中
                resList.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return resList;
    }


    // 测试
    public static void main(String[] args) {
        List<JSONObject> jsonObjectList = queryList("select * from dim_base_trademark", JSONObject.class);
        System.out.println(jsonObjectList);
    }

}
