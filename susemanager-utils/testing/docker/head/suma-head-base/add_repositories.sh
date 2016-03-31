#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/GA/standard "SLE12 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/Update/standard/ "SLE12 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/GA/standard "SLE12 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/Update/standard "SLE12 SP1 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/Update:/Products/standard "SLE12 SP1 Update Products"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_12_SP1/ "Devel:Galaxy:Manager:Head"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/pylint/SLE_12_SP1/ "pylint"
