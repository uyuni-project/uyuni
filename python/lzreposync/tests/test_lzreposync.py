import io
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


def test_parse_primary_missing_element_attributes():
    """
    Testing the parsing with a primary.xml file of 2 packages, one of which have missing element attributes:
    'epoch' and the 'ver' of the second package are missing.
    The current parser's behaviour is to ignore the missing attribute, and continue parsing
    """
    test_primary_path = "tests/primary_test_missing_element_attributes.xml.gz"
    test_cache_dir = ".cache"
    handler = Handler()

    with open(test_primary_path, "rb") as primary_gz:
        file_obj = io.BytesIO(primary_gz.read())

        rpm_repo = RPMRepo(None, test_cache_dir, None, handler)
        parsed_packages_count = rpm_repo.parse_metadata_file(file_obj)

    assert parsed_packages_count == 2


def test_verify_signature():
    """
    Note: this test case assumes that the repomd.xml file hasn't been altered, if so it will fail
    """
    test_repo = "https://download.opensuse.org/update/leap/15.5/oss/repodata/"
    test_cache_dir = ".cache"

    rpm_repo = RPMRepo(None, test_cache_dir, test_repo, None)

    assert rpm_repo.verify_signature()
