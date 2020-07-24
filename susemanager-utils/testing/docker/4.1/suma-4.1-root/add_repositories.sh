#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15:/GA/standard "SLE15 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15:/Update/standard/ "SLE15 Updates"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/GA/standard "SLE15 SP1 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP1:/Update/standard "SLE15 SP1 Update"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP2:/GA/standard "SLE15 SP2 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP2:/Update/standard "SLE15 SP2 Update"

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-15-SP2:/Update:/Products:/Manager41/standard/ "SUSE Manager 4.1 GA" # python3-urlgrabber
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/4.1/SLE_15_SP2/ "Devel:Galaxy:Manager:4.1"

zypper ar -f https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Leap_15.2/ "Uyuni:Master:Utils:15.2" # obs-to-maven
