# pylint: disable=missing-module-docstring
import pytest

from uyuni.common import fileutils


@pytest.mark.parametrize(
    "path,expected",
    [
        (
            "/var/cache/rhn/foo/",
            ["/", "/var", "/var/cache", "/var/cache/rhn", "/var/cache/rhn/foo"],
        ),
        ("foo/bar/", ["foo", "foo/bar"]),
        ("baz/", ["baz"]),
        (".cache", [".cache"]),
    ],
)
def test_split_dirs(path, expected):
    # pylint: disable-next=protected-access
    assert fileutils._split_dirs(path) == expected
