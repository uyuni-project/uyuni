#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

import sys
from contextlib import ExitStack
from unittest.mock import Mock, patch

import pytest

DB_CFG_MODULES = (
    "spacewalk.satellite_tools.download",
    "spacewalk.satellite_tools.reposync",
    "spacewalk.satellite_tools.repo_plugins.deb_src",
    "spacewalk.satellite_tools.repo_plugins.yum_src",
)


@pytest.fixture(autouse=True)
def db_cfg():
    """Do not read configuration values from the database"""
    cfg = Mock()
    cfg.REPOSYNC_DOWNLOAD_THREADS = 5
    cfg.REPOSYNC_MINRATE = 1000
    cfg.REPOSYNC_TIMEOUT = 300
    with ExitStack() as stack:
        for module in DB_CFG_MODULES:
            if module in sys.modules:
                stack.enter_context(patch(module + ".DB_CFG", cfg))
        yield cfg
