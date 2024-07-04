from pyflink.common.serialization import SimpleStringSchema
from pyflink.datastream.formats.json import JsonRowSerializationSchema
from pyflink.common.typeinfo import Types
from pyflink.datastream import StreamExecutionEnvironment, CheckpointingMode, ExternalizedCheckpointCleanup, EmbeddedRocksDBStateBackend, KeySelector, RuntimeContext
from pyflink.datastream.connectors.kafka import KafkaSink, KafkaRecordSerializationSchema, DeliveryGuarantee, KafkaSource, KafkaOffsetsInitializer
from pyflink.datastream.functions import KeyedProcessFunction
from pyflink.datastream.state import ValueStateDescriptor
from pyflink.datastream.time_characteristic import TimeCharacteristic
# from pyflink.table import StreamTableEnvironment
from pyflink.common import Row, Duration
from pyflink.common.watermark_strategy import WatermarkStrategy, TimestampAssigner
import json
from datetime import datetime


class CaptureEventChangeProcessFunction(KeyedProcessFunction):
    ## Initializing state for the given Key
    def __init__(self):
        self.last_state = None

    def open(self, runtime_context: RuntimeContext):
        state_descriptor = ValueStateDescriptor("last_state", Types.STRING())
        self.last_state = runtime_context.get_state(state_descriptor)

    def process_element(self, value, ctx) -> Row:
        # Get the current state for the key
        current_state = value["event_value"]

        key = ctx.get_current_key()
        if not isinstance(key, int):
            key = int(key) # Accessing the key
        
        timestamp = ctx.timestamp() # Accessing the timestamp
        if isinstance(timestamp, int):
            ## This is the working one
            timestamp = datetime.fromtimestamp(timestamp / 1000)
        elif isinstance(timestamp, str):
            timestamp = datetime.strptime(timestamp, "%Y-%m-%d %H:%M:%S")
        elif isinstance(timestamp, datetime):
            pass
        else:
            timestamp = datetime.utcnow()

        # Get the last state for the key from the value state
        last_state = self.last_state.value()

        current_state_hash = hash(current_state)
        last_state_hash = hash(last_state)

        if current_state_hash != last_state_hash:
            # If the current state is different from the last state, update the last state
            self.last_state.update(current_state)

            yield Row(event_id=key, modified_at=timestamp, before_value=last_state, after_value=current_state)

def parse_event_key(element: str) -> Row:
    ## Event Key Extraction Logic
    val = json.loads(element)
    event_id = val["id"]
    return Row(event_id=event_id, event_value=element)

class EventKeySelector(KeySelector):
    ## Setting Event Key as Record Key
    def get_key(self, value):
        return value["event_id"]

class ApiLogsTimestampAssigner(TimestampAssigner):

    def extract_timestamp(self, value, record_timestamp) -> int:
        ## Event Time Logic for Stream
        modified_at = datetime.utcnow()
        return modified_at


env = StreamExecutionEnvironment.get_execution_environment()

env.set_parallelism(1)

env.set_stream_time_characteristic(TimeCharacteristic.EventTime)

# start a checkpoint every 1000 ms
env.enable_checkpointing(1000)

# set mode to exactly-once (this is the default)
env.get_checkpoint_config().set_checkpointing_mode(CheckpointingMode.EXACTLY_ONCE)

# make sure 500 ms of progress happen between checkpoints
env.get_checkpoint_config().set_min_pause_between_checkpoints(500)

# checkpoints have to complete within one minute, or are discarded
env.get_checkpoint_config().set_checkpoint_timeout(10000)

# only two consecutive checkpoint failures are tolerated
env.get_checkpoint_config().set_tolerable_checkpoint_failure_number(2)

# allow only one checkpoint to be in progress at the same time
env.get_checkpoint_config().set_max_concurrent_checkpoints(1)

# enable externalized checkpoints which are retained after job cancellation
env.get_checkpoint_config().enable_externalized_checkpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

# enables the unaligned checkpoints
env.get_checkpoint_config().enable_unaligned_checkpoints()

# Checkpoint storage location
env.get_checkpoint_config().set_checkpoint_storage_dir("s3a://odx-flink-bucket/rocksdb-state")

# State backend settings
env.set_state_backend(EmbeddedRocksDBStateBackend(enable_incremental_checkpointing=True))

# t_env = StreamTableEnvironment.create(env)


api_logs_watermark_strategy = WatermarkStrategy.for_bounded_out_of_orderness(Duration.of_seconds(10)).with_idleness(Duration.of_seconds(2)).with_timestamp_assigner(ApiLogsTimestampAssigner())

source = KafkaSource.builder() \
    .set_bootstrap_servers("broker:29092") \
    .set_topics("api_logs_stream") \
    .set_group_id("logsGroup1") \
    .set_property("max.partition.fetch.bytes", "900000") \
    .set_starting_offsets(KafkaOffsetsInitializer.earliest()) \
    .set_value_only_deserializer(SimpleStringSchema()) \
    .build()

api_logs = env.from_source(source, api_logs_watermark_strategy, "kafka_source")

row_type_info = Types.ROW_NAMED(field_names=["event_id", "modified_at", "before_value", "after_value"], field_types=[Types.LONG(), Types.SQL_TIMESTAMP(), Types.STRING(), Types.STRING()])
cdc_json_format = JsonRowSerializationSchema.builder().with_type_info(row_type_info).build()

sink = KafkaSink.builder() \
        .set_bootstrap_servers("broker:29092") \
        .set_record_serializer(
            KafkaRecordSerializationSchema.builder() \
                .set_topic("demo-api-events") \
                .set_value_serialization_schema(cdc_json_format) \
                .build()
        ) \
        .set_delivery_guarantee(DeliveryGuarantee.EXACTLY_ONCE) \
        .set_property("batch.size", "450000") \
        .set_property("max.request.size", "974857") \
        .set_transactional_id_prefix("apicdc") \
        .set_property("transaction.timeout.ms", "60000") \
        .build()

api_cdc = api_logs.map(lambda value: parse_event_key(value), output_type=Types.ROW_NAMED(field_names=["event_id", "event_value"], field_types=[Types.LONG(), Types.STRING()])) \
        .key_by(EventKeySelector(), key_type=Types.LONG()) \
        .process(CaptureEventChangeProcessFunction(), output_type=row_type_info) \
        .sink_to(sink)
        

# Execute the job
env.execute("Capture Api Event Change")
