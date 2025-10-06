#  pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# Copyright (c) 2021 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
import os
import shutil
import tempfile
import textwrap

import pytest
import spacewalk


# pylint: disable-next=unused-argument
def pytest_sessionstart(session):
    # Skip if we already have done it
    if "RHN_CONFIG_PATH" in os.environ:
        return

    tmp_path = tempfile.mkdtemp()
    # rhnConfig requires the /etc/rhn/rhn.conf even if empty at import time
    # pylint: disable-next=unspecified-encoding
    with open(os.path.join(tmp_path, "rhn.conf"), "w"):
        pass

    # rhnConfig requires the defaults config files at import time
    defaults_path = os.path.join(tmp_path, "defaults")
    os.mkdir(defaults_path)
    for conf_file in ["rhn.conf", "rhn_server.conf", "rhn_server_satellite.conf"]:
        shutil.copy(
            os.path.join(os.path.dirname(spacewalk.__file__), "rhn-conf", conf_file),
            defaults_path,
        )

    os.environ["RHN_CONFIG_PATH"] = tmp_path
    os.environ["RHN_CONFIG_DEFAULTS_PATH"] = defaults_path


# pylint: disable-next=unused-argument
def pytest_sessionfinish(session, exitstatus):
    if "RHN_CONFIG_PATH" in os.environ:
        shutil.rmtree(os.environ["RHN_CONFIG_PATH"])


# --- Fixtures


@pytest.fixture
def gpg_verify_output():
    # Debian 12 (Bookworm) InRelease signed on 2025-08-09
    # Keyring contains
    # debian-archive-key-12-B7C5D7D6350947F8.asc
    # debian-archive-key-12-security-254CF3B5AEC0A8F0.asc
    # debian-release-12-F8D2585B8783D481.asc
    yield textwrap.dedent(
        """
        [GNUPG:] NEWSIG
        [GNUPG:] ERRSIG 0E98404D386FA1D9 1 8 01 1754729632 9 A7236886F3CCCAAD148A27F80E98404D386FA1D9
        [GNUPG:] NO_PUBKEY 0E98404D386FA1D9
        [GNUPG:] NEWSIG
        [GNUPG:] KEY_CONSIDERED B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8 0
        [GNUPG:] SIG_ID JsSTrRiC5jaEhikim91zf/+2VjY 2025-08-09 1754729632
        [GNUPG:] GOODSIG 6ED0E7B82643E131 Debian Archive Automatic Signing Key (12/bookworm) <ftpmaster@debian.org>
        [GNUPG:] VALIDSIG 4CB50190207B4758A3F73A796ED0E7B82643E131 2025-08-09 1754729632 0 4 0 1 8 01 B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8
        [GNUPG:] KEY_CONSIDERED B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8 0
        [GNUPG:] TRUST_UNDEFINED 0 pgp
        [GNUPG:] NEWSIG
        [GNUPG:] KEY_CONSIDERED 4D64FEC119C2029067D6E791F8D2585B8783D481 0
        [GNUPG:] SIG_ID 3Ya31wODEo6h+7bAfSp4t1wMMuw 2025-08-09 1754731533
        [GNUPG:] GOODSIG F8D2585B8783D481 Debian Stable Release Key (12/bookworm) <debian-release@lists.debian.org>
        [GNUPG:] VALIDSIG 4D64FEC119C2029067D6E791F8D2585B8783D481 2025-08-09 1754731533 0 4 0 22 8 01 4D64FEC119C2029067D6E791F8D2585B8783D481
        [GNUPG:] TRUST_UNDEFINED 0 pgp
        [GNUPG:] FAILURE gpg-exit 33554433
        """
    )
