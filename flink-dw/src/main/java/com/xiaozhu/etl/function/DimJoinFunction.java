package com.xiaozhu.etl.function;

import com.alibaba.fastjson.JSONObject;

// 维度关联查询的接口
public interface DimJoinFunction<T> {

    /**
     * 需要实现如何把结果装配给数据流对象
     * @param obj  数据流对象
     * @param dimJsonObj   异步查询结果
     * @throws Exception
     */
    void join(T obj, JSONObject dimJsonObj) throws Exception;

    /**
     * 需要实现如何从流中对象获取主键
     * @param obj  数据流对象
     */
    String getKey(T obj);
}
