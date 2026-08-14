#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

"""Unit tests for the TFTP wrapper script."""

# pylint: disable=redefined-outer-name,protected-access

import sys
from unittest.mock import MagicMock, patch

# Dynamically mock fbtftp if not installed
try:
    import fbtftp  # pylint: disable=unused-import
except ImportError:

    class FakeResponseData:
        pass

    class FakeBaseHandler:
        # pylint: disable=unused-argument
        def __init__(self, server_addr, peer, path, options, stats):
            self._path = path
            self._stats = MagicMock()

        def run(self):
            pass

    class FakeBaseServer:
        def __init__(self, *args, **kwargs):
            pass

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

# Dynamically mock requests if not installed
try:
    import requests  # pylint: disable=unused-import
except ImportError:
    sys.modules["requests"] = MagicMock()

# Dynamically mock yaml if not installed
try:
    import yaml  # pylint: disable=unused-import
except ImportError:
    sys.modules["yaml"] = MagicMock()

import os
import pytest

# Add the directory containing tftp_wrapper.py to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
# pylint: disable=wrong-import-position
import tftp_wrapper


@pytest.fixture
def config_data():
    return {
        "proxy_fqdn_1234": "proxy1234.example.com",
        "proxy_fqdn_abcd": "proxyABCD.example.com",
        "server_fqdn": "server.example.com",
        "replace_fqdns": ["other.example.com"],
    }


@pytest.fixture
def pxe_example():
    with open(
        os.path.join(os.path.dirname(__file__), "pxe-example.cfg"),
        "r",
        encoding="utf-8",
    ) as f:
        return f.read()


@pytest.fixture
def grub_example():
    with open(
        os.path.join(os.path.dirname(__file__), "grub-example.cfg"),
        "r",
        encoding="utf-8",
    ) as f:
        return f.read()


def test_pxe_filter_saltboot_match(config_data, pxe_example):
    with patch("tftp_wrapper.requests.get") as mock_get:
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.content = pxe_example.encode("utf-8")
        mock_get.return_value = mock_response

        pxe_filter = tftp_wrapper.HttpResponseDataFilteredPXE(
            "http://localhost/tftp/pxelinux.cfg/01-mac",
            None,
            config_data["proxy_fqdn_1234"],
            config_data["server_fqdn"],
            config_data["replace_fqdns"],
        )

        filtered_content = pxe_filter._content.decode("utf-8")
        # Should contain the matched saltboot entry
        assert "LABEL 1234:S:1:Organization" in filtered_content
        assert "MENU DEFAULT" in filtered_content
        assert "ONTIMEOUT 1234:S:1:Organization" in filtered_content
        # Should NOT contain other saltboot entries
        assert "LABEL ABCD:S:1:Organization" not in filtered_content
        # Should NOT contain cobbler entries
        assert "LABEL profile:1:MyOrganizationInc" not in filtered_content


def test_pxe_filter_cobbler_fallback(config_data, pxe_example):
    with patch("tftp_wrapper.requests.get") as mock_get:
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.content = pxe_example.encode("utf-8")
        mock_get.return_value = mock_response

        pxe_filter = tftp_wrapper.HttpResponseDataFilteredPXE(
            "http://localhost/tftp/pxelinux.cfg/01-mac",
            None,
            "nonexistent-proxy.example.com",
            config_data["server_fqdn"],
            config_data["replace_fqdns"],
        )

        filtered_content = pxe_filter._content.decode("utf-8")
        # Should contain filtered cobbler entries
        assert "LABEL profile:1:MyOrganizationInc" in filtered_content
        assert (
            "http://nonexistent-proxy.example.com/cblr/svc/op/autoinstall"
            in filtered_content
        )
        assert config_data["server_fqdn"] not in filtered_content
        # Should contain other saltboot entries as filtered (but they won't match as saltboot)
        assert "LABEL 1234:S:1:Organization" in filtered_content


