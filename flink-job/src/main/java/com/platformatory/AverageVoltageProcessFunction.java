package com.platformatory;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AverageVoltageProcessFunction extends KeyedProcessFunction<String, SensorReading, String> {
    private transient ValueState<Tuple2<Double, Integer>> voltageState;

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) {
        ValueStateDescriptor<Tuple2<Double, Integer>> descriptor =
                new ValueStateDescriptor<>("voltageState", Types.TUPLE(Types.DOUBLE, Types.INT));
        voltageState = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void processElement(SensorReading value, Context ctx, Collector<String> out) throws Exception {
        Tuple2<Double, Integer> currentState = voltageState.value();
        if (currentState == null) {
            currentState = Tuple2.of(0.0, 0);
        }

        currentState.f0 += value.getVoltage();
        currentState.f1 += 1;
        voltageState.update(currentState);

        LocalDateTime dateTime = LocalDateTime.parse(value.getDate() + " " + value.getTime(),
                DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss"));
        LocalDateTime hourKey = dateTime.withMinute(0).withSecond(0).withNano(0);

        Double averageVoltage = currentState.f0 / currentState.f1;

        String result = String.format("{\"hour\":\"%s\",\"average_voltage\":%.2f}",
                hourKey.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), averageVoltage);

        out.collect(result);
    }
}
