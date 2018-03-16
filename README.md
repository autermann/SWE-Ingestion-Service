#### status: in development progress

# SWE-Ingestion-Service

The SensorWebEnabled-Ingestion-Service is an easy to configure and easy to deploy Data Flow Web application. It utilizes the Spring Cloud DataFlow Server technology serving as a **TODO: bla bla.** make use of the Source-Processor-Sink 


## Libraries and Licenses

#### Third party libraries and licenses

|Library|License|Link/Source|
|:----:|:----:|:----:|
|Spring-framework|Apache License Version 2.0|[https://github.com/spring-projects/spring-framework/blob/master/src/docs/dist/license.txt](https://github.com/spring-projects/spring-framework/blob/master/src/docs/dist/license.txt)|

#### SWE-Ingestion-Service

|Library|License|Link/Source|
|:----:|:----:|:-----:|
|SWE-Ingestion-Service|Apache License Version 2.0|[https://github.com/MojioMS/SWE-Ingestion-Service/blob/master/LICENSE](https://github.com/MojioMS/SWE-Ingestion-Service/blob/master/LICENSE)|

## Requirements

1. A running instance of rabbitmq. A docker container is available:

`docker run -p 5672:5672 -p 15672:15672 rabbitmq:3-management`

2. A running instance of the Local Spring Cloud Data Flow Server. We recommend Version 1.4.0.RC1 available at [https://cloud.spring.io/spring-cloud-dataflow/](https://cloud.spring.io/spring-cloud-dataflow/) After download, run the local server with:

`java -jar spring-cloud-dataflow-server-local-1.4.0.RC1.jar`

3. You need a modified and working mqtt-source-rabbit.jar. Ask Maurin in Slack for it.
 

## Installation

  1. Clone the repository: `git clone https://github.com/MojioMS/SWE-Ingestion-Service`.

  2. Build the project: `mvn clean install`.

## Configuration

#### Configure application address and ports:
  Each Sink, Processor, and Source is an executable Spring Boot Application, that starts on a configurable port. The RestController is also an executable Spring Boot Application. Modify the ports in the application.yml file to your desire. The application.yml files are located in:
  `\LogSink\src\main\resources\` - default port: 8082
  
  `\marineprocessor\src\main\resources\` - default port: 8083
  
  `\RestController\src\main\resources\` - default port: 8080

    
## Bugs and Feedback **TODO: fix this**
Developer feedback goes a long way towards making this adapter even better. Submit a bug report or request feature enhancements to [via mail to enviroCar@52north.org](mailto:enviroCar@52north.org?Subject=wfs4BIGIoT-Adapter) or open a issue on this github repository.

## Funding **TODO: fix this**
This project has received funding from the European Union's Horizon 2020 research and innovation programme 
under grant agreement No 688038. 

[enviroCar](https://enviroCar.org) was successfully applied in the First Open Call of the EC funded BIG IoT project. Thus, the enviroCar 
project is one of the first projects to appear on the [BIG IoT marketplace](https://market.big-iot.org/).