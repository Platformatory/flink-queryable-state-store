# Queryable State Store in Apache Flink

Source code for a demo on querying the state store in Apache Flink for a talk at the Bengaluru Streams meetup. The recoding of the talk can be found on [YouTube](https://youtu.be/RmZUgSQwVRM).

## Setup

1. Build the Flink job JAR

```bash
cd flink-job
mvn clean package
```

2. Bring up the environment

```bash
docker-compose up -d broker flink-jobmanager flink-taskmanager
```

3. Start the producer

```bash
docker-compose up -d producer
```

4. Submit job to jobmanager

```bash
docker-compose up -d submit
```

5. Start query client application

```bash
docker-compose logs submit
# Copy job ID from the line `Job has been submitted with JobID cece02e443b75ce6dcbe46d2a4f5a742`
# Here cece02e443b75ce6dcbe46d2a4f5a742 is the JobID, replace it with your own
# Replace the job ID in the client parameters
sed -i 's/flinkJobID/cece02e443b75ce6dcbe46d2a4f5a742/g' docker-compose.yml

docker-compose up -d query-client
```

6. Query the state store

```bash
curl localhost:8080/query/household_1
# {"key":"household_1", "average_voltage":241.12, "minimum_voltage":0.00, "maximum_voltage":249.48}
curl localhost:8080/query/household_2
# {"key":"household_2", "average_voltage":391.97, "minimum_voltage":0.00, "maximum_voltage":448.97}
```