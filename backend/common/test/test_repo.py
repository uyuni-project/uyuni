# coding: utf-8
"""
Py-test based unit tests for common/repo
"""
from mock import MagicMock, patch
from spacewalk.common.repo import DpkgRepo, GeneralRepoException
import http
import pytest
import lzma
import zlib


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
    def test_verify_packages_index(self):
        """
        Test verify_packages_index method.

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
        assert DpkgRepo._get_parent_url(url, depth=0) == "http://mygreathost.com/ubuntu/dists/bionic/restricted/binary-amd64/"
        assert DpkgRepo._get_parent_url(url, depth=2) == "http://mygreathost.com/ubuntu/dists/bionic/"
        assert DpkgRepo._get_parent_url(url, depth=3) == "http://mygreathost.com/ubuntu/dists/"
        assert DpkgRepo._get_parent_url(url, depth=4) == "http://mygreathost.com/ubuntu/"
        assert DpkgRepo._get_parent_url(url, depth=5) == "http://mygreathost.com/"
        assert DpkgRepo._get_parent_url(url, depth=6) == "http://mygreathost.com/"
        assert DpkgRepo._get_parent_url(url, depth=7) == "http://mygreathost.com/"

    @patch("spacewalk.common.repo.requests.get", MagicMock(
        return_value=FakeRequests().conf(status_code=http.HTTPStatus.NOT_FOUND, content=b"")))
    @patch("spacewalk.common.repo.DpkgRepo._parse_release_index", MagicMock(return_value="Plasma conduit overflow"))
    def test_get_release_index_flat(self):
        """
        Get release index file contents, flat.

        :return:
        """
        repo = DpkgRepo("http://dummy/url")
        assert repo.get_release_index() == "Plasma conduit overflow"
        assert repo.is_flat()

    @patch("spacewalk.common.repo.requests.get", MagicMock(
        return_value=FakeRequests().conf(status_code=http.HTTPStatus.OK, content=b"")))
    @patch("spacewalk.common.repo.DpkgRepo._parse_release_index", MagicMock(return_value="Plasma conduit overflow"))
    def test_get_release_index_standard(self):
        """
        Get release index file contents, standard.

        :return:
        """
        repo = DpkgRepo("http://dummy/url")
        assert repo.get_release_index() == "Plasma conduit overflow"
        assert not repo.is_flat()

    @patch("spacewalk.common.repo.requests.get", MagicMock(
        return_value=FakeRequests().conf(status_code=http.HTTPStatus.NO_CONTENT, content=b"")))
    @patch("spacewalk.common.repo.DpkgRepo._parse_release_index", MagicMock(return_value="Plasma conduit overflow"))
    def test_get_release_index_exception(self):
        """
        Get release index file contents, other than flat or standard error code.

        :return:
        """
        repo = DpkgRepo("http://dummy/url")

        with pytest.raises(GeneralRepoException) as exc:
            assert repo.get_release_index()
        assert str(exc.value) == "HTTP error 204 occurred while connecting to the URL"

    def test_parse_release_index(self):
        """
        Parse release index.

        :return:
        """
        release = """
