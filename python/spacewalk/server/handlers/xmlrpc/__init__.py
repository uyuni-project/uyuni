#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2015 Red Hat, Inc.
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
#
# This file defines the classes available for the XMLRPC receiver
#

__all__ = []

from . import registration
from . import up2date
from . import queue
from . import errata
from . import proxy
from . import get_handler
from . import scap

rpcClasses = {
    "registration": registration.Registration,
    "up2date": up2date.Up2date,
    "queue": queue.Queue,
    "errata": errata.Errata,
    "proxy": proxy.Proxy,
    "servers": up2date.Servers,
    "scap": scap.Scap,
}

# pylint: disable-next=invalid-name
getHandler = get_handler.GetHandler
