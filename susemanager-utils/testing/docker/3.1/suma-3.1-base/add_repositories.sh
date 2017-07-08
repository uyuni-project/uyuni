#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/GA/standard "SLE12 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/Update/standard/ "SLE12 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/GA/standard "SLE12 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/Update/standard "SLE12 SP1 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP2:/GA/standard "SLE12 SP2 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP2:/Update/standard "SLE12 SP2 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP2:/Update:/Products:/Manager31/standard/ "Manager31 Pool"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP2:/Update:/Products:/Manager31:/Update/standard/ "Manager31 Update"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.1/SLE_12_SP2/ "Devel:Galaxy:Manager:3.1"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/pylint/SLE_12_SP2/ "pylint"
