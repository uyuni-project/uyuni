#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/GA/standard "SLE12 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/Update/standard/ "SLE12 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12-SP1:/GA/standard "SLE12 SP1 GA"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/salt:/SLE12/SLE_12_SP1/ "Devel:Galaxy:salt:SLE12"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head:/SLE12_Products_Test/SLE_12_SP1/ "Devel:Galaxy:Manager:Head:SLE12_Products_Test"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_12_SP1/ "Devel:Galaxy:Manager:Head"
