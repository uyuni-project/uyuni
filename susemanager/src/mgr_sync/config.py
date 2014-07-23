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
import socket
from configobj import ConfigObj

_CONFIG = None

class Config:
    """
    Config constants and processing.
    """

    # Configuration location
    RHN = "/etc/rhn"
    HOME = os.path.expanduser("~")
    DOTFILE = os.path.join(HOME, ".mgr-sync")
    RHNFILE = os.path.join(RHN, "rhn.conf")

    # Keys of the configuration
    K_PREF = "mgrsync"
    USER = K_PREF + ".user"
    PASSWORD = K_PREF + ".password"
    HOST = K_PREF + ".host"
    PORT = K_PREF + ".port"
    URI = K_PREF + ".uri"
    TOKEN = K_PREF + ".session.token"

    def __init__(self):
        # Default configuration, if not specified otherwise
        self.config = ConfigObj()
        self.config[Config.USER] = None
        self.config[Config.PASSWORD] = None,
        self.config[Config.HOST] = socket.getfqdn()
        self.config[Config.PORT] = 80
        self.config[Config.URI] = "/rpc/api"
        self.config[Config.TOKEN] = None


    @staticmethod
    def get_config():
        """
        Get the configuration or place defaults.
        """
        global _CONFIG
        if _CONFIG:
            return _CONFIG

        cfg = Config()

        # Read /etc/rhn/rhn.conf if any
        if os.path.exists(Config.RHN) and os.access(Config.RHNFILE, os.R_OK):
            cfg.config.merge(ConfigObj(Config.RHNFILE))

        # Read ~/.mgr-sync if any and override
        if os.path.exists(Config.DOTFILE) and os.access(Config.DOTFILE, os.R_OK):
            cfg.config.merge(ConfigObj(Config.DOTFILE))

        # Remove unnesessary items
        for key in cfg.config.keys():
            if not key.startswith(Config.K_PREF):
                del cfg.config[key]

        # Write to local
        cfg.config.filename = Config.DOTFILE

        # Do it all once per runtime
        _CONFIG = cfg.config
        return _CONFIG
