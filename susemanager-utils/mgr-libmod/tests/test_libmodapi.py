"""
Unit test for LibmodProc class.
"""
from mgrlibmod.mllib import MLLibmodProc
from mgrlibmod.mltypes import MLStreamType
import pytest
from unittest import mock
from unittest.mock import mock_open, MagicMock

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

    def test_meta_compressed(self):
        """
        test_meta_compressed -- test if a file is compressed by Gzip.
        """
        data = b'\x1f\x8b'
        with mock.patch("mgrlibmod.mllib.open", mock_open(read_data=data), create=True):
            assert self.libmodproc._is_meta_compressed("dummy.gz")

    def test_enable_stream(self):
        """
        test_enable_stream -- enables selected stream.
        """
        s_obj = MagicMock()
        s_obj.get_module_name = MagicMock(return_value="a")
        self.libmodproc.enable_stream(s_obj=s_obj)
        stream = MLStreamType("a", None)
        assert self.libmodproc._is_stream_enabled(stream)
