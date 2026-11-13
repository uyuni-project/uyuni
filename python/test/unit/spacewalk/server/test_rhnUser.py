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

from spacewalk.server.rhnUser import check_password, encrypt_password


def test_check_password_pbkdf2():
    stored = encrypt_password("secret")
    assert check_password("secret", stored) == 1
    assert check_password("wrong", stored) == 0


def test_check_password_rejects_plain_text():
    assert check_password("secret", "secret") == 0


def test_check_password_rejects_empty_key():
    assert check_password("", encrypt_password("secret")) == 0
