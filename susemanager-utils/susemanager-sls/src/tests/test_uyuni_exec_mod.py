# coding: utf-8
"""
Unit tests for modules/uyuni_users.py execution module
"""

import sys
sys.path.append("../_modules")
import uyuni_users
from uyuni_users import RPCClient, UyuniUsersException, UyuniRemoteObject
from unittest.mock import patch, MagicMock, mock_open
import pytest


class TestRPCClient:
    """
    Test RPCClient object
    """
    rpc_client = None

    @patch("uyuni_users.ssl", MagicMock())
    @patch("uyuni_users.xmlrpc", MagicMock())
    @patch("uyuni_users.os.makedirs", MagicMock())
    def setup_method(self, method):
        """
        Setup state per test.

        :param method:
        :return:
        """
        self.rpc_client = RPCClient(url="https://somewhere", user="user", password="password")
        self.rpc_client.conn = MagicMock()

    def teardown_method(self, method):
        """
        Tear-down state per test.

        :param method:
        :return:
        """
        self.rpc_client = None

    def test_session_load(self):
        """
        Load session.

        :return:
        """
        with patch.object(uyuni_users, "open", mock_open(read_data=b"disk spinning backwards")) as mo:
            out = self.rpc_client.load_session()
            assert mo.called
            assert mo.call_count == 1
            assert mo.call_args == [('/var/cache/salt/minion/uyuni.rpc.s', 'rb')]
            assert out is not None
            assert out.startswith("disk")

    def test_session_save(self):
        """
        Save session.

        :return:
        """
        # Method "save_session" is called on the auth automatically and is not meant to be directly accessed.
        # For this reason it is shifted away during testing and a Mock is called instead
        self.rpc_client.conn.auth.login = MagicMock(return_value="28000 bps connection")
        self.rpc_client._save_session = self.rpc_client.save_session
        self.rpc_client.save_session = MagicMock()

        with patch.object(uyuni_users, "open", mock_open()) as mo:
            out = self.rpc_client._save_session()
            assert out is True
            assert len(mo.mock_calls) == 4
            assert mo.mock_calls[2][1][0] == b"28000 bps connection"

    def test_get_token(self):
        """
        Get XML-RPC token from the Uyuni.

        :return: string
        """
        self.rpc_client.conn.auth.login = MagicMock(return_value="improperly oriented keyboard")
        self.rpc_client.token = None
        self.rpc_client.save_session = MagicMock()
        self.rpc_client.get_token()

        assert self.rpc_client.save_session.called
        assert self.rpc_client.token is not None
        assert self.rpc_client.token.startswith("improperly")

    def test_get_token_recall(self):
        """
        Get XML-RPC token from the Uyuni, second call should be skipped.

        :return: string
        """
        self.rpc_client.conn.auth.login = MagicMock(return_value="recursive recursion error")
        self.rpc_client.token = "sunspot activity"
        self.rpc_client.save_session = MagicMock()
        self.rpc_client.get_token()

        assert not self.rpc_client.save_session.called
        assert self.rpc_client.token is not None
        assert self.rpc_client.token.startswith("sunspot")

    def test_call_rpc(self):
        """
        Call any XML-RPC method.

        :return:
        """
        self.rpc_client.token = "The Borg"
        out = self.rpc_client("heavymetal.playLoud", self.rpc_client.get_token())
        mo = getattr(self.rpc_client.conn, "heavymetal.playLoud")

        assert out is not None
        assert mo.called
        assert mo.call_args == [("The Borg",)]

    def test_call_rpc_crash_handle(self):
        """
        Handle XML-RPC method crash.

        :return:
        """
        self.rpc_client.token = "The Borg"
        setattr(self.rpc_client.conn, "heavymetal.playLoud",
                MagicMock(side_effect=Exception("Chewing gum on /dev/sd3c")))

        with patch("uyuni_users.log") as logger:
            with pytest.raises(UyuniUsersException):
                out = self.rpc_client("heavymetal.playLoud", self.rpc_client.get_token())
                mo = getattr(self.rpc_client.conn, "heavymetal.playLoud")

                assert out is not None
                assert mo.called
                assert mo.call_args == [("The Borg",)]
            assert logger.error.call_args[0] == ('Unable to call RPC function: %s', 'Chewing gum on /dev/sd3c')


class TestUyuniRemoteObject:
    """
    Test UyuniRemoteObject base class.
    """

    @patch("uyuni_users.RPCClient", MagicMock())
    def test_get_proto_return(self):
        """
        Get protocol return.
        """
        uro = UyuniRemoteObject()
        err = Exception("Suboptimal routing experience")

        assert uro.get_proto_return() == {}
        assert uro.get_proto_return(exc=err) == {"error": str(err)}
