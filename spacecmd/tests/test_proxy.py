"""
Test suite for "proxy"
"""

from spacecmd import proxy

from unittest.mock import mock_open, patch

# pylint: disable-next=unused-import
from helpers import shell  # used by pytest
import pytest


@pytest.mark.parametrize(
    "args, calls_help",
    [
        (
            "proxy.lab server.lab 1024 proxy@acme.org root_ca.crt proxy.crt proxy.key",
            False,
        ),
        ("proxy.lab server.lab 1024 proxy@acme.org", True),
    ],
)
# pylint: disable-next=redefined-outer-name
def test_proxy_container_config_invokes_help_when_needed(shell, args, calls_help):

    m_open = mock_open()
    with patch("spacecmd.proxy.read_file", return_value=""), patch(
        "spacecmd.proxy.open", m_open
    ):
        proxy.do_proxy_container_config(shell, args)
    if calls_help:
        assert shell.help_proxy_container_config.called
    else:
        assert not shell.help_proxy_container_config.called


@pytest.mark.parametrize(
    "args, calls_help",
    [
        (
            "proxy.lab server.lab 1024 proxy@acme.org root_ca.crt proxy.crt proxy.key",
            True,
        ),
        ("proxy.lab server.lab 1024 proxy@acme.org", False),
    ],
)
def test_proxy_container_config_generate_cert_invokes_help_when_needed(
    # pylint: disable-next=redefined-outer-name
    shell,
    args,
    calls_help,
):

    m_open = mock_open()
    with patch("spacecmd.proxy.read_file", return_value=""), patch(
        "spacecmd.proxy.open", m_open
    ), patch("spacecmd.proxy.getpass.getpass", return_value="password"):
        proxy.do_proxy_container_config_generate_cert(shell, args)

    if calls_help:
        assert shell.help_proxy_container_config_generate_cert.called
    else:
        assert not shell.help_proxy_container_config_generate_cert.called
