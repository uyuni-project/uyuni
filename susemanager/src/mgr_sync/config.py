#  pylint: disable=missing-module-docstring
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

# pylint: disable-next=unused-import
import socket
from configobj import ConfigObj


class Config(object):
    """
    Handle mgr-sync settings.
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
    DEBUG = K_PREF + ".debug"

    def __init__(self):
        # Default configuration, if not specified otherwise
        self._config = ConfigObj()
        self._config[Config.USER] = ""
        self._config[Config.PASSWORD] = ""
        self._config[Config.HOST] = "localhost"
        self._config[Config.PORT] = 80
        self._config[Config.URI] = "/rpc/api"
        self._config[Config.TOKEN] = ""
        self._config[Config.DEBUG] = ""
        self._parse_config()

    def write(self):
        """
        Write the configuration to the local file defined by Config.DOTFILE
        """

        self._config.write()

    def _parse_config(self):
        """
        Get the configuration or place defaults.
        """

        # Read /etc/rhn/rhn.conf if any
        if os.access(Config.RHNFILE, os.R_OK):
            try:
                self._config.merge(ConfigObj(Config.RHNFILE))
            except SyntaxError as ex:
                raise SyntaxError(
                    # pylint: disable-next=consider-using-f-string
                    "Error while parsing file {0}: {1}".format(Config.RHNFILE, ex)
                ) from ex

        # Read ~/.mgr-sync if any and override
        if os.access(Config.DOTFILE, os.R_OK):
            try:
                self._config.merge(ConfigObj(Config.DOTFILE))
            except SyntaxError as ex:
                raise SyntaxError(
                    # pylint: disable-next=consider-using-f-string
                    "Error while parsing file{0}: {1}".format(Config.DOTFILE, ex)
                ) from ex

        # Remove unnesessary items
        for key in list(self._config.keys()):
            if not key.startswith(Config.K_PREF):
                del self._config[key]

        # Write to local
        self._config.filename = Config.DOTFILE

    @property
    def user(self):
        return self._config[Config.USER]

    @user.setter  # pylint: disable=E1101
    def user(self, value):  # pylint: disable=E0102
        self._config[Config.USER] = value

    @property
    def password(self):
        return self._config[Config.PASSWORD]

    @password.setter  # pylint: disable=E1101
    def password(self, value):  # pylint: disable=E0102
        self._config[Config.PASSWORD] = value

    @property
    def host(self):
        return self._config[Config.HOST]

    @property
    def port(self):
        return self._config[Config.PORT]

    @property
    def uri(self):
        return self._config[Config.URI]

    @property
    def token(self):
        return self._config[Config.TOKEN]

    @token.setter  # pylint: disable=E1101
    def token(self, value):  # pylint: disable=E0102
        self._config[Config.TOKEN] = value

    @staticmethod
    @property
    def dotfile():
        return Config.DOTFILE

    @property
    def debug(self):
        return self._config[Config.DEBUG]
