#!/bin/bash
set -e

zypper ar -f http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/openSUSE_Leap_42.3/ "Uyuni:Master:42.3"
zypper ar -f http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_42.3/ "Uyuni:Master:Other:42.3"
