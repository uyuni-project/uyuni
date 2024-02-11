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
from __future__ import print_function

from spacewalk.susemanager.helpers import cli_ask
from spacewalk.susemanager.helpers import timeout

try:
    import xmlrpc.client as xmlrpc_client
except ImportError:
    import xmlrpclib as xmlrpc_client

# pylint: disable=line-too-long


class MaximumNumberOfAuthenticationFailures(Exception):
    pass


class Authenticator(object):
    """
    Cache authentication, implements password-less connect.
    """

    MAX_NUM_OF_CREDENTIAL_FAILURES_ALLOWED = 3  # pylint: disable=invalid-name

    def __init__(self, connection, user, password, token):
        self.connection = connection
        self._token = token
        self.user = user
        self.password = password
        self.credentials_prompts = 0
        self.cached_credentials_used = not self.has_credentials()

    def token(self):
        """
        Authenticate user.

        This method obtains a new token when `self.token` is `None`.

        The code uses the cached username/password when available.
        These cached credentials are used just once, they are discarded if they
        do not work.

        The code asks the user to enter a new pair of username/password
        when either the cached credentials are not available or when they have
        been discarded.

        If an interactively entered pair of credentials does not work it
        is discarded and a new one is requested to the user. The user has a
        limited number of attempts to enter the right username/password; then
        the code raises a `MaximumNumberOfAuthenticationFailures` exception.

        This mimics how other Unix programs handle credentials (e.g. `sudo`).
        """

        if not self._token:
            if not self.has_credentials():
                self._get_credentials_interactive()
                self.credentials_prompts += 1

            try:
                self._token = self.connection.auth.login(self.user, self.password)
            except xmlrpc_client.Fault as ex:
                if (
                    ex.faultCode == 2950
                    and "Either the password or username is incorrect" in ex.faultString
                ):
                    if self.has_credentials() and not self.cached_credentials_used:
                        # Try to reuse the credentials stored into the configuration file
                        # to obtain a token. However ensure these are no longer used if
                        # they are not valid.
                        self.cached_credentials_used = True
                        self._discard_credentials()
                    elif (
                        self.credentials_prompts
                        < Authenticator.MAX_NUM_OF_CREDENTIAL_FAILURES_ALLOWED
                    ):
                        # The cached credentials are either invalid or have
                        # never been stored inside of the local configuration
                        # file. Ask the user to enter new credentials
                        self.credentials_prompts += 1
                        self._get_credentials_interactive()
                    else:
                        # - The cached credentials failed or have never been
                        #   stored into the local configuration file.
                        # - The user has already tried to authenticate with
                        #   new credentials but they didn't work.
                        #   The credential prompt has been shown
                        #   MAX_NUM_OF_CREDENTIAL_FAILURES_ALLOWED times.
                        # pylint: disable-next=raise-missing-from
                        raise MaximumNumberOfAuthenticationFailures
                    return self.token()
                else:
                    raise ex

        return self._token

    def has_token(self):
        return self._token is not None and len(self._token) > 0

    def has_credentials(self):
        return self.user and self.password

    def discard_token(self):
        """
        Discard the cached token.
        """

        self._token = None

    def _discard_credentials(self):
        self.user = None
        self.password = None

    @timeout(60, "Timeout. No user input for 60 seconds. Exiting...")
    def _get_credentials_interactive(self):
        """
        Get credentials from CLI interactively.
        """

        print("Please enter the credentials of SUSE Manager Administrator.")
        self.user = cli_ask("Login")
        self.password = cli_ask("Password", password=True)

        self.credentials_prompts += 1
