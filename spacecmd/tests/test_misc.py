# coding utf-8
"""
Test spacecmd.misc
"""
from unittest.mock import MagicMock, patch, mock_open
import pytest
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.misc
from xmlrpc import client as xmlrpclib
import os
import tempfile
import shutil
import datetime
import pickle
import hashlib
import time


class TestSCMisc:
    """
    Test suite for misc methods/funtions.
    """
    def test_clear_caches(self, shell):
        """
        Test clear caches.

        :param shell:
        :return:
        """
        shell.clear_system_cache = MagicMock()
        shell.clear_package_cache = MagicMock()
        shell.clear_errata_cache = MagicMock()

        spacecmd.misc.do_clear_caches(shell, "")

        assert shell.clear_system_cache.called
        assert shell.clear_package_cache.called
        assert shell.clear_errata_cache.called

    def test_get_api_version(self, shell):
        """
        Get API version.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        with patch("spacecmd.misc.print", mprint) as prt:
            spacecmd.misc.do_get_serverversion(shell, "")
        assert mprint.called
        assert shell.client.api.systemVersion.called

    def test_list_proxies(self, shell):
        """
        Test proxy listing.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        with patch("spacecmd.misc.print", mprint) as prt:
            spacecmd.misc.do_list_proxies(shell, "")
        assert mprint.called
        assert shell.client.satellite.listProxies.called

    def test_get_session(self, shell):
        """
        Test getting current user session.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.do_get_session(shell, "")

        assert not logger.error.called
        assert mprint.called
        assert_expect(mprint.call_args_list, shell.session)
