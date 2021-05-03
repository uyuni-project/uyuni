'''
Author: Bo Maryniuk <bo@suse.de>
'''

from mock import MagicMock, patch
from ..beacons import pkgset

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
    with patch.object(pkgset.os.path, "exists", MagicMock(return_value=True)):
        res, msg = pkgset.validate({})
        assert res is True
        assert msg == 'Configuration validated'
    with patch.object(pkgset.os.path, "exists", MagicMock(return_value=False)):
        res, msg = pkgset.validate({})
        assert res is False
        assert msg == 'Cookie path has not been set.'


@patch.object(pkgset.os.path, 'exists', MagicMock(return_value=True))
@patch.object(pkgset, '__context__', {pkgset.__virtualname__: ""})
def test_beacon():
    '''
    Test beacon functionality.
    '''
    mock_content = MagicMock(
        **{'return_value.__enter__.return_value.read.return_value.strip.return_value': 'test'}
    )
    with patch.object(pkgset, 'open', mock_content):
        data = pkgset.beacon({})
        assert data == [{'tag': 'changed'}]
