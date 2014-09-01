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

from spacewalk.susemanager.helpers import cli_ask


class Authenticator(object):
    """
    Cache authentication, implements password-less connect.
    """

    def __init__(self, connection, user, password, token):
        self.connection = connection

        self._token = token
        self.user = user
        self.password = password

    def token(self, connect=True):
        """
        Authenticate user.
        """
        if not self._token and connect:
            if not self.user or not self.password:
                self._get_credentials_interactive()
            self._token = self.connection.auth.login(self.user, self.password)

        return self._token

    def has_credentials(self):
        return self.user and self.password

    def discard_token(self):
        """
        Discard the cached token.
        """

        self._token = None

    def _get_credentials_interactive(self):
        """
        Get credentials from CLI interactively.
        """

        print "Please enter the credentials of SUSE Manager Administrator."
        self.user = cli_ask("Login: ")
        self.password = cli_ask("Password: ", password=True)


