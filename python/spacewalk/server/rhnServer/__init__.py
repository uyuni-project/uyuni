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
# Module for handling the rhnServer objects.
#


from spacewalk.common.rhnLog import log_debug

# these are pretty much the only entry points
from uyuni.common.usix import StringType, UnicodeType

from .server_certificate import Certificate

# Local imports
from .server_class import Server


def get(system_id, load_user=1):
    """retrieve the server with matching certificate from the database"""
    # pylint: disable-next=consider-using-f-string
    log_debug(3, "load_user = %s" % load_user)
    # This has to be a string
    if not isinstance(system_id, (StringType, UnicodeType)):
        return None
    # Try to initialize the certificate object
    cert = Certificate()
    if not cert.reload(system_id) == 0:
        return None
    # if invalid, stop here
    if not cert.valid():
        return None

    # this looks like a real server
    server = Server()
    # and load it up
    if not server.loadcert(cert, load_user) == 0:
        return None
    # okay, it is a valid certificate
    return server
