"""
Unit test for LibmodProc class.
"""
from mgrlibmod.mllib import MLLibmodProc
from mgrlibmod.mltypes import MLStreamType
import pytest

try:
    import gi
    from gi.repository import Modulemd
except ImportError:
    gi = None

@pytest.mark.skipif(gi is None, reason="libmodulemd Python bindings is missing")
class TestLibmodProc:
    """

    """
    def setup_method(self):
        self.libmodproc = MLLibmodProc([])

    def teardown_method(self):
        del self.libmodproc

    def test_stream_enabled(self):
        """
        test_stream_enabled -- tests if stream is enabled.
        """
        stream = MLStreamType("a", "b")
        self.libmodproc._enabled_stream_modules = {"a": "b"}
        assert self.libmodproc._is_stream_enabled(stream)
