# coding: utf-8
"""
Unit tests for the ULN authentication library.
"""
import sys
import pytest

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
