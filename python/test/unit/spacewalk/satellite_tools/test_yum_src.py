#!/usr/bin/env python
# pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# Copyright (c) 2011 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.

import tempfile
import shutil
import os
import solv
import unittest
import pytest
from urllib.parse import quote
from urlgrabber.grabber import URLGrabError
import xml.etree.ElementTree as etree

try:
    from io import StringIO
except ImportError:
    from StringIO import StringIO

try:
    from urlparse import urlparse, urlunparse
# pylint: disable-next=bare-except
except:
    # pylint: disable-next=ungrouped-imports
    from urllib.parse import urlparse, urlunparse

from collections import namedtuple

from mock import Mock, MagicMock, patch, mock_open

from spacewalk.satellite_tools.repo_plugins import yum_src, ContentPackage
from spacewalk.satellite_tools.repo_plugins.yum_src import UpdateNotice


class YumSrcTest(unittest.TestCase):
    def _make_dummy_cs(self):
        """Create a dummy ContentSource object that only talks to a mocked yum"""
        real_setup_repo = yum_src.ContentSource.setup_repo

        # don't read configs
        patch("spacewalk.common.suseLib.initCFG").start()
        patch("spacewalk.common.suseLib.CFG").start()
        yum_src.initCFG = Mock()
        yum_src.CFG = Mock()
        yum_src.CFG.MOUNT_POINT = ""
        yum_src.CFG.PREPENDED_DIR = ""
        yum_src.CFG.REPOSYNC_MINRATE = 1000
        yum_src.CFG.REPOSYNC_TIMEOUT = 300
        yum_src.fileutils.makedirs = Mock()
        yum_src.os.makedirs = Mock()
        yum_src.os.path.isdir = Mock()

        yum_src.get_proxy = Mock(return_value=(None, None, None))

        cs = yum_src.ContentSource("http://example.com/fake_path/", "test_repo", org="")
        # pylint: disable-next=invalid-name
        mockReturnPackages = MagicMock()
        mockReturnPackages.returnPackages = MagicMock(name="returnPackages")
        mockReturnPackages.returnPackages.return_value = []
        cs.repo.is_configured = True
        cs.repo.includepkgs = []
        cs.repo.exclude = []
        cs.repo.root = os.path.dirname(__file__)
        cs.channel_label = "."

        yum_src.ContentSource.setup_repo = real_setup_repo

        return cs

    @pytest.fixture(autouse=True)
    def set_temp_path(self, tmpdir):
        self.tmpdir = tmpdir.strpath

    def setUp(self):
        patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.fileutils.makedirs"
        ).start()
        self.repo = yum_src.ZypperRepo(
            tempfile.mkdtemp(), "http://example.com/fake_path/", "1"
        )

    def tearDown(self):
        shutil.rmtree(self.repo.root)

    def test_content_source_init(self):
        cs = self._make_dummy_cs()

        self.assertFalse(cs.insecure)
        self.assertTrue(cs.interactive)
        assert isinstance(cs.repo, yum_src.ZypperRepo)

    @unittest.skip
    def test_list_packages_empty(self):
        cs = self._make_dummy_cs()

        os.path.isfile = Mock(return_value=True)
        solv.Pool = Mock()

        self.assertEqual(cs.list_packages(filters=None, latest=False), [])

    @unittest.skip
    def test_list_packages_with_pack(self):
        cs = self._make_dummy_cs()

        package_attrs = [
            "name",
            "version",
            "release",
            "epoch",
            "arch",
            "checksums",
            "repoid",
            "pkgtup",
        ]
        Package = namedtuple("Package", package_attrs)
        mocked_packs = [
            Package(
                "n1",
                "v1",
                "r1",
                "e1",
                "a1",
                [("c1", "cs")],
                "rid1",
                ("n1", "a1", "e1", "v1", "r1"),
            ),
            Package(
                "n2",
                "v2",
                "r2",
                "e2",
                "a2",
                [("c2", "cs")],
                "rid2",
                ("n2", "a2", "e2", "v2", "r2"),
            ),
        ]

        os.path.isfile = Mock(return_value=True)
        solv.Pool = Mock()

        listed_packages = cs.list_packages(filters=None, latest=False)

        self.assertEqual(len(listed_packages), 2)
        for pack, mocked_pack in zip(listed_packages, mocked_packs):
            # listed_packages should return ContentPackages
            self.assertTrue(isinstance(pack, ContentPackage))

            # all the attributes should be rightly imported from yum's
            # returnPackages which we've mocked above
            for attr in package_attrs:
                if attr == "checksums":
                    # checksums are transformed by list_packages from
                    # yum's list of tuples to ContentSource's dictionary
                    self.assertEqual(
                        pack.checksums,
                        {mocked_pack.checksums[0][0]: mocked_pack.checksums[0][1]},
                    )
                elif attr in ["repoid", "pkgtup"]:
                    continue
                else:
                    self.assertEqual(getattr(pack, attr), getattr(mocked_pack, attr))

    @unittest.skip
    def test_get_updates_suse_patches(self):
        cs = self._make_dummy_cs()

        patches_xml = StringIO(
            """<?xml version="1.0" encoding="UTF-8"?>
                <patches xmlns="http://novell.com/package/metadata/suse/patches">
                  <patch id="smcl3-cobbler-7778">
                    <checksum type="sha">ec34048ebda707a83190056d832d43c9fbb55ca6</checksum>
                    <location href="/patch-smcl3-cobbler-7778.xml"/>
                    <someother>weird element</someother>
                  </patch>
                  <patch id="smcl3-code11-update-stack-7779">
                    <checksum type="sha">51a736a468ebf53d7a4084cf0ca72a87427cdeba</checksum>
                    <location href="/patch-smcl3-code11-update-stack-7779.xml"/>
                  </patch>
                </patches>
                """
        )
        # pylint: disable-next=protected-access
        cs._md_exists = Mock(side_effect=[False, True, True])
        # pylint: disable-next=protected-access
        cs._retrieve_md_path = Mock(return_value="patches.xml")
        # pylint: disable-next=redefined-builtin,unused-variable
        open = Mock(return_value=patches_xml)
        os.path.join = Mock(side_effect=lambda *args: StringIO("<xml></xml>"))

        patches = cs.get_updates()
        self.assertEqual(patches[0], "patches")
        self.assertEqual(len(patches[1]), 2)

    @patch("spacewalk.common.rhnConfig.initCFG", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.os.unlink", Mock())
    @patch("urlgrabber.grabber.PyCurlFileObject", Mock())
    @patch("spacewalk.common.rhnLog", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.fileutils.makedirs", Mock())
    def test_minrate_timeout_config(self):
        # pylint: disable-next=invalid-name
        CFG = Mock()
        CFG.REPOSYNC_TIMEOUT = 42
        CFG.REPOSYNC_MINRATE = 42
        CFG.REPOSYNC_DOWNLOAD_THREADS = 42  # Throws ValueError if not defined
        CFG.MOUNT_POINT = ""
        CFG.PREPENDED_DIR = ""
        CFG.http_proxy = False

        urlgrabber_spy = Mock()
        urlgrabber_spy.urlread = Mock()
        mirror_group_mock = Mock(return_value=urlgrabber_spy)

        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.MirrorGroup",
            mirror_group_mock,
        ), patch("spacewalk.common.rhnConfig.CFG", CFG), patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.os.path.isfile",
            Mock(return_value=False),
        ):
            cs = yum_src.ContentSource("http://example.com/foo/", "test_repo", org="")
            cs.get_file("bar")

            self.assertEqual(urlgrabber_spy.urlread.call_args[1]["timeout"], 42)
            self.assertEqual(urlgrabber_spy.urlread.call_args[1]["minrate"], 42)

    @patch("spacewalk.common.rhnConfig.initCFG", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.os.unlink", Mock())
    @patch("urlgrabber.grabber.PyCurlFileObject", Mock())
    @patch("spacewalk.common.rhnLog", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.fileutils.makedirs", Mock())
    @patch(
        "spacewalk.satellite_tools.repo_plugins.yum_src.etree.parse",
        MagicMock(side_effect=Exception),
    )
    def test_get_file_with_mirrorlist_repo(self):
        cs = self._make_dummy_cs()
        cs.url = "http://example.com/url_with_mirrorlist/"

        urlgrabber_spy = Mock()
        urlgrabber_spy.urlread = Mock()
        mirror_group_mock = Mock(return_value=urlgrabber_spy)
        subprocess_mock = Mock()
        subprocess_mock.returncode = 0
        subprocess_mock.stderr = False

        # pylint: disable-next=invalid-name
        MIRROR_LIST = [
            "http://example/base/arch1/os/",
            "http://example/",
            "http://example.com/",
            "https://example.org/repo/path/?token",
        ]

        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.MirrorGroup",
            mirror_group_mock,
        ), patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.os.path.isfile",
            Mock(return_value=False),
        ), patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.ZyppoSync._init_root",
            MagicMock(),
        ), patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.ContentSource._get_mirror_list",
            MagicMock(return_value=MIRROR_LIST),
        ), patch.object(
            yum_src.ZyppoSync, "_ZyppoSync__synchronize_gpg_keys", MagicMock()
        ), patch(
            "builtins.open", mock_open()
        ), patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.subprocess.run",
            MagicMock(return_value=subprocess_mock),
        ):
            repo = yum_src.ZypperRepo(
                tempfile.mkdtemp(), "http://example.com/url_with_mirrorlist/", "1"
            )
            cs.repo = repo
            cs.setup_repo(repo)
            cs.get_file("foobar")
            self.assertEqual(urlgrabber_spy.urlread.call_args[0][0], "foobar")
            self.assertEqual(
                urlgrabber_spy.urlread.call_args.kwargs["urls"], MIRROR_LIST
            )

    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.ZYPP_RAW_CACHE_PATH", "./")
    def test_get_comps_and_modules(self):
        cs = self._make_dummy_cs()

        comps = cs.get_groups()
        self.assertTrue(
            comps.endswith(
                "751019aa91884285a99d1a62a8c653a3ce41fb4e235f11077c3de52925e16ef7-comps-AppStream.x86_64.xml"
            ),
            # pylint: disable-next=consider-using-f-string
            msg="Expected: ends with ...'{}' but got '{}'".format(
                "751019aa91884285a99d1a62a8c653a3ce41fb4e235f11077c3de52925e16ef7-comps-AppStream.x86_64.xml",
                comps,
            ),
        )
        modules = cs.get_modules()
        self.assertTrue(
            modules.endswith(
                "2c3714db39642790c8a1922c6cae04e7b95af59b234af60f15778d5550e3a546-modules.yaml.gz"
            )
        )

    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.initCFG", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.os.unlink", Mock())
    @patch("urlgrabber.grabber.PyCurlFileObject", Mock())
    @patch("spacewalk.common.rhnLog", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.fileutils.makedirs", Mock())
    @patch(
        "spacewalk.satellite_tools.repo_plugins.yum_src.etree.parse",
        MagicMock(side_effect=Exception),
    )
    def test_mirror_list_arch(self):
        cs = self._make_dummy_cs()
        fake_mirrorlist_file = self.tmpdir + "/mirrorlist.txt"
        self.repo.root = self.tmpdir
        cs.channel_arch = "arch1"
        grabber_mock = Mock()
        mirror_request_mock = Mock()
        mirror_request_mock.return_value.headers = {"Content-Type": "text/plain"}
        nonmirror_request_mock = Mock()
        nonmirror_request_mock.return_value.headers = {"Content-Type": "text/html"}

        # If webpage is plaintext format, check list of mirrors is returned
        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.requests.get",
            mirror_request_mock,
        ):
            with patch(
                "spacewalk.satellite_tools.repo_plugins.yum_src.urlgrabber.urlgrab",
                grabber_mock,
            ):
                # pylint: disable-next=unspecified-encoding
                with open(fake_mirrorlist_file, "w") as fake_list:
                    fake_list.writelines(
                        [
                            "http://host1/base/$basearch/os/\n",
                            "http://host2/base/$BASEARCH/os/\n",
                            "http://host3/base/$ARCH/os/\n",
                        ]
                    )
                # pylint: disable-next=protected-access
                mirrors = cs._get_mirror_list(self.repo, "https://fake/repo/url")
                self.assertEqual(
                    mirrors,
                    [
                        "http://host1/base/arch1/os/",
                        "http://host2/base/arch1/os/",
                        "http://host3/base/arch1/os/",
                    ],
                )

        # If webpage is html format, and so not mirrorlist, check list of 'mirrors' is blank
        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.requests.get",
            nonmirror_request_mock,
        ):
            with patch(
                "spacewalk.satellite_tools.repo_plugins.yum_src.urlgrabber.urlgrab",
                grabber_mock,
            ):
                # pylint: disable-next=unspecified-encoding
                with open(fake_mirrorlist_file, "w") as fake_list:
                    fake_list.writelines(
                        [
                            "http://host1/base/$basearch/os/\n",
                            "http://host2/base/$BASEARCH/os/\n",
                            "http://host3/base/$ARCH/os/\n",
                        ]
                    )
                # pylint: disable-next=protected-access
                mirrors = cs._get_mirror_list(self.repo, "https://fake/repo/url")
                self.assertEqual(mirrors, [])

        # If mirrorlist contains invalid repos, check they are discarded
        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.requests.get",
            mirror_request_mock,
        ):
            with patch(
                "spacewalk.satellite_tools.repo_plugins.yum_src.urlgrabber.urlgrab",
                grabber_mock,
            ):
                # pylint: disable-next=unspecified-encoding
                with open(fake_mirrorlist_file, "w") as fake_list:
                    fake_list.writelines(
                        [
                            "http://host1/base/$basearch/os/\n",
                            "http://NOPE/<fdsfsk>\n"
                            "http://host2/base/$BASEARCH/os/\n",
                            "http://[{fdsfkjnn`fds/\n",
                            "http://host3/base/$ARCH/os/\n",
                        ]
                    )
                # pylint: disable-next=protected-access
                mirrors = cs._get_mirror_list(self.repo, "https://fake/repo/url")
                self.assertEqual(
                    mirrors,
                    [
                        "http://host1/base/arch1/os/",
                        "http://host2/base/arch1/os/",
                        "http://host3/base/arch1/os/",
                    ],
                )

    def test_get_mediaproduct_no_logging_when_local_file_not_found(self):
        cs = self._make_dummy_cs()
        urlgrab_exc = URLGrabError()
        urlgrab_exc.errno = 2
        grabber_mock = Mock(side_effect=urlgrab_exc)
        log_mock = Mock()
        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.urlgrabber.urlgrab",
            grabber_mock,
        ), patch("spacewalk.satellite_tools.repo_plugins.yum_src.log", log_mock):
            assert not cs.get_mediaproducts()
            log_mock.assert_not_called()

    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.initCFG", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.os.unlink", Mock())
    @patch("urlgrabber.grabber.PyCurlFileObject", Mock())
    @patch("spacewalk.common.rhnLog", Mock())
    @patch("spacewalk.satellite_tools.repo_plugins.yum_src.fileutils.makedirs", Mock())
    @patch(
        "spacewalk.satellite_tools.repo_plugins.yum_src.etree.parse",
        MagicMock(side_effect=Exception),
    )
    def test_proxy_usage_with_mirrorlist(self):
        cs = self._make_dummy_cs()
        fake_mirrorlist_file = self.tmpdir + "/mirrorlist.txt"
        self.repo.root = self.tmpdir
        proxy_url = "http://proxy.example.com:8080"
        proxy_user = "user"
        proxy_pass = "pass"
        cs.proxy_hostname = proxy_url
        cs.proxy_user = proxy_user
        cs.proxy_pass = proxy_pass
        expected_url_list = []
        url_list = [
            "http://example/base/arch1/os/",
            "http://example/",
            "http://example.com/",
            "https://example.org/repo/path/?token",
        ]
        for url in url_list:
            if "?" in url:
                separator = "&"
            else:
                separator = "?"
            expected_url_list.append(
                # pylint: disable-next=consider-using-f-string
                "{}{}proxy={}&proxyuser={}&proxypass={}".format(
                    url, separator, quote(proxy_url), proxy_user, proxy_pass
                )
            )
        grabber_mock = Mock()
        mirror_request_mock = Mock()
        mirror_request_mock.return_value.headers = {"Content-Type": "text/plain"}

        with patch(
            "spacewalk.satellite_tools.repo_plugins.yum_src.requests.get",
            mirror_request_mock,
        ):
            with patch(
                "spacewalk.satellite_tools.repo_plugins.yum_src.urlgrabber.urlgrab",
                grabber_mock,
            ):
                # pylint: disable-next=unspecified-encoding
                with open(fake_mirrorlist_file, "w") as fake_list:
                    fake_list.writelines(
                        [
                            "http://example/base/arch1/os/\n",
                            "http://example/\n",
                            "http://example.com/\n",
                            "https://example.org/repo/path/?token\n",
                        ]
                    )
                # pylint: disable-next=protected-access
                mirrors = cs._get_mirror_list(self.repo, "https://fake/repo/url")

                self.assertEqual(mirrors, expected_url_list)

    def test_prep_zypp_repo_url_with_proxy(self):
        cs = self._make_dummy_cs()
        urls = [
            ("http://example.com/", False),
            ("https://example.com/", False),
            ("https://example.org/repo/path/?token", False),
            ("uln://example.com/", True),
            ("uln:///channel", True),
            ("https://user:password@example.org/repo/path/", False),
        ]
        proxy_url = "http://proxy.example.com:8080"
        proxy_user = "user"
        proxy_pass = "pass"
        cs.proxy_hostname = proxy_url
        cs.proxy_user = proxy_user
        cs.proxy_pass = proxy_pass
        cs.channel_label = "testchannel"

        for url, is_uln in urls:
            if is_uln or "?" in url:
                separator = "&"
            else:
                separator = "?"

            exp_url = url
            parsed_url = urlparse(url)
            extra_query = ""
            if parsed_url.username and parsed_url.password:
                netloc = parsed_url.hostname
                if parsed_url.port:
                    # pylint: disable-next=consider-using-f-string
                    netloc = "{1}:{2}".format(netloc, parsed_url.port)

                exp_url = urlunparse(
                    (
                        parsed_url.scheme,
                        netloc,
                        parsed_url.path,
                        parsed_url.params,
                        parsed_url.query,
                        parsed_url.fragment,
                    )
                )
                # pylint: disable-next=consider-using-f-string
                extra_query = "&credentials={}".format(cs.channel_label)

            # pylint: disable-next=consider-using-f-string
            expected_url = "{}{}proxy={}&proxyuser={}&proxypass={}{}".format(
                exp_url,
                separator,
                quote(proxy_url),
                proxy_user,
                proxy_pass,
                extra_query,
            )
            with patch("builtins.open", mock_open()), patch(
                "spacewalk.satellite_tools.repo_plugins.yum_src.os.chmod", Mock()
            ):
                # pylint: disable-next=protected-access
                comp_url = cs._prep_zypp_repo_url(url, is_uln)

                assert expected_url == comp_url

    def test_update_notice_parse(self):
        update_notice_xml = StringIO(
            """<?xml version="1.0" encoding="UTF-8"?>
<updates>
    <update from="exampleProvider" type="security" status="stable" version="1">
        <id>exampleProvider-test-1</id>
        <title>Testing Update Notice</title>
        <severity>Moderate</severity>
        <release>exampleProvider</release>
        <issued date="2022-01-16"></issued>
        <references>
            <reference href="https://nvd.nist.gov/vuln/detail/CVE-0000-01234" id="CVE-0000-01234" title="CVE-0000-01234" type="cve"></reference>
        </references>
        <description>Testing Update Notice</description>
        <pkglist>
            <collection>
                <name>exampleProvider</name>
                <package arch="noarch" name="testing" release="1" version="1.0.0">
                    <filename>testing-1.0.0-1.noarch.rpm</filename>
                </package>
            </collection>
        </pkglist>
    </update>
    <update from="exampleProvider" type="security" status="stable">
        <id>exampleProvider-test-0</id>
        <title>Testing Update Notice</title>
        <severity>Moderate</severity>
        <release>exampleProvider</release>
        <issued date="2022-01-16"></issued>
        <references>
            <reference href="https://nvd.nist.gov/vuln/detail/CVE-0000-12345" id="CVE-0000-12345" title="CVE-0000-12345" type="cve"></reference>
        </references>
        <description>Testing Update Notice</description>
        <pkglist>
            <collection>
                <name>exampleProvider</name>
                <package arch="noarch" name="testing0" release="1" version="1.0.0">
                    <filename>testing0-1.0.0-1.noarch.rpm</filename>
                </package>
            </collection>
        </pkglist>
    </update>
</updates>
            """
        )
        expected_list = [
            {"update_id": "exampleProvider-test-1", "version": "1"},
            {"update_id": "exampleProvider-test-0", "version": "0"},
        ]
        for un_elem in etree.parse(update_notice_xml).getroot():
            expected = expected_list.pop(0)
            un = UpdateNotice(un_elem)
            for un_key in expected.keys():
                self.assertEqual(un[un_key], expected[un_key])
