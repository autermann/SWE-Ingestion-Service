//
// CONSTANTS
//
// const mqttUrl = 'tcp://localhost:1884';
const mqttUrl = 'mqtt://mosca';
const mqttTopic = 'airmar-rinville-1';
var messageGeneratorFrequencyInMillis = 5000;
// next is only required when NOT running in docker as docker adds timestamps to logs by default
// require('log-timestamp');

const client = require('mqtt').connect(mqttUrl);


client.on('connect', () => {
    
  console.log('Message generation activated.')
  //
  //  Data generator
  //
  function randomize(start, end) {
      return ((Math.random() * (end * 10.0 - start)) / 10.0 + start).toFixed(2);
  }

  function generateDataMessage() {
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
  function generateData(generateCounter) {
      setTimeout(function () {
          var msg = generateDataMessage();
          client.publish(mqttTopic, msg, null, function() {
            console.log("message published in " + mqttTopic + ": " + msg, ...arguments);
          })
          generateData(generateCounter + 1);
      }, messageGeneratorFrequencyInMillis);
  };
  generateData(generateCounter);
});
