package com.xiaozhu.etl.function;

import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.utils.DimUtil;
import com.xiaozhu.etl.utils.ThreadPoolUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.util.Collections;
import java.util.concurrent.ExecutorService;


/**
 * 实现维度的异步关联
 * 模板方法设计模式：在父类中定义实现某一个功能的核心算法骨架，将具体的实现延迟到子类中去完成
 *                  子类不改变父类核心算法骨架的前提下，每一个子类都可以有自己的独立实现。
 */
public abstract class DimAsyncFunction<T> extends RichAsyncFunction<T,T> implements DimJoinFunction<T>{

    private ExecutorService executorService;
    private String tableName;

    public DimAsyncFunction(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // 创建线程池对象
        executorService = ThreadPoolUtil.getInstance();
    }

    // 发送异步请求，完成维度关联
    // 通过创建多线程的方式发送异步请求
    // 此方法每处理流中的一条数据，都会执行一次
    @Override
    public void asyncInvoke(T obj, ResultFuture<T> resultFuture) throws Exception {
        // 通过线程池获取线程
        executorService.submit(new Runnable() {
            // run中的代码实现的就是异步维度关联操作
            @Override
            public void run() {
                try {
                    long start = System.currentTimeMillis();
                    // 从对象中获取维度关联的key
                    String key = getKey(obj);
                    // 根据key到维度表中获取维度对象
                    JSONObject dimJsonObj = DimUtil.getDimInfo(tableName, key);
                    // 把维度对象的属性赋值给流中对象属性（维度关联）
                    if (dimJsonObj != null){
                        join(obj,dimJsonObj);
                    }
                    long end = System.currentTimeMillis();
                    System.out.println("维度异步查询耗时：" + (end - start) + "毫秒");
                    resultFuture.complete(Collections.singleton(obj));
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("维度异步查询发生异常...");
                }
            }
        });
    }
}
