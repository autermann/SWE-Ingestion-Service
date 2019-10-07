//
// CONSTANTS
//
// const mqttUrl = 'tcp://localhost:1884';
const mqttUrl = 'tcp://mqtt.marine.ie:1883';
//const mqttUrl = 'tcp://nexos.dev.52north.org:1884';
const mqttTopic = 'airmar-rinville-1';
//const mqttTopic = 'airmar-rinville-1-generated';
var redisHost = 'localhost';
//const redisHost = 'redis';
const redisPort = 6379;
var messageGeneratorFrequencyInMillis = 1000;
const moscaPort = 1883;
require('log-timestamp');
// We skip the client if message generation is requested
let generate = false;
var myArgs = process.argv.slice(2);
myArgs.forEach(a => {
    if ('generate'.match(a.toLowerCase())) {
        generate = true;
    }
});
if (myArgs[1] != null && myArgs[1] != "") {
    redisHost = myArgs[1];
}
if (myArgs[2] != null && myArgs[2] != "") {
    messageGeneratorFrequencyInMillis = myArgs[2];
}
// 
// Set-Up local MQTT SERVER based on mosca
//
var mosca = require('mosca');

var redisServer = {
    type: 'redis',
    redis: require('redis'),
    db: 12,
    port: redisPort,
    return_buffers: true, // to handle binary payloads
    host: redisHost
};

var moscaSettings = {
    port: moscaPort,
    backend: redisServer,
    persistence: {
        factory: mosca.persistence.Redis
    }
};

var server = new mosca.Server(moscaSettings);
server.on('ready', function() {
    console.log('mqtt SERVER Mosca server is up and running: redis: ' +
        redisHost + ':' + redisPort + '; mosca.port: ' + moscaPort);
    setup();
});
server.on('clientConnected', function (client) {
    console.log('mqtt SERVER: client connected: ' + client.id);
});
server.on('clientDisconnected', function (client) {
    console.log('mqtt SERVER: client DISconnected: ' + client.id);
});
function publishMessage(message, isGenerated = false) {
    var messageType = 'received';
    if (isGenerated) {
        messageType = 'generated';
    }
    server.publish(message, function (err) {
        if (err) {
            console.error(err);
        } else 
        console.log('mqtt SERVER published ' + messageType + ' message in "' + message.topic  + '": ' + message.payload);
    });
}
function setup() {
//
// Set-Up local MQTT CLIENT
//
if (!generate) {
    const mqtt = require('mqtt');
    const client = mqtt.connect(mqttUrl);

    var garageState = '';
    var connected = false;

    client.on('connect', () => {
        client.subscribe(mqttTopic, function (err) {
            if(!err) {
                console.log('mqtt CLIENT:\n' +
                    '\tsubscribed to remote mqtt server:\n' +
                    '\t\turl   : ' + mqttUrl + '\n' +
                    '\t\ttopic : ' + mqttTopic);
            } else {
                console.log('mqtt CLIENT:\n' +
                    '\tFAILED to subscribe to remote mqtt server:\n' +
                    '\t\turl   : ' + mqttUrl + '\n' +
                    '\t\ttopic : ' + mqttTopic);
            }
        });
        
    });

    client.on('message', (topic, message) => {
        console.log('mqtt CLIENT:\n' +
            '\tmessage received from remote mqtt server for topic "' +
            topic +
            '": \n' +
            message.toString());
        if (topic === mqttTopic) {
            connected = (message.toString() === 'true');
            var msg = message.toString();
    //        console.log(msg);
            if (msg.indexOf('$WIMDA') > -1) {

                // todo: parsing:
                var modifiedMessage = "";

                // get datetime:
                var datetime = msg.substring(0, 24) + ";";
                modifiedMessage = datetime;
                msg = msg.substring(25, msg.length);

                // get "name":
                modifiedMessage += msg.substring(0, msg.indexOf('|')) + ";";
                msg = msg.substring(msg.indexOf('|') + 1, msg.length);

                // get "Air temperature":
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);

                modifiedMessage += msg.substring(0, msg.indexOf(',')) + ";";
                msg = msg.substring(msg.indexOf(',') + 1);

                // get "Water temperature":
                msg = msg.substring(msg.indexOf(',') + 1);
    //            modifiedMessage += msg.substring(0, msg.indexOf(',')) + ";";
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);

                // get "humidity":
                modifiedMessage += msg.substring(0, msg.indexOf(',')) + ";";
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);

                // get "dew point":
                modifiedMessage += msg.substring(0, msg.indexOf(',')) + ";";
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);

                // get "wind direction":
                modifiedMessage += msg.substring(0, msg.indexOf(',')) + ";";
                msg = msg.substring(msg.indexOf(',') + 1);

                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);
                msg = msg.substring(msg.indexOf(',') + 1);

                // get "wind speed":
                modifiedMessage += msg.substring(0, msg.indexOf(','));

                var pubMessage = {
                    topic: mqttTopic,
                    payload: modifiedMessage, // or a Buffer
                    qos: 0, // 0, 1, or 2
                    retain: false // or true
                };
                publishMessage(pubMessage, false);
            }
        }
    });
} else {
    console.log('Message generation activated.')
    //
    //  Data generator
    //
    function randomize(start, end) {
        return ((Math.random() * (end * 10.0 - start)) / 10.0 + start).toFixed(2);
    }

    generateDataMessage = function () {
    var msg = "";

    // get datetime:
    var dateNow = new Date();
    msg = dateNow.toISOString() + ";";

    // get "name":
    msg += "AIRMAR-RINVILLE-1" + ";";

    // get "Air temperature":
    //    msg += "11.4;";
    msg += randomize(-10.0,30.0) + ";";

    // get "humidity":
    //    msg += "70.0;";
    msg += randomize(0.0,100.0) + ";";

    // get "dew point":
    //    msg += "3.2;";
    msg += randomize(-10.0,30.0) + ";";

    // get "wind direction":
    //    msg += "131.0;";
    msg += randomize(0.0,359.0) + ";";

    // get "wind speed":
    //    msg += "4.3";
    msg += randomize(0.0,35.0);

    return msg;
    };

    var generateCounter = 0;
    generateData = function (generateCounter) {
        setTimeout(
            function () {
                var msg = generateDataMessage();
                var pubMessage = {
                    topic: mqttTopic,
                    payload: msg,
                    qos: 0,
                    retain: false
                };
                publishMessage(pubMessage, true);
                generateData(generateCounter + 1);
            },
            messageGeneratorFrequencyInMillis);
    };
    generateData(generateCounter);
}

}