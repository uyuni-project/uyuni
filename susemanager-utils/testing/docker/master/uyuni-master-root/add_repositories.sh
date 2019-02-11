#!/bin/bash
set -e

zypper rr 'NON OSS'
zypper rr 'NON OSS Update'

zypper ar -f http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/openSUSE_Leap_15.0/ "Uyuni:Master:15.0"
zypper ar -f http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_15.0/ "Uyuni:Master:Other:15.0"
#zypper ar -f http://download.suse.de/ibs/Devel:/Galaxy:/pylint/SLE_12_SP3/ "pylint"
