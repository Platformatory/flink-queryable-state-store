package com.platformatory;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.queryablestate.client.QueryableStateClient;
import org.apache.flink.api.common.JobID;

import java.util.concurrent.CompletableFuture;

public class StateQueryClient {

    private final QueryableStateClient client;
    private final JobID jobId;

    public StateQueryClient(String hostname, int port, String jobId) throws Exception {
        this.client = new QueryableStateClient(hostname, port);
        this.jobId = JobID.fromHexString(jobId);
    }

    public String queryState(String key) throws Exception {
        ValueStateDescriptor<Tuple4<Double, Integer, Double, Double>> descriptor =
                new ValueStateDescriptor<>(
                        "average-voltage-query",
                        TypeInformation.of(new TypeHint<Tuple4<Double, Integer, Double, Double>>() {})
                );

        CompletableFuture<ValueState<Tuple4<Double, Integer, Double, Double>>> resultFuture = client.getKvState(
                jobId,
                "average-voltage-query",
                key,
                TypeInformation.of(String.class),
                descriptor);

        ValueState<Tuple4<Double, Integer, Double, Double>> valueState = resultFuture.get();
        Tuple4<Double, Integer, Double, Double> result = valueState.value();

        if (result != null) {
            double averageVoltage = result.f0 / result.f1;
            return String.format("{\"key\":\"%s\", \"average_voltage\":%.2f, \"minimum_voltage\":%.2f, \"maximum_voltage\":%.2f}", key, averageVoltage, result.f2, result.f3);
        } else {
            return String.format("{\"key\":\"%s\", \"message\":\"No state found for the given key.\"}", key);
        }
    }
}
