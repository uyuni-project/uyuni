#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2021 SUSE LLC.
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

import pytest

from spacewalk.server import rhnSQL


@pytest.fixture(autouse=True, scope="module")
def pgsql_db():
    # pylint: disable-next=invalid-name
    PG_HOST = "localhost"
    # pylint: disable-next=invalid-name
    PG_USER = "spacewalk"
    # pylint: disable-next=invalid-name
    PG_PASSWORD = "spacewalk"
    # pylint: disable-next=invalid-name
    PG_DATABASE = "susemanager"

    rhnSQL.initDB(
        backend="postgresql",
        host=PG_HOST,
        username=PG_USER,
        password=PG_PASSWORD,
        database=PG_DATABASE,
    )

    # Re-initialize to test re-use of connections:
    rhnSQL.initDB(
        backend="postgresql",
        host=PG_HOST,
        username=PG_USER,
        password=PG_PASSWORD,
        database=PG_DATABASE,
    )
