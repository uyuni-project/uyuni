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

import getpass
import re
import sys


def cli_msg(message, stderr=True):
    """
    Print the message to the STDERR or STDOUT.
    """
    print >> (stderr and sys.stderr or sys.stdout), message + "\n"


def cli_ask(msg, password=False, validator=None):
    """
    Ask input from the console. Hide the echo, in case of password or
    sensitive information.

    :param msg: message shown to the user
    :param password: boolean value, when True hides user input while typing
    :param validator: can be list or tuple containing the accepted values or
                      a custom function to use to validate user's input
    """

    msg += ": "
    value = None
    while True:
        value = (password and getpass.getpass(msg) or raw_input(msg))
        if not validator and value:
            break
        elif validator:
            if hasattr(validator, '__call__'):
                if validator(value):
                    break
            elif type(validator) in (tuple, list):
                if value in validator:
                    break
            elif re.search(validator, value):
                break

    return value
