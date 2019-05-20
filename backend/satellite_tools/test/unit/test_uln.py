# coding: utf-8
"""
Unit tests for the ULN authentication library.
"""
import sys
import pytest
from unittest.mock import MagicMock, patch

try:
    sys.path.insert(0, __file__.split("/test/unit")[0])
    import ulnauth
except ImportError as ex:
    ulnauth = None


def uln_auth_instance():
    """
    Instantiate ULNAuth.
    """
    return ulnauth.ULNAuth()


def cfg_parser():
    """
    Get cfgparser dummy class
    """
    class CfgParser(dict):
        cfg = {}
        def read(self, path):
            self["main"] = self.cfg

    return CfgParser


@pytest.mark.skipif(ulnauth is None, reason="'ulnauth' failed to be imported")
class TestULNAuth:
    """
    Test ULN auth.
    """
    def setUp(self):
        self.uln_auth_instance = uln_auth_instance()
        self.cfg_parser = cfg_parser()

    def test_get_hostname_uln(self):
        """
        Test ULN uri raises an exception if protocol is not ULN.
        """
        with pytest.raises(ulnauth.RhnSyncException) as exc:
            self.uln_auth_instance.get_hostname("foo://something/else")
        assert "URL must start with 'uln://'" in str(exc)

    def test_get_hostname_default(self):
        """
        Test ULN uri inserts a default hostname, if not specified.
        """
        netloc, path  = uln_auth_instance.get_hostname("uln:///suse")
        assert netloc == ulnauth.ULNAuth.ULN_DEFAULT_HOST
        assert path == "/suse"

    def test_get_hostname_custom(self):
        """
        Test ULN uri inserts a custom hostname, if specified.
        """
        netloc, path  = uln_auth_instance.get_hostname("uln://scc.suse.de/suse")
        assert netloc == "scc.suse.de"
        assert path == "/suse"

    @patch("os.path.exists", MagicMock(return_value=False))
    @patch("os.access", MagicMock(return_value=False))
    def test_get_credentials_not_found(self):
        """
        Test credentials ULN configuration exists.
        """
        with pytest.raises(ulnauth.RhnSyncException) as exc:
            self.uln_auth_instance.get_credentials()
        assert "'/etc/rhn/spacewalk-repo-sync/uln.conf' does not exists" in str(exc)

    @patch("os.path.exists", MagicMock(return_value=True))
    @patch("os.access", MagicMock(return_value=False))
    def test_get_credentials_access_denied(self):
        """
        Test credentials ULN configuration readable.
        """
        with pytest.raises(ulnauth.RhnSyncException) as exc:
            self.uln_auth_instance.get_credentials()
        assert "Permission denied to '/etc/rhn/spacewalk-repo-sync/uln.conf'" in str(exc)

    @patch("os.path.exists", MagicMock(return_value=True))
    @patch("os.access", MagicMock(return_value=False))
    def test_get_credentials_access_denied(self):
        """
        Test credentials ULN configuration readable.
        """
        with pytest.raises(ulnauth.RhnSyncException) as exc:
            self.uln_auth_instance.get_credentials()
        assert "Permission denied to '/etc/rhn/spacewalk-repo-sync/uln.conf'" in str(exc)

    @patch("os.path.exists", MagicMock(return_value=True))
    @patch("os.access", MagicMock(return_value=True))
    def test_get_credentials_credentials(self):
        """
        Test credentials ULN
        """
        self.cfg_parser.cfg = {"username": "Darth Vader", "password": "f1ndE4rth"}
        with patch("configparser.ConfigParser", self.cfg_parser):
            username, password = self.uln_auth_instance.get_credentials()
            assert username == "Darth Vader"
            assert password == "f1ndE4rth"

    @patch("os.path.exists", MagicMock(return_value=True))
    @patch("os.access", MagicMock(return_value=True))
    def test_get_credentials_credentials_not_found(self):
        """
        Test credentials ULN was not found
        """
        with patch("configparser.ConfigParser", self.cfg_parser):
            with pytest.raises(AssertionError) as exc:
                self.uln_auth_instance.get_credentials()
            assert "Credentials were not found in the configuration" in str(exc)

    @patch("os.path.exists", MagicMock(return_value=True))
    @patch("os.access", MagicMock(return_value=True))
    def test_get_credentials_not_all_credentials_found(self):
        """
        Test partial credentials ULN found
        """
        self.cfg_parser.cfg = {"username": "Darth Vader"}
        with patch("configparser.ConfigParser", self.cfg_parser):
            with pytest.raises(AssertionError) as exc:
                self.uln_auth_instance.get_credentials()
            assert "Credentials were not found in the configuration" in str(exc)

        self.cfg_parser.cfg["password"] = "something"
        del self.cfg_parser.cfg["username"]
        with patch("configparser.ConfigParser", self.cfg_parser):
            with pytest.raises(AssertionError) as exc:
                self.uln_auth_instance.get_credentials()
            assert "Credentials were not found in the configuration" in str(exc)

    @patch("ulnauth.get_proxy", MagicMock(return_value=("uln:///suse", "user", "password")))
    def test_auth_uln(self):
        """
        Authenticate ULN, getting its token.
        """

        class ServerList:
            """
            Dummy server list mock.
            """
            def server(self): pass

        server_list_instance = ServerList()
        server_list = MagicMock(return_value=server_list_instance)
        retry_server_instance = MagicMock()
        retry_server_instance.auth = MagicMock()
        retry_server_instance.auth.login = MagicMock(return_value="12345")
        retry_server = MagicMock(return_value=retry_server_instance)
        uri = "uln:///suse"
        with patch("ulnauth.ServerList", server_list) as srv_lst, patch("ulnauth.RetryServer", retry_server) as rtr_srv:
            self.uln_auth_instance.get_credentials = MagicMock(return_value=("uln_user", "uln_password",))
            token = self.uln_auth_instance.authenticate(uri)
            assert server_list.call_args_list[0][0] == (['https://linux-update.oracle.com/rpc/api'],)
            rs_call = retry_server.call_args_list[0][1]
            for p_name, p_val in {'refreshCallback': None, 'username': 'user',
                          'proxy': None, 'password': 'password', 'timeout': 5}.items():
                assert p_name in rs_call
                assert rs_call[p_name] == p_val

            assert retry_server_instance.addServerList.call_args_list[0][0] == (server_list_instance,)
            assert token == "12345"
            assert retry_server_instance.auth.login.call_args_list[0][0] == ("uln_user", "uln_password")
