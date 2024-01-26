"""
Author: cbbayburt@suse.com
"""

import sys
from unittest.mock import MagicMock, patch, call
from . import mockery

mockery.setup_environment()

# pylint: disable-next=wrong-import-position
from ..states import product

# Mock globals
product.log = MagicMock()
product.__salt__ = {}
product.__grains__ = {}


@patch.dict(product.__grains__, {"os_family": "Suse"})
def test_suse_with_zypper():
    """
    Test if the state module is available for SUSE OS only with a
    supported version of zypper (>= 1.8.13) available.
    """
    # Supported zypper version
    with patch.dict(
        product.__salt__,
        {
            "pkg.info_installed": MagicMock(
                return_value={"zypper": {"version": "1.9.0"}}
            )
        },
    ):
        with patch.object(product, "version_cmp", MagicMock(return_value=1)):
            assert product.__virtual__() is "product"
            product.version_cmp.assert_called_once_with("1.9.0", "1.8.13")

    # Unsupported zypper version
    with patch.dict(
        product.__salt__,
        {
            "pkg.info_installed": MagicMock(
                return_value={"zypper": {"version": "1.8.0"}}
            )
        },
    ):
        with patch.object(product, "version_cmp", MagicMock(return_value=-1)):
            assert product.__virtual__() == (
                False,
                "Module product: zypper 1.8.13 or greater required",
            )
            product.version_cmp.assert_called_once_with("1.8.0", "1.8.13")

    # No zypper available
    with patch.dict(
        product.__salt__,
        {
            "pkg.info_installed": MagicMock(
                return_value=sys.modules["salt.exceptions"].CommandExecutionError
            )
        },
    ):
        assert product.__virtual__() == (
            False,
            "Module product: zypper package manager not found",
        )


@patch.dict(product.__grains__, {"os_family": "Non-Suse"})
def test_non_suse():
    """
    Test if the state module is unavailable for Non-SUSE OS
    """
    assert product.__virtual__() == (False, "Module product: non SUSE OS not supported")


def test_get_missing_products():
    """
    Test if the missing products are returned correctly, excluding
    the ones that are provided by another installed product.
    """
    test_data = {
        "not_installed": {"product1": True, "product2": True},
        "provides-product1": {"product1": True, "this-provides-product1": True},
        "provides-product2": {"product2": True},
    }

    pkg_search_mock = MagicMock(
        side_effect=[
            test_data["not_installed"],
            test_data["provides-product1"],
            test_data["provides-product2"],
        ]
    )

    with patch.dict(product.__salt__, {"pkg.search": pkg_search_mock}):
        # pylint: disable-next=protected-access
        res = product._get_missing_products(False)

        # Expected pkg.search calls
        calls = [
            call(
                "product()",
                refresh=False,
                match="exact",
                provides=True,
                not_installed_only=True,
            ),
            call("product1", match="exact", provides=True),
            call("product2", match="exact", provides=True),
        ]

        pkg_search_mock.assert_has_calls(calls)
        assert pkg_search_mock.call_count == 3
        # Assert that only the non-provided product is returned
        assert res == ["product2"]


def test_not_installed_provides():
    """
    Test if the provided packages are correctly excluded when
    provided by another missing product.
    """
    test_data = {
        "not_installed": {"product1": True, "this-provides-product1": True},
        "provides-product1": {"product1": True, "this-provides-product1": True},
        "provides-product2": {"this-provides-product1": True},
    }

    pkg_search_mock = MagicMock(
        side_effect=[
            test_data["not_installed"],
            test_data["provides-product1"],
            test_data["provides-product2"],
        ]
    )

    with patch.dict(product.__salt__, {"pkg.search": pkg_search_mock}):
        # pylint: disable-next=protected-access
        res = product._get_missing_products(False)

        # Expected pkg.search calls
        calls = [
            call(
                "product()",
                refresh=False,
                match="exact",
                provides=True,
                not_installed_only=True,
            ),
            call("product1", match="exact", provides=True),
            call("this-provides-product1", match="exact", provides=True),
        ]

        pkg_search_mock.assert_has_calls(calls)
        assert pkg_search_mock.call_count == 3
        # Assert that not both products are returned
        assert len(res) == 1
        # Assert that the provided product is not returned
        assert "product1" not in res
        # Assert that the providing product is returned
        assert "this-provides-product1" in res
