#!/usr/bin/python3

import pymysql


host='localhost',
user='root',
port=3306,
password=''

def connectAndRunData(producingFunction):
    # Open database connection
    db = pymysql.connect( host="localhost",user="root",password="admin",database="moodle", port=3309 )
    # prepare a cursor object using cursor() method
    cursor = db.cursor()

    producingFunction(cursor, db)

    # disconnect from server
    db.close()
