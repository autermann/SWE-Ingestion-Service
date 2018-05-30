#### status: in development process

[![Build Status](https://travis-ci.org/52North/SWE-Ingestion-Service.svg?branch=dev)](https://travis-ci.org/52North/SWE-Ingestion-Service)

# SWE-Ingestion-Service

The SensorWebEnabled-Ingestion-Service is an easy to configure and easy to deploy Data Flow Web application. It utilizes the Spring Cloud DataFlow Server technology and makes use of the Source-Processor-Sink pipelines stream approach.

## Libraries and Licenses
TODO: ...

#### Third party libraries and licenses

|Library|License|Link/Source|
|:----:|:----:|:----:|
|Spring-framework|Apache License Version 2.0|[https://github.com/spring-projects/spring-framework/blob/master/src/docs/dist/license.txt](https://github.com/spring-projects/spring-framework/blob/master/src/docs/dist/license.txt)|

#### SWE-Ingestion-Service

|Library|License|Link/Source|
|:----:|:----:|:-----:|
|SWE-Ingestion-Service|GNU GENERAL PUBLIC LICENSE 2.0|[https://github.com/52North/SWE-Ingestion-Service/blob/dev/LICENSE](https://github.com/52North/SWE-Ingestion-Service/blob/dev/LICENSE)|


## Installation

  1. Clone the repository: `git clone https://github.com/52north/SWE-Ingestion-Service`.
  2. Change into new directoy: `cd SWE-Ingestion-Service`.
  3. Build the project: `mvn clean install`.

## How to Run

  1. Execute `docker-compose --file etc/docker-compose.yml up`.

#### Creating and starting a Stream:

1. Create a Stream: Send an aggregateProcess to the CnCService via a POST request (see [API section](#streams) for detailed description). On success, your response payload contains name, status, and definition of the created stream, such as:

  	```
   {
     "name": "sb68a63d8-cc63-4ce3-9212-09b7a1f47740",
     "status": "undeployed",
     "definition": "mqtt-source-rabbit --url=tcp://nexos.dev.52north.org:1883 --topics=spiddal-fluorometer | csv-processor | db-sink"
   }
	```
    The `"name"`-field value contains the `streamId` of the created stream. Use this value in order to start the streaming process of the stream as described in the next step.

2. Deploy/Start (or Undeploy/Pause) the stream: Send a PUT request to the CnCService at the resource of the stream (see [API section](#streams) for detailed description), e.g.:
 /cnc/api/streams/sb68a63d8-cc63-4ce3-9212-09b7a1f47740

 	with the status "deployed" (or "undeployed") to start (or pause) the stream process, e.g.:
 	```
  	{
    	"status": "deployed"
  	}
	```
On success, the response status code is 204 - `no content` and the stream is running.

## API:

#### general:
* GET [http://localhost:8082/cnc/api](http://localhost:8082/cnc/api) with `Accept` header `application/json` - 		gets a list of resources, i.e.:
	```
	{
    	"resources": [
        	{
            	"name": "sources",
                "decription": "List of registered sources.",
                "href": "http://cnc:8082/cnc/api/sources"
            },
            {
            	"name": "processors",
                "decription": "List of registered processors.",
                "href": "http://cnc:8082/cnc/api/processors"
            },
            {
            	"name": "sinks",
                "decription": "List of registered sinks.",
                "href": "http://cnc:8082/cnc/api/sinks"
            },
            {
            	"name": "streams",
                "decription": "List of registered streams.",
                "href": "http://cnc:8082/cnc/api/streams"
            }
        ]
    }
    ```

#### Sources:

  * GET [http://localhost:8082/cnc/api/sources](http://localhost:8082/cnc/api/sources) with `Accept`header "application/json` - gets all registered sources, e.g.:
	```
    {
        "sources": [
            {
                "name": "mqtt-source-rabbit",
                "options": [
                    {
                        "name": "qos",
                        "type": "java.lang.Integer[]",
                        "description": "the qos; a single value for all topics or a comma-delimited list to match the topics",
                        "defaultValue": ""
                    },
                    {
                        "name": "binary",
                        "type": "java.lang.Boolean",
                        "description": "true to leave the payload as bytes",
                        "defaultValue": "false"
                    },
                    {
                        "name": "charset",
                        "type": "java.lang.String",
                        "description": "the charset used to convert bytes to String (when binary is false)",
                        "defaultValue": "UTF-8"
                    },
                    {
                        "name": "clean-session",
                        "type": "java.lang.Boolean",
                        "description": "whether the client and server should remember state across restarts and reconnects",
                        "defaultValue": "true"
                    },
                    {
                        "name": "persistence-directory",
                        "type": "java.lang.String",
                        "description": "Persistence directory",
                        "defaultValue": "/tmp/paho"
                    },
                    {
                        "name": "client-id",
                        "type": "java.lang.String",
                        "description": "identifies the client",
                        "defaultValue": "stream.client.id.source"
                    },
                    {
                        "name": "keep-alive-interval",
                        "type": "java.lang.Integer",
                        "description": "the ping interval in seconds",
                        "defaultValue": "60"
                    },
                    {
                        "name": "url",
                        "type": "java.lang.String[]",
                        "description": "location of the mqtt broker(s) (comma-delimited list)",
                        "defaultValue": ""
                    },
                    {
                        "name": "persistence",
                        "type": "java.lang.String",
                        "description": "'memory' or 'file'",
                        "defaultValue": "memory"
                    },
                    {
                        "name": "username",
                        "type": "java.lang.String",
                        "description": "the username to use when connecting to the broker",
                        "defaultValue": "guest"
                    },
                    {
                        "name": "password",
                        "type": "java.lang.String",
                        "description": "the password to use when connecting to the broker",
                        "defaultValue": "guest"
                    },
                    {
                        "name": "connection-timeout",
                        "type": "java.lang.Integer",
                        "description": "the connection timeout in seconds",
                        "defaultValue": "30"
                    },
                    {
                        "name": "topics",
                        "type": "java.lang.String[]",
                        "description": "the topic(s) (comma-delimited) to which the source will subscribe",
                        "defaultValue": ""
                    }
                ]
            }
        ]
    }
    ```


#### Processors:

  * GET [http://localhost:8082/cnc/api/processors](http://localhost:8082/cnc/api/processors) with `Accept`header `application/json` - gets all registered processors, e.g.:
	```
    {
        "processors": [
            {
                "name": "csv-processor",
                "options": [
                    {
                        "name": "offering",
                        "type": "java.lang.String",
                        "description": "offering field desc",
                        "defaultValue": "offering-default-value"
                    },
                    {
                        "name": "sensor",
                        "type": "java.lang.String",
                        "description": "sensor field desc",
                        "defaultValue": "sensor-default-value"
                    },
                    {
                        "name": "sensormlurl",
                        "type": "java.lang.String",
                        "description": "sensormlurl field desc",
                        "defaultValue": "http://example.com/process-description.xml"
                    }
                ]
            }
        ]
	}
    ```

#### Sinks:

  * GET [http://localhost:8082/cnc/api/sinks](http://localhost:8082/cnc/api/sinks) with `Accept`header `application/json` - gets all registered sinks, e.g.:
	```
	{
      	"sinks": [
            {
                "name": "db-sink",
                "options": [
                    {
                        "name": "offering",
                        "type": "java.lang.String",
                        "description": "offering field desc",
                        "defaultValue": "offering-default-value"
                    },
                    {
                        "name": "sensor",
                        "type": "java.lang.String",
                        "description": "sensor field desc",
                        "defaultValue": "sensor-default-value"
                    },
                    {
                        "name": "sensormlurl",
                        "type": "java.lang.String",
                        "description": "sensormlurl field desc",
                        "defaultValue": "http://example.com/process-description.xml"
                    },
                    {
                        "name": "password",
                        "type": "java.lang.String",
                        "description": "Login password of the database.",
                        "defaultValue": "null"
                    },
                    {
                        "name": "username",
                        "type": "java.lang.String",
                        "description": "Login username of the database.",
                        "defaultValue": "null"
                    },
                    {
                        "name": "url",
                        "type": "java.lang.String",
                        "description": "JDBC URL of the database.",
                        "defaultValue": "null"
                    }
                ]
            },
            {
                "name": "log-sink",
                "options": []
            }
      	]
	}
    ```

#### Streams:

  * GET [http://localhost:8082/cnc/api/streams](http://localhost:8082/cnc/api/streams) with `Accept`header `application/json` - gets all registered streams, e.g.:
	```
    {
        "streams": [
            {
                "name": "s27219dc2-a962-4566-85e9-fc24f0ef8aef",
                "status": "deployed",
                "definition": "mqtt-source-rabbit --url=tcp://nexos.dev.52north.org:1884 --topics=airmar-rinville-1 | csv-processor --sensormlurl=http://cnc:8082/cnc/api/streams/s27219dc2-a962-4566-85e9-fc24f0ef8aef --offering=AIRMAR-RINVILLE-2/observations --sensor=AIRMAR-RINVILLE-2 | db-sink --sensormlurl=http://cnc:8082/cnc/api/streams/s27219dc2-a962-4566-85e9-fc24f0ef8aef --offering=AIRMAR-RINVILLE-2/observations --sensor=AIRMAR-RINVILLE-2 --url=jdbc:postgresql://database:5432/sos --username=postgres --password=****** "
            },
            {
                "name": "s8e72442f-9102-4f1b-a3cf-367053765e92",
                "status": "deployed",
                "definition": "mqtt-source-rabbit --url=tcp://nexos.dev.52north.org:1884 --topics=airmar-rinville-1-generated | csv-processor --sensormlurl=http://cnc:8082/cnc/api/streams/s8e72442f-9102-4f1b-a3cf-367053765e92 --offering=AIRMAR-RINVILLE-1/observations --sensor=AIRMAR-RINVILLE-1 | db-sink --sensormlurl=http://cnc:8082/cnc/api/streams/s8e72442f-9102-4f1b-a3cf-367053765e92 --offering=AIRMAR-RINVILLE-1/observations --sensor=AIRMAR-RINVILLE-1 --url=jdbc:postgresql://database:5432/sos --username=postgres --password=****** "
            }
        ]
    }
    ```

  * GET [http://localhost:8082/cnc/api/streams/{streamName}](http://localhost:8082/cnc/api/streams/{streamName}) with `Accept` header `application/json` - gets the registered stream with name `streamName`, e.g.:
	```
    {
        "name": "s27219dc2-a962-4566-85e9-fc24f0ef8aef",
        "status": "deployed",
        "definition": "mqtt-source-rabbit --url=tcp://nexos.dev.52north.org:1884 --topics=airmar-rinville-1 | csv-processor --sensormlurl=http://cnc:8082/cnc/api/streams/s27219dc2-a962-4566-85e9-fc24f0ef8aef --offering=AIRMAR-RINVILLE-2/observations --sensor=AIRMAR-RINVILLE-2 | db-sink --sensormlurl=http://cnc:8082/cnc/api/streams/s27219dc2-a962-4566-85e9-fc24f0ef8aef --offering=AIRMAR-RINVILLE-2/observations --sensor=AIRMAR-RINVILLE-2 --url=jdbc:postgresql://database:5432/sos --username=postgres --password=****** "
	}
    ```
    possible Responses:

    * 200 - OK
    * 404 - NOT FOUND: The Stream with name `streamName` is not found.

  * GET [http://localhost:8082/cnc/api/streams/{streamName}](http://localhost:8082/cnc/api/streams/{streamName})  `Accept` header `application/xml` - gets the sensorml processDescription of the registered stream with name `streamName`, e.g.:
	```
    <?xml version="1.0" encoding="UTF-8"?>
    <!--

        Copyright (C) 2018-2018 52°North Initiative for Geospatial Open Source
        Software GmbH

        This program is free software; you can redistribute it and/or modify it
        under the terms of the GNU General Public License version 2 as published
        by the Free Software Foundation.

        If the program is linked with libraries which are licensed under one of
        the following licenses, the combination of the program with the linked
        library is not considered a "derivative work" of the program:

            - Apache License, version 2.0
            - Apache Software License, version 1.0
            - GNU Lesser General Public License, version 3
            - Mozilla Public License, versions 1.0, 1.1 and 2.0
            - Common Development and Distribution License (CDDL), version 1.0

        Therefore the distribution of the program linked with libraries licensed
        under the aforementioned licenses, is permitted by the copyright holders
        if the distribution is compliant with both the GNU General Public
        License version 2 and the aforementioned licenses.

        This program is distributed in the hope that it will be useful, but
        WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
        Public License for more details.

    -->
    <sml:AggregateProcess gml:id="sdc"
        xmlns:swe="http://www.opengis.net/swe/2.0"
        xmlns:sml="http://www.opengis.net/sensorml/2.0"
        xmlns:gml="http://www.opengis.net/gml/3.2"
        xmlns:om="http://www.opengis.net/om/2.0"
        xmlns:sams="http://www.opengis.net/samplingSpatial/2.0"
        xmlns:sf="http://www.opengis.net/sampling/2.0"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:gco="http://www.isotc211.org/2005/gco"
        xmlns:gmd="http://www.isotc211.org/2005/gmd"
        xsi:schemaLocation="http://www.opengis.net/sensorml/2.0 http://schemas.opengis.net/sensorML/2.0/sensorML.xsd http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd">
        <sml:components>
            <sml:ComponentList>
                <sml:component name="source_output">
                    <sml:PhysicalSystem gml:id="marine-weather">
                        <gml:identifier codeSpace="uniqueID">AIRMAR-RINVILLE-2</gml:identifier>
                        <sml:identification>
                            <sml:IdentifierList>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0002/">
                                        <sml:label>Long name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR Weather Station</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0006/">
                                        <sml:label>Short name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR WX Series WeatherStation</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0012/">
                                        <sml:label>Manufacturer</sml:label>
                                        <sml:value>AIRMAR</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0003/">
                                        <sml:label>Model name</sml:label>
                                        <sml:value>300WX</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0005/">
                                        <sml:label>Serial number</sml:label>
                                        <sml:value>4252</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                            </sml:IdentifierList>
                        </sml:identification>
                        <sml:featuresOfInterest>
                            <sml:FeatureList definition="http://www.opengis.net/def/featureOfInterest/identifier">
                                <swe:label>Marine Institute</swe:label>
                                <sml:feature>
                                    <sams:SF_SpatialSamplingFeature gml:id="airmar-rinville-1">
                                        <gml:identifier codeSpace="">Marine Institute</gml:identifier>
                                        <gml:name>Marine Intitute Ireland</gml:name>
                                        <sf:type xlink:href="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint" />
                                        <sf:sampledFeature xlink:href="http://www.opengis.net/def/nil/OGC/0/unknown" />
                                        <sams:shape>
                                            <gml:Point gml:id="point-airmar-rinville-1">
                                                <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">53.247642 -8.977098</gml:pos>
                                            </gml:Point>
                                        </sams:shape>
                                    </sams:SF_SpatialSamplingFeature>
                                </sml:feature>
                            </sml:FeatureList>
                        </sml:featuresOfInterest>
                        <sml:outputs>
                            <sml:OutputList>
                                <sml:output name="streamOutput">
                                    <sml:DataInterface>
                                        <sml:data>
                                            <swe:DataStream>
                                                <swe:elementType name="weatherOutputStream">
                                                    <swe:DataRecord>
                                                        <swe:field name="datetime">
                                                            <swe:Time definition="http://www.opengis.net/def/property/OGC/0/PhenomenonTime">
                                                                <swe:label>Date and time</swe:label>
                                                                <swe:description>Date-time is a time stamp from a Global Positioning System receiver at the cable observatory shore station in the format YYYY-MM-DDThh:mm:ss.sss .</swe:description>
                                                                <swe:uom/>
                                                            </swe:Time>
                                                        </swe:field>
                                                        <swe:field name="instrument_id">
                                                            <swe:Text>
                                                                <swe:label>Instrument ID</swe:label>
                                                                <swe:description>Instrument-ID is a unique identifier for the instrument based on its manufacturer, model number and serial number</swe:description>
                                                            </swe:Text>
                                                        </swe:field>
                                                        <swe:field name="AirTemperature">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/airtemp/">
                                                                <swe:label>air temperature</swe:label>
                                                                <swe:description>Air temperature is the bulk temperature of the air, not the surface (skin) temperature.
                                                    </swe:description>
                                                                <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="RelativeHumidity">
                                                            <swe:Quantity  definition="http://vocab.nerc.ac.uk/collection/B39/current/humidity/">
                                                                <swe:label>relative_humidity</swe:label>
                                                                <swe:description>The ratio of the amount of water vapour in the air compared to the maximum amount of water vapour  that can theoretically be held at the air's temperature
                                                    </swe:description>
                                                                <swe:uom code="%" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPCT/" xlink:title="Percent" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="DewPoint">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/P01/current/CDEWZZ01/">
                                                                <swe:label>DewPoint</swe:label>
                                                                <swe:description>The temperature to which air must cool to
                                                        become saturated with water vapour</swe:description>
                                                                <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="WindDirection">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewinddir/">
                                                                <swe:label>earthrelativewinddirection</swe:label>
                                                                <swe:description>Direction from of wind relative to True North {wind direction} in the atmosphere by in-situ anemometer</swe:description>
                                                                <swe:uom code="degT" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UABB/" xlink:title="Degrees True" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="WindSpeed">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewindspeed/">
                                                                <swe:label> earth relative wind speed</swe:label>
                                                                <swe:description>Speed of wind {wind speed} in the atmosphere by in-situ anemometer</swe:description>
                                                                <swe:uom code="m/s" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UVAA/" xlink:title="Metres per second" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                    </swe:DataRecord>
                                                </swe:elementType>
                                                <swe:encoding>
                                                    <!-- Abhängig vom EncodingTypen den Processor wählen.-->
                                                    <!-- Es gibt \{TextEncoding} auch: {XmlEncoding, BinaryEncoding}-->
                                                    <swe:TextEncoding tokenSeparator=";" blockSeparator="\n"/>
                                                </swe:encoding>
                                                <swe:values/>
                                            </swe:DataStream>
                                        </sml:data>
                                        <sml:interfaceParameters>
                                            <swe:DataRecord definition="https://52north.org/swe-ingestion/mqtt/3.1">
                                                <swe:field name="mqtt_broker_url">
                                                    <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#url">
                                                        <swe:value>tcp://nexos.dev.52north.org:1884</swe:value>
                                                    </swe:Text>
                                                </swe:field>
                                                <swe:field name="mqtt_broker_topics">
                                                    <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#topics">
                                                        <swe:value>airmar-rinville-1</swe:value>
                                                    </swe:Text>
                                                </swe:field>
                                            </swe:DataRecord>
                                        </sml:interfaceParameters>
                                    </sml:DataInterface>
                                </sml:output>
                            </sml:OutputList>
                        </sml:outputs>
                        <sml:position>
                            <swe:Vector referenceFrame="urn:ogc:def:crs:EPSG::4326">
                                <swe:coordinate name="easting">
                                    <swe:Quantity axisID="x">
                                        <swe:uom code="degree"/>
                                        <swe:value>-8.977098</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="northing">
                                    <swe:Quantity axisID="y">
                                        <swe:uom code="degree"/>
                                        <swe:value>53.247642</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="altitude">
                                    <swe:Quantity axisID="z">
                                        <swe:uom code="m"/>
                                        <swe:value>17.0</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                            </swe:Vector>
                        </sml:position>
                    </sml:PhysicalSystem>
                </sml:component>
                <sml:component name="sos_input">
                    <sml:PhysicalSystem gml:id="marine-weather-sos">
                        <gml:identifier codeSpace="uniqueID">AIRMAR-RINVILLE-2</gml:identifier>
                        <sml:identification>
                            <sml:IdentifierList>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0002/">
                                        <sml:label>Long name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR Weather Station</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0006/">
                                        <sml:label>Short name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR WX Series WeatherStation</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0012/">
                                        <sml:label>Manufacturer</sml:label>
                                        <sml:value>AIRMAR</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0003/">
                                        <sml:label>Model name</sml:label>
                                        <sml:value>300WX</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0005/">
                                        <sml:label>Serial number</sml:label>
                                        <sml:value>4252</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                            </sml:IdentifierList>
                        </sml:identification>
                        <sml:featuresOfInterest>
                            <sml:FeatureList definition="http://www.opengis.net/def/featureOfInterest/identifier">
                                <swe:label>Marine Institute</swe:label>
                                <sml:feature>
                                    <sams:SF_SpatialSamplingFeature gml:id="sf-airmar-rinville-1">
                                        <gml:identifier codeSpace="">Marine Institute</gml:identifier>
                                        <gml:name>Marine Intitute Ireland</gml:name>
                                        <sf:type xlink:href="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint" />
                                        <sf:sampledFeature xlink:href="http://www.opengis.net/def/nil/OGC/0/unknown" />
                                        <sams:shape>
                                            <gml:Point gml:id="p-airmar-rinville-1">
                                                <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">53.247642 -8.977098</gml:pos>
                                            </gml:Point>
                                        </sams:shape>
                                    </sams:SF_SpatialSamplingFeature>
                                </sml:feature>
                            </sml:FeatureList>
                        </sml:featuresOfInterest>
                        <sml:outputs>
                            <sml:OutputList>
                                <sml:output name="AirTemperature">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/airtemp/">
                                        <swe:label>air temperature</swe:label>
                                        <swe:description>Air temperature is the bulk temperature of the air, not the surface (skin) temperature.</swe:description>
                                        <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius"/>
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="RelativeHumidity">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/humidity/">
                                        <swe:label>relative_humidity</swe:label>
                                        <swe:description>The ratio of the amount of water vapour in the air compared to the maximum amount of water vapour that can theoretically be held at the air's temperature</swe:description>
                                        <swe:uom code="%" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPCT/" xlink:title="Percent" />
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="DewPoint">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/P01/current/CDEWZZ01/">
                                        <swe:label>DewPoint</swe:label>
                                        <swe:description>The temperature to which air must cool to become saturated with water vapour</swe:description>
                                        <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius"/>
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="WindDirection">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewinddir/">
                                        <swe:label>earthrelativewinddirection</swe:label>
                                        <swe:description>Direction from of wind relative to True North {wind direction} in the atmosphere by in-situ anemometer</swe:description>
                                        <swe:uom code="degT" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UABB/" xlink:title="Degrees True" />
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="WindSpeed">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewindspeed/">
                                        <swe:label>	earth relative wind speed</swe:label>
                                        <swe:description>Speed of wind {wind speed} in the atmosphere by in-situ anemometer</swe:description>
                                        <swe:uom code="m/s" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UVAA/" xlink:title="Metres per second" />
                                    </swe:Quantity>
                                </sml:output>
                            </sml:OutputList>
                        </sml:outputs>
                        <sml:position>
                            <swe:Vector referenceFrame="urn:ogc:def:crs:EPSG::4326">
                                <swe:coordinate name="easting">
                                    <swe:Quantity axisID="x">
                                        <swe:uom code="degree"/>
                                        <swe:value>-8.977098</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="northing">
                                    <swe:Quantity axisID="y">
                                        <swe:uom code="degree"/>
                                        <swe:value>53.247642</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="altitude">
                                    <swe:Quantity axisID="z">
                                        <swe:uom code="m"/>
                                        <swe:value>17.0</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                            </swe:Vector>
                        </sml:position>
                    </sml:PhysicalSystem>
                </sml:component>
            </sml:ComponentList>
        </sml:components>
        <sml:connections>
            <sml:ConnectionList>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/AirTemperature"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/AirTemperature"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/RelativeHumidity"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/RelativeHumidity"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/DewPoint"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/DewPoint"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/WindDirection"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/WindDirection"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/WindSpeed"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/WindSpeed"/>
                    </sml:Link>
                </sml:connection>
            </sml:ConnectionList>
        </sml:connections>
    </sml:AggregateProcess>
    ```
    possible Responses:
    * 200 - OK
    * 404 - NOT FOUND: "Stream with name `streamName` is not found." / "sensorML process description for stream `streamName` not found."

  * POST [http://localhost:8082/cnc/api/streams](http://localhost:8082/cnc/api/streams) with `Content-Type` header `application/xml` and request body containing a `AggregateProcess` description creates a  new undeployed stream.

    RequestBody payload example:

    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <!--

        Copyright (C) 2018-2018 52°North Initiative for Geospatial Open Source
        Software GmbH

        This program is free software; you can redistribute it and/or modify it
        under the terms of the GNU General Public License version 2 as published
        by the Free Software Foundation.

        If the program is linked with libraries which are licensed under one of
        the following licenses, the combination of the program with the linked
        library is not considered a "derivative work" of the program:

            - Apache License, version 2.0
            - Apache Software License, version 1.0
            - GNU Lesser General Public License, version 3
            - Mozilla Public License, versions 1.0, 1.1 and 2.0
            - Common Development and Distribution License (CDDL), version 1.0

        Therefore the distribution of the program linked with libraries licensed
        under the aforementioned licenses, is permitted by the copyright holders
        if the distribution is compliant with both the GNU General Public
        License version 2 and the aforementioned licenses.

        This program is distributed in the hope that it will be useful, but
        WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
        Public License for more details.

    -->
    <sml:AggregateProcess gml:id="sdc"
        xmlns:swe="http://www.opengis.net/swe/2.0"
        xmlns:sml="http://www.opengis.net/sensorml/2.0"
        xmlns:gml="http://www.opengis.net/gml/3.2"
        xmlns:om="http://www.opengis.net/om/2.0"
        xmlns:sams="http://www.opengis.net/samplingSpatial/2.0"
        xmlns:sf="http://www.opengis.net/sampling/2.0"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:gco="http://www.isotc211.org/2005/gco"
        xmlns:gmd="http://www.isotc211.org/2005/gmd"
        xsi:schemaLocation="http://www.opengis.net/sensorml/2.0 http://schemas.opengis.net/sensorML/2.0/sensorML.xsd http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd">
        <sml:components>
            <sml:ComponentList>
                <sml:component name="source_output">
                    <sml:PhysicalSystem gml:id="marine-weather">
                        <gml:identifier codeSpace="uniqueID">AIRMAR-RINVILLE-2</gml:identifier>
                        <sml:identification>
                            <sml:IdentifierList>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0002/">
                                        <sml:label>Long name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR Weather Station</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0006/">
                                        <sml:label>Short name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR WX Series WeatherStation</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0012/">
                                        <sml:label>Manufacturer</sml:label>
                                        <sml:value>AIRMAR</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0003/">
                                        <sml:label>Model name</sml:label>
                                        <sml:value>300WX</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0005/">
                                        <sml:label>Serial number</sml:label>
                                        <sml:value>4252</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                            </sml:IdentifierList>
                        </sml:identification>
                        <sml:featuresOfInterest>
                            <sml:FeatureList definition="http://www.opengis.net/def/featureOfInterest/identifier">
                                <swe:label>Marine Institute</swe:label>
                                <sml:feature>
                                    <sams:SF_SpatialSamplingFeature gml:id="airmar-rinville-1">
                                        <gml:identifier codeSpace="">Marine Institute</gml:identifier>
                                        <gml:name>Marine Intitute Ireland</gml:name>
                                        <sf:type xlink:href="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint" />
                                        <sf:sampledFeature xlink:href="http://www.opengis.net/def/nil/OGC/0/unknown" />
                                        <sams:shape>
                                            <gml:Point gml:id="point-airmar-rinville-1">
                                                <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">53.247642 -8.977098</gml:pos>
                                            </gml:Point>
                                        </sams:shape>
                                    </sams:SF_SpatialSamplingFeature>
                                </sml:feature>
                            </sml:FeatureList>
                        </sml:featuresOfInterest>
                        <sml:outputs>
                            <sml:OutputList>
                                <sml:output name="streamOutput">
                                    <sml:DataInterface>
                                        <sml:data>
                                            <swe:DataStream>
                                                <swe:elementType name="weatherOutputStream">
                                                    <swe:DataRecord>
                                                        <swe:field name="datetime">
                                                            <swe:Time definition="http://www.opengis.net/def/property/OGC/0/PhenomenonTime">
                                                                <swe:label>Date and time</swe:label>
                                                                <swe:description>Date-time is a time stamp from a Global Positioning System receiver at the cable observatory shore station in the format YYYY-MM-DDThh:mm:ss.sss .</swe:description>
                                                                <swe:uom/>
                                                            </swe:Time>
                                                        </swe:field>
                                                        <swe:field name="instrument_id">
                                                            <swe:Text>
                                                                <swe:label>Instrument ID</swe:label>
                                                                <swe:description>Instrument-ID is a unique identifier for the instrument based on its manufacturer, model number and serial number</swe:description>
                                                            </swe:Text>
                                                        </swe:field>
                                                        <swe:field name="AirTemperature">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/airtemp/">
                                                                <swe:label>air temperature</swe:label>
                                                                <swe:description>Air temperature is the bulk temperature of the air, not the surface (skin) temperature.
                                                    </swe:description>
                                                                <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="RelativeHumidity">
                                                            <swe:Quantity  definition="http://vocab.nerc.ac.uk/collection/B39/current/humidity/">
                                                                <swe:label>relative_humidity</swe:label>
                                                                <swe:description>The ratio of the amount of water vapour in the air compared to the maximum amount of water vapour  that can theoretically be held at the air's temperature
                                                    </swe:description>
                                                                <swe:uom code="%" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPCT/" xlink:title="Percent" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="DewPoint">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/P01/current/CDEWZZ01/">
                                                                <swe:label>DewPoint</swe:label>
                                                                <swe:description>The temperature to which air must cool to
                                                        become saturated with water vapour</swe:description>
                                                                <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="WindDirection">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewinddir/">
                                                                <swe:label>earthrelativewinddirection</swe:label>
                                                                <swe:description>Direction from of wind relative to True North {wind direction} in the atmosphere by in-situ anemometer</swe:description>
                                                                <swe:uom code="degT" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UABB/" xlink:title="Degrees True" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                        <swe:field name="WindSpeed">
                                                            <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewindspeed/">
                                                                <swe:label> earth relative wind speed</swe:label>
                                                                <swe:description>Speed of wind {wind speed} in the atmosphere by in-situ anemometer</swe:description>
                                                                <swe:uom code="m/s" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UVAA/" xlink:title="Metres per second" />
                                                            </swe:Quantity>
                                                        </swe:field>
                                                    </swe:DataRecord>
                                                </swe:elementType>
                                                <swe:encoding>
                                                    <!-- Abhängig vom EncodingTypen den Processor wählen.-->
                                                    <!-- Es gibt \{TextEncoding} auch: {XmlEncoding, BinaryEncoding}-->
                                                    <swe:TextEncoding tokenSeparator=";" blockSeparator="\n"/>
                                                </swe:encoding>
                                                <swe:values/>
                                            </swe:DataStream>
                                        </sml:data>
                                        <sml:interfaceParameters>
                                            <swe:DataRecord definition="https://52north.org/swe-ingestion/mqtt/3.1">
                                                <swe:field name="mqtt_broker_url">
                                                    <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#url">
                                                        <swe:value>tcp://nexos.dev.52north.org:1884</swe:value>
                                                    </swe:Text>
                                                </swe:field>
                                                <swe:field name="mqtt_broker_topics">
                                                    <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#topics">
                                                        <swe:value>airmar-rinville-1</swe:value>
                                                    </swe:Text>
                                                </swe:field>
                                            </swe:DataRecord>
                                        </sml:interfaceParameters>
                                    </sml:DataInterface>
                                </sml:output>
                            </sml:OutputList>
                        </sml:outputs>
                        <sml:position>
                            <swe:Vector referenceFrame="urn:ogc:def:crs:EPSG::4326">
                                <swe:coordinate name="easting">
                                    <swe:Quantity axisID="x">
                                        <swe:uom code="degree"/>
                                        <swe:value>-8.977098</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="northing">
                                    <swe:Quantity axisID="y">
                                        <swe:uom code="degree"/>
                                        <swe:value>53.247642</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="altitude">
                                    <swe:Quantity axisID="z">
                                        <swe:uom code="m"/>
                                        <swe:value>17.0</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                            </swe:Vector>
                        </sml:position>
                    </sml:PhysicalSystem>
                </sml:component>
                <sml:component name="sos_input">
                    <sml:PhysicalSystem gml:id="marine-weather-sos">
                        <gml:identifier codeSpace="uniqueID">AIRMAR-RINVILLE-2</gml:identifier>
                        <sml:identification>
                            <sml:IdentifierList>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0002/">
                                        <sml:label>Long name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR Weather Station</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0006/">
                                        <sml:label>Short name</sml:label>
                                        <sml:value>Marine Institute - AIRMAR WX Series WeatherStation</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0012/">
                                        <sml:label>Manufacturer</sml:label>
                                        <sml:value>AIRMAR</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0003/">
                                        <sml:label>Model name</sml:label>
                                        <sml:value>300WX</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                                <sml:identifier>
                                    <sml:Term definition="http://vocab.nerc.ac.uk/collection/W07/current/IDEN0005/">
                                        <sml:label>Serial number</sml:label>
                                        <sml:value>4252</sml:value>
                                    </sml:Term>
                                </sml:identifier>
                            </sml:IdentifierList>
                        </sml:identification>
                        <sml:featuresOfInterest>
                            <sml:FeatureList definition="http://www.opengis.net/def/featureOfInterest/identifier">
                                <swe:label>Marine Institute</swe:label>
                                <sml:feature>
                                    <sams:SF_SpatialSamplingFeature gml:id="sf-airmar-rinville-1">
                                        <gml:identifier codeSpace="">Marine Institute</gml:identifier>
                                        <gml:name>Marine Intitute Ireland</gml:name>
                                        <sf:type xlink:href="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint" />
                                        <sf:sampledFeature xlink:href="http://www.opengis.net/def/nil/OGC/0/unknown" />
                                        <sams:shape>
                                            <gml:Point gml:id="p-airmar-rinville-1">
                                                <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">53.247642 -8.977098</gml:pos>
                                            </gml:Point>
                                        </sams:shape>
                                    </sams:SF_SpatialSamplingFeature>
                                </sml:feature>
                            </sml:FeatureList>
                        </sml:featuresOfInterest>
                        <sml:outputs>
                            <sml:OutputList>
                                <sml:output name="AirTemperature">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/airtemp/">
                                        <swe:label>air temperature</swe:label>
                                        <swe:description>Air temperature is the bulk temperature of the air, not the surface (skin) temperature.</swe:description>
                                        <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius"/>
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="RelativeHumidity">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/humidity/">
                                        <swe:label>relative_humidity</swe:label>
                                        <swe:description>The ratio of the amount of water vapour in the air compared to the maximum amount of water vapour that can theoretically be held at the air's temperature</swe:description>
                                        <swe:uom code="%" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPCT/" xlink:title="Percent" />
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="DewPoint">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/P01/current/CDEWZZ01/">
                                        <swe:label>DewPoint</swe:label>
                                        <swe:description>The temperature to which air must cool to become saturated with water vapour</swe:description>
                                        <swe:uom code="degC" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UPAA/" xlink:title="Degrees Celsius"/>
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="WindDirection">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewinddir/">
                                        <swe:label>earthrelativewinddirection</swe:label>
                                        <swe:description>Direction from of wind relative to True North {wind direction} in the atmosphere by in-situ anemometer</swe:description>
                                        <swe:uom code="degT" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UABB/" xlink:title="Degrees True" />
                                    </swe:Quantity>
                                </sml:output>
                                <sml:output name="WindSpeed">
                                    <swe:Quantity definition="http://vocab.nerc.ac.uk/collection/B39/current/truewindspeed/">
                                        <swe:label>	earth relative wind speed</swe:label>
                                        <swe:description>Speed of wind {wind speed} in the atmosphere by in-situ anemometer</swe:description>
                                        <swe:uom code="m/s" xlink:href="http://vocab.nerc.ac.uk/collection/P06/current/UVAA/" xlink:title="Metres per second" />
                                    </swe:Quantity>
                                </sml:output>
                            </sml:OutputList>
                        </sml:outputs>
                        <sml:position>
                            <swe:Vector referenceFrame="urn:ogc:def:crs:EPSG::4326">
                                <swe:coordinate name="easting">
                                    <swe:Quantity axisID="x">
                                        <swe:uom code="degree"/>
                                        <swe:value>-8.977098</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="northing">
                                    <swe:Quantity axisID="y">
                                        <swe:uom code="degree"/>
                                        <swe:value>53.247642</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                                <swe:coordinate name="altitude">
                                    <swe:Quantity axisID="z">
                                        <swe:uom code="m"/>
                                        <swe:value>17.0</swe:value>
                                    </swe:Quantity>
                                </swe:coordinate>
                            </swe:Vector>
                        </sml:position>
                    </sml:PhysicalSystem>
                </sml:component>
            </sml:ComponentList>
        </sml:components>
        <sml:connections>
            <sml:ConnectionList>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/AirTemperature"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/AirTemperature"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/RelativeHumidity"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/RelativeHumidity"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/DewPoint"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/DewPoint"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/WindDirection"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/WindDirection"/>
                    </sml:Link>
                </sml:connection>
                <sml:connection>
                    <sml:Link>
                        <sml:source ref="components/marine-weather/outputs/WindSpeed"/>
                        <sml:destination ref="components/marine-weather-sos/outputs/WindSpeed"/>
                    </sml:Link>
                </sml:connection>
            </sml:ConnectionList>
        </sml:connections>
    </sml:AggregateProcess>
    ```

	possible Responses:

* 201 - Created with json response of the created Stream, e.g.

      ```
         {
             "name": "sb68a63d8-cc63-4ce3-9212-09b7a1f47740",
             "status": "undeployed",
             "definition": "mqtt-source-rabbit --url=tcp://nexos.dev.52north.org:1883 --topics=spiddal-fluorometer | csv-processor | db-sink"
         }
      ```
	* 400 - BAD REQUEST: "swe:Text definition `optionUrl` requires a hashtag ( # ) option."
	* 400 - BAD REQUEST: "Option `appOptionName` is not supported by source `sourceName`."
	* 404 - NOT FOUND: "No supported Source found for DataRecord definition `dataRecordDefinition`."
	* 404 - NOT FOUND: "DataRecord definition `dataRecordDefinition` is supposed to be supported by Source `sourceName`, but Source `sourceName` not found."
	* 404 - BAD REQUEST: "The xml request body is no valid aggregateProcess sensorML description."
	* 409 - CONFLICT: "A stream with name `streamName` already exists."

* PUT [http://localhost:8082/cnc/api/streams/{streamName}](http://localhost:8082/cnc/api/streams/{streamName}) with `Content-Type` header `application/json` changes the deploy-status of the registered Stream 'streamName' according to the request body.
  Example Payload:
  ```
  {
    	"status": "deployed"
  }
  ```
  possible Responses:

    * 200 - OK: The Streams status has been changed to the requested status.
	* 202 - ACCEPTED: "The Stream {streamName} is currently `deploying` and thus, the resource status will not be changed."
	* 400 - BAD REQUEST: "Request is missing required field `status`."
	* 400 - BAD REQUEST: "The requested status `statusValue` is not supported. Supported status are: 'deployed' and 'undeployed'."
    * 404 - NOT FOUND: "Stream {streamName} not found."

* PUT [http://localhost:8082/cnc/api/streams/{streamName}](http://localhost:8082/cnc/api/streams/{streamName}) with `Content-Type` header `application/xml` changes the registered Stream 'streamName' according to the request body.
  Example Payload cf. POST on ```cnc/api/streams```


## Bugs and Feedback **TODO: fix this**
Developer feedback goes a long way towards making this SWE-Ingestion-Service even better. Submit a bug report or request feature enhancements to [via mail to s.jirka@52north.org](mailto:s.jirka@52north.org?Subject=SWE-Ingestion-Service) or open an issue on this github repository.

## Documentation

* [Process description](https://github.com/52North/SWE-Ingestion-Service/blob/dev/docs/ProcessDescription.md)

## Funding
**TODO: this**
