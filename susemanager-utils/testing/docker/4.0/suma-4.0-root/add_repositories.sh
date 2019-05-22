#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15:/GA/standard "SLE15 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15:/Update/standard/ "SLE15 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/GA/standard "SLE15 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/Update/standard "SLE15 SP1 Update"

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/Update:/Products:/Manager40/standard/ "SUSE Manager 4.0 GA" # python3-urlgrabber

#zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/4.0/SLE_15_SP1/ "Devel:Galaxy:Manager:4.0"
# Use HEAD until we have Devel:Galaxy:Manager:4.0 working
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_15_SP1/ "Devel:Galaxy:Manager:Head"
