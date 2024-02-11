"""
Author: Ricardo Mateus <rmateus@suse.com>
"""

import pytest
from unittest.mock import MagicMock, patch, call
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position,unused-import
import sys

# pylint: disable-next=wrong-import-position
from ..modules import uyuni_config

# pylint: disable-next=wrong-import-position,unused-import
from ..modules.uyuni_config import (
    RPCClient,
    UyuniChannelsException,
    UyuniUsersException,
)


class TestRPCClient:
    """
    Test RPCClient object
    """

    rpc_client = None

    @patch("src.modules.uyuni_config.ssl", MagicMock())
    @patch("src.modules.uyuni_config.xmlrpc", MagicMock())
    # pylint: disable-next=unused-argument
    def setup_method(self, method):
        """
        Setup state per test.

        :param method:
        :return:
        """
        self.rpc_client = RPCClient(
            user="user", password="password", url="https://somewhere"
        )
        self.rpc_client.conn.auth.login = MagicMock(return_value="My_token")
        self.rpc_client.conn = MagicMock()

    # pylint: disable-next=unused-argument
    def teardown_method(self, method):
        """
        Tear-down state per test.

        :param method:
        :return:
        """
        self.rpc_client = None
        uyuni_config.__pillar__ = {}

    def test_init_called(self):
        """
        Init method called

        :return:
        """
        assert self.rpc_client.get_user() == "user"
        assert self.rpc_client.token is None

    def test_init_called_without_pillar(self):
        """
        Init method called without user password and without any pillar data

        :return:
        """
        with pytest.raises(UyuniUsersException):
            RPCClient(user="user")

    def test_init_called_with_pillar(self):
        """
        Init method called without user password and with pillar data defined

        :return:
        """
        uyuni_config.__pillar__ = {
            "uyuni": {"xmlrpc": {"user": "admin_user", "password": "password_user"}}
        }

        rpc_client = RPCClient(user="user")
        assert rpc_client.get_user() == "admin_user"
        # pylint: disable-next=protected-access
        assert rpc_client._user == "admin_user"
        # pylint: disable-next=protected-access
        assert rpc_client._password == "password_user"
        assert rpc_client.token is None

    def test_get_token(self):
        """
        Test get_token method with reuse token

        :return:
        """
        my_mock1 = MagicMock(return_value="My_Special_Token")
        my_mock2 = MagicMock(return_value="My_Special_Token_2")
        self.rpc_client.conn.auth.login = my_mock1
        token = self.rpc_client.get_token()

        assert my_mock1.call_count == 1
        assert token == "My_Special_Token"
        assert (
            uyuni_config.__context__.get("uyuni.auth_token_user") == "My_Special_Token"
        )

        self.rpc_client.get_token()
        assert my_mock1.call_count == 1

        self.rpc_client.conn.auth.login = my_mock2
        self.rpc_client.get_token()
        assert my_mock1.call_count == 1
        assert my_mock2.call_count == 0

        token = self.rpc_client.get_token(True)
        assert my_mock1.call_count == 1
        assert my_mock2.call_count == 1
        assert token == "My_Special_Token_2"
        assert (
            uyuni_config.__context__.get("uyuni.auth_token_user")
            == "My_Special_Token_2"
        )

    def test_call_rpc(self):
        """
        Call any XML-RPC method.

        :return:
        """
        self.rpc_client.token = "My_token"
        out = self.rpc_client("uyuni.some_method")
        mo = getattr(self.rpc_client.conn, "uyuni.some_method")
        assert out is not None
        assert mo.called
        mo.assert_called_with("My_token")

        out2 = self.rpc_client("uyuni.some_method_2", "my_arg")
        mo2 = getattr(self.rpc_client.conn, "uyuni.some_method_2")
        assert out2 is not None
        assert mo2.called
        mo2.assert_called_with("My_token", "my_arg")

    def test_call_rpc_crash_handle_generic(self):
        """
        Handle XML-RPC method crash wiht generic error

        :return:
        """
        self.rpc_client.token = "the_token"
        exc = Exception("generic error when processing")
        exc.faultCode = 2951
        setattr(self.rpc_client.conn, "uyuni.some_method", MagicMock(side_effect=exc))

        with patch("src.modules.uyuni_config.log") as logger:
            with pytest.raises(Exception):
                self.rpc_client("uyuni.some_method")
            mo = getattr(self.rpc_client.conn, "uyuni.some_method")
            assert mo.called
            mo.assert_called_with("the_token")
            assert logger.error.call_args[0] == (
                "Unable to call RPC function: %s",
                "generic error when processing",
            )

    def test_call_rpc_crash_handle_reauthenticate_error(self):
        """
        Handle XML-RPC method crash whit reauthenticate error

        :return:
        """
        self.rpc_client.token = "the_token"
        self.rpc_client.conn.auth.login = MagicMock(return_value="the_token_new")

        exc = Exception("generic error when processing")
        exc.faultCode = 2950
        setattr(self.rpc_client.conn, "uyuni.some_method", MagicMock(side_effect=exc))

        with patch("src.modules.uyuni_config.log") as logger:
            with pytest.raises(Exception):
                self.rpc_client("uyuni.some_method")
            mo = getattr(self.rpc_client.conn, "uyuni.some_method")
            assert mo.call_count == 2
            mo.assert_has_calls([call("the_token"), call("the_token_new")])
            self.rpc_client.conn.auth.login.assert_called_once_with("user", "password")
            assert self.rpc_client.get_token() == "the_token_new"
            assert logger.error.call_args[0] == (
                "Unable to call RPC function: %s",
                "generic error when processing",
            )

    def test_call_rpc_handle_reauthenticate(self):
        """
        Handle XML-RPC method and reauthenticate

        :return:
        """
        self.rpc_client.token = "the_token"
        self.rpc_client.conn.auth.login = MagicMock(return_value="the_token_new")

        exc = Exception("generic error when processing")
        exc.faultCode = 2950

        setattr(
            self.rpc_client.conn,
            "uyuni.some_method",
            MagicMock(side_effect=[exc, "return string"]),
        )

        assert self.rpc_client.get_token() == "the_token"
        with patch("src.modules.uyuni_config.log") as logger:
            out = self.rpc_client("uyuni.some_method")
            mo = getattr(self.rpc_client.conn, "uyuni.some_method")
            # pdb.set_trace()
            assert out is not None
            assert out == "return string"
            assert mo.call_count == 2
            mo.assert_has_calls([call("the_token"), call("the_token_new")])
            self.rpc_client.conn.auth.login.assert_called_once_with("user", "password")
            assert self.rpc_client.get_token() == "the_token_new"
            assert logger.warning.call_args[0] == (
                "Fall back to the second try due to %s",
                "generic error when processing",
            )
