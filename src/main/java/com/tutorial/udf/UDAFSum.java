package com.tutorial.udf;

import org.apache.flink.api.java.aggregation.AggregationFunction;

public class UDAFSum extends AggregationFunction{
    @Override
    public void initializeAggregate() {

    }

    @Override
    public void aggregate(Object value) {

    }

    @Override
    public Long getAggregate() {
        return null;
    }

    public static class SumAccumulator {
        public long sumPrice;
    }
}
