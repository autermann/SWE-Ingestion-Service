#### status: in development progress

# SWE-Ingestion-Service

The SensorWebEnabled-Ingestion-Service is an easy to configure and easy to deploy Data Flow Web application. It utilizes the Spring Cloud DataFlow Server technology serving as a **TODO: bla bla.** makes use of the Source-Processor-Sink pipelines stream approach.

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

2. A running instance of the Local Spring Cloud DataFlow Server. We recommend Version 1.4.0.RC1 available at [https://cloud.spring.io/spring-cloud-dataflow/](https://cloud.spring.io/spring-cloud-dataflow/) After download, run the local server with:

`java -jar spring-cloud-dataflow-server-local-1.4.0.RC1.jar`

3. You need a modified and working mqtt-source-rabbit.jar. Ask Eike where to find it on the File-Server.
 
## Installation

  1. Clone the repository: `git clone https://github.com/MojioMS/SWE-Ingestion-Service`.

  2. Build the project: `mvn clean install`.
  
     The Build process currently uses TestClasses in order to register the sources, processors, and sinks into the DataFlow Cloud server. This automatic registration requires setting correct paths to your target/ directory.
	 
  3. Set the correct filepaths to your executable *.jar files in the Tests [here](https://github.com/MojioMS/SWE-Ingestion-Service/blob/dev/RestController/src/test/java/org/n52/stream/seadatacloud/restcontroller/test/RegisterAppsOnStartTest.java#L26), [here](https://github.com/MojioMS/SWE-Ingestion-Service/blob/dev/RestController/src/test/java/org/n52/stream/seadatacloud/restcontroller/test/RegisterAppsOnStartTest.java#L33), [here](https://github.com/MojioMS/SWE-Ingestion-Service/blob/dev/RestController/src/test/java/org/n52/stream/seadatacloud/restcontroller/test/RegisterAppsOnStartTest.java#L35), and [here](https://github.com/MojioMS/SWE-Ingestion-Service/blob/dev/RestController/src/test/java/org/n52/stream/seadatacloud/restcontroller/test/RegisterAppsOnStartTest.java#L42).

## Configuration

#### Configure application address and ports:
  Each Sink, Processor, and Source is an executable Spring Boot Application, that starts on a configurable port. The RestController is also an executable Spring Boot Application. Modify the ports in the application.yml file to your desire. The application.yml files are located in:
  `\LogSink\src\main\resources\` - default port: 8082
  
  `\MarineCTDProcessor\src\main\resources\` - default port: 8083
  
  `\MarineWeatherProcessor\src\main\resources\` - default port: 8083
  
  `\RestController\src\main\resources\` - default port: 8080

    
## Bugs and Feedback **TODO: fix this**
Developer feedback goes a long way towards making this adapter even better. Submit a bug report or request feature enhancements to [via mail to s.jirka@52north.org](mailto:s.jirka@52north.org?Subject=SWE-Ingestion-Service) or open a issue on this github repository.

## Funding 
**TODO: this**