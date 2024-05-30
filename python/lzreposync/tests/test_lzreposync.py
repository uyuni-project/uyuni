import os.path
from unittest.mock import patch, mock_open

from lzreposync import repo, Handler


def test_download_and_parse_metadata():
    with pytest.raises(ValueError, match="Repository URL missing"):
        parse.download_and_parse_metadata("")
