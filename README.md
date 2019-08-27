# Apache Kafka sampler for JMeter

TODO: brief info

## Prerequisites

To run tests locally the following tools are required:

* Java 1.8+
* Maven
* JMeter 5
* Docker compose

## Running locally

There is prepared the ``docker-compose.yaml`` file which allows you to run the following services:

* Kafka
* Zookeeper

To run all services, execute the following command:

```bash
docker-compose up -d
```

To crate JMeter's plugin, execute the following command:

```bash
mvn package
```

To install plugin in JMeter, copy the compiled java library to the JMeter's extensions catalog: ``${JMETER_HOME}\lib\ext``.

## Configuration

TODO