The mounted volume's sql-scripts need to be executable. If the mysql-container does not start, with reason "permission denied", then
ascribe correct rights to all files in ./init-db:

chmod 755 ...
