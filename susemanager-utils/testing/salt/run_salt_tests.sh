#! /bin/bash

#########################################
# docker
# docker run -t -i --privileged --device /dev/mem --rm -v "/path/to/git:/manager" registry.mgr.suse.de/suma-head-salt /bin/bash
#########################################
# update packages

set -ex

zypper ref

# must come in via dependency
# zypper --non-interactive up salt

zypper --non-interactive in salt-master
zypper --non-interactive in salt-minion

# enforce old version for pkg.latest below
zypper --non-interactive in --oldpackage test-package=42:0.0

# salt state directories and files
zypper --non-interactive in susemanager-sls

#--------------------------------------
# Ensure corresponding Salt Master confuguration

cat << EOF > /etc/salt/master
ipv6: False
user: salt
syndic_user: salt
EOF

echo "master: localhost" > /etc/salt/minion

cat << EOF > /etc/salt/master.d/susemanager.conf
file_roots:
  base:
    - /usr/share/susemanager/salt
EOF


#--------------------------------------
# start salt

salt-master -d
sleep 1
salt-minion -d

export HOST_MINION=`uname -n`

sleep 2
for x in 1 2 3 4 5 6 7 8 9 0; do
    if salt-key -L | grep "$HOST_MINION"; then
        break
    fi
    sleep 2
done

salt-key -L
salt-key -a $HOST_MINION -y
sleep 2
for x in 1 2 3 4 5 6 7 8 9 0; do
    if salt "$HOST_MINION" test.ping | grep -i "true"; then
        break
    fi
    sleep 2
done

salt "$HOST_MINION" saltutil.sync_grains


#--------------------------------------
# ping
salt "$HOST_MINION" test.ping

#--------------------------------------
# check if pkg.info_installed works

salt "$HOST_MINION" pkg.info_installed test-package

# does it report epoch ?

salt "$HOST_MINION" pkg.info_installed test-package attr=epoch | grep 42 || false

# does it fix the description ?

#salt "$HOST_MINION" pkg.info_installed test-package attr=description --out=json

# does it supress description ?

salt "$HOST_MINION" pkg.info_installed test-package errors=report | grep UTF-8 || false

#--------------------------------------
# get hardware grains

HDDEVICE=`salt "$HOST_MINION" disk.blkid | grep dev | head -1 | sed 's|[[:space:]]*\(/dev/.\+\):$|\1|'`

salt "$HOST_MINION" grains.get cpuarch | grep x86_64 || false

salt "$HOST_MINION" udev.info $HDDEVICE | grep dev || false

salt "$HOST_MINION" grains.get total_num_cpus | grep 2 || false

# https://docs.saltstack.com/en/latest/ref/modules/all/salt.modules.smbios.html

salt "$HOST_MINION" smbios.get bios-version | grep Bochs || false

# https://docs.saltstack.com/en/latest/ref/modules/all/salt.modules.network.html

salt "$HOST_MINION" network.hw_addr eth0 | grep fa:16 || false

# https://docs.saltstack.com/en/latest/ref/modules/all/salt.modules.status.html

salt "$HOST_MINION" status.diskusage $HDDEVICE | grep dev  || false

# Testing pkg.latest

# ensure that test-package has version 0.0
salt "$HOST_MINION" pkg.info_installed test-package attr=version | grep 0.0 || false

# establish 'latest' state for test-package
cat << EOF > /usr/share/susemanager/salt/pkglatest.sls
pkglatest:
    pkg.latest:
    - refresh: true
    - pkgs:
      - test-package
EOF

salt "$HOST_MINION" state.apply pkglatest | grep "Succeeded: 1" || false

# ensure that test-package has version 0.1 now
salt "$HOST_MINION" pkg.info_installed test-package attr=version | grep 0.1 || false

# Set of package manager tests
salt $HOST_MINION pkg.owner /etc/salt/ | (grep salt > /dev/null && echo 'Succeed pkg.owner') || (echo 'Failed pkg.owner' && false)
salt $HOST_MINION pkg.list_products | (grep SLES > /dev/null && echo 'Succeed pkg.list_products') || (echo 'Failed pkg.list_products' && false)
salt $HOST_MINION pkg.add_lock ba1f2511fc | (grep ba1f2511fc > /dev/null && echo 'Succeed pkg.add_lock') || (echo 'Failed pkg.add_lock' && false)
salt $HOST_MINION pkg.list_locks | (grep ba1f2511fc > /dev/null && echo 'Succeed pkg.list_locks') || (echo 'Failed pkg.list_locks' && false)
salt $HOST_MINION pkg.remove_lock ba1f2511fc
salt $HOST_MINION pkg.list_locks | (! grep ba1f2511fc > /dev/null && echo 'Succeed pkg.remove_lock') || (echo 'Failed pkg.remove_lock' && false)

# Restart Salt
killall salt-minion
killall salt-master

echo "### END BUILD ###"

