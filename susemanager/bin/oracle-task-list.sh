#! /bin/bash

user=`grep db_user /etc/rhn/rhn.conf | awk '{print $3}'`
pass=`grep db_password /etc/rhn/rhn.conf | awk '{print $3}'`
sid=`grep db_name /etc/rhn/rhn.conf | awk '{print $3}'`

file=`mktemp`

echo "
set linesize 150
set pagesize 2000
select ADVISOR_NAME, TASK_NAME, DESCRIPTION, OWNER, STATUS, EXECUTION_START,
       (24*60*60*(EXECUTION_END - EXECUTION_START)) as DURATION
  from DBA_ADVISOR_TASKS order by last_modified DESC;
quit" > $file
sqlplus -S $user/$pass@$sid @$file
rm -f $file
