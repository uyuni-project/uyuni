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

from spacewalk.server import rhnSession


@patch("spacewalk.server.rhnSession.rhnSQL", Mock())
@patch("spacewalk.server.rhnSession.DB_CFG", Mock(PUSH_SESSION_LIFETIME=600))
def test_generate_uses_session_lifetime_from_db():
    session = rhnSession.Session().generate()
    assert session.duration == 600


@patch("spacewalk.server.rhnSession.rhnSQL", Mock())
@patch("spacewalk.server.rhnSession.DB_CFG", Mock(PUSH_SESSION_LIFETIME=600))
def test_generate_prefers_explicit_duration():
    session = rhnSession.Session().generate(duration=60)
    assert session.duration == 60
