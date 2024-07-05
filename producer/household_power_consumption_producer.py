import json
from kafka import KafkaProducer
import os
import random

# Initialize Kafka producer
broker = os.getenv('KAFKA_BROKER', 'broker:29092')
producer = KafkaProducer(
    bootstrap_servers=broker,
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)
# Define the topic name
topic = 'household_power_consumption'

# Path to the data file
file_path = 'household_power_consumption.txt'


# Define the field names based on the file structure
field_names = [
    'date', 'time', 'global_active_power', 'global_reactive_power', 'voltage',
    'global_intensity', 'sub_metering_1', 'sub_metering_2', 'sub_metering_3'
]

# Read and produce messages from the file
with open(file_path, 'r') as file:
    # Skip the header line
    next(file)
    
    for line in file:
        # Split the line by semicolon to get the values
        values = line.strip().split(';')
        
        # Create a dictionary from field names and values
        message = dict(zip(field_names, values))

        household_name = b'household_1'

        message['household'] = "household_1"
        
        # Produce a message for each line with the household name as the key
        producer.send(topic, key=household_name, value=message)
        producer.flush()  # Ensure the message is sent

        household_name = b'household_2'

        message['household'] = "household_2"
        message['voltage'] = float(message['voltage']) + round(random.uniform(100,200), 3)

        # Produce a message for each line with the household name as the key
        producer.send(topic, key=household_name, value=message)
        producer.flush()  # Ensure the message is sent

# Close the producer
producer.close()

print("Messages sent successfully.")