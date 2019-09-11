#!/usr/bin/python3
import sys
import json

# insert at 1, 0 is the script path (or '' in REPL)
sys.path.append('./../in-stream')

import moodle_mock_data_base as Base
import moodle_db_connector as Dbconnector

import time
from random import *


'''
    publishes a 100 users, 10 forums, 1000 forum posts with the following conditions: 
        - each user exactly 10 on each forum 
        - each post published every other second
'''


def publishMoodleUser(cursor, db):
    print ('Publishing Moodle User ...')
    userCounter = 1
    starttime = time.time()
    while userCounter <= 100:
        user = Base.Mdl_User(userCounter, 'fkafka' + str(userCounter), 'franz' + str(userCounter),
                             'kafka' + str(userCounter), 'franz@kafka')
        # Prepare SQL query to INSERT a record into the database.
        try:
            # Execute the SQL command
            cursor.execute("""INSERT INTO mdl_user(auth,confirmed,policyagreed,deleted,suspended,mnethostid, username, password,idnumber,firstname,lastname,
            email,emailstop, icq,skype,yahoo,aim,msn,phone1,phone2,institution,department,address,city,country,lang,calendartype,theme,timezone,firstaccess,
            lastaccess,lastlogin, currentlogin,lastip,secret,picture,url,description,descriptionformat,mailformat,maildigest,maildisplay,autosubscribe,trackforums,
            timecreated,timemodified,trustbitmask,imagealt,lastnamephonetic,firstnamephonetic,middlename,alternatename) 
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                           (user.auth, user.confirmed, user.policyagreed, user.deleted, user.suspended, user.mnethostid,
                            user.username, user.password, user.idnumber,
                            user.firstname, user.lastname, user.email, user.emailstop, user.icq, user.skype, user.yahoo,
                            user.aim, user.msn, user.phone1,
                            user.phone2, user.institution, user.department, user.address, user.city, user.country,
                            user.lang, user.calendartype, user.theme,
                            user.timezone, user.firstaccess, user.lastaccess, user.lastlogin, user.currentlogin,
                            user.lastip, user.secret, user.picture, user.url,
                            user.description, user.descriptionformat, user.mailformat, user.maildigest,
                            user.maildisplay, user.autosubscribe, user.trackforums,
                            user.timecreated, user.timemodified, user.trustbitmask, user.imagealt,
                            user.lastnamephonetic, user.firstnamephonetic, user.middlename,
                            user.alternatename))
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()
            print ("something went wrong with user construction")
        userCounter = userCounter + 1
        time.sleep(0.2 - ((time.time() - starttime) % 0.2))  # five times the second


def publishMoodleForum(cursor, db):
    print('Publishing Moodle Forum ...')
    forumCounter = 1
    starttime = time.time()
    while forumCounter <= 10:
        forum = Base.Mdl_Forum(forumCounter)
        try:

            # Execute the SQL command
            cursor.execute("""INSERT INTO mdl_forum (course,type,name,intro,introformat,assessed,assesstimestart,assesstimefinish,scale,maxbytes,maxattachments,
            forcesubscribe,trackingtype,rsstype,rssarticles,timemodified,warnafter,blockafter,blockperiod,completiondiscussions,completionreplies,
            completionposts,displaywordcount,lockdiscussionafter)
                    VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                           (forum.course, forum.type, forum.name, forum.intro, forum.introformat, forum.assessed,
                            forum.assesstimestart,
                            forum.assesstimefinish, forum.scale, forum.maxbytes, forum.maxattachments,
                            forum.forcesubscribe, forum.trackingtype,
                            forum.rsstype, forum.rssarticles, forum.timemodified, forum.warnafter, forum.blockafter,
                            forum.blockperiod,
                            forum.completiondiscussions, forum.completionreplies, forum.completionposts,
                            forum.displaywordcount, forum.lockdiscussionafter))
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()
            print ("something went wrong with forum production")
        forumCounter = forumCounter + 1
        time.sleep(0.1 - ((time.time() - starttime) % 0.1))


