#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/GA/standard "SLE12 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/Update/standard/ "SLE12 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/GA/standard "SLE12 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/Update/standard "SLE12 SP1 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP2:/GA/standard "SLE12 SP2 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP2:/Update/standard "SLE12 SP2 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP3:/GA/standard "SLE12 SP3 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP3:/Update/standard "SLE12 SP3 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP3:/Update:/Products:/Manager32/standard/ "Manager32 Pool"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head:/Other/SLE_12_SP3/ "Devel:Galaxy:Manager:Head:Other"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_12_SP3/ "Devel:Galaxy:Manager:Head"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/pylint/SLE_12_SP3/ "pylint"
