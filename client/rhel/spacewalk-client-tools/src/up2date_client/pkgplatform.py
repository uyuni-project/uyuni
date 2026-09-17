# pylint: disable=missing-module-docstring
# Client code for Update Agent
# Copyright (c) 2011--2012 Red Hat, Inc.
# SPDX-License-Identifier: GPL-2.0-only
#
# Author: Simon Lukasik
#

# substituted to the prefered platfrom by Makefile
_platform = "@PLATFORM@"


# pylint: disable-next=invalid-name
def getPlatform():
    if _platform != "@PLAT" + "FORM@":
        return _platform
    else:
        return "rpm"
