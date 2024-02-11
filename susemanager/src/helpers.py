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

import getpass
import re
from functools import wraps
import errno
import os
import signal

try:
    input = raw_input  # pylint: disable=redefined-builtin,invalid-name
except NameError:
    pass


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
        value = password and getpass.getpass(msg) or input(msg)
        if not validator and value:
            break
        elif validator:
            if hasattr(validator, "__call__"):
                if validator(value):
                    break
            elif type(validator) in (tuple, list):
                if value in validator:
                    break
            elif re.search(validator, value):
                break

    return value


# pylint: disable-next=redefined-builtin
class TimeoutError(Exception):
    pass


def timeout(seconds=10, error_message=os.strerror(errno.ETIME)):
    def decorator(func):
        def _handle_timeout(signum, frame):  # pylint: disable=unused-argument
            raise TimeoutError(error_message)

        def wrapper(*args, **kwargs):
            signal.signal(signal.SIGALRM, _handle_timeout)
            signal.alarm(seconds)
            try:
                result = func(*args, **kwargs)
            finally:
                signal.alarm(0)
            return result

        return wraps(func)(wrapper)

    return decorator
