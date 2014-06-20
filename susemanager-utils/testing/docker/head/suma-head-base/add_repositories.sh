#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_11_SP3/ "Devel:Galaxy:Manager:Head"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head:/JavaRefresh/SLE_11_SP3/ "Devel:Galaxy:Manager:Head:JavaRefresh"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP1:/GA/standard/ "SUSE:SLE-11-SP1:GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP1:/Update/standard "SUSE:SLE-11-SP1:Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP1:/GA/standard/ "SUSE:SLE-11-SP2:GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP2:/Update/standard/ "SUSE:SLE-11-SP2:Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP2:/Update:/Products:/Test/standard/ "SUSE:SLE-11-SP2:Update:Products:Test"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP3:/GA/standard/ "SUSE:SLE-11-SP3:GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP3:/Update/standard "SUSE:SLE-11-SP3:Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11-SP3:/Update:/Products:/Test/standard/ "SUSE:SLE-11-SP3:Update:Products:Test"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11:/GA/standard/ "SUSE:SLE-11:GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-11:/Update:/Test/standard/ "SUSE:SLE-11:Update:Test"