def publishMoodleForumPosts(cursor, db):
    print('Publishing Moodle Forum Posts ..')
    t0 = int(time.time())
    forumPostCounter = 1
    starttime = time.time()
    while forumPostCounter <= 1000:
        forumID = int((forumPostCounter - 1) / 100) + 1
        userID = int((forumPostCounter - 1) / 10) + 1
        ''' publish as if after a second '''
        t0 = time.time() # t0 + 2
        parentForumPost = forumPostCounter - 1
        ''' if forum post is published in a new discussion set parent to 0'''
        if (int((forumPostCounter - 1) / 100)) == ((forumPostCounter - 1) / 100000):
            parentForumPost = 0

        forumPost = Base.Mdl_ForumPost(forumPostCounter, forumID, parentForumPost, userID, t0)
        try:
            # Execute the SQL command
            cursor.execute("""INSERT INTO mdl_forum_posts (discussion, parent, userid, created, modified, mailed, subject, message, messageformat, messagetrust, attachment, totalscore, mailnow, deleted)
                        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                           (forumPost.discussion, forumPost.parent, forumPost.userid, forumPost.created,
                            forumPost.modified, forumPost.mailed,
                            forumPost.subject, forumPost.message, forumPost.messageformat, forumPost.messagetrust,
                            forumPost.attachment,
                            forumPost.totalscore, forumPost.mailnow, forumPost.deleted))
            # Commit your changes in the database
            db.commit()
            if forumPostCounter % 1000 == 0:
                print (forumPostCounter)
        except:
            # Rollback in case there is any error
            db.rollback()
            print ("something went wrong with forum post production")
        forumPostCounter = forumPostCounter + 1
        time.sleep(0.5 - ((time.time() - starttime) % 0.5))  # five times the second


def publishMoodleQuiz(cursor, db):
    t0 = int(time.time())
    quizCounter = 1
    starttime = time.time()
    while quizCounter <= 2:
        quiz = Base.Mdl_Quiz(course=quizCounter, name="quiz " + str(quizCounter) + " name")
        print (quiz.name)
        try:
            # Execute the SQL command
            cursor.execute("""INSERT INTO mdl_quiz (course, name, intro, introformat, timeopen, timeclose, timelimit, overduehandling, 
             graceperiod, preferredbehaviour, canredoquestions, attempts, attemptonlast, grademethod, 
             decimalpoints, questiondecimalpoints, reviewattempt, reviewcorrectness, reviewmarks, 
             reviewspecificfeedback, reviewgeneralfeedback, reviewrightanswer, reviewoverallfeedback, 
             questionsperpage, navmethod, shuffleanswers, sumgrades, grade, timecreated, timemodified, 
             password, subnet, browsersecurity, delay1, delay2, showuserpicture, showblocks, 
             completionattemptsexhausted, completionpass, allowofflineattempts)                   
                VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
            (quiz.course,quiz.name,quiz.intro,quiz.introformat,quiz.timeopen,quiz.timeclose,quiz.timelimit,
                quiz.overduehandling,quiz.graceperiod,quiz.preferredbehaviour,quiz.canredoquestions,
             quiz.attempts,quiz.attemptonlast,quiz.grademethod,quiz.decimalpoints,quiz.questiondecimalpoints,
             quiz.reviewattempt,quiz.reviewcorrectness,quiz.reviewmarks,quiz.reviewspecificfeedback,
             quiz.reviewgeneralfeedback,quiz.reviewrightanswer,quiz.reviewoverallfeedback,quiz.questionsperpage,
             quiz.navmethod,quiz.shuffleanswers,quiz.sumgrades,quiz.grade,quiz.timecreated,quiz.timemodified,
             quiz.password,quiz.subnet,quiz.browsersecurity,quiz.delay1,quiz.delay2,quiz.showuserpicture,
             quiz.showblocks,quiz.completionattemptsexhausted,quiz.completionpass,quiz.allowofflineattempts))

            # Commit your changes in the database
            db.commit()
            print ("Successfully inserted quiz")
        except:
            # Rollback in case there is any error
            db.rollback()
            print ("something went wrong with quiz production")
        quizCounter = quizCounter + 1
        time.sleep(0.1 - ((time.time() - starttime) % 0.1))




