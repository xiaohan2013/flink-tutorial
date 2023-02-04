package com.tutorial.state;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.ArrayList;
import java.util.List;


/**
 * Operator State两种方式
 * 1. 实现checkpointedFunction接口
 * 2. 实现ListCheckpointed
 * 两个方法
 * 1. initialedState：每个Function在最开始的实例化调用，方法内，实例化状态
 * 2. snapshotState，每次checkpoint时调用，将操作最新数据放到检查中
 * 3. invoke， 没来一个数据调用一次，把所有来到的数据放到缓存中目的为了checkpoint时从缓存那数据
 *
 */
public class OperatorState implements SinkFunction<Tuple2<Long, Long>>, CheckpointedFunction {
    ListState<Tuple2<Long, Long>> operatorState;

    int threshold;

    private List<Tuple2<Long, Long>> bufferedElements;

    public OperatorState(int threshold) {
        this.threshold = threshold;
        this.bufferedElements = new ArrayList<>();
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        this.operatorState.clear();
        for(Tuple2<Long, Long> element: bufferedElements) {
            operatorState.add(element);
        }
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        //
        ListStateDescriptor<Tuple2<Long, Long>> operatorDescriptor = new ListStateDescriptor<Tuple2<Long, Long>>(
                "operator_state",
                TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})
        );

        operatorState = context.getOperatorStateStore().getListState(operatorDescriptor);
        if(context.isRestored()) { // 说明程序中断，容错中。。
            for (Tuple2<Long, Long> element: operatorState.get()) {
                bufferedElements.add(element);
            }
        }
    }

    @Override
    public void invoke(Tuple2<Long, Long> value, Context context) {
        bufferedElements.add(value);
        if(bufferedElements.size() == threshold) {
            for (Tuple2<Long, Long> element: bufferedElements) {
                System.out.println("...out:" + element);
            }
            bufferedElements.clear();
        }
    }
}
