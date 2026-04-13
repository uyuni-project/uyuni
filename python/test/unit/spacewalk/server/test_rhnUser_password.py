#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import base64
import crypt as _crypt  # pylint: disable=deprecated-module
import hashlib
import sys
from unittest.mock import MagicMock

# Stub out heavy server dependencies before importing rhnUser
for mod in [
    "rhn",
    "rhn.UserDictCase",
    "spacewalk.common.rhnLog",
    "spacewalk.common.rhnConfig",
    "spacewalk.common.rhnException",
    "spacewalk.common.rhnTranslate",
    "spacewalk.server.db_config",
    "spacewalk.server.rhnSQL",
    "spacewalk.server.rhnSession",
]:
    sys.modules.setdefault(mod, MagicMock())

# Provide the bare minimum attributes consumed at module level
sys.modules["rhn.UserDictCase"].UserDictCase = dict
sys.modules["spacewalk.common.rhnLog"].log_debug = MagicMock()
sys.modules["spacewalk.common.rhnLog"].log_error = MagicMock()
sys.modules["spacewalk.common.rhnConfig"].CFG = MagicMock(encrypted_passwords=True)
sys.modules["spacewalk.common.rhnTranslate"]._ = lambda x: x

from spacewalk.server.rhnUser import (  # noqa: E402  pylint: disable=wrong-import-position
    _PBKDF2_PREFIX,
    _PBKDF2_ITERATIONS,
    check_password,
    encrypt_password,
)


class TestEncryptPassword:
    """Tests for the encrypt_password function."""

    def test_output_starts_with_prefix(self):
        result = encrypt_password("secret")
        assert result.startswith(_PBKDF2_PREFIX)

    def test_output_has_four_dollar_separated_fields(self):
        # Expected format: $pbkdf2-sha256$<iter>$<b64salt>$<b64hash>
        result = encrypt_password("secret")
        parts = result.split("$")
        # ['', 'pbkdf2-sha256', '<iter>', '<b64salt>', '<b64hash>']
        assert len(parts) == 5

    def test_iteration_count_in_output(self):
        result = encrypt_password("secret")
        parts = result.split("$")
        assert int(parts[2]) == _PBKDF2_ITERATIONS

    def test_different_salts_per_call(self):
        h1 = encrypt_password("secret")
        h2 = encrypt_password("secret")
        # Random salt means the full hash strings must differ
        assert h1 != h2


class TestCheckPassword:
    """Tests for the check_password function."""

    def test_roundtrip_new_password(self):
        hashed = encrypt_password("correcthorsebatterystaple")
        assert check_password("correcthorsebatterystaple", hashed) == 1

    def test_wrong_password_rejected(self):
        hashed = encrypt_password("correcthorsebatterystaple")
        assert check_password("wrongpassword", hashed) == 0

    def test_empty_key_rejected(self):
        hashed = encrypt_password("secret")
        assert check_password("", hashed) == 0

    def test_legacy_crypt_hash_accepted(self):
        # SHA-256 crypt(3) hashes start with $5$; verify the legacy path still works
        salt = "$5$testsalt"
        legacy_hash = _crypt.crypt("mypassword", salt)
        assert check_password("mypassword", legacy_hash) == 1

    def test_legacy_crypt_hash_wrong_password(self):
        salt = "$5$testsalt"
        legacy_hash = _crypt.crypt("mypassword", salt)
        assert check_password("wrongpassword", legacy_hash) == 0

    def test_malformed_pbkdf2_hash_does_not_crash(self):
        assert check_password("secret", "$pbkdf2-sha256$notvalid") == 0

    def test_pbkdf2_hash_with_wrong_iteration_count_rejected(self):
        # Craft a hash with iteration count out of the allowed range
        raw_salt = b"testsalt12345678"
        bad_iters = 9_999_999
        dk = hashlib.pbkdf2_hmac("sha256", b"secret", raw_salt, bad_iters)
        crafted = (
            f"$pbkdf2-sha256${bad_iters}"
            f"${base64.b64encode(raw_salt).decode()}"
            f"${base64.b64encode(dk).decode()}"
        )
        assert check_password("secret", crafted) == 0

    def test_auto_upgrade_legacy_hash_on_login(self):
        """Simulates the scenario where a legacy $5$ hash is in the DB and the
        correct password is supplied — login must succeed so the caller can
        re-hash with PBKDF2 afterwards."""
        salt = "$5$upgradetest"
        legacy_hash = _crypt.crypt("hunter2", salt)
        # Correct password returns 1 (caller may then call encrypt_password to upgrade)
        assert check_password("hunter2", legacy_hash) == 1
        # Verify newly generated PBKDF2 hash also works for the same password
        new_hash = encrypt_password("hunter2")
        assert check_password("hunter2", new_hash) == 1
