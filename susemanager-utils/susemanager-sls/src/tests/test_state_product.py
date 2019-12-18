'''
Author: cbbayburt@suse.com
'''

import sys
from mock import MagicMock, patch, call
from . import mockery
mockery.setup_environment()

from ..states import product

# Mock globals
product.log = MagicMock()
product.__salt__ = {}
product.__grains__ = {}

def test_virtual():
    '''
    Test if the state module is only available for SUSE OS
    with Zypper available.
    '''
    with patch.dict(product.__grains__, {'os_family': 'Suse'}):
        with patch('src.states.product.salt.utils.path.which', MagicMock(return_value="/my/zypper")):
            assert product.__virtual__() is 'product'
            product.salt.utils.path.which.assert_called_once_with('zypper')
        with patch('src.states.product.salt.utils.path.which', MagicMock(return_value=None)):
            assert product.__virtual__() == (False, "Module product: zypper package manager not found")
            product.salt.utils.path.which.assert_called_once_with('zypper')

    with patch.dict(product.__grains__, {'os_family': 'Non-Suse'}):
            assert product.__virtual__() == (False, "Module product: non SUSE OS not supported")

def test_get_missing_products():
    '''
    Test if the missing products are returned correctly, excluding
    the ones that are provided by another installed product.
    '''
    test_data = {
        'not_installed': {'product1': True, 'product2': True},
        'provides-product1': {'this-provides-product1': True}
    }

    pkg_search_mock = MagicMock(side_effect=[
        test_data['not_installed'],
        test_data['provides-product1'],
        sys.modules['salt.exceptions'].CommandExecutionError])

    with patch.dict(product.__salt__, {'pkg.search': pkg_search_mock}):
        res = product._get_missing_products(False)

        # Expected pkg.search calls
        calls = [
            call('product()', refresh=False, match='exact', provides=True, not_installed_only=True),
            call('product1', match='exact', provides=True),
            call('product2', match='exact', provides=True)
        ]

        pkg_search_mock.assert_has_calls(calls)
        assert pkg_search_mock.call_count == 3
        # Assert that only the non-provided product is returned
        assert res == ['product2']


def test_not_installed_provides():
    '''
    Test if the provided packages are correctly excluded when
    provided by another missing product.
    '''
    test_data = {
        'not_installed': {'product1': True, 'this-provides-product1': True},
        'provides-product1': {'this-provides-product1': True}
    }

    pkg_search_mock = MagicMock(side_effect=[
        test_data['not_installed'],
        test_data['provides-product1'],
        sys.modules['salt.exceptions'].CommandExecutionError])

    with patch.dict(product.__salt__, {'pkg.search': pkg_search_mock}):
        res = product._get_missing_products(False)

        # Expected pkg.search calls
        calls = [
            call('product()', refresh=False, match='exact', provides=True, not_installed_only=True),
            call('product1', match='exact', provides=True),
            call('this-provides-product1', match='exact', provides=True)
        ]

        pkg_search_mock.assert_has_calls(calls)
        assert pkg_search_mock.call_count == 3
        # Assert that not both products are returned
        assert len(res) == 1
        # Assert that the provided product is not returned
        assert 'product1' not in res
        # Assert that the providing product is returned
        assert 'this-provides-product1' in res
