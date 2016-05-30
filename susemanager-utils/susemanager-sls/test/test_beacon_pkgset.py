'''
Author: Bo Maryniuk <bo@suse.de>
'''
import os
import sys
from mock import MagicMock, patch
from testing_utils import load_module

pkgset = load_module("salt/_beacons/pkgset.py")
pkgset.__context__ = dict()


def test_virtual():
    '''
    Test virtual function.
    '''
    with patch.object(pkgset.os.path, "exists", MagicMock(return_value=True)):
        assert pkgset.__virtual__() == pkgset.__virtualname__
    with patch.object(pkgset.os.path, "exists", MagicMock(return_value=False)):
        assert pkgset.__virtual__() != pkgset.__virtualname__


def test_validate():
    '''
    Test validate() function
    '''
    res, msg = pkgset.validate({'cookie': '/bogus/path'})
    assert res is True
    assert msg == 'Configuration validated'

    for cfg in [{}, {'bogus': 'data'}]:
        res, msg = pkgset.validate(cfg)
        assert res is False
        assert msg == 'Cookie path has not been set.'


def test_beacon():
    '''
    Test beacon functionality.
    '''
    mock_content = MagicMock(
        **{'return_value.__enter__.return_value.read.return_value.strip.return_value': 'test'}
    )
    with patch.object(pkgset.os.path, 'exists', MagicMock(return_value=True)):
        with patch.object(pkgset, 'open', mock_content):
            data = pkgset.beacon({})
            assert data == [{'zypper/pkgset/changed': 'true'}]
