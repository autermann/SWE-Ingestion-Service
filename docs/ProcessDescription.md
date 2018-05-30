# Process Description definiton

This document describes the definition of the process description to ingest data from a source into the database.

Currently, the following sources with CSV formatted payload are supported:

* MQTT
* FTP

To describe the ingestion process from the source into the database the AggregateProcess of the [OGC SensorML 2.0](http://www.opengeospatial.org/standards/sensorml) standard (SML) is used.
How the AggregateProcess should be defined is described in the following sections.

## AggregateProcess

The AggregateProcess contains as a minimum [components](#components) and [connections](#connections).

### Components

An *AggregateProcess* should have two or more components in which the [first component](#source-component) defines the source while the [last component](#sink-component) defines the sink (SOS). Other components can be inserted in between to describe further processing steps.

#### Source component

The **first** component of the list describes the sensor which produces the observed data and how the data is provided by this sensor.

To identify this component as source, the name attribute should be set to *source_output*.

The content of the component should be of type **PhysicalSystem** and should contain the information described in [Default source and sink](#default-source-and-sink).

The *sml:outputs* of this component contains a *sml:outputList* with several one *sml:output* that contains a *sml:DataInterface* that describes the data stream of the source which is different for each source. More information about the definition of the *sml:DataInterface* is described in the [Sources](#sources) section.

#### Sink component

The **last** component of the list describes the sensor that would be provided by the SOS service

To identify this component as sink, the name attribute should be set to *sos_input*.

The content of the component should be of type **PhysicalSystem** and should contain the information described in [Default source and sink](#default-source-and-sink).

##### Sink sml:outputs

The *sml:outputs* contains a *sml:outputList* with several *sml:output* that describes the observedProperties/phenomenon that would be provided by the SOS server.

Each *sml:output* requires a *name* attribute that is [NCName](https://www.w3.org/TR/1999/WD-xmlschema-2-19990924/#NCName) conform. The content of this element should be a *swe:AbstractSimpleComponent* such as:

* swe:Quantity
* swe:Count
* swe:Text
* ...

Description of the elements and attributes of the *swe:AbstractSimpleComponent*s:

| name| (a)ttribute/(e)lement | description |
| :-------------: | :-------------:| :-------------: |
| definition | a |the identifier of the observedProperty/phenomenon, should be a link to a vocabulary entry |
| sml:label | e | the label/name of the observedProperty/phenomenon |
| sml:description | e | the description of the observedProperty/phenomenon |
| sml:uom | e | the unit of measure of the observedProperty/phenomenon (only for swe:Quantity) |

Description of the attributes of the *swe:uom* element of the *swe:Quantity*

| name | description |
| :-------------: | :-------------: |
| code | the symbol of the unit like *degC*, *%*, *m/s* |
| xlink:href | link to the definition in a vocabulary |
| xlink:title| the human readable name of the unit |

Here is an example of the *sml:outputs*:

```
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
```

#### Default source and sink 

##### gml:identifier

The *unique identifier* of the sensor, which would also be used to query the SOS server, should be defined in the *gml:identifier* element of the PhysicalProcess with *codeSpace="uniqueID"*.

Here is an example of the gml:identifier with identifier *DWD-OPENDATA-10315*:

```
<gml:identifier codeSpace="uniqueID">DWD-OPENDATA-10315</gml:identifier>
```

##### sml:identification

The **sml:identification** should contain more detailed information to identify the sensor if they are available. This can be a *long name, a *short name*, the *manufacturer*, a *model name*, a *serial number* or further information.

Here is an example of the sml:identification:

```
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
```

##### sml:featuresOfInterest

The **sml:sml:featuresOfInterest** provides the *identifier* and the *name* of the *featureOfInterest* that relates to the sensor.

The *identifier* should be defined in the *xlink:href* attribute and the *name* in the *xlink:title* of the *sml:feature* element.

Here is an example description:

```
<sml:featuresOfInterest>
    <sml:FeatureList>
        <sml:feature xlink:href="Muenster-Osnabrueck" xlink:title="Airport Muenster/Osnabrueck (FMO)" />
    </sml:FeatureList>
</sml:featuresOfInterest>
```

##### sml:position

The **sml:position** should contain the coordinates of the sensor.

Here is an example description:

```
<sml:position>
    <swe:Vector referenceFrame="urn:ogc:def:crs:EPSG::4326">
        <swe:coordinate name="easting">
            <swe:Quantity axisID="x">
                <swe:uom code="degree"/>
                <swe:value>7.7</swe:value>
            </swe:Quantity>
        </swe:coordinate>
        <swe:coordinate name="northing">
            <swe:Quantity axisID="y">
                <swe:uom code="degree"/>
                <swe:value>52.133333</swe:value>
            </swe:Quantity>
        </swe:coordinate>
        <swe:coordinate name="altitude">
            <swe:Quantity axisID="z">
                <swe:uom code="m"/>
                <swe:value>53.0</swe:value>
            </swe:Quantity>
        </swe:coordinate>
    </swe:Vector>
</sml:position>
```

### Connections

The Connections contains multiple links between the outputs of the source and the sink component to define which 

In the following example the the *@source* is a placeholder for the *gml:id* of the source (first) component and the *@sink* is a placeholder for the *gml:id* of the sink (last) component. The *@name* is the placeholder for the *name* of the *sml:output* elements. 

```
<sml:connections>
	<sml:ConnectionList>
		<sml:connection>
			<sml:Link>
				<sml:source ref="components/@source/outputs/@name"/>
				<sml:destination ref="components/@sink/outputs/@name"/>
			</sml:Link>
	</sml:connection>
   ...
</sml:connections>

```

## Sources

This section describes how the *sml:DataInterface* should be defined an which conventions should be followed.

In the *sml:DataInterface/sml:data/swe:DataStream/swe:elementType/swe:DataRecod* the elements of the data stream are defined with the following conventions:

1. The element that describes values/columns which should not be processed/passed to the sink should **NOT** have defined a *definition* attribute
1. The sampling/phenomenon time should be defined as a swe:Time with definition **http://www.opengis.net/def/property/OGC/0/PhenomenonTime**
   1. If the sampling/phenomenon time is splitted into a date and a time value/column the definition value should be expanded with **#date** for the date and **#time** for time
1. The *swe:encoding* defines the encoding of the data stream, for example *swe:TextEncoding* with the definition of the separators.
1. In the *sml:interfaceParameters* the *definition* attribute defines the *name of the source sink* that should be used.


Here is a reduced example of the *sml:outputs* for the sources:

```
<sml:outputs>
    <sml:OutputList>
        <sml:output name="streamOutput">
            <sml:DataInterface>
                <sml:data>
                    <swe:DataStream>
                        <swe:elementType name="outputStream">
                            <swe:DataRecord>
                                <swe:field name="datetime">
                                  <swe:Time definition="http://www.opengis.net/def/property/OGC/0/PhenomenonTime">
                                     <swe:label>Date and time</swe:label>
                                     <swe:description>Date-time is a time stamp from a Global Positioning System receiver at the cable observatory shore station in the format YYYY-MM-DDThh:mm:ss.sss .</swe:description>
                                     <swe:uom/>
                                  </swe:Time>
                                </swe:field>
                                <swe:field ...
                            </swe:DataRecord>
                        </swe:elementType>
                        <swe:encoding>
                            <swe:TextEncoding tokenSeparator=";" blockSeparator="\n" decimalSeparator="."/>
                        </swe:encoding>
                        <swe:values/>
                    </swe:DataStream>
                </sml:data>
                <sml:interfaceParameters>
                    <swe:DataRecord definition="https://52north.org/swe-ingestion/ftp">
                        <swe:field ...
                    </swe:DataRecord>
                </sml:interfaceParameters>
            </sml:DataInterface>
        </sml:output>
    </sml:OutputList>
</sml:outputs>
```

### MQTT source

The MQTT source can be used to get data from a MQTT stream. The following definitions should be used to identify the source and the interface parameters:


| defininition of | value | description |
| :-------------: | :-------------: | :-------------: |
| swe:DataRecord | https://52north.org/swe-ingestion/mqtt/3.1 | definition of the source
| swe:Text | https://52north.org/swe-ingestion/mqtt/3.1#url | the URL to the MQTT service |
| swe:Text | https://52north.org/swe-ingestion/mqtt/3.1#password | the password for the MQTT service |
| swe:Text | https://52north.org/swe-ingestion/mqtt/3.1#username | the password for the MQTT service |
| swe:Text | https://52north.org/swe-ingestion/mqtt/3.1#topics | the topic that should be subscribed to |

```
<sml:interfaceParameters>
  <swe:DataRecord definition="https://52north.org/swe-ingestion/mqtt/3.1">
     <swe:field name="mqtt_broker_url">
        <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#url">
           <swe:value>tcp://mqtt.stream.org:1884</swe:value>
        </swe:Text>
     </swe:field>
     <swe:field name="mqtt_broker_password">
        <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#password">
           <swe:value>password</swe:value>
        </swe:Text>
     </swe:field>
     <swe:field name="mqtt_broker_username">
        <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#username">
           <swe:value>username</swe:value>
        </swe:Text>
     </swe:field>
     <swe:field name="mqtt_broker_topics">
        <swe:Text definition="https://52north.org/swe-ingestion/mqtt/3.1#topics">
           <swe:value>topic_name</swe:value>
        </swe:Text>
     </swe:field>
  </swe:DataRecord>
</sml:interfaceParameters>
```

##### MQTT example process description

An example process description is located here:

[MQTT AggreagateProcess for Weather data](https://github.com/52North/SWE-Ingestion-Service/blob/dev/etc/sensors/AggregateProcess-Weather.xml)

### FTP source

The FTP source can be used to get data from a FTP server.

To process only the latest data from the FTP server that have not yet been processed, an additional component should be defined that defines the filtering processes. How the component should be defined is described in the [Filter component](#filter-component).

The following definitions should be used to identify the source and the interface parameters:


| defininition of | value | description |
| :-------------: | :-------------: | :-------------: |
| swe:DataRecord | https://52north.org/swe-ingestion/ftp | definition of the source
| swe:Text | https://52north.org/swe-ingestion/ftp#host | the URL to the FTP service |
| swe:Text | https://52north.org/swe-ingestion/ftp#password | the password for the FTP service |
| swe:Text | https://52north.org/swe-ingestion/ftp#username | the password for the FTP service |
| swe:Text | https://52north.org/swe-ingestion/ftp#remote-dir | the remote directory that should be read |
| swe:Text | https://52north.org/swe-ingestion/ftp#filename-pattern | the file that should be read |

```
<sml:interfaceParameters>
    <swe:DataRecord definition="https://52north.org/swe-ingestion/ftp">
        <swe:field name="ftp_host">
            <swe:Text definition="https://52north.org/swe-ingestion/ftp#host">
                <swe:value>opendata.dwd.de</swe:value>
            </swe:Text>
        </swe:field>
        <!-- optional, default: guest -->
        <swe:field name="ftp_password">
            <swe:Text definition="https://52north.org/swe-ingestion/ftp#password">
                <swe:value>guest</swe:value>
            </swe:Text>
        </swe:field>
        <!-- optional, default: anonymous -->
        <swe:field name="ftp_username">
            <swe:Text definition="https://52north.org/swe-ingestion/ftp#username">
                <swe:value>anonymous</swe:value>
            </swe:Text>
        </swe:field>
        <swe:field name="ftp_remote-dir">
            <swe:Text definition="https://52north.org/swe-ingestion/ftp#remote-dir">
                <swe:value>/weather/weather_reports/poi</swe:value>
            </swe:Text>
        </swe:field>
        <swe:field name="ftp_filename-pattern">
            <swe:Text definition="https://52north.org/swe-ingestion/ftp#filename-pattern">
                <swe:value>10315-BEOB.csv</swe:value>
            </swe:Text>
        </swe:field>
    </swe:DataRecord>
</sml:interfaceParameters>
```

#### Filter component

The filter component should be a **sml:SimpleProcess** with *sml:inputs* and the *sml:outputs* which should be equal to the *sml:outputs* of the [Source component](#source-component) without *sml:interfaceParameters* definition.

Additionally, the filter component should define *sml:parameters* which defines configuration parameter for the filter processors and a sml:method that should be defined as follows:

```
<sml:method xlink:href="https://52north.org/swe-ingestion/csv-file-filter"/>
```

In the *sml:parameters* you should define the header line count, the date, time or datetime format if it is not ISO 8601 formatted.

```
<sml:parameters>
    <sml:ParameterList>
        <sml:parameter name="file-filter-config">
            <swe:Count definition="https://52north.org/swe-ingestion/csv-file-filter#header-line-count">
                <swe:label>Header Line Count</swe:label>
                <swe:description>The number of lines to strip from the csv file</swe:description>
                <swe:value>3</swe:value>
            </swe:Count>
        </sml:parameter>
		  <sml:parameter ...
    </sml:ParameterList>
</sml:parameters>
```

Definition of parameters for data and time format:

```
<sml:parameter name="configuration3">
    <swe:Text definition="https://52north.org/swe-ingestion/csv-timestamp-filter#date-column-format">
        <swe:label>Format of date</swe:label>
        <swe:description>The format of the date</swe:description>
        <swe:value>dd.MM.yy</swe:value>
    </swe:Text>
</sml:parameter>
<sml:parameter name="configuration5">
    <swe:Text definition="https://52north.org/swe-ingestion/csv-timestamp-filter#time-column-format">
        <swe:label>Format of time</swe:label>
        <swe:description>The format of the time in UTC</swe:description>
        <swe:value>HH:mm</swe:value>
    </swe:Text>
</sml:parameter>
```

Definition of parameter for datatime format:

```        
<sml:parameter name="configuration3">
    <swe:Text definition="https://52north.org/swe-ingestion/csv-timestamp-filter#datetime-column-format">
        <swe:label>Format of date</swe:label>
        <swe:description>The format of the date</swe:description>
        <swe:value>dd.MM.yy HH:mm</swe:value>
    </swe:Text>
</sml:parameter>
```

##### FTP example process description

An example process description is located here:

[FTP AggregateProcess for DWD Weather data](https://github.com/52North/SWE-Ingestion-Service/blob/dev/etc/sensors/AggregateProcess-dwd.xml)