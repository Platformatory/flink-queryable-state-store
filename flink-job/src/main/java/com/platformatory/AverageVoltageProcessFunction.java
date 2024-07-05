package com.platformatory;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AverageVoltageProcessFunction extends KeyedProcessFunction<String, SensorReading, String> {
    private transient ValueState<Tuple4<Double, Integer, Double, Double>> voltageState;

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) {
        ValueStateDescriptor<Tuple4<Double, Integer, Double, Double>> descriptor =
                new ValueStateDescriptor<>("voltageState", Types.TUPLE(Types.DOUBLE, Types.INT, Types.DOUBLE, Types.DOUBLE));
        descriptor.setQueryable("average-voltage-query");
        voltageState = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void processElement(SensorReading value, Context ctx, Collector<String> out) throws Exception {
        System.out.println("Key - "+ctx.getCurrentKey());
        Tuple4<Double, Integer, Double, Double> currentState = voltageState.value();
        if (currentState == null) {
            currentState = Tuple4.of(0.0, 0, 0.0, 0.0);
        }

        currentState.f0 += value.getVoltage();
        currentState.f1 += 1;
        currentState.f2 = Math.min(currentState.f2, value.getVoltage());
        currentState.f3 = Math.max(currentState.f3, value.getVoltage());
        voltageState.update(currentState);

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getTimestamp()), ZoneId.systemDefault());
        LocalDateTime hourKey = dateTime.withMinute(0).withSecond(0).withNano(0);

        Double averageVoltage = currentState.f0 / currentState.f1;

        String result = String.format("{\"household\":\"%s\",\"hour\":\"%s\",\"average_voltage\":%.2f,\"minimum_voltage\":%.2f,\"maximum_voltage\":%.2f}",
                ctx.getCurrentKey(), hourKey.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), averageVoltage, currentState.f2, currentState.f3);

        out.collect(result);
    }
}
