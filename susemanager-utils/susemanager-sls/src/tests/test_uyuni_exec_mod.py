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
        with patch.object(uyuni_users, "open", mock_open(read_data="blet")) as mo:
            out = self.rpc_client.load_session()
            print(">>>>", out)
            print("....", mo.mock_calls)

    def test_session_save(self):
        """
        Save session.

        :return:
        """
        with patch.object(uyuni_users, "open", mock_open()) as mo:
            out = self.rpc_client.save_session()
            print(">>>>", out)
            print("....", mo.mock_calls)

