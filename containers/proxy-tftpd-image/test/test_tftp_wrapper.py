#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

import sys
from unittest.mock import MagicMock


# Define fake base classes
class FakeResponseData:
    pass


class FakeBaseHandler:
    def __init__(self, server_addr, peer, path, options, stats):
        self._path = path
        self._stats = MagicMock()
        pass

    def run(self):
        pass


class FakeBaseServer:
    def __init__(self, *args, **kwargs):
        pass


# Mock dependencies
fbtftp_mock = MagicMock()
fbtftp_base_handler_mock = MagicMock()
fbtftp_base_handler_mock.ResponseData = FakeResponseData
fbtftp_base_handler_mock.BaseHandler = FakeBaseHandler
fbtftp_base_server_mock = MagicMock()
fbtftp_base_server_mock.BaseServer = FakeBaseServer

sys.modules["fbtftp"] = fbtftp_mock
sys.modules["fbtftp.base_handler"] = fbtftp_base_handler_mock
sys.modules["fbtftp.base_server"] = fbtftp_base_server_mock
sys.modules["fbtftp.constants"] = MagicMock()
sys.modules["requests"] = MagicMock()
sys.modules["yaml"] = MagicMock()

import unittest
import os

# Add the directory containing tftp_wrapper.py to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
import tftp_wrapper


