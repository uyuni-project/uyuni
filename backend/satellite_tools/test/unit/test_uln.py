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


@pytest.fixture
def uln_auth_instance():
    """
    Instantiate ULNAuth.
    """
    return ulnauth.ULNAuth()


@pytest.mark.skipif(ulnauth is None, reason="'ulnauth' failed to be imported")
class TestULNAuth:
    """
    Test ULN auth.
    """
    def test_get_hostname_uln(self, uln_auth_instance):
        """
        Test ULN uri raises an exception if protocol is not ULN.
        """
        with pytest.raises(ulnauth.RhnSyncException) as exc:
            uln_auth_instance._get_hostname("foo://something/else")
        assert "URL must start with 'uln://'" in str(exc)

    def test_get_hostname_default(self, uln_auth_instance):
        """
        Test ULN uri inserts a default hostname, if not specified.
        """
        netloc, path  = uln_auth_instance._get_hostname("uln:///suse")
        assert netloc == ulnauth.ULNAuth.ULN_DEFAULT_HOST
        assert path == "/suse"

    def test_get_hostname_custom(self, uln_auth_instance):
        """
        Test ULN uri inserts a custom hostname, if specified.
        """
        netloc, path  = uln_auth_instance._get_hostname("uln://scc.suse.de/suse")
        assert netloc == "scc.suse.de"
        assert path == "/suse"

    @patch("os.path.exists", MagicMock(return_value=False))
    @patch("os.access", MagicMock(return_value=False))
    def test_get_credentials_not_found(self, uln_auth_instance):
        """
        Test fetching proper credentials from the ULN configuration.
        """
        with pytest.raises(ulnauth.RhnSyncException) as exc:
            uln_auth_instance._get_credentials()
        assert "'/etc/rhn/spacewalk-repo-sync/uln.conf' does not exists" in str(exc)
