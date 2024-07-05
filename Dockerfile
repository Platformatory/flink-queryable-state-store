FROM flink:1.19.1-scala_2.12
RUN echo "Downloading https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.2.0-1.19/flink-sql-connector-kafka-3.2.0-1.19.jar" && \
  wget -q -O /opt/flink/lib/flink-sql-connector-kafka-3.2.0-1.19.jar https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.2.0-1.19/flink-sql-connector-kafka-3.2.0-1.19.jar
# Install Python and pyflink .
RUN apt-get update && \
  apt-get install -y build-essential libssl-dev zlib1g-dev liblzma-dev libbz2-dev libffi-dev && \
  wget https://www.python.org/ftp/python/3.10.14/Python-3.10.14.tgz && \
  tar -xvf Python-3.10.14.tgz && \
  cd Python-3.10.14 && \
  ./configure --without-tests --enable-shared --enable-optimizations && \
  make -j6 && \
  make install && \
  ldconfig /usr/local/lib && \
  cd .. && rm -f Python-3.10.14.tgz && rm -rf Python-3.10.14 && \
  ln -s /usr/local/bin/python3 /usr/local/bin/python && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  pip3 install apache-flink==1.19.1 jsonpath_ng==1.6.0 geopy==2.4.0 scikit-learn
COPY code /opt/flink/code