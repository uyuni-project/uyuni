#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15:/GA/standard "SLE15 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15:/Update/standard/ "SLE15 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/GA/standard "SLE15 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/Update/standard "SLE15 SP1 Update"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head:/Other/SLE_15_SP1/ "Devel:Galaxy:Manager:Head:Other"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_15_SP1/ "Devel:Galaxy:Manager:Head"
#zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/pylint/SLE_12_SP3/ "pylint"
