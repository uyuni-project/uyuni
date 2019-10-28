#!/bin/bash

OLD_COBBLER_DIR=/var/lib/cobbler/config
NEW_COBBLER_DIR=/var/lib/cobbler/collections

for OLDDIR in $OLD_COBBLER_DIR/*.d
do
    if [ ! -z "$(ls $OLDDIR)" ]; then
        NEWDIR=$NEW_COBBLER_DIR/`basename $OLDDIR | sed -e "s/\.d$//"`
        if [ ! -d $NEWDIR ]; then
            mkdir $NEWDIR
        fi
        echo "`date +"%H:%M:%S"`   Converting $OLDDIR --> $NEWDIR"
        for FILE in $OLDDIR/*
        do
            sed -e "s;/var/lib/rhn/kickstarts;;g" \
                -e "s/kickstart/autoinstall/" \
                -e "s/ks_meta/autoinstall_meta/" \
                -e "s/ksmeta/autoinstall_meta/" \
                -e "s/kopts/kernel_options/" \
                -e "s/kopts_post/kernel_options_post/" \
            $FILE > $NEWDIR/`basename $FILE`
        done
    fi
done

if [ -d /var/lib/rhn/kickstarts/upload ] && [ ! -z "$(ls /var/lib/rhn/kickstarts/upload)" ]; then
    mkdir -p /var/lib/cobbler/templates/upload
    cp -a /var/lib/rhn/kickstarts/upload/* /var/lib/cobbler/templates/upload
fi
if [ -d /var/lib/rhn/kickstarts/wizard ] && [ ! -z "$(ls /var/lib/rhn/kickstarts/wizard)" ]; then
    mkdir -p /var/lib/rhn/kickstarts/wizard
    cp -a /var/lib/rhn/kickstarts/wizard/* /var/lib/cobbler/templates/wizard
fi
if [ -d /var/lib/rhn/kickstarts/snippets ] && [ ! -z "$(ls /var/lib/rhn/kickstarts/snippets)" ]; then
    mkdir -p /var/lib/rhn/kickstarts/snippets
    cp -a /var/lib/rhn/kickstarts/snippets/* /var/lib/cobbler/snippets/spacewalk
fi
