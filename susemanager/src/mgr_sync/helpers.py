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

import sys
import getpass


def cli_msg(message, stderr=True):
    """
    Print the message to the STDERR or STDOUT.
    """
    print >> (stderr and sys.stderr or sys.stdout), message + "\n"


def cli_ask(msg, password=False):
    """
    Ask input from the console. Hide the echo, in case of password or
    sensitive information.
    """

    msg += ": "
    value = None
    while not value:
        value = (password and getpass.getpass(msg) or raw_input(msg))
    return value
