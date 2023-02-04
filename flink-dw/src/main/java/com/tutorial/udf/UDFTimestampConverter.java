package com.tutorial.udf;

import org.apache.flink.table.functions.ScalarFunction;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UDFTimestampConverter extends ScalarFunction {
    public String eval(Timestamp timestamp, String format) {
        LocalDateTime nonZoneDateTime = timestamp.toLocalDateTime();
        ZonedDateTime utcZoneDateTime = ZonedDateTime.of(nonZoneDateTime, ZoneId.of("UTC"));

        ZonedDateTime targetZoneDateTime = utcZoneDateTime.withZoneSameInstant(ZoneId.of("+08:00"));

        return targetZoneDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public String eval(Timestamp timestamp, String format, String zoneOffset) {
        LocalDateTime noZoneDatatime = timestamp.toLocalDateTime();
        ZonedDateTime utcZoneDatetime = ZonedDateTime.of(noZoneDatatime, ZoneId.of("UTC"));
        ZonedDateTime targetZoneDatatime = utcZoneDatetime.withZoneSameInstant(ZoneId.of(zoneOffset));
        return targetZoneDatatime.format(DateTimeFormatter.ofPattern(format));
    }
}