def test_grub_filter_saltboot_match(config_data, grub_example):
    with patch("tftp_wrapper.requests.get") as mock_get:
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.content = grub_example.encode("utf-8")
        mock_get.return_value = mock_response

        grub_filter = tftp_wrapper.HttpResponseDataFilteredGrub(
            "http://localhost/tftp/grub/system",
            None,
            config_data["proxy_fqdn_abcd"],
            config_data["server_fqdn"],
            config_data["replace_fqdns"],
        )

        filtered_content = grub_filter._content.decode("utf-8")
        entry_name = "'ABCD:S:1:Organization'"

        # Should contain matched saltboot entry with ID
        assert f"menuentry {entry_name}" in filtered_content
        assert "--id " in filtered_content
        assert f"--id {tftp_wrapper.DEFAULT_ENTRY_IDENTIFIER}" in filtered_content
        assert (
            f"set default={tftp_wrapper.DEFAULT_ENTRY_IDENTIFIER}" in filtered_content
        )
        # Should NOT contain other saltboot entries
        assert "menuentry '1234:S:1:Organization'" not in filtered_content
        # Should NOT contain cobbler entries
        assert "menuentry 'profile:1:MyOrganizationInc'" not in filtered_content


def test_grub_filter_cobbler_fallback(config_data, grub_example):
    with patch("tftp_wrapper.requests.get") as mock_get:
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.content = grub_example.encode("utf-8")
        mock_get.return_value = mock_response

        grub_filter = tftp_wrapper.HttpResponseDataFilteredGrub(
            "http://localhost/tftp/grub/system",
            None,
            "nonexistent-proxy.example.com",
            config_data["server_fqdn"],
            config_data["replace_fqdns"],
        )

        filtered_content = grub_filter._content.decode("utf-8")
        # Should contain filtered cobbler entries
        assert "menuentry 'profile:1:MyOrganizationInc'" in filtered_content
        assert (
            "http://nonexistent-proxy.example.com/cblr/svc/op/autoinstall"
            in filtered_content
        )
        assert config_data["server_fqdn"] not in filtered_content


def test_handler_routing(config_data):
    handler = tftp_wrapper.TFTPHandler(
        "127.0.0.1",
        "127.0.0.2",
        "pxelinux.cfg/01-mac",
        {},
        "/root",
        "http://localhost",
        config_data["proxy_fqdn_1234"],
        config_data["server_fqdn"],
        None,
        config_data["replace_fqdns"],
    )

    with patch("tftp_wrapper.HttpResponseDataFilteredPXE") as mock_pxe:
        handler.get_response_data_delayed()
        mock_pxe.assert_called_once()
        assert mock_pxe.call_args[0][0] == "http://localhost/tftp/pxelinux.cfg/01-mac"

    handler._path = "grub/system"
    with patch("tftp_wrapper.HttpResponseDataFilteredGrub") as mock_grub:
        handler.get_response_data_delayed()
        mock_grub.assert_called_once()
        assert mock_grub.call_args[0][0] == "http://localhost/tftp/grub/system"

    handler._path = "other/file"
    with patch("tftp_wrapper.HttpResponseData") as mock_http:
        handler.get_response_data_delayed()
        mock_http.assert_called_once()
        assert mock_http.call_args[0][0] == "http://localhost/tftp/other/file"


def test_http_response_data_read():
    url = "http://localhost/tftp/file"
    with patch("tftp_wrapper.requests.get") as mock_get:
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.headers = {"content-length": "10"}
        mock_response.iter_content.return_value = iter([b"abc", b"def", b"ghi", b"j"])
        mock_get.return_value = mock_response

        resp = tftp_wrapper.HttpResponseData(url, None)

        assert resp.read(2) == b"ab"
        assert resp._content == b"c"
        assert resp.read(5) == b"cdefg"
        assert resp._content == b"hi"
        assert resp.read(10) == b"hij"
        assert resp._content == b""
