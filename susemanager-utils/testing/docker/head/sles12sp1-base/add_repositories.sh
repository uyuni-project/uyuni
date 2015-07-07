#!/bin/bash
set -e

zypper ar -f http://euklid.suse.de/mirror/SuSE/build.suse.de/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product/ "SLES12 SP1 GA"
zypper ar -f http://euklid.suse.de/mirror/SuSE/build.suse.de/SUSE/Updates/SLE-SERVER/12-SP1/x86_64/update/ "SLES12 Updates"
