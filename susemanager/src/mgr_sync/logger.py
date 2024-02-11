#  pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.

from spacewalk.common import rhnLog
from spacewalk.common.rhnLog import log_debug


class Logger(object):
    """
    Log mgr-sync activity.

    debug: Additional information
    info: General activities
    error: Errors
    """

    def __init__(self, debug, logfile):
        rhnLog.initLOG(logfile, int(debug))

    @staticmethod
    def debug(msg):
        log_debug(3, msg)

    @staticmethod
    def info(msg):
        log_debug(2, msg)

    @staticmethod
    def error(msg):
        log_debug(1, msg)
