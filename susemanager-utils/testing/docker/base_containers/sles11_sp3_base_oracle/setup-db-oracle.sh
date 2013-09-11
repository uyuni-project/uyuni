#!/bin/bash

compute_oracle_mem() {
  # SGA & PGA algo
  sgamin=146800640
  pgamin=16777216
  TM=`cat /proc/meminfo | grep '^MemTotal' | awk '{print $2}'`
  TM=`echo $TM / 1024 | bc`
  TM=`echo 0.40 \* $TM | bc | sed "s/\..*//"`
  TMSP=`echo $TM-40 | bc`
  sga_target=`echo 0.75 \* $TMSP | bc`
  pga_target=`echo 0.25 \* $TMSP | bc `
  sga=`echo $sga_target \* 1048576  | bc | sed "s/\..*//"`
  pga=`echo $pga_target \* 1048576  | bc | sed "s/\..*//"`
  check=`echo $sga \< $sgamin | bc`
  if test $check != 0
  then
    sga=$sgamin
  fi

  check=`echo $pga \< $pgamin | bc`
  if test $check != 0
  then
    pga=$pgamin
  fi

  echo "sga=$sga"
  echo "pga=$pga"
}

if ! chkconfig -c oracle ; then
  insserv oracle
fi

/opt/apps/oracle/setup "SYS_DB_PASS"
# remove suid bits for bnc#736240
find /opt/apps/oracle/product/ -perm -4000 -exec chmod -s {} \;
cp /opt/apps/oracle/product/11gR2/dbhome_1/network/admin/tnsnames.ora /etc
smdba-netswitch localhost
compute_oracle_mem
echo "Create database user for SUSE Manager..."
echo "select value from nls_database_parameters where parameter='NLS_CHARACTERSET';
shutdown immediate;
startup mount;
alter system enable restricted session;
alter system set job_queue_processes=0;
alter database open;
alter database character set internal_use utf8;
shutdown immediate;
startup;
select value from nls_database_parameters where parameter='NLS_CHARACTERSET';
alter system set job_queue_processes=1000;
alter profile DEFAULT limit PASSWORD_LIFE_TIME unlimited;
create smallfile tablespace data_tbs datafile '/opt/apps/oracle/oradata/susemanager/data_01.dbf' size 500M autoextend on blocksize 8192;
create user spacewalk identified by \"spacewalk\" default tablespace data_tbs;
grant dba to spacewalk;
grant connect to spacewalk;
grant alter session to spacewalk;
grant create table to spacewalk;
grant create view to spacewalk;
grant create type to spacewalk;
grant create sequence to spacewalk;
grant create procedure to spacewalk;
grant create operator to spacewalk;
grant create synonym to spacewalk;
grant create trigger to spacewalk;
grant create role to spacewalk;
grant alter session to spacewalk;
alter system set processes = 400 scope=spfile;
alter system set deferred_segment_creation=FALSE;
alter system set sga_target=$sga scope=spfile;
alter system set pga_aggregate_target=$pga scope=spfile;
alter system set nls_territory='AMERICA' scope=spfile;
BEGIN
dbms_sqltune.set_auto_tuning_task_parameter( 'ACCEPT_SQL_PROFILES', 'TRUE');
END;
/
quit
" > /tmp/dbsetup.sql

# See http://stackoverflow.com/questions/4153807/oracle-sequence-starting-with-2-instead-of-1
#
# alter system set deferred_segment_creation=FALSE;
#

su -s /bin/bash - oracle -c "ORACLE_SID=susemanager sqlplus / as sysdba @/tmp/dbsetup.sql;"
rm /tmp/dbsetup.sql

