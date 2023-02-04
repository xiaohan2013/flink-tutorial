package com.tutorial.kafka;

import com.tutorial.kafka.proto.AccessLogProto;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.springframework.beans.BeanUtils;

import java.io.IOException;

public class CustomSerialSchema implements DeserializationSchema<AccessLog>, SerializationSchema<AccessLog> {
    private static final long serialVersionUID = 1L;

    @Override
    public AccessLog deserialize(byte[] message) throws IOException {
        AccessLog accessLog = null;
        AccessLogProto.AccessLog accessLogProto = AccessLogProto.AccessLog.parseFrom(message);
        accessLog = new AccessLog();
        BeanUtils.copyProperties(accessLogProto, accessLog);
        return accessLog;
    }

    @Override
    public boolean isEndOfStream(AccessLog nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(AccessLog element) {
        AccessLogProto.AccessLog.Builder builder = AccessLogProto.AccessLog.newBuilder();
        BeanUtils.copyProperties(element, builder);
        return builder.build().toByteArray();
    }

    @Override
    public TypeInformation<AccessLog> getProducedType() {
        return TypeInformation.of(AccessLog.class);
    }
}
