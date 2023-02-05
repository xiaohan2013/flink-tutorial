package com.xiaozhu.etl.utils;

import com.google.common.base.CaseFormat;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtil {

    public static <T> List<T> queryList(Connection connection,
                                        String querySql,
                                        Class<T> clz,
                                        boolean underScoreToCamel) throws Exception {


        List<T> list = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(querySql);
        ResultSet resultSet = preparedStatement.executeQuery();

        // 获取元数据信息
        ResultSetMetaData metaData = resultSet.getMetaData();
        // 获取列的个数
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()){
            // 创建泛型对象
            T t = clz.newInstance();

            // 给泛型对象赋值
            for (int i = 1; i < columnCount + 1; i++) {
                // 获取列名称
                String columnName = metaData.getColumnName(i);

                if(underScoreToCamel){
                    // 驼峰命名
                    columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,columnName.toLowerCase());
                }
                // 获取列值
                Object value = resultSet.getObject(i);
                //给T进行赋值
                BeanUtils.setProperty(t,columnName,value);
            }
            // 将该对象添加到集合
            list.add(t);
        }
        resultSet.close();
        preparedStatement.close();
        return list;
    }


}