class TestTftpWrapper(unittest.TestCase):

    def setUp(self):
        self.proxy_fqdn_1234 = "proxy1234.example.com"
        self.proxy_fqdn_abcd = "proxyABCD.example.com"
        self.server_fqdn = "server.example.com"
        self.replace_fqdns = ["other.example.com"]

        # Load examples
        with open(os.path.join(os.path.dirname(__file__), "pxe-example.cfg"), "r") as f:
            self.pxe_example = f.read()
        with open(
            os.path.join(os.path.dirname(__file__), "grub-example.cfg"), "r"
        ) as f:
            self.grub_example = f.read()

    def test_pxe_filter_saltboot_match(self):
        with unittest.mock.patch("tftp_wrapper.requests.get") as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.content = self.pxe_example.encode("utf-8")
            mock_get.return_value = mock_response

            pxe_filter = tftp_wrapper.HttpResponseDataFilteredPXE(
                "http://localhost/tftp/pxelinux.cfg/01-mac",
                None,
                self.proxy_fqdn_1234,
                self.server_fqdn,
                self.replace_fqdns,
            )

            filtered_content = pxe_filter._content.decode("utf-8")
            # Should contain the matched saltboot entry
            self.assertIn("LABEL 1234:S:1:Organization", filtered_content)
            self.assertIn("MENU DEFAULT", filtered_content)
            self.assertIn("ONTIMEOUT 1234:S:1:Organization", filtered_content)
            # Should NOT contain other saltboot entries
            self.assertNotIn("LABEL ABCD:S:1:Organization", filtered_content)
            # Should NOT contain cobbler entries
            self.assertNotIn("LABEL profile:1:MyOrganizationInc", filtered_content)

    def test_pxe_filter_cobbler_fallback(self):
        with unittest.mock.patch("tftp_wrapper.requests.get") as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.content = self.pxe_example.encode("utf-8")
            mock_get.return_value = mock_response

            pxe_filter = tftp_wrapper.HttpResponseDataFilteredPXE(
                "http://localhost/tftp/pxelinux.cfg/01-mac",
                None,
                "nonexistent-proxy.example.com",
                self.server_fqdn,
                self.replace_fqdns,
            )

            filtered_content = pxe_filter._content.decode("utf-8")
            # Should contain filtered cobbler entries
            self.assertIn("LABEL profile:1:MyOrganizationInc", filtered_content)
            self.assertIn(
                "http://nonexistent-proxy.example.com/cblr/svc/op/autoinstall",
                filtered_content,
            )
            self.assertNotIn(self.server_fqdn, filtered_content)
            # Should contain other saltboot entries as filtered (but they won't match as saltboot)
            self.assertIn("LABEL 1234:S:1:Organization", filtered_content)

    def test_grub_filter_saltboot_match(self):
        with unittest.mock.patch("tftp_wrapper.requests.get") as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.content = self.grub_example.encode("utf-8")
            mock_get.return_value = mock_response

            grub_filter = tftp_wrapper.HttpResponseDataFilteredGrub(
                "http://localhost/tftp/grub/system",
                None,
                self.proxy_fqdn_abcd,
                self.server_fqdn,
                self.replace_fqdns,
            )

            filtered_content = grub_filter._content.decode("utf-8")
            entry_name = "'ABCD:S:1:Organization'"

            # Should contain matched saltboot entry with ID
            self.assertIn(f"menuentry {entry_name}", filtered_content)
            self.assertIn("--id ", filtered_content)
            self.assertIn(
                f"--id {tftp_wrapper.DEFAULT_ENTRY_IDENTIFIER}", filtered_content
            )
            self.assertIn(
                f"set default={tftp_wrapper.DEFAULT_ENTRY_IDENTIFIER}", filtered_content
            )
            # Should NOT contain other saltboot entries
            self.assertNotIn("menuentry '1234:S:1:Organization'", filtered_content)
            # Should NOT contain cobbler entries
            self.assertNotIn(
                "menuentry 'profile:1:MyOrganizationInc'", filtered_content
            )

    def test_grub_filter_cobbler_fallback(self):
        with unittest.mock.patch("tftp_wrapper.requests.get") as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.content = self.grub_example.encode("utf-8")
            mock_get.return_value = mock_response

            grub_filter = tftp_wrapper.HttpResponseDataFilteredGrub(
                "http://localhost/tftp/grub/system",
                None,
                "nonexistent-proxy.example.com",
                self.server_fqdn,
                self.replace_fqdns,
            )

            filtered_content = grub_filter._content.decode("utf-8")
            # Should contain filtered cobbler entries
            self.assertIn("menuentry 'profile:1:MyOrganizationInc'", filtered_content)
            self.assertIn(
                "http://nonexistent-proxy.example.com/cblr/svc/op/autoinstall",
                filtered_content,
            )
            self.assertNotIn(self.server_fqdn, filtered_content)

    def test_handler_routing(self):
        handler = tftp_wrapper.TFTPHandler(
            "127.0.0.1",
            "127.0.0.2",
            "pxelinux.cfg/01-mac",
            {},
            "/root",
            "http://localhost",
            self.proxy_fqdn_1234,
            self.server_fqdn,
            None,
            self.replace_fqdns,
        )

        with unittest.mock.patch(
            "tftp_wrapper.HttpResponseDataFilteredPXE"
        ) as mock_pxe:
            handler.get_response_data_delayed()
            mock_pxe.assert_called_once()
            self.assertEqual(
                mock_pxe.call_args[0][0], "http://localhost/tftp/pxelinux.cfg/01-mac"
            )

        handler._path = "grub/system"
        with unittest.mock.patch(
            "tftp_wrapper.HttpResponseDataFilteredGrub"
        ) as mock_grub:
            handler.get_response_data_delayed()
            mock_grub.assert_called_once()
            self.assertEqual(
                mock_grub.call_args[0][0], "http://localhost/tftp/grub/system"
            )

        handler._path = "other/file"
        with unittest.mock.patch("tftp_wrapper.HttpResponseData") as mock_http:
            handler.get_response_data_delayed()
            mock_http.assert_called_once()
            self.assertEqual(
                mock_http.call_args[0][0], "http://localhost/tftp/other/file"
            )

    def test_http_response_data_read(self):
        url = "http://localhost/tftp/file"
        with unittest.mock.patch("tftp_wrapper.requests.get") as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.headers = {"content-length": "10"}
            mock_response.iter_content.return_value = iter(
                [b"abc", b"def", b"ghi", b"j"]
            )
            mock_get.return_value = mock_response

            resp = tftp_wrapper.HttpResponseData(url, None)

            self.assertEqual(resp.read(2), b"ab")
            self.assertEqual(resp._content, b"c")
            self.assertEqual(resp.read(5), b"cdefg")
            self.assertEqual(resp._content, b"hi")
            self.assertEqual(resp.read(10), b"hij")
            self.assertEqual(resp._content, b"")


if __name__ == "__main__":
    unittest.main()
