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

    # Default configuration, if not specified otherwise
    DEFAULTS = {
        USER: None,
        PASSWORD: None,
        HOST: socket.getfqdn(),
        PORT: 80,
        URI: "/rpc/api",
        TOKEN: None,
        }

    @staticmethod
    def _parse(filename):
        """
        Parse key=value structures into a dict, ignoring commented lines
        by # and empty lines.
        """
        return dict(map(lambda elm: tuple(map(lambda opt: opt.strip(), elm.split("=", 1))),
                        filter(None, [line.strip() for line in open(filename).readlines()
                                      if not line.strip().startswith("#") and line.strip().startswith(Config.K_PREF)])))

    @staticmethod
    def get_config():
        """
        Get the configuration or place defaults.
        """

        # Create default config
        conf = {}
        for k, v in Config.DEFAULTS.items():
            conf[k] = conf.get(k, v)

        # Read /etc/rhn/rhn.conf if any
        if os.path.exists(Config.RHN) and os.access(Config.RHNFILE, os.R_OK):
            for k, v in Config._parse(Config.RHNFILE).items():
                if v:
                    conf[k] = v

        # Read ~/.mgr-sync if any and override
        if os.path.exists(Config.DOTFILE) and os.access(Config.DOTFILE, os.R_OK):
            for k, v in Config._parse(Config.DOTFILE).items():
                if v:
                    conf[k] = v

        return conf

    @staticmethod
    def save_local_config(data):
        """
        Save local config to a dot file.
        """
        f = open(Config.DOTFILE, "w")
        for item in sorted(data.items()):
            f.write("%s = %s\n" % tuple(map(lambda e: e is not None and e or "", item)))
        f.close()

