#! /bin/bash

user=`grep db_user /etc/rhn/rhn.conf | awk '{print $3}'`
pass=`grep db_password /etc/rhn/rhn.conf | awk '{print $3}'`
sid=`grep db_name /etc/rhn/rhn.conf | awk '{print $3}'`

file=`mktemp`

echo "
--
-- return a report for the last auto tuning task
--
set linesize 150
set pagesize 2000
set long 80000
col recs format a90
select dbms_sqltune.report_auto_tuning_task() as recs from dual;
quit" > $file
sqlplus -S $user/$pass@$sid @$file
rm -f $file
