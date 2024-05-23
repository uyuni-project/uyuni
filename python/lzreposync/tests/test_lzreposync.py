import pytest
from lzreposync import parse


def test_download_and_parse_metadata():
    with pytest.raises(ValueError, match="Repository URL missing"):
        parse.download_and_parse_metadata("")
