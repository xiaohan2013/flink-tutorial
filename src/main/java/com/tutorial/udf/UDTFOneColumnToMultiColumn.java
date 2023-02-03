package com.tutorial.udf;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.scala.typeutils.Types;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;



public class UDTFOneColumnToMultiColumn extends TableFunction<Row> {
    public void eval(String value) {

    }

    @Override
    public TypeInformation<Row> getResultType() {
        return new RowTypeInfo(Types.STRING(), Types.STRING());
    }
}
