#!/bin/bash

variables[0]="MANAGER_IP"
variables[1]="MANAGER_USER"
variables[2]="MANAGER_PASS"
variables[3]="MANAGER_PASS2"
variables[4]="MANAGER_ADMIN_EMAIL"

variables[5]="CERT_O"
variables[6]="CERT_OU"
variables[7]="CERT_CITY"
variables[8]="CERT_STATE"
variables[9]="CERT_COUNTRY"
variables[10]="CERT_EMAIL"
variables[11]="CERT_PASS"
variables[12]="CERT_PASS2"

variables[13]="MANAGER_DB_NAME"
variables[14]="MANAGER_DB_HOST"
variables[15]="MANAGER_DB_PORT"
variables[16]="MANAGER_DB_PROTOCOL"
variables[17]="MANAGER_ENABLE_TFTP"

RESULT_ENV="/tmp/setup_env.sh"
RESULT_ENV1="/tmp/setup_env_manager.sh"
RESULT_ENV2="/tmp/setup_env_cert.sh"
RESULT_ENV3="/tmp/setup_env_db.sh"

TITLE="Migration from Red Hat Satellite to SUSE Manager"

manager_dialog() {
    rm $RESULT_ENV1
    dialog --backtitle "$TITLE" \
        --title "Please fillout the values" \
        --insecure \
        --mixedform "" 21 70 18 \
               "IP Address of the SUSE Manager Server" 2 4 `ip -f inet -o addr show scope global | awk '{print $4}' | awk -F \/ '{print $1}'` 2 40 20 0 0\
               "SUSE Manager DB User"                  4 4 "spacewalk" 4 40 20 0 0\
               "SUSE Manager DB Password"              6 4 "spacewalk" 6 40 20 0 1\
               "repeat Manager DB Password"            8 4 "spacewalk" 8 40 20 0 1\
               "Admin eMail Adresse"                  10 4 "admin@..." 10 40 20 0 0\
     2>>$RESULT_ENV1
}

cert_dialog() {
    rm $RESULT_ENV2
    dialog --backtitle "$TITLE" \
        --title "Please fillout the Certificate values" \
        --insecure \
        --mixedform "" 21 70 18 \
               "Organisation"      2 4 "" 2 40 20 0 0\
               "Organisation Unit" 4 4 "" 4 40 20 0 0\
               "City"              6 4 "" 6 40 20 0 0\
               "State"             8 4 "" 8 40 20 0 0\
               "Country"           10 4 "DE" 10 40 20 0 0\
               "EMail"             12 4 ""   12 40 20 0 0\
               "SSLPassword"       14 4 ""   14 40 20 0 1\
               "repeat Password"   16 4 ""   16 40 20 0 1\
     2>>$RESULT_ENV2
}

db_dialog() {
    rm $RESULT_ENV3
    dialog --backtitle "$TITLE" \
        --title "Please fillout the database settings" \
        --insecure \
        --mixedform "" 21 70 18 \
               "Database Name"      2 4 "xe"         2 40 20 0 0\
               "Database Host"      4 4 "localhost"  4 40 20 0 0\
               "Database Port"      6 4 "1521"       6 40 20 0 0\
               "Database Protocol"  8 4 "TCP"        8 40 20 0 0\
               "Enable TFTP"       10 4 "y"         10 40 20 0 0\
     2>>$RESULT_ENV3
}

create_export() {
    let i=$2;
    while read line
    do
        echo -n "export " >> /tmp/env
        echo -n ${variables[$i]} >> /tmp/env
        echo "=\"$line\"" >> /tmp/env
        let i=i+1
    done < $1
    mv /tmp/env $1
}

RUN="TRUE"
rm -f $RESULT_ENV

while [ $RUN = "TRUE" ];do
    manager_dialog
    create_export $RESULT_ENV1 0
    . $RESULT_ENV1
    if [ "$MANAGER_PASS" = "$MANAGER_PASS2" ]; then
        RUN="FALSE"
    else
        dialog --backtitle "$TITLE" \
            --title "Passwords mismatch" \
            --msgbox "The passwords don't match" 5 30 
    fi
done

RUN="TRUE"
while [ $RUN = "TRUE" ];do
    cert_dialog
    create_export $RESULT_ENV2 5
    . $RESULT_ENV2
    if [ "$CERT_PASS" = "$CERT_PASS2" ]; then
        RUN="FALSE"
    else
        dialog --backtitle "$TITLE" \
            --title "Passwords mismatch" \
            --msgbox "The passwords don't match" 5 30 
    fi
done

db_dialog
create_export $RESULT_ENV3 13

rm -f $RESULT_ENV
cat $RESULT_ENV1 $RESULT_ENV2 $RESULT_ENV3 >> $RESULT_ENV
rm -f $RESULT_ENV1 $RESULT_ENV2 $RESULT_ENV3
