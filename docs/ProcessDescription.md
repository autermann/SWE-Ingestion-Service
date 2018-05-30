# Process Description definiton

This document describes the definition of the process description to ingest data from a source into the database.

Currently, the following sources with CSV formatted payload are supported:

* MQTT
* FTP

To describe the ingestion process from the source into the database the AggregateProcess of the [OGC SensorML 2.0](http://www.opengeospatial.org/standards/sensorml) standard (SML) is used.
How the AggregateProcess should be defined is described in the following sections.

## AggregateProcess

The AggregateProcess contains as a minimum **components** and **connections**.

### Components
#### First component
#### Last component

### Connections

The Connections contains multiple links between the outputs of the source and the sink component to define which 

In the following example the the *@source* is a placeholder for the *gml:id* of the source (first) component and the *@sink* is a placeholder for the *gml:id* of the sink (last) component.

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

## MQTT source
## FTP source

