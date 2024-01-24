#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2016 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#

import pam
import sys

from uyuni.common.usix import raise_with_tb
from spacewalk.common.rhnLog import log_error
from spacewalk.common.rhnException import rhnException


def check_password(username, password, service):
    try:
        auth = pam.pam()
        if not auth.authenticate(username, password, service=service):
            # pylint: disable-next=consider-using-f-string
            log_error("Password check failed (%s): %s" % (auth.code, auth.reason))
            return 0
        else:
            return 1
    # pylint: disable-next=bare-except
    except:
        raise_with_tb(rhnException("Internal PAM error"), sys.exc_info()[2])
