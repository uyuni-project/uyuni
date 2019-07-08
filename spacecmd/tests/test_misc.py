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

    def test_get_session_missing(self, shell):
        """
        Test handling missing current user session.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.logging", logger) as lgr:
            shell.session = None
            spacecmd.misc.do_get_session(shell, "")

        assert not mprint.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "No session found")

    # No test for listing history, as it is just lister from the readline.

    def test_do_toggle_confirmations(self, shell):
        """
        Test confirmation messages toggle

        :param shell:
        :return:
        """
        mprint = MagicMock()
        shell.options.yes = True
        with patch("spacecmd.misc.print", mprint) as prt:
            spacecmd.misc.do_toggle_confirmations(shell, "")
            spacecmd.misc.do_toggle_confirmations(shell, "")

        assert_args_expect(mprint.call_args_list,
                           [(("Confirmation messages are", "enabled"), {}),
                            (("Confirmation messages are", "disabled"), {})])

    def test_login_already_logged_in(self, shell):
        """
        Test login (already logged in).

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        gpass = MagicMock()
        mkd = MagicMock()
        shell.config = {}
        shell.conf_dir = "/tmp"
        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not shell.load_config_section.called
        assert not logger.debug.called
        assert not logger.error.called
        assert not logger.info.called
        assert not shell.client.user.listAssignableRoles.called
        assert not shell.client.auth.login.called
        assert not gpass.called
        assert not prompter.called
        assert not mprint.called
        assert not mkd.called
        assert not shell.load_caches.called
        assert out
        assert logger.warning.called

        assert_expect(logger.warning.call_args_list,
                      "You are already logged in")

    def test_login_no_server_specified(self, shell):
        """
        Test login without specified server.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        gpass = MagicMock()
        mkd = MagicMock()

        shell.session = None
        shell.config = {}
        shell.conf_dir = "/tmp"

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not shell.load_config_section.called
        assert not logger.debug.called
        assert not logger.error.called
        assert not logger.info.called
        assert not shell.client.user.listAssignableRoles.called
        assert not shell.client.auth.login.called
        assert not gpass.called
        assert not prompter.called
        assert not mprint.called
        assert not mkd.called
        assert not shell.load_caches.called
        assert not out
        assert logger.warning.called

        assert_expect(logger.warning.call_args_list,
                      "No server specified")

    def test_login_connection_error(self, shell):
        """
        Test handling connection error at login.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        gpass = MagicMock()
        mkd = MagicMock()

        client = MagicMock()
        rpc_server = MagicMock(return_value=client)

        shell.session = None
        shell.options.debug = 2
        shell.config = {"server": "no.mans.land"}
        shell.conf_dir = "/tmp"
        client.api.getVersion = MagicMock(side_effect=Exception("Insert coin"))

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.xmlrpclib.Server", rpc_server) as rpcs, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not logger.info.called
        assert not client.user.listAssignableRoles.called
        assert not client.auth.login.called
        assert not gpass.called
        assert not prompter.called
        assert not mprint.called
        assert not mkd.called
        assert not shell.load_caches.called
        assert not out
        assert not logger.warning.called
        assert shell.load_config_section.called
        assert logger.debug.called
        assert logger.error.called
        assert shell.client is None

        assert_args_expect(logger.error.call_args_list,
                           [(('Failed to connect to %s', 'https://no.mans.land/rpc/api'), {})])
        assert_args_expect(logger.debug.call_args_list,
                           [(('Connecting to %s', 'https://no.mans.land/rpc/api'), {}),
                            (('Error while connecting to the server %s: %s',
                              'https://no.mans.land/rpc/api', 'Insert coin'), {})])

    def test_login_api_version_mismatch(self, shell):
        """
        Test handling API version mismatch error at login.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        gpass = MagicMock()
        mkd = MagicMock()

        client = MagicMock()
        rpc_server = MagicMock(return_value=client)

        shell.session = None
        shell.options.debug = 2
        shell.config = {"server": "no.mans.land"}
        shell.conf_dir = "/tmp"
        shell.MINIMUM_API_VERSION = 10.8
        client.api.getVersion = MagicMock(return_value=1.5)

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.xmlrpclib.Server", rpc_server) as rpcs, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not logger.info.called
        assert not client.user.listAssignableRoles.called
        assert not client.auth.login.called
        assert not gpass.called
        assert not prompter.called
        assert not mprint.called
        assert not mkd.called
        assert not shell.load_caches.called
        assert not out
        assert not logger.warning.called
        assert shell.load_config_section.called
        assert logger.debug.called
        assert logger.error.called
        assert shell.client is None

        assert_args_expect(logger.error.call_args_list,
                           [(('API (%s) is too old (>= %s required)', 1.5, 10.8), {})])

    @patch("spacecmd.misc.os.path.isfile", MagicMock(return_value=True))
    def test_login_reuse_cached_session(self, shell):
        """
        Test handling cached available session.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        gpass = MagicMock()
        mkd = MagicMock()

        client = MagicMock()
        rpc_server = MagicMock(return_value=client)

        shell.session = None
        shell.options.debug = 2
        shell.options.password = None
        shell.options.username = None
        shell.config = {"server": "no.mans.land"}
        shell.conf_dir = "/tmp"
        shell.MINIMUM_API_VERSION = 10.8
        client.api.getVersion = MagicMock(return_value=11.5)

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.xmlrpclib.Server", rpc_server) as rpcs, \
            patch("spacecmd.misc.open", new_callable=mock_open,
                  read_data="bofh:5adf5cc50929f71a899b81c2c2eb0979") as fmk, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not client.auth.login.called
        assert not gpass.called
        assert not prompter.called
        assert not mprint.called
        assert not mkd.called
        assert not logger.warning.called
        assert not logger.error.called
        assert logger.info.called
        assert client.user.listAssignableRoles.called
        assert shell.load_caches.called
        assert shell.load_config_section.called
        assert logger.debug.called
        assert shell.client is not None
        assert shell.session == "5adf5cc50929f71a899b81c2c2eb0979"
        assert shell.current_user == "bofh"
        assert shell.server == "no.mans.land"
        assert out

        assert_args_expect(logger.info.call_args_list,
                           [(('Connected to %s as %s', 'https://no.mans.land/rpc/api', 'bofh'), {})])
        assert_args_expect(logger.debug.call_args_list,
                           [(('Connecting to %s', 'https://no.mans.land/rpc/api'), {}),
                            (('Server API Version = %s', 11.5), {}),
                            (('Using cached credentials from %s', '/tmp/no.mans.land/session'), {})])

    @patch("spacecmd.misc.os.path.isfile", MagicMock(return_value=False))
    def test_login_no_cached_session_opt_pwd(self, shell):
        """
        Test create new session, password from the options.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(return_value="bofh")
        gpass = MagicMock(return_value=None)
        mkd = MagicMock()
        file_writer = MagicMock()

        client = MagicMock()
        rpc_server = MagicMock(return_value=client)

        shell.session = None
        shell.options.debug = 2
        shell.options.password = "foobar"
        shell.options.username = None
        shell.config = {"server": "no.mans.land"}
        shell.conf_dir = "/tmp"
        shell.MINIMUM_API_VERSION = 10.8
        client.api.getVersion = MagicMock(return_value=11.5)
        client.auth.login = MagicMock(return_value="5adf5cc50929f71a899b81c2c2eb0979")

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.xmlrpclib.Server", rpc_server) as rpcs, \
            patch("spacecmd.misc.open", file_writer) as fmk, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not gpass.called
        assert not mprint.called
        assert not logger.warning.called
        assert not logger.error.called
        assert not client.user.listAssignableRoles.called
        assert client.auth.login.called
        assert prompter.called
        assert logger.info.called
        assert shell.load_caches.called
        assert shell.load_config_section.called
        assert logger.debug.called
        assert shell.client is not None
        assert shell.session == "5adf5cc50929f71a899b81c2c2eb0979"
        assert shell.current_user == "bofh"
        assert shell.server == "no.mans.land"
        assert out
        assert mkd.called

        assert_args_expect(client.auth.login.call_args_list,
                           [(('bofh', "foobar"), {})])
        assert_args_expect(mkd.call_args_list,
                           [(('/tmp/no.mans.land', 448), {})])
        assert_args_expect(shell.load_caches.call_args_list,
                           [(('no.mans.land', 'bofh'), {})])
        assert_args_expect(logger.debug.call_args_list,
                           [(('Connecting to %s', 'https://no.mans.land/rpc/api'), {}),
                            (('Server API Version = %s', 11.5), {})])
        assert_args_expect(logger.info.call_args_list,
                           [(('Connected to %s as %s', 'https://no.mans.land/rpc/api', 'bofh'), {})])
