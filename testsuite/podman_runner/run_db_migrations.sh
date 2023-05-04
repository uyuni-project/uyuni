#!/bin/bash -e
for i in $(ls /etc/sysconfig/rhn/schema-upgrade/ | tail -n1);do
    for j in $(ls /etc/sysconfig/rhn/schema-upgrade/$i);do
        echo $i;spacewalk-sql /etc/sysconfig/rhn/schema-upgrade/$i/$j;
    done;
done

