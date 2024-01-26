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
