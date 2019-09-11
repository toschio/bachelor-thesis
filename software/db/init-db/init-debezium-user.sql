--     
-- creates a user in mysql 5 docker container used by moodle     
-- jhardison/moodle:latest     
-- user for the debezium cdc connector     
--    
create user 'debezium'@'%' identified with mysql_native_password by 'dbz';
--     
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium';
