FROM ubuntu:jammy

RUN apt-get update \
&& apt-get install -y openjdk-17-jdk wget tar curl

ENV SPARK_VERSION=3.4.3 \
SCALA_VERSION=2.13 \
HADOOP_VERSION=3 \
SPARK_HOME=/opt/spark \
PATH=$PATH:$SPARK_HOME/bin

RUN wget --no-verbose -O apache-spark.tgz "https://dlcdn.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" \
&& mkdir -p /opt/spark \
&& tar -xf apache-spark.tgz -C /opt/spark --strip-components=1 \
&& rm apache-spark.tgz

RUN curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup -y