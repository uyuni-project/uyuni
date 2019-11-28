# coding: utf-8
"""
Unit tests for modules/uyuni_users.py execution module
"""

import sys
sys.path.append("../_modules")
import uyuni_users
from uyuni_users import RPCClient
from unittest.mock import patch, MagicMock, mock_open


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

