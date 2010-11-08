#! /bin/bash

export SETUP_ONLY="yes"
LOGFILE="/var/log/susemanager_setup.log"

/root/bin/setup_dialog.sh
if [ $? -ne 0 ]; then
    echo "Aborted."
    exit 1
fi
/root/bin/migration.sh -s -l $LOGFILE