Irrelevant data
9f067fc9044265ff132cefef4f741ede     999 one/Release.gz
ee27992a1c0154454601fd8e5a808256    1298 two/Release.gz
some more irrelevant data here
c373e78514584fc3a6b3505aa0ce1525b83e4ceb     999 one/Release.gz
70d7c8225fb5b6ee67ba089681425c5b712e0b19    1298 two/Release.gz
something in between
ef73ea0cc071ad4a13fb52717f99b1951bb41bc2198d2307b30cc0edfb3aed03     999 one/Release.gz
4843bbb823a63f3bde6104ceecc86daebf8b84efd4f6fb1373138b4589a84803    1298 two/Release.gz
Some more irrelevant data
"""
        repo = DpkgRepo("http://dummy/url")
        parsed = repo._parse_release_index(release)

        assert len(parsed) == 2
        assert "one/Release.gz" in parsed
        assert "two/Release.gz" in parsed

        assert parsed["one/Release.gz"].checksum.md5 == "9f067fc9044265ff132cefef4f741ede"
        assert parsed["one/Release.gz"].checksum.sha1 == "c373e78514584fc3a6b3505aa0ce1525b83e4ceb"
        assert parsed["one/Release.gz"].checksum.sha256 == "ef73ea0cc071ad4a13fb52717f99b1951bb41bc2198d2307b30cc0edfb3aed03"
        assert parsed["one/Release.gz"].size == 999

        assert parsed["two/Release.gz"].checksum.md5 == "ee27992a1c0154454601fd8e5a808256"
        assert parsed["two/Release.gz"].checksum.sha1 == "70d7c8225fb5b6ee67ba089681425c5b712e0b19"
        assert parsed["two/Release.gz"].checksum.sha256 == "4843bbb823a63f3bde6104ceecc86daebf8b84efd4f6fb1373138b4589a84803"
        assert parsed["two/Release.gz"].size == 1298

    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.gz", "content")))
    def test_decompress_pkg_index_gz(self):
        """
        Test decompression for Packages.gz file.

        :return:
        """
        data = b'\xd0\xbc\xd0\xb0\xd0\xba\xd0\xb0\xd1\x80\xd0\xbe\xd0\xbd\xd0\xb8'
        zdcmp = MagicMock(return_value=data)
        xdcmp = MagicMock(return_value=data)
        with patch("spacewalk.common.repo.zlib.decompress", zdcmp) as m_zlib, \
            patch("spacewalk.common.repo.lzma.decompress", xdcmp) as m_lzma:
            out = DpkgRepo("http://dummy_url").decompress_pkg_index()

        assert not xdcmp.called
        assert zdcmp.called
        assert out == "макарони"

    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.xz", "content")))
    def test_decompress_pkg_index_xz(self):
        """
        Test decompression for Packages.xz file.

        :return:
        """
        data = b'\xd0\xbc\xd0\xb0\xd0\xba\xd0\xb0\xd1\x80\xd0\xbe\xd0\xbd\xd0\xb8'
        zdcmp = MagicMock(return_value=data)
        xdcmp = MagicMock(return_value=data)
        with patch("spacewalk.common.repo.zlib.decompress", zdcmp) as m_zlib, \
            patch("spacewalk.common.repo.lzma.decompress", xdcmp) as m_lzma:
            out = DpkgRepo("http://dummy_url").decompress_pkg_index()

        assert not zdcmp.called
        assert xdcmp.called
        assert out == "макарони"

    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.gz", "content")))
    def test_decompress_pkg_index_gz_general_failure(self):
        """
        Test decompression for Packages.gz file general failure handling.

        :return:
        """
        zdcmp = MagicMock(side_effect=GeneralRepoException("Too many symlinks found in binary data"))
        xdcmp = MagicMock(side_effect=GeneralRepoException(""))
        with patch("spacewalk.common.repo.zlib.decompress", zdcmp) as m_zlib, \
            patch("spacewalk.common.repo.lzma.decompress", xdcmp) as m_lzma:
            with pytest.raises(GeneralRepoException) as exc:
                DpkgRepo("http://dummy_url").decompress_pkg_index()

        assert not xdcmp.called
        assert zdcmp.called

        err = str(exc.value)
        assert err.startswith("Unhandled exception occurred while decompressing Packages.gz:")
        assert "symlinks" in err

    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.xz", "content")))
    def test_decompress_pkg_index_xz_general_failure(self):
        """
        Test decompression for Packages.xz file general failure handling.

        :return:
        """
        zdcmp = MagicMock(side_effect=GeneralRepoException(""))
        xdcmp = MagicMock(side_effect=GeneralRepoException("Software design limitation"))
        with patch("spacewalk.common.repo.zlib.decompress", zdcmp) as m_zlib, \
            patch("spacewalk.common.repo.lzma.decompress", xdcmp) as m_lzma:
            with pytest.raises(GeneralRepoException) as exc:
                DpkgRepo("http://dummy_url").decompress_pkg_index()

        assert not zdcmp.called
        assert xdcmp.called

        err = str(exc.value)
        assert err.startswith("Unhandled exception occurred while decompressing Packages.xz:")
        assert "Software" in err

    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.xz", "content")))
    def test_decompress_pkg_index_xz_failure(self):
        """
        Test decompression for Packages.xz file failure handling.

        :return:
        """
        zdcmp = MagicMock(side_effect=zlib.error(""))
        xdcmp = MagicMock(side_effect=lzma.LZMAError("/dev/null is busy while upgrading"))
        with patch("spacewalk.common.repo.zlib.decompress", zdcmp) as m_zlib, \
            patch("spacewalk.common.repo.lzma.decompress", xdcmp) as m_lzma:
            with pytest.raises(GeneralRepoException) as exc:
                DpkgRepo("http://dummy_url").decompress_pkg_index()

        assert not zdcmp.called
        assert xdcmp.called
        assert "/dev/null" in str(exc.value)

    @patch("spacewalk.common.repo.DpkgRepo.get_pkg_index_raw", MagicMock(return_value=("Packages.gz", "content")))
    def test_decompress_pkg_index_gz_failure(self):
        """
        Test decompression for Packages.gz file failure handling.

        :return:
        """
        zdcmp = MagicMock(side_effect=zlib.error("Firewall currently too hot"))
        xdcmp = MagicMock(side_effect=lzma.LZMAError(""))
        with patch("spacewalk.common.repo.zlib.decompress", zdcmp) as m_zlib, \
            patch("spacewalk.common.repo.lzma.decompress", xdcmp) as m_lzma:
            with pytest.raises(GeneralRepoException) as exc:
                DpkgRepo("http://dummy_url").decompress_pkg_index()

        assert not xdcmp.called
        assert zdcmp.called
        assert "hot" in str(exc.value)

    @patch("spacewalk.common.repo.requests.get", MagicMock(
        return_value=FakeRequests().conf(status_code=http.HTTPStatus.NOT_FOUND, content=b"")))
    def test_get_pkg_index_raw_exception(self):
        """
        Test getting package index file exception handling

        :return:
        """
        with pytest.raises(GeneralRepoException) as exc:
            DpkgRepo("http://dummy/url").get_pkg_index_raw()

        assert "No variants of package index has been found on http://dummy/url repo" == str(exc.value)

    def test_append_index_file_to_url(self):
        """
        Test append index files to the given url.
        :return:
        """
        url = "https://domainname.ext/PATH/Updates/Distro/version/arch/update/?very_long_auth_token"
        dpr = DpkgRepo(url)

        assert dpr.append_index_file(DpkgRepo.PKG_GZ) == \
            "https://domainname.ext/PATH/Updates/Distro/version/arch/update/Packages.gz?very_long_auth_token"

        assert dpr.append_index_file(DpkgRepo.PKG_XZ) == \
            "https://domainname.ext/PATH/Updates/Distro/version/arch/update/Packages.xz?very_long_auth_token"
