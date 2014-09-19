#!/bin/bash
set -e

zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/GA/standard "SLE12 GA"
zypper ar -f http://download.suse.de/ibs/SUSE:/SLE-12:/Update/standard/ "SLE12 Updates"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_12/ "Devel:Galaxy:Manager:Head"
zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head:/SLE12_Products_Test/SLE_12/ "Devel:Galaxy:Manager:Head:SLE12_Products_Test"
