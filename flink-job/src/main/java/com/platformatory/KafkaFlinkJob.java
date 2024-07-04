package com.platformatory;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
// import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

public class KafkaFlinkJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:29092");
        properties.setProperty("group.id", "flink_group");

        FlinkKafkaConsumer<SensorReading> kafkaSource = new FlinkKafkaConsumer<>(
                "household_power_consumption",
                new SensorReadingDeserializationSchema(),
                properties);

        kafkaSource.setStartFromEarliest();

        kafkaSource.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<SensorReading>(Time.seconds(10)) {
            @Override
            public long extractTimestamp(SensorReading element) {
                return element.getTimestamp();
            }
        });

        FlinkKafkaProducer<String> kafkaSink = new FlinkKafkaProducer<>(
                "average_voltage",
                new SimpleStringSchema(),
                properties);

        env.addSource(kafkaSource)
                .keyBy(value -> "static_key")
                .process(new AverageVoltageProcessFunction())
                .addSink(kafkaSink);

        env.execute("Kafka Flink Average Voltage Job");
    }
}
