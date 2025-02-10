# Copy rbac schema DDL and execute on container server

mgrctl cp ddl.sql server:ddl.sql
mgrctl exec -ti -- "spacewalk-sql -i < ddl.sql"
