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

import os
from enum import Enum

MASTER_SWITCH_FILE = "/var/lib/spacewalk/scc/migrated"


class BackendType(str, Enum):
    NCC = "NCC"
    SCC = "SCC"


def current_backend():
    """ Returns an instance of `BackendType` """

    if os.path.isfile(MASTER_SWITCH_FILE):
        return BackendType.SCC
    else:
        return BackendType.NCC


def switch_to_scc(connection, token):
    connection.sync.content.performMigration(token)
