"use strict";

const ACTOR_NAME = "Admin User"
const LEAP_WEBSOCKET_URL = 'ws://localhost:6437/';
const KAFKA_HOST = 'localhost:9092';
const TOPIC = "__.goethe-universitaet-frankfurt.myo.frames";

// websocket
const WebSocketClient = require('websocket').client;

const websocketClient = new WebSocketClient();
// kafka producer
const Kafka = require('kafka-node');
const producerOptions = {
    requireAcks: 0,    // Configuration for when to consider a message as acknowledged, default 1
                        // 0 means, try once, if it fails - go on (no retries)
    ackTimeoutMs: 100,    // The amount of time in milliseconds to wait for all acks before considered, default 100ms
    partitionerType: 2    // Partitioner type (default = 0, random = 1, cyclic = 2, keyed = 3, custom = 4), default 0
};
// create kafka producer
let Producer = Kafka.Producer;
let kafkaClient = new Kafka.KafkaClient({kafkaHost: KAFKA_HOST});
let producer = new Producer(kafkaClient, producerOptions);

/**
 *  cb for producer ready
  * @param callback
 * @returns {Kafka.Producer}
 */
let producerReadyCallback = function () {
    console.log("kafka producer is connected and ready");
    /**
     * cb for websocket connected
     * @param wsConnection
     */
    let websocketConnectedCallback = function(wsConnection) {
        console.log("websocket connection is established and ready");
        // ws errorhandling
        wsConnection.on('error', function(err) {
            console.log("websocket error: " + err.toString());
        });
        // on receiving a message from leapmotions websocket server
        wsConnection.on('message', function(websocketMessage) {
            if (websocketMessage.type !== 'utf8') {
                return;
            }
            const kafkaMessage = [{topic: TOPIC, messages: websocketMessage.utf8Data, key: ACTOR_NAME, timestamp: Date.now()}];
            producer.send(kafkaMessage, function (err, data) {});
        });

        wsConnection.on('close', function() {
            console.log('websocket closed, producer kafka connection will be closed, too');
            producer.close()
        });

        wsConnection.sendUTF("{\"enableGestures\": true}");
    };
    websocketClient.on('connect', websocketConnectedCallback);
    // connect to websocket
    websocketClient.connect(LEAP_WEBSOCKET_URL);
};

/**
 * cb for kafka producer error
 * @param err
 */
let producerErrorCallback = function (err) {
    console.log("error in kafka producer: " + err.toString())
};

// attach callbacks to producer
producer.on('ready', producerReadyCallback);
producer.on('error', producerErrorCallback);


