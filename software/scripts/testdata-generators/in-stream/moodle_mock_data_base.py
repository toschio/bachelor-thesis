import json
import time
from kafka import KafkaProducer
from random import *

'''
is an object attached by debezium
'''


class Source:
    def __init__(self, table):
        self.version = '0.9.3.Final'
        self.connector = 'mysql'
        self.name = 'edutec'
        self.server_id = 0
        self.ts_sec = 0
        self.gtid = None
        self.file = 'mysql-bin.000006'
        self.pos = 154
        self.row = 0
        self.snapshot = True
        self.thread = None
        self.db = "moodle"
        self.table = table
        self.query = None


'''
the wraper object constructed by debezium
captures the former and the current state from binary log
'''


class Cdc:
    def __init__(self, value, source, topicname):
        self.before = None
        self.after = value
        self.topic = topicname
        self.source = source
        self.op = 'c'
        self.ts_ms = time.time()


'''
the Moodle User entity
'''


class Mdl_User:
    def __init__(self, user_id, username, firstname, lastname, email):
        self.email = 'emailadress'
        self.name = 'firstname'
        self.id = user_id
        self.auth = "manual"
        self.confirmed = 1
        self.policyagreed = 0
        self.deleted = 0
        self.suspended = 0
        self.mnethostid = 1
        self.username = username
        self.password = "$2y$10$dWTO17/bcvTMAqF7SSLRzuv9S59rc45X8lQADpeU.aNOCwQP2khHu"
        self.idnumber = "someidnumber"
        self.firstname = firstname
        self.lastname = lastname
        self.email = email
        self.emailstop = 0
        self.icq = "icq"
        self.skype = "skype"
        self.yahoo = "yahoo"
        self.aim = "aim"
        self.msn = "msn"
        self.phone1 = "01234"
        self.phone2 = "40123"
        self.institution = "an institution"
        self.department = "a department"
        self.address = "an adress"
        self.city = "a city"
        self.country = "de"
        self.lang = "en"
        self.calendartype = "gregorian"
        self.theme = "dark"
        self.timezone = "99"
        self.firstaccess = 1560043501
        self.lastaccess = 1560048856
        self.lastlogin = 1560043501
        self.currentlogin = 1560043646
        self.lastip = "172.18.0.1"
        self.secret = "asecret"
        self.picture = 0
        self.url = ""
        self.description = "some url"
        self.descriptionformat = 1
        self.mailformat = 1
        self.maildigest = 0
        self.maildisplay = 1
        self.autosubscribe = 1
        self.trackforums = 0
        self.timecreated = 1560043574
        self.timemodified = 1560043574
        self.trustbitmask = 10
        self.imagealt = "someimagealt"
        self.lastnamephonetic = "phonetics"
        self.firstnamephonetic = "phonetics"
        self.middlename = "not catholic"
        self.alternatename = "not in the net"


'''
Moodle Forum User
'''


class Mdl_ForumPost:
    def __init__(self, mdlForumPostID, discussionID, parentID, userID, t0):
        self.id = mdlForumPostID
        self.discussion = discussionID
        self.parent = parentID
        self.userid = userID
        self.created = t0
        self.modified = self.created
        self.mailed = 0
        self.subject = "some subject"
        self.message = "some message " + str(self.id)
        self.messageformat = 0
        self.messagetrust = 0
        self.attachment = "nonetoattach"
        self.totalscore = 0
        self.mailnow = 0
        self.deleted = 0


'''
Moodle Forum
'''


class Mdl_Forum:

    def __init__(self, id):
        self.id = id
        self.course = id
        self.type = "type"
        self.name = "name " + str(id)
        self.intro = "intro " + str(id)
        self.introformat = 0
        self.assessed = False
        self.assesstimestart = 0
        self.assesstimefinish = 0
        self.scale = 0
        self.maxbytes = 0
        self.maxattachments = 0
        self.forcesubscribe = 0
        self.trackingtype = 0
        self.rsstype = 0
        self.rssarticles = 0
        self.timemodified = time.time()
        self.warnafter = 0
        self.blockafter = 0
        self.blockperiod = 0
        self.completiondiscussions = 0
        self.completionreplies = 0
        self.completionposts = 0
        self.displaywordcount = False
        self.lockdiscussionafter = False


'''
 Moodle Quiz
'''
class Mdl_Quiz:
    def __init__(self, course, name):

        self.course = course
        self.name = name
        self.intro = "some intro of " + name
        self.introformat = 1
        self.timeopen = 0
        self.timeclose = 0
        self.timelimit = 0
        self.overduehandling = "autosubmit"
        self.graceperiod = 0
        self.preferredbehaviour = "deferredfeedback"
        self.canredoquestions = 0
        self.attempts = 0
        self.attemptonlast = 0
        self.grademethod = 1
        self.decimalpoints = 2
        self.questiondecimalpoints = -1
        self.reviewattempt = 69888
        self.reviewcorrectness = 4352
        self.reviewmarks = 4352
        self.reviewspecificfeedback = 4352
        self.reviewgeneralfeedback = 4352
        self.reviewrightanswer = 4352
        self.reviewoverallfeedback = 4352
        self.questionsperpage = 1
        self.navmethod = "free"
        self.shuffleanswers = 1
        self.sumgrades = 0.00000
        self.grade = 10.00000
        self.timecreated = time.time()
        self.timemodified = time.time()
        self.password = ""
        self.subnet = ""
        self.browsersecurity = "-"
        self.delay1 = 0
        self.delay2 = 0
        self.showuserpicture = 0
        self.showblocks = 0
        self.completionattemptsexhausted = 0
        self.completionpass = 0
        self.allowofflineattempts = 0


class Mdl_Quiz_Attempt:
    def __init__(self, quizid, userid):
        self.quiz = quizid
        self.userid = userid
        self.attempt = 1
        self.uniqueid = quizid * userid * 100000
        self.layout = "smol"
        self.currentpage = 1
        self.preview = 1
        self.state = "started"
        self.timestart = time.time()
        self.timefinish = time.time() + 10000
        self.timemodified = time.time()
        self.timemodifiedoffline = time.time()
        self.timecheckstate = 1
        self.sumgrades = 1

def connect():
    producer = None
    try:
        producer = KafkaProducer(bootstrap_servers='localhost:9092', client_id='python-leap-motion-mock-data-producer')
        print('Successful connection wit Kafka ...')
    except Exception as ex:
        print('Failure:')
        print(str(ex))
    finally:
        return producer


def send(producer, message):
    try:
        m = json.dumps(message, default=lambda o: o.__dict__).encode('utf-8')
        producer.send(message.topic, value=m)
        producer.flush()
    except Exception as ex:
        print('Failure:')
        print(str(ex))

def sendJson(producer, message, topic, key):
    try:
        producer.send(topic = topic, value=json.dumps(message).encode('utf-8'), key = bytes(key, 'utf-8'))
        producer.flush()
    except Exception as ex:
        print('Failure:')
        print(str(ex))




def constructKafkaObject(value, source, topic):
    source = Source(source)
    cdc = Cdc(value, source, topic)
    return cdc
