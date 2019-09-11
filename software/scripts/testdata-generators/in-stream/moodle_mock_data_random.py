import moodle_mock_data_base as Base
import time
from random import *

def publishMoodleUser(producer):
    userCounter = 1
    starttime= time.time()

    while userCounter <= 10:
        print(userCounter)
        
        user = Base.Mdl_User(userCounter, 'fkafka' + str(userCounter), 'franz' + str(userCounter), 'kafka' + str(userCounter), 'franz@kafka')
        cdc = Base.constructKafkaObject(user, 'mdl_user', '__.goethe-universitaet-frankfurt.moodle.db.user')
        Base.send(producer, cdc)
        
        userCounter= userCounter+1
        '''every ten seconds'''
        time.sleep(1 - ((time.time() - starttime) % 1))

def publishMoodleForum(producer):
    forumCounter = 1
    starttime = time.time()
    while forumCounter <= 5:
        forum = Base.Mdl_Forum(forumCounter)
        print(forum)
        cdc = Base.constructKafkaObject(forum, 'mdl_forum', '__.goethe-universitaet-frankfurt.moodle.db.forum')
        Base.send(producer, cdc)
        forumCounter = forumCounter + 1
        time.sleep(1 - ((time.time() - starttime) % 1))

def publishMoodleForumPosts(producer):
    forumPostCounter = 1
    starttime = time.time()
    while forumPostCounter < 10:
        '''self, mdlForumPostID, discussionID, parentID, userID'''
        forumID = randint(1, 5);
        userID = randint(1, 10)
        forumPost = Base.Mdl_ForumPost(forumPostCounter, forumID, forumPostCounter - 1, userID)
        cdc = Base.constructKafkaObject(forumPost, 'mdl_forum_post', '__.goethe-universitaet-frankfurt.moodle.db.forum-posts')
        Base.send(producer, cdc)
        forumPostCounter = forumPostCounter + 1
        time.sleep(1-((time.time() - starttime) % 1))

producer = Base.connect()
publishMoodleUser(producer)
publishMoodleForum(producer)
publishMoodleForumPosts(producer)


