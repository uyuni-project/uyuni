# coding: utf-8
"""
Tests for usix.
"""
import sys
import types
import pytest
from common import usix


class TestUsixCommon:
    """
    Tests for usix library.
    """

    @pytest.mark.skipif(sys.version_info < (3, 0), reason="Requires Python 3.x")
    def test_types(self):
        """
        Test py3 types
        """
        assert usix.BufferType == usix.UnicodeType == bytes
        assert usix.StringType == str
        assert usix.DictType == dict
        assert usix.IntType == usix.IntType == int
        assert usix.ListType == list
        assert usix.ClassType == usix.TypeType == type
        assert usix.FloatType == float
        assert usix.TupleType == tuple
        assert usix.InstanceType == object

    def test_max_int(self):
        """
        Test maximal size of an integer.
        """
        assert usix.MaxInt == sys.maxsize

    def test_next_iterator(self):
        """
        Test 'next' is defined.
        """
        assert hasattr(usix, "next")
        assert type(usix.next) == types.BuiltinFunctionType