def publishMoodleQuizAttempt(cursor, db):
    t0 = int(time.time())
    quizAttemptCounter = 1
    starttime = time.time()
    while quizAttemptCounter <= 10:
        quizIdRef = 1 if quizAttemptCounter <= 5 else 2
        quizAttempt = Base.Mdl_Quiz_Attempt(quizid=quizIdRef, userid=quizAttemptCounter)
        try:
            # Execute the SQL command
            cursor.execute("""INSERT INTO mdl_quiz_attempts (quiz,userid,attempt,uniqueid,layout,currentpage,preview,state,timestart,timefinish,timemodified,timemodifiedoffline,timecheckstate,sumgrades)
                        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                           (quizAttempt.quiz, quizAttempt.userid, quizAttempt.attempt, quizAttempt.uniqueid,
                            quizAttempt.layout, quizAttempt.currentpage, quizAttempt.preview, quizAttempt.state,
                            quizAttempt.timestart, quizAttempt.timefinish, quizAttempt.timemodified,
                            quizAttempt.timemodifiedoffline, quizAttempt.timecheckstate, quizAttempt.sumgrades))
            # Commit your changes in the database
            db.commit()
            print ("Successfully inserted quiz attempt")
        except:
            # Rollback in case there is any error
            db.rollback()
            print ("something went wrong with quiz attempt production")
        quizAttemptCounter = quizAttemptCounter + 1
        time.sleep(0.1 - ((time.time() - starttime) % 0.1))



def publishStreamOfLeapMotionFrames(producer):
    id = 1
    gestureId = 1
    normal = """{"currentFrameRate": 30.0480,"devices": [],"gestures": [],"hands": [],"id": 8138,"interactionBox": {"center": [0.00000,200.000,0.00000],"size": [235.247,235.247,147.751]},"pointables": [],"r": [[1.00000,0.00000,0.00000],[0.00000,1.00000,0.00000],[0.00000,0.00000,1.00000]    ],"s": 1.00000,"t": [0.00000,0.00000,0.00000],"timestamp": 1567136253042747}"""
    start = """{"currentFrameRate": 57.8642, "devices": [],"gestures": [{"center": [-26.2711,142.601, 13.7979 ], "duration": 0, "handIds": [ 2 ], "id": 1, "normal": [ 0.520307, 0.846426, 0.113332 ], "pointableIds": [ 21 ], "progress": 0.778895, "radius": 74.7446, "state": "start", "type": "circle" } ], "hands": [ { "direction": [ -0.184329, -0.607927, -0.772300 ], "id": 2, "palmNormal": [ 0.468849, -0.744988, 0.474525 ], "palmPosition": [ 30.3926, 206.083, -2.69748 ], "palmVelocity": [ -79.8849, 143.827, -112.318 ], "r": [ [ 0.938237, 0.284939, 0.196270 ], [ -0.120208, 0.800375, -0.587324 ], [ -0.324441, 0.527456, 0.785193 ] ], "s": 0.751796, "sphereCenter": [ 56.4650, 164.145, 3.77728 ], "sphereRadius": 45.4444, "stabilizedPalmPosition": [ 28.4409, 201.277, 25.3778 ], "t": [ 15.1184, -21.7497, 0.628200 ], "timeVisible": 0.693755 } ], "id": 8693, "interactionBox": { "center": [ 0.00000, 200.000, 0.00000 ], "size": [ 235.247, 235.247, 147.751 ] }, "pointables": [ { "direction": [ 0.107980, -0.970747, -0.214455 ], "handId": 2, "id": 21, "length": 54.4514, "stabilizedTipPosition": [ 31.0863, 105.986, 29.9547 ], "timeVisible": 0.693755, "tipPosition": [ 9.32668, 117.500, -33.0536 ], "tipVelocity": [ -416.386, 250.653, -511.237 ], "tool": false, "touchDistance": -0.182868, "touchZone": "hovering", "width": 17.9100 } ], "r": [ [ 0.938237, 0.284939, 0.196270 ], [ -0.120208, 0.800375, -0.587324 ], [ -0.324441, 0.527456, 0.785193 ] ], "s": 0.751796, "t": [ 15.1184, -21.7497, 0.628200 ], "timestamp": 1567136258696664}"""
    normalJson = json.loads(normal)
    circleJson = json.loads(start)
    topic = '__.goethe-universitaet-frankfurt.myo.frames'
    counter = 1
    starttime = time.time()
    while True:
        key = 'franz1 kafka1'
        internalCounter  = 1
        startTimeOfCircle = time.time()
        if counter % 100 == 0:
            circleJson['gestures'][0]['duration'] = 0
            circleJson['gestures'][0]['state'] = "start"
            circleJson['timestamp'] = time.time()
            circleJson['id'] = id
            circleJson['gestures'][0]['id'] = gestureId
            id = id + 1
            print (" sending a circle")
            Base.sendJson(producer=producer, message=circleJson, topic= topic, key=key)
            while internalCounter <= 48:
                now =  time.time()
                circleJson['gestures'][0]['duration'] = now - startTimeOfCircle
                circleJson['timestamp'] = now
                circleJson['id'] = id
                circleJson['gestures'][0]['id'] = gestureId
                circleJson['gestures'][0]['state'] = "update"
                id = id + 1
                print (" sending a circle")
                Base.sendJson(producer=producer, message=circleJson, topic= topic, key=key)
                internalCounter = internalCounter + 1
                time.sleep(0.05)
            circleJson['timestamp'] = time.time()
            circleJson['id'] = id
            circleJson['gestures'][0]['id'] = gestureId
            circleJson['gestures'][0]['state'] = "stop"
            id = id + 1
            gestureId = gestureId + 1
            circleJson['gestures'][0]['duration'] = circleJson['timestamp'] - startTimeOfCircle
            print (" sending a circle")
            Base.sendJson(producer=producer, message=circleJson, topic= topic, key=key)
        else:
            print (" sending no gesture")
            normalJson['id'] = id
            id = id + 1
            normalJson['timestamp'] = time.time()
            Base.sendJson(producer=producer, message=normalJson, topic= topic, key=key)
        counter = counter + 1
        time.sleep(0.08 - ((time.time() - starttime) % 0.08))


def produceData(cursor, db):
    publishMoodleUser(cursor, db)
    time.sleep(1)
    publishMoodleForum(cursor, db)
    time.sleep(1)
    publishMoodleForumPosts(cursor, db)
    time.sleep(1)
    # publishMoodleQuiz(cursor, db)
    # time.sleep(1)
    # publishMoodleQuizAttempt(cursor, db)
    # time.sleep(1)


Dbconnector.connectAndRunData(produceData)
producer = Base.connect()
# publishStreamOfLeapMotionFrames(producer)
