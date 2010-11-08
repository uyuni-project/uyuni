#! /bin/bash

export SETUP_ONLY="yes"
LOGFILE="/var/log/susemanager_setup.log"

/usr/lib/susemanager/bin/setup_dialog.sh
if [ $? -ne 0 ]; then
    echo "Aborted."
    exit 1
fi
/usr/lib/susemanager/bin/migration.sh -s -l $LOGFILE


