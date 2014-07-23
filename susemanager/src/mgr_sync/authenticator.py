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

import getpass
from config import Config
from helpers import cli_msg


class Authenticator(object):
    """
    Cache authentication, implements password-less connect.
    """

    def __init__(self, conn, config):
        self.conn = conn
        self.config = config
        self.persist = None

        self.token = self.config[Config.TOKEN]
        self.uid = self.config[Config.USER]
        self.password = self.config[Config.PASSWORD]

    def __call__(self):
        """
        Authenticate user.
        """
        if not self.token:
            if not self.uid or not self.password:
                self._get_credentials_interactive()
            self.token = self.conn.auth.login(self.uid, self.password)
            self.config[Config.TOKEN] = self.token

            if self.persist:
                self.config[Config.USER] = self.uid
                self.config[Config.PASSWORD] = self.password
                cli_msg("Credentials has been saved to the %s file.\n" % Config.DOTFILE)

        self.config.write()

        return self.token

    def _get_credentials_interactive(self):
        """
        Get credentials from CLI interactively.
        """

        print "SUSE Manager needs you to login as an administrator."
        self.uid = self._ask_cli("    User")
        self.password = self._ask_cli("Password", password=True)

    def _ask_cli(self, msg, password=False):
        """
        Ask input from the console. Hide the echo, in case of password or
        sensitive information.
        """

        msg += ": "
        value = None
        while not value:
            value = (password and getpass.getpass(msg) or raw_input(msg))
        return value

