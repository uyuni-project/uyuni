#!/bin/bash

###################################################
#
# The script will display the progress of latest
# channel sync and update every 3 seconds
#
# Anthony Tortola 2019
#
###################################################

while [ 1 ]
do
        filename=`ls -tr /var/log/rhn/reposync|tail -1`
        clear
        echo -e "\nWatching $filename\n\n\tPress Ctrl-C to Break\n"
        echo -e "\t$(date)\n"
        tail -n20 /var/log/rhn/reposync/$filename
        sleep 3
done
exit
