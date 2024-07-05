package com.platformatory;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.queryablestate.client.QueryableStateClient;
import org.apache.flink.api.common.JobID;

import java.util.concurrent.CompletableFuture;

public class StateQueryClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: StateQueryClient <hostname> <jobid> <key>");
            return;
        }

        String hostname = args[0];
        JobID jobId = JobID.fromHexString(args[1]);
        String key = args[2];

        QueryableStateClient client = new QueryableStateClient(hostname, 9069);

        ValueStateDescriptor<Tuple2<Double, Integer>> descriptor =
                new ValueStateDescriptor<>(
                        "average-voltage-query",
                        TypeInformation.of(new TypeHint<Tuple2<Double, Integer>>() {})
                );

        CompletableFuture<ValueState<Tuple2<Double, Integer>>> resultFuture = client.getKvState(
                jobId,
                "average-voltage-query",
                key,
                TypeInformation.of(String.class),
                descriptor);

        ValueState<Tuple2<Double, Integer>> valueState = resultFuture.get();
        Tuple2<Double, Integer> result = valueState.value();

        if (result != null) {
            double averageVoltage = result.f0 / result.f1;
            System.out.println("Average Voltage: " + averageVoltage);
        } else {
            System.out.println("No state found for the given key.");
        }
    }
}
