"""
Author: Bo Maryniuk <bo@suse.de>
"""

from unittest.mock import MagicMock, patch

from . import mockery

mockery.setup_environment()

with patch(
    "salt.config.minion_config", return_value={"cachedir": "/var/cache/salt/minion"}
):
    from ..beacons import pkgset


pkgset.__context__ = dict()


@patch.object(pkgset.os.path, "exists", MagicMock(return_value=True))
@patch.object(pkgset, "__context__", {pkgset.__virtualname__: ""})
@patch.object(pkgset, "CACHE", MagicMock())
def test_beacon():
    """
    Test beacon functionality.
    """

    mock_content = MagicMock(
        **{
            "return_value.__enter__.return_value.read.return_value.strip.return_value": "test"
        }
    )

    # pylint: disable-next=pointless-string-statement
    """
    The __context__ has no pkgset data, the cache contains the same data as in the cookie.
    """
    with patch.object(pkgset, "open", mock_content), patch.object(
        pkgset, "__context__", {}
    ) as mock_context, patch.object(
        pkgset.CACHE, "fetch", return_value={}
    ), patch.object(
        pkgset.CACHE, "store"
    ) as mock_cache_store:
        data = pkgset.beacon({})
        assert mock_context["pkgset"] == "test"
        # pylint: disable-next=use-implicit-booleaness-not-comparison
        assert data == []
        mock_cache_store.assert_called_once()

    # pylint: disable-next=pointless-string-statement
    """
    The __context__ has no pkgset data, the cache contains the different data than the cookie.
    """
    with patch.object(pkgset, "open", mock_content), patch.object(
        pkgset, "__context__", {}
    ) as mock_context, patch.object(
        pkgset.CACHE, "fetch", return_value={"data": "other"}
    ), patch.object(
        pkgset.CACHE, "store"
    ) as mock_cache_store:
        data = pkgset.beacon({})
        assert mock_context["pkgset"] == "test"
        assert data == [{"tag": "changed"}]
        mock_cache_store.assert_called_once()

    # pylint: disable-next=pointless-string-statement
    """
    The __context__ has pkgset data, but the data is different than the cookie.
    """
    with patch.object(pkgset, "open", mock_content), patch.object(
        pkgset, "__context__", {"pkgset": "other"}
    ) as mock_context, patch.object(pkgset.CACHE, "store") as mock_cache_store:
        data = pkgset.beacon({})
        assert mock_context["pkgset"] == "test"
        assert data == [{"tag": "changed"}]
        mock_cache_store.assert_called_once()

    # pylint: disable-next=pointless-string-statement
    """
    The __context__ has pkgset data, the data is the same as the cookie.
    """
    with patch.object(pkgset, "open", mock_content), patch.object(
        pkgset, "__context__", {"pkgset": "test"}
    ) as mock_context, patch.object(pkgset.CACHE, "store") as mock_cache_store:
        data = pkgset.beacon({})
        assert mock_context["pkgset"] == "test"
        # pylint: disable-next=use-implicit-booleaness-not-comparison
        assert data == []
        mock_cache_store.assert_not_called()
