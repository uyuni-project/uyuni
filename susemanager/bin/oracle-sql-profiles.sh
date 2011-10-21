#! /bin/bash

user=`grep db_user /etc/rhn/rhn.conf | awk '{print $3}'`
pass=`grep db_password /etc/rhn/rhn.conf | awk '{print $3}'`
sid=`grep db_name /etc/rhn/rhn.conf | awk '{print $3}'`

file=`mktemp`

echo "
set linesize 100
set pagesize 2000
set long 80000
col SQL_TEXT format a90
select row_number() OVER (ORDER BY LAST_MODIFIED DESC) NUM,
       NAME, CATEGORY, STATUS, TYPE,
       to_char(CREATED, 'YYYY-MM-DD HH24:MI:SS') CREATED,
       to_char(last_modified, 'YYYY-MM-DD HH24:MI:SS') last_modified,
       SQL_TEXT
from DBA_SQL_PROFILES
order by LAST_MODIFIED DESC;
quit" > $file
sqlplus -S $user/$pass@$sid @$file
rm -f $file
