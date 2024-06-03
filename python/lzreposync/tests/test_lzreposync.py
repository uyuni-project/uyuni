import os.path
from unittest.mock import patch, mock_open

from lzreposync import Handler, RPMRepo


def test_download_and_parse_metadata():
    test_hash = "00c99a7e67dac325f1cbeeedddea3e4001a8f803c9bf43d9f50b5f1c756b0887"
    test_name = "Leap15"
    test_cache_dir = ".cache"
    test_repo = "https://download.opensuse.org/update/leap/15.5/oss/repodata/"
    cache_file = os.path.join("{}/{}".format(test_cache_dir, test_name), test_name + ".hash")
    test_rpm_handler = Handler()

    with patch("builtins.open", mock_open()) as mocked_file:
        rpm_repo = RPMRepo(test_name, test_cache_dir,
                           test_repo,
                           test_rpm_handler)
        rpm_repo.get_packages_metadata()

        mocked_file.assert_called_once_with(cache_file, 'w')
        mocked_file().write.assert_called_once_with(test_hash)
