# coding: utf-8
"""
Py-test based unit tests for common/repo
"""
from mock import MagicMock, patch
from spacewalk.common.repo import DpkgRepo
import http


class FakeRequests:
    """
    Fake requests object.
    """

    class FakeResponse:
        """
        Fake request's response struct
        """

    def close(self):
        """
        Close request. Does nothing actually.

        :return:
        """

    def conf(self, **kwargs):
        """
        Configure response for the test purposes.

        :param kwargs:
        :return:
        """
        for kkw, vkw in kwargs.items():
            setattr(self, kkw, vkw)

        return self


def mock_release_index(self):
    """
    Set flat to True.

    :param self:
    :return:
    """
    self._flat = True


class TestCommonRepo:
    """
    Test suite for common/repo.
    """
    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.gz", b"\x00")))
    @patch("spacewalk.common.repo.DpkgRepo.is_flat", MagicMock(return_value=False))
    def test_get_pkg_index_raw(self):
        """
        Test get_pkg_index_raw method.

        :return:
        """
        gri = DpkgRepo.ReleaseEntry(size=999, uri="restricted/binary-amd64")
        gri.checksum.md5 = "93b885adfe0da089cdf634904fd59f71"
        gri.checksum.sha1 = "5ba93c9db0cff93f52b521d7420e43f6eda2784f"
        gri.checksum.sha256 = "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d"

        release_index = MagicMock()
        release_index().get = MagicMock(return_value=gri)
        with patch("spacewalk.common.repo.DpkgRepo.get_release_index", release_index):
            repo = DpkgRepo("http://mygreathost.com/ubuntu/dists/bionic/restricted/binary-amd64/")
            assert repo.verify_packages_index()

    @patch("spacewalk.common.repo.DpkgRepo.get_release_index", mock_release_index)
    def test_is_flat(self):
        """
        Return True or (False) if repo has flat (or not) format.

        :return:
        """
        assert DpkgRepo("http://dummy").is_flat()

    def test_get_parent_url_no_subpath_default(self):
        """
        Return parent URL without adding subpath and default params.

        :return:
        """
        url = "http://mygreathost.com/ubuntu/dists/bionic/restricted/binary-amd64/"
        assert DpkgRepo._get_parent_url(url) == "http://mygreathost.com/ubuntu/dists/bionic/restricted/"

    def test_get_parent_url_no_subpath_with_depth(self):
        """
        Return parent URL without adding subpath with depth.

        :return:
        """
        url = "http://mygreathost.com/ubuntu/dists/bionic/restricted/binary-amd64/"
        assert DpkgRepo._get_parent_url(url, depth=2) == "http://mygreathost.com/ubuntu/dists/bionic/"
        assert DpkgRepo._get_parent_url(url, depth=3) == "http://mygreathost.com/ubuntu/dists/"
        assert DpkgRepo._get_parent_url(url, depth=4) == "http://mygreathost.com/ubuntu/"
        assert DpkgRepo._get_parent_url(url, depth=5) == "http://mygreathost.com/"

    @patch("spacewalk.common.repo.requests.get", MagicMock(
        return_value=FakeRequests().conf(status_code=http.HTTPStatus.NOT_FOUND, content=b"")))
    @patch("spacewalk.common.repo.DpkgRepo._parse_release_index", MagicMock(return_value="Plasma conduit overflow"))
    def test_get_release_index_flat(self):
        """
        Get release index file contents.

        :return:
        """
        repo = DpkgRepo("http://dummy/url")
        assert repo.get_release_index() == "Plasma conduit overflow"
        assert repo.is_flat()
