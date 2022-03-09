# -*- coding: utf-8 -*-
#
# Copyright (c) 2018 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import sys
from spacewalk.common.rhnLog import log_debug
from spacewalk.common.rhnException import rhnFault
from spacewalk.server.rhnLib import ShadowAction

# the "exposed" functions
__rhnexport__ = ['subscribe']

def subscribe(serverId, actionId, dry_run=0):
    log_debug(3)

    raise ShadowAction("subscribe channel requested - internal DB operation only")
