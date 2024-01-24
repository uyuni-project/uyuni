#  pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# Copyright (C) 2014 Novell, Inc.
#   This library is free software; you can redistribute it and/or modify
# it only under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
#   This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
#   You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

try:
    import unittest2 as unittest
except ImportError:
    import unittest

try:
    import xmlrpc.client as xmlrpc_client
except ImportError:
    import xmlrpclib as xmlrpc_client

import os.path
import sys

try:
    from unittest.mock import MagicMock, call, patch
except ImportError:
    from mock import MagicMock, call, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

# pylint: disable-next=wrong-import-position
from spacewalk.susemanager.authenticator import (
    Authenticator,
    MaximumNumberOfAuthenticationFailures,
)


class AuthenticatorTest(unittest.TestCase):
    def setUp(self):
        self.mock_connection = MagicMock()

    def test_do_not_ask_credentials_if_cached_token_is_available(self):
        expected = "test token"
        auth = Authenticator(
            connection=self.mock_connection, user=None, password=None, token=expected
        )

        with patch("spacewalk.susemanager.authenticator.cli_ask") as mock:
            self.assertEqual(expected, auth.token())
            self.assertEqual(0, mock.call_count)

        self.assertEqual(0, self.mock_connection.call_count)

    def test_ask_credentials_when_nothing_is_cached(self):
        expected_user = "test user"
        expected_password = "test password"
        expected_token = "test token"
        auth = Authenticator(
            connection=self.mock_connection, user=None, password=None, token=None
        )
        auth.connection.auth.login = MagicMock(return_value=expected_token)

        # pylint: disable-next=protected-access
        auth._get_credentials_interactive = MagicMock()
        interactive_credentials = [[expected_user, expected_password]]
        # pylint: disable-next=protected-access
        auth._get_credentials_interactive.side_effect = (
            lambda: _set_username_and_password(auth, interactive_credentials)
        )

        self.assertEqual(expected_token, auth.token())
        # pylint: disable-next=protected-access
        self.assertEqual(1, auth._get_credentials_interactive.call_count)
        auth.connection.auth.login.assert_called_once_with(
            expected_user, expected_password
        )

    def test_use_cached_credentials_first(self):
        expected_user = "test user"
        expected_password = "test password"
        expected_token = "test token"

        auth = Authenticator(
            connection=self.mock_connection,
            user=expected_user,
            password=expected_password,
            token=None,
        )
        auth.connection.auth.login = MagicMock(return_value=expected_token)

        # pylint: disable-next=protected-access
        auth._get_credentials_interactive = MagicMock()

        self.assertEqual(expected_token, auth.token())
        # pylint: disable-next=protected-access
        self.assertEqual(0, auth._get_credentials_interactive.call_count)
        auth.connection.auth.login.assert_called_once_with(
            expected_user, expected_password
        )

    def test_ask_credentials_when_cached_credentials_are_not_working(self):
        cached_credentials = ["cached_username", "cached_password"]
        interactive_credentials = [["interactive_username", "interactive_password"]]
        expected_token = "test token"
        exception = xmlrpc_client.Fault(
            2950, "Either the password or username is incorrect"
        )
        login_mocked_return_values = [
            exception,  # raised by the cached credentials
            expected_token,  # 1st pair of interactive credentials works fine
        ]

        auth = Authenticator(
            connection=self.mock_connection,
            user=cached_credentials[0],
            password=cached_credentials[1],
            token=None,
        )

        auth.connection.auth.login.side_effect = (
            lambda username, password: _side_effect_return_from_list(
                login_mocked_return_values
            )
        )

        # pylint: disable-next=protected-access
        auth._get_credentials_interactive = MagicMock()
        # pylint: disable-next=protected-access
        auth._get_credentials_interactive.side_effect = (
            lambda: _set_username_and_password(auth, interactive_credentials)
        )

        self.assertEqual(expected_token, auth.token())
        # pylint: disable-next=protected-access
        self.assertEqual(1, auth._get_credentials_interactive.call_count)

        expected_login_calls = []
        for c in [cached_credentials] + interactive_credentials:
            expected_login_calls.append(call.login(c[0], c[1]))
        auth.connection.auth.assert_has_calls(expected_login_calls)
        self.assertEqual(2, auth.connection.auth.login.call_count)

    def test_use_cached_credentials_and_then_keep_asking_credentials_if_they_are_wrong(
        self,
    ):
        interactive_credentials = [
            ["interactive_username1", "interactive_password1"],
            ["interactive_username2", "interactive_password2"],
            ["interactive_username3", "interactive_password3"],
        ]
        cached_credentials = ["cached_username", "cached_password"]
        expected_token = "test token"
        exception = xmlrpc_client.Fault(
            2950, "Either the password or username is incorrect"
        )
        login_mocked_return_values = [
            exception,  # raised by the cached credentials
            exception,  # raised by the 1st pair of interactive credentials
            exception,  # raised by the 2nd pair of interactive credentials
            expected_token,  # 3rd pair of interactive credentials works fine
        ]

        auth = Authenticator(
            connection=self.mock_connection,
            user=cached_credentials[0],
            password=cached_credentials[1],
            token=None,
        )
        auth.connection.auth.login.side_effect = (
            lambda username, password: _side_effect_return_from_list(
                login_mocked_return_values
            )
        )

        # pylint: disable-next=protected-access
        auth._get_credentials_interactive = MagicMock()
        # pylint: disable-next=protected-access
        auth._get_credentials_interactive.side_effect = (
            lambda: _set_username_and_password(auth, interactive_credentials)
        )

        self.assertEqual(expected_token, auth.token())

        # pylint: disable-next=protected-access
        self.assertEqual(3, auth._get_credentials_interactive.call_count)

        expected_login_calls = []
        for c in [cached_credentials] + interactive_credentials:
            expected_login_calls.append(call.login(c[0], c[1]))
        auth.connection.auth.assert_has_calls(expected_login_calls)
        self.assertEqual(4, auth.connection.auth.login.call_count)

    def test_keep_asking_credentials_if_they_are_wrong(self):
        interactive_credentials = [
            ["interactive_username1", "interactive_password1"],
            ["interactive_username2", "interactive_password2"],
            ["interactive_username3", "interactive_password3"],
        ]
        expected_token = "test token"
        exception = xmlrpc_client.Fault(
            2950, "Either the password or username is incorrect"
        )
        login_mocked_return_values = [
            exception,  # raised by the 1st pair of interactive credentials
            exception,  # raised by the 2nd pair of interactive credentials
            expected_token,  # 3rd pair of interactive credentials works fine
        ]

        # Note well: there are no cached credentials
        auth = Authenticator(
            connection=self.mock_connection, user=None, password=None, token=None
        )
        auth.connection.auth.login.side_effect = (
            lambda username, password: _side_effect_return_from_list(
                login_mocked_return_values
            )
        )

        # pylint: disable-next=protected-access
        auth._get_credentials_interactive = MagicMock()
        # pylint: disable-next=protected-access
        auth._get_credentials_interactive.side_effect = (
            lambda: _set_username_and_password(auth, interactive_credentials)
        )

        self.assertEqual(expected_token, auth.token())

        # pylint: disable-next=protected-access
        self.assertEqual(3, auth._get_credentials_interactive.call_count)

        expected_login_calls = []
        for c in interactive_credentials:
            expected_login_calls.append(call.login(c[0], c[1]))
        auth.connection.auth.assert_has_calls(expected_login_calls)
        self.assertEqual(3, auth.connection.auth.login.call_count)

    def test_should_raise_an_exception_after_the_user_provides_wrong_credentials_three_times_in_a_row(
        self,
    ):
        interactive_credentials = [
            ["interactive_username1", "interactive_password1"],
            ["interactive_username2", "interactive_password2"],
            ["interactive_username3", "interactive_password3"],
        ]
        cached_credentials = ["cached_username", "cached_password"]
        exception = xmlrpc_client.Fault(
            2950, "Either the password or username is incorrect"
        )
        login_mocked_return_values = [
            exception,  # raised by the cached credentials
            exception,  # raised by the 1st pair of interactive credentials
            exception,  # raised by the 2nd pair of interactive credentials
            exception,  # raised by the 3rd pair of interactive credentials
        ]

        auth = Authenticator(
            connection=self.mock_connection,
            user=cached_credentials[0],
            password=cached_credentials[1],
            token=None,
        )
        auth.connection.auth.login.side_effect = (
            lambda username, password: _side_effect_return_from_list(
                login_mocked_return_values
            )
        )

        # pylint: disable-next=protected-access
        auth._get_credentials_interactive = MagicMock()
        # pylint: disable-next=protected-access
        auth._get_credentials_interactive.side_effect = (
            lambda: _set_username_and_password(auth, interactive_credentials)
        )

        with self.assertRaises(MaximumNumberOfAuthenticationFailures):
            auth.token()
        self.assertEqual(
            Authenticator.MAX_NUM_OF_CREDENTIAL_FAILURES_ALLOWED,
            # pylint: disable-next=protected-access
            auth._get_credentials_interactive.call_count,
        )

        expected_login_calls = []
        for c in [cached_credentials] + interactive_credentials:
            expected_login_calls.append(call.login(c[0], c[1]))
        auth.connection.auth.assert_has_calls(expected_login_calls)
        self.assertEqual(4, auth.connection.auth.login.call_count)


# pylint: disable-next=redefined-builtin
def _side_effect_return_from_list(list):
    result = list.pop(0)
    if isinstance(result, Exception):
        raise result
    return result


def _set_username_and_password(auth, credentials):
    if len(credentials) == 0:
        # pylint: disable-next=broad-exception-raised
        raise Exception("No more credentials to use")
    pair = credentials.pop(0)
    auth.user = pair[0]
    auth.password = pair[1]
