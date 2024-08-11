FROM flink:1.19.1

RUN apt update && apt install net-tools

RUN echo "Downloading https://repo1.maven.org/maven2/org/apache/flink/flink-queryable-state-runtime/1.19.1/flink-queryable-state-runtime-1.19.1.jar" && \
  wget -q -O /opt/flink/lib/flink-queryable-state-runtime-1.19.1.jar https://repo1.maven.org/maven2/org/apache/flink/flink-queryable-state-runtime/1.19.1/flink-queryable-state-runtime-1.19.1.jar