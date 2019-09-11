from kafka import KafkaProducer
import json
import time
from random import *



def connect():
    producer = None
    try:
        producer = KafkaProducer(bootstrap_servers='localhost:9092')
        print('Successful connection wit Kafka ...')
    except Exception as ex:
        print('Failure:')
        print(str(ex))
    finally:
        return producer

'''publishes myo testdata under recording id'''
def send(producer, message, topic):
    try:
        m = json.dumps(message, default=lambda o: o.__dict__).encode('utf-8')
        k = (message["recordingID"]).encode('utf-8')
        producer.send(topic, value=m, key=k)
        producer.flush()
        print("sent: ")
        print(m)
    except Exception as ex:
        # print('Failure:')
        None
        # print(str(ex))

'''loads testdata from myo-testdata.json file'''
def loadMyoJson():
    with open('myo-testdata.json') as json_file:
        data = json.load(json_file)
        return data

'''publsihes myo testdata frame for frame'''
def publishMyoTestData(producer):
    data = loadMyoJson()
    recordingID = data["recordingID"]
    applicationName = data["applicationName"]
    topic = "__.goethe-universitaet-frankfurt.myo.frames"
    print(recordingID)
    print(applicationName)
    for frame in data["frames"]:
        frame["recordingID"] = recordingID
        frame["applicationName"] = applicationName
        send(producer, frame, topic)


producer = connect()
publishMyoTestData(producer)
