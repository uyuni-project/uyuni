# coding: utf-8
"""
Test argument parser.
"""
import pytest
import spacecmd.argumentparser


class TestSCArgumentParser:
    """
    Test argument parser subclass.
    """
    def test_argparse_raise_exception(self):
        """
        Test argparse raise exception.
        """
        msg = "not enough memory, get system upgrade"
        argparse = spacecmd.argumentparser.SpacecmdArgumentParser()
        with pytest.raises(Exception) as exc:
            argparse.error(msg)
        assert msg in str(exc)
