import moodle_mock_data_base as Base
import time
from random import *

'''
    publishes a 100 users, 10 forums, 1000 forum posts with the following conditions: 
        - each user exactly 10 on each forum 
        - each post published every other second
'''

def publishMoodleUser(producer):
    userCounter = 1
    starttime= time.time()
    while userCounter <= 100:
        print(userCounter)
        user = Base.Mdl_User(userCounter, 'fkafka' + str(userCounter), 'franz' + str(userCounter), 'kafka' + str(userCounter), 'franz@kafka')
        cdc = Base.constructKafkaObject(user, 'mdl_user', '__.goethe-universitaet-frankfurt.db.moodle.mdl_user')
        Base.send(producer, cdc)
        userCounter= userCounter+1
        '''every ten seconds'''
'''        time.sleep(1 - ((time.time() - starttime) % 1)) '''


def publishMoodleForum(producer):
    forumCounter = 1
    starttime = time.time()
    while forumCounter <= 10:
        forum = Base.Mdl_Forum(forumCounter)
        cdc = Base.constructKafkaObject(forum, 'mdl_forum', '__.goethe-universitaet-frankfurt.db.moodle.mdl_forum')
        Base.send(producer, cdc)
        forumCounter = forumCounter + 1
'''        time.sleep(1 - ((time.time() - starttime) % 1)) '''


def publishMoodleForumPosts(producer):
    t0 = int(time.time())
    forumPostCounter = 1
    starttime = time.time()
    while forumPostCounter <= 1000:
        forumID = int((forumPostCounter - 1) / 100) + 1
        userID = int ((forumPostCounter - 1) / 10) + 1
        ''' publish as if after a second '''
        t0 = t0 + 2
        parentForumPost = forumPostCounter - 1
        ''' if forum post is published in a new discussion set parent to 0'''
        if (int((forumPostCounter - 1) / 100)) == ((forumPostCounter - 1) / 100):
            parentForumPost = 0

        forumPost = Base.Mdl_ForumPost(forumPostCounter, forumID, parentForumPost, userID, t0)
        cdc = Base.constructKafkaObject(forumPost, 'mdl_forum_post', '__.goethe-universitaet-frankfurt.db.moodle.mdl_forum_posts')
        Base.send(producer, cdc)
        forumPostCounter = forumPostCounter + 1
        time.sleep(1-((time.time() - starttime) % 1))


producer = Base.connect()
publishMoodleUser(producer)
publishMoodleForum(producer)
publishMoodleForumPosts(producer)


