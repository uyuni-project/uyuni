#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/GA/standard "SLE12 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/Update/standard/ "SLE12 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/GA/standard "SLE12 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/Update/standard "SLE12 SP1 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/Update:/Products/standard "SLE12 SP1 Update Products"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/SLE_12_SP1_Update/ "Devel:Galaxy:Manager:3.0"
