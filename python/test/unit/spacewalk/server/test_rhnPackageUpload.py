#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only

from unittest.mock import Mock, patch

import pytest

from spacewalk.common.rhnException import rhnFault
from spacewalk.server import rhnPackageUpload


@patch(
    "spacewalk.server.rhnPackageUpload.DB_CFG",
    Mock(get_bool=Mock(return_value=False)),
)
def test_forced_upload_rejected_when_disabled():
    with pytest.raises(rhnFault):
        # pylint: disable-next=protected-access
        rhnPackageUpload._authenticate(Mock(), [], None, True)


@patch(
    "spacewalk.server.rhnPackageUpload.DB_CFG",
    Mock(get_bool=Mock(return_value=True)),
)
def test_forced_upload_allowed_when_enabled():
    authobj = Mock(org_id=42)
    # pylint: disable-next=protected-access
    assert rhnPackageUpload._authenticate(authobj, [], None, True) == (42, True)
