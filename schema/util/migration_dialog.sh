#!/bin/bash

variables[0]="SATELLITE_HOST"
variables[1]="SATELLITE_DOMAIN"
variables[2]="SATELLITE_DB_USER"
variables[3]="SATELLITE_DB_PASS"
variables[4]="SATELLITE_DB_SID"
variables[5]="MANAGER_IP"
variables[6]="MANAGER_USER"
variables[7]="MANAGER_PASS"
variables[8]="SYS_DB_PASS"
variables[9]="MANAGER_DB_NAME"

MIGRATION_ENV="/tmp/migration_env.sh"

sat_dialog() {
    dialog --backtitle "Migration from Red Hat Satellite to SUSE Manager" \
        --title "Please fillout the values" \
        --insecure \
        --mixedform "" 21 70 18 \
               "Hostname of the Satellite Server" 2 4 "satellite" 2 40 20 0 0\
               "Domainname"                       4 4 `hostname -d` 4 40 20 0 0\
               "Satellite Database Username"      6 4 "rhnsat" 6 40 20 0 0\
               "Satellite Database Password"      8 4 "rhnsat" 8 40 20 0 1\
               "Satellite Database SID"          10 4 "rhnsat" 10 40 20 0 0 \
     2>$MIGRATION_ENV
}

manager_dialog() {
    dialog --backtitle "Migration from Red Hat Satellite to SUSE Manager" \
        --title "Please fillout the values" \
        --insecure \
        --mixedform "" 21 70 18 \
               "IP Address of the SUSE Manager Server" 2 4 `ip -f inet -o addr show scope global | awk '{print $4}' | awk -F \/ '{print $1}'` 2 40 20 0 0\
               "SUSE Manager DB User"                  4 4 "spacewalk" 4 40 20 0 0\
               "SUSE Manager DB Password"              6 4 "spacewalk" 6 40 20 0 1\
               "DB Password for Admin"                 8 4 "novell"    8 40 20 0 1\
     2>>$MIGRATION_ENV
     echo "XE" >> $MIGRATION_ENV
}

sat_dialog
manager_dialog

let i=0;
while read line
do
    echo -n "export " >> /tmp/env
    echo -n ${variables[$i]} >> /tmp/env
    echo "=\"$line\"" >> /tmp/env
    let i=i+1
done < "$MIGRATION_ENV"

mv /tmp/env $MIGRATION_ENV

# vim: set expandtab:
