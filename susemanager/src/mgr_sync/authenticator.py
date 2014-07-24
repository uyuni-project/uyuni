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
from helpers import cli_msg


class Authenticator(object):
    """
    Cache authentication, implements password-less connect.
    """

    def __init__(self, conn, config):
        self.conn = conn
        self.config = config
        self.persist = None

        self._token = self.config.token
        self.uid = self.config.user
        self.password = self.config.password

    @property
    def token(self):
        """
        Authenticate user.
        """
        if not self._token:
            if not self.uid or not self.password:
                self._get_credentials_interactive()
            self._token = self.conn.auth.login(self.uid, self.password)
            self.config.token = self._token

            if self.persist:
                self.config.user = self.uid
                self.config.password = self.password
                cli_msg("Credentials has been saved to the %s file.\n" % self.config.dotfile)

        self.config.write()

        return self._token

    def discard_token(self):
        """
        Discard the cached token.
        """

        self._token = None
        self.config.write()

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

