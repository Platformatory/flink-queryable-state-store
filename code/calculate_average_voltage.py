from pyflink.common import SimpleStringSchema, Types
from pyflink.datastream import StreamExecutionEnvironment, TimeCharacteristic
from pyflink.datastream.connectors import FlinkKafkaConsumer, FlinkKafkaProducer
from pyflink.datastream.functions import KeyedProcessFunction, RuntimeContext
from pyflink.common.typeinfo import Types
from pyflink.datastream.state import ValueStateDescriptor
from pyflink.datastream.watermark_strategy import WatermarkStrategy

import json
from datetime import datetime

class AverageVoltageProcessFunction(KeyedProcessFunction):
    def open(self, runtime_context: RuntimeContext):
        self.voltage_state = runtime_context.get_state(
            ValueStateDescriptor("voltage_state", Types.TUPLE([Types.FLOAT(), Types.INT()]))
        )

    def process_element(self, value, ctx: 'KeyedProcessFunction.Context'):
        current_voltage = float(value['Voltage'])
        state = self.voltage_state.value()

        if state is not None:
            total_voltage, count = state
            total_voltage += current_voltage
            count += 1
        else:
            total_voltage, count = current_voltage, 1

        self.voltage_state.update((total_voltage, count))

        # Calculate average hourly voltage based on the timestamp in the input data
        date_str = value['Date']
        time_str = value['Time']
        timestamp = datetime.strptime(f"{date_str} {time_str}", "%Y-%m-%d %H:%M:%S")
        hour_key = timestamp.replace(minute=0, second=0, microsecond=0)

        average_voltage = total_voltage / count
        result = {
            "household": ctx.get_current_key(),
            "hour": hour_key.strftime("%Y-%m-%d %H:%M:%S"),
            "average_voltage": average_voltage
        }

        ctx.output(result)

def main():
    # Set up the execution environment
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_stream_time_characteristic(TimeCharacteristic.EventTime)

    # Set up the Kafka source
    kafka_source = FlinkKafkaConsumer(
        topics='household_power_consumption',
        deserialization_schema=SimpleStringSchema(),
        properties={
            'bootstrap.servers': 'broker:29092',
            'group.id': 'flink_group'
        }
    )

    # Set up the Kafka sink
    kafka_sink = FlinkKafkaProducer(
        topic='average_voltage',
        serialization_schema=SimpleStringSchema(),
        producer_config={'bootstrap.servers': 'broker:29092'}
    )

    # Create the data stream from Kafka source
    data_stream = env.add_source(kafka_source)

    # Parse the input data
    parsed_stream = data_stream.map(lambda x: json.loads(x), Types.MAP(Types.STRING(), Types.STRING()))

    # Assign timestamps and watermarks
    watermark_strategy = WatermarkStrategy.for_monotonous_timestamps() \
        .with_timestamp_assigner(lambda x, _: int(datetime.strptime(f"{x['Date']} {x['Time']}", "%Y-%m-%d %H:%M:%S").timestamp() * 1000))

    parsed_stream = parsed_stream.assign_timestamps_and_watermarks(watermark_strategy)

    # Key by household and apply the KeyedProcessFunction
    result_stream = parsed_stream.key_by(lambda x: x['household']) \
        .process(AverageVoltageProcessFunction(), output_type=Types.STRING())

    # Sink the result to Kafka
    result_stream.add_sink(kafka_sink)

    # Execute the job
    env.execute("Kafka Flink Average Voltage Job")

if __name__ == '__main__':
    main()
