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

    @patch("spacecmd.misc.os.path.isfile", MagicMock(return_value=False))
    def test_login_no_cached_session_cfg_pwd(self, shell):
        """
        Test create new session, password from the command line interface.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(return_value="bofh")
        gpass = MagicMock(return_value="foobar")
        mkd = MagicMock()
        file_writer = MagicMock()

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
        client.auth.login = MagicMock(return_value="5adf5cc50929f71a899b81c2c2eb0979")

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.xmlrpclib.Server", rpc_server) as rpcs, \
            patch("spacecmd.misc.open", file_writer) as fmk, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not mprint.called
        assert not logger.warning.called
        assert not logger.error.called
        assert not client.user.listAssignableRoles.called
        assert gpass.called
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

    @patch("spacecmd.misc.os.path.isfile", MagicMock(return_value=False))
    def test_login_no_cached_session_cli_pwd(self, shell):
        """
        Test create new session, password from the configuration.

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
        shell.options.password = None
        shell.options.username = None
        shell.config = {"server": "no.mans.land", "password": "foobar"}
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

    @patch("spacecmd.misc.os.path.isfile", MagicMock(return_value=False))
    def test_login_no_cached_session_bad_credentials(self, shell):
        """
        Test fail to create new session due to wrong credentials.

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
        shell.options.password = None
        shell.options.username = None
        shell.config = {"server": "no.mans.land", "password": "foobar"}
        shell.conf_dir = "/tmp"
        shell.MINIMUM_API_VERSION = 10.8
        client.api.getVersion = MagicMock(return_value=11.5)
        client.auth.login = MagicMock(side_effect=xmlrpclib.Fault(faultCode=42, faultString="Click harder"))

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
        assert logger.error.called
        assert not client.user.listAssignableRoles.called
        assert client.auth.login.called
        assert prompter.called
        assert not logger.info.called
        assert not shell.load_caches.called
        assert shell.load_config_section.called
        assert logger.debug.called
        assert shell.client is not None
        assert shell.session is None
        assert not out
        assert not mkd.called

        assert_args_expect(client.auth.login.call_args_list,
                           [(('bofh', "foobar"), {})])
        assert_args_expect(logger.debug.call_args_list,
                           [(('Connecting to %s', 'https://no.mans.land/rpc/api'), {}),
                            (('Server API Version = %s', 11.5), {}),
                            (('Login error: %s (%s)', 'Click harder', 42), {})])
        assert_args_expect(logger.error.call_args_list,
                           [(('Invalid credentials', ), {})])

    @patch("spacecmd.misc.os.path.isfile", MagicMock(return_value=False))
    def test_login_handle_cache_write_error_mkdir(self, shell):
        """
        Test fail to save session due to directory permission denial

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(return_value="bofh")
        gpass = MagicMock(return_value="foobar")
        mkd = MagicMock(side_effect=IOError("Intel inside"))
        file_writer = MagicMock()

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
        client.auth.login = MagicMock(return_value="5adf5cc50929f71a899b81c2c2eb0979")

        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.prompt_user", prompter) as pmt, \
            patch("spacecmd.misc.getpass", gpass) as gtp, \
            patch("spacecmd.misc.os.mkdir", mkd) as mkdr, \
            patch("spacecmd.misc.xmlrpclib.Server", rpc_server) as rpcs, \
            patch("spacecmd.misc.open", file_writer) as fmk, \
            patch("spacecmd.misc.logging", logger) as lgr:
            out = spacecmd.misc.do_login(shell, "")

        assert not mprint.called
        assert not logger.warning.called
        assert logger.error.called
        assert not client.user.listAssignableRoles.called
        assert gpass.called
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
        assert_args_expect(logger.error.call_args_list,
                           [(('Could not write session file: %s', 'Intel inside'), {})])

    def test_logout(self, shell):
        """
        Test logout.

        :param shell:
        :return:
        """

        assert bool(shell.session)

        spacecmd.misc.do_logout(shell, "")

        assert not bool(shell.session)
        assert not shell.current_user
        assert not shell.server
        assert shell.do_clear_caches.called
        assert_args_expect(shell.do_clear_caches.call_args_list, [(("", ), {})])

    def test_whoamitalkingto(self, shell):
        """
        Test to display what server is connected.

        :param shell:
        :return:
        """
        shell.server = "no.mans.land"
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.do_whoamitalkingto(shell, "")

        assert not logger.warning.called
        assert mprint.called
        assert_args_expect(mprint.call_args_list, [((shell.server, ), {})])

    def test_whoamitalkingto_no_session(self, shell):
        """
        Test to display no server is connected yet.

        :param shell:
        :return:
        """
        shell.server = None
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.misc.print", mprint) as prt, \
            patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.do_whoamitalkingto(shell, "")

        assert not mprint.called
        assert logger.warning.called
        assert_args_expect(logger.warning.call_args_list, [(("Yourself", ), {})])

    def test_clear_errata_cache(self, shell):
        """
        Test errata cache cleared.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2019, 1, 1, 0, 0)
        shell.all_errata = [{"advisory_name": "cve-123"}]
        shell.errata_cache_expire = tst

        assert bool(len(shell.all_errata))

        spacecmd.misc.clear_errata_cache(shell)

        assert shell.all_errata == {}
        assert shell.errata_cache_expire > tst
        assert shell.save_errata_cache.called

    def test_get_sorted_errata_names(self, shell):
        """
        Test getting errata names.

        :param shell:
        :return:
        """
        shell.all_errata = [{"advisory_name": "cve-123"},
                            {"advisory_name": "cve-aaa"},
                            {"advisory_name": "cve-zzz"}]
        assert spacecmd.misc.get_errata_names(shell) == ['cve-123', 'cve-aaa', 'cve-zzz']

    def test_get_erratum_id(self, shell):
        """
        Test to get erratum ID.

        :param shell:
        :return:
        """
        shell.all_errata = {"cve-zzz": {"id": 3}}
        assert spacecmd.misc.get_erratum_id(shell, "cve-zzz") == 3

    def test_get_erratum_name(self, shell):
        """
        Test to get erratum name.

        :param shell:
        :return:
        """
        shell.all_errata = {"cve-zzz": {"id": 3}}
        assert spacecmd.misc.get_erratum_name(shell, 3) == "cve-zzz"

    def test_generate_errata_cache_no_expired(self, shell):
        """
        Test generate errata cache (no expired, i.e. should not be generated).

        :return:
        """
        shell.options.quiet = False
        shell.errata_cache_expire = datetime.datetime(2099, 1, 1)

        assert spacecmd.misc.generate_errata_cache(shell) is None
        assert not shell.client.channel.listSoftwareChannels.called
        assert not shell.client.channel.software.listErrata.called
        assert not shell.replace_line_buffer.called
        assert not shell.save_errata_cache.called

    def test_generate_errata_cache_force(self, shell):
        """
        Test generate errata cache, forced

        :return:
        """
        shell.ERRATA_CACHE_TTL = 86400
        shell.all_errata = {}
        shell.options.quiet = False
        shell.errata_cache_expire = datetime.datetime(2099, 1, 1)
        shell.client.channel.listSoftwareChannels = MagicMock(return_value=[
            {"label": "locked_channel"}, {"label": "base_channel"}
        ])
        shell.client.channel.software.listErrata = MagicMock(side_effect=[
            xmlrpclib.Fault(faultCode=42, faultString="Sales staff sold a product we don't offer"),
            [{
                "id": 123,
                "advisory_name": "cve-123",
                "advisory_type": "mockery", "date": "2019.1.1",
                "advisory_synopsis": "some text here",
            }]
        ])

        logger = MagicMock()
        with patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.generate_errata_cache(shell, force=True)

        assert logger.debug.called
        assert spacecmd.misc.generate_errata_cache(shell) is None
        assert shell.client.channel.listSoftwareChannels.called
        assert shell.client.channel.software.listErrata.called
        assert shell.replace_line_buffer.called
        assert shell.save_errata_cache.called
        assert "cve-123" in shell.all_errata
        assert shell.all_errata["cve-123"]["id"] == 123
        assert shell.all_errata["cve-123"]["advisory_type"] == "mockery"
        assert shell.all_errata["cve-123"]["advisory_synopsis"] == "some text here"
        assert shell.all_errata["cve-123"]["advisory_name"] == "cve-123"
        assert shell.all_errata["cve-123"]["date"] == "2019.1.1"
        assert_args_expect(logger.debug.call_args_list,
                           [(('No access to %s (%s): %s', 'locked_channel',
                              42, "Sales staff sold a product we don't offer"), {})])

    def test_clear_package_cache(self, shell):
        """
        Test clear package cache.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2019, 1, 1, 0, 0)
        shell.package_cache_expire = tst

        spacecmd.misc.clear_package_cache(shell)

        assert shell.all_packages_short == {}
        assert shell.all_packages == {}
        assert shell.all_packages_by_id == {}
        assert shell.package_cache_expire is not None
        assert shell.package_cache_expire != tst
        assert shell.save_package_caches.called

    def test_generate_package_cache_cache_not_expired(self, shell):
        """
        Test generate package cache is not yet expired.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2099, 1, 1, 0, 0)
        pkgbuild = MagicMock()
        logger = MagicMock()

        shell.options.quiet = True
        shell.all_packages = {}
        shell.all_packages_short = {}
        shell.all_packages_by_id = {}
        shell.package_cache_expire = tst
        shell.PACKAGE_CACHE_TTL = 8000

        with patch("spacecmd.misc.build_package_names", pkgbuild) as pkgb, \
                patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.generate_package_cache(shell, force=False)

        assert not shell.client.channel.listSoftwareChannels.called
        assert not shell.client.channel.software.listAllPackages.called
        assert not pkgbuild.called
        assert not shell.replace_line_buffer.called
        assert not logger.debug.called
        assert not shell.save_package_caches.called
        assert shell.package_cache_expire == tst

    def test_generate_package_cache_cache_not_expired_forced(self, shell):
        """
        Test generate package cache not expired, but forced to.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2099, 1, 1, 0, 0)
        pkgbuild = MagicMock()
        logger = MagicMock()

        shell.options.quiet = True
        shell.all_packages = {}
        shell.all_packages_short = {}
        shell.all_packages_by_id = {}
        shell.package_cache_expire = tst
        shell.PACKAGE_CACHE_TTL = 8000
        shell.client.channel.listSoftwareChannels = MagicMock(return_value=[])

        with patch("spacecmd.misc.build_package_names", pkgbuild) as pkgb, \
                patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.generate_package_cache(shell, force=True)

        assert not shell.client.channel.software.listAllPackages.called
        assert not pkgbuild.called
        assert not shell.replace_line_buffer.called
        assert not logger.debug.called
        assert shell.client.channel.listSoftwareChannels.called
        assert shell.save_package_caches.called
        assert shell.package_cache_expire != tst
        assert shell.package_cache_expire is not None

    def test_generate_package_cache_verbose(self, shell):
        """
        Test generate package cache verbose mode.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2000, 1, 1, 0, 0)
        pkgbuild = MagicMock()
        logger = MagicMock()

        shell.options.quiet = False
        shell.all_packages = {}
        shell.all_packages_short = {}
        shell.all_packages_by_id = {}
        shell.package_cache_expire = tst
        shell.PACKAGE_CACHE_TTL = 8000
        shell.client.channel.listSoftwareChannels = MagicMock(return_value=[])

        with patch("spacecmd.misc.build_package_names", pkgbuild) as pkgb, \
                patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.generate_package_cache(shell, force=False)

        assert not logger.debug.called
        assert not shell.client.channel.software.listAllPackages.called
        assert not pkgbuild.called
        assert shell.client.channel.listSoftwareChannels.called
        assert shell.replace_line_buffer.called
        assert shell.save_package_caches.called
        assert shell.package_cache_expire != tst
        assert shell.package_cache_expire is not None

    def test_generate_package_cache_uqid(self, shell):
        """
        Test generate package cache, unique IDs.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2000, 1, 1, 0, 0)
        logger = MagicMock()

        shell.options.quiet = False
        shell.all_packages = {}
        shell.all_packages_short = {}
        shell.all_packages_by_id = {}
        shell.package_cache_expire = tst
        shell.PACKAGE_CACHE_TTL = 8000
        shell.client.channel.software.listAllPackages = MagicMock(
            side_effect=[
                [
                    {"name": "emacs", "version": 42, "release": 3, "id": 42},
                    {"name": "gedit", "version": 1, "release": 2, "id": 69},
                ],
                xmlrpclib.Fault(faultString="Interrupt configuration interference error",
                                faultCode=13)
            ]
        )
        shell.client.channel.listSoftwareChannels = MagicMock(
            return_value=[
                {"label": "basic_channel"},
                {"label": "locked_channel"},
            ]
        )

        with patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.generate_package_cache(shell, force=False)

        assert logger.debug.called
        assert shell.client.channel.software.listAllPackages.called
        assert shell.client.channel.listSoftwareChannels.called
        assert shell.replace_line_buffer.called
        assert shell.save_package_caches.called
        assert shell.package_cache_expire != tst
        assert shell.package_cache_expire is not None

        for pkgname, pkgid in [("emacs-42-3", 42), ("gedit-1-2", 69)]:
            assert pkgname in shell.all_packages
            assert shell.all_packages[pkgname] == [pkgid]
            assert pkgid in shell.all_packages_by_id
            assert shell.all_packages_by_id[pkgid] == pkgname
            assert pkgname.split("-")[0] in shell.all_packages_short

    def test_generate_package_cache_duplicate_ids(self, shell):
        """
        Test generate package cache, handling duplicate IDs.

        :param shell:
        :return:
        """
        tst = datetime.datetime(2000, 1, 1, 0, 0)
        logger = MagicMock()

        shell.options.quiet = False
        shell.all_packages = {}
        shell.all_packages_short = {}
        shell.all_packages_by_id = {}
        shell.package_cache_expire = tst
        shell.PACKAGE_CACHE_TTL = 8000
        shell.client.channel.software.listAllPackages = MagicMock(
            side_effect=[
                [
                    {"name": "emacs", "version": 42, "release": 3, "id": 42},
                    {"name": "gedit", "version": 1, "release": 2, "id": 69},
                    {"name": "vim", "version": 1, "release": 2, "id": 69},
                ],
                xmlrpclib.Fault(faultString="Interrupt configuration interference error",
                                faultCode=13)
            ]
        )
        shell.client.channel.listSoftwareChannels = MagicMock(
            return_value=[
                {"label": "basic_channel"},
                {"label": "locked_channel"},
            ]
        )

        with patch("spacecmd.misc.logging", logger) as lgr:
            spacecmd.misc.generate_package_cache(shell, force=False)

        assert logger.debug.called
        assert shell.client.channel.software.listAllPackages.called
        assert shell.client.channel.listSoftwareChannels.called
        assert shell.replace_line_buffer.called
        assert shell.save_package_caches.called
        assert shell.package_cache_expire != tst
        assert shell.package_cache_expire is not None

        assert_args_expect(logger.debug.call_args_list,
                           [(('No access to %s', 'locked_channel',), {}),
                            (('Non-unique package id "69" is detected. '
                              'Taking "vim-1-2" instead of "gedit-1-2"',), {})])

        for pkgname, pkgid in [("emacs-42-3", 42), ("vim-1-2", 69)]:
            assert pkgname in shell.all_packages
            assert shell.all_packages[pkgname] == [pkgid]
            assert pkgid in shell.all_packages_by_id
            assert shell.all_packages_by_id[pkgid] == pkgname
            assert pkgname.split("-")[0] in shell.all_packages_short

    def test_save_package_caches(self, shell):
        """
        Test saving package caches.

        :param shell:
        :return:
        """
        savecache = MagicMock()

        shell.all_packages = {"emacs-41-1": [42]}
        shell.all_packages_short = {"emacs": ""}
        shell.all_packages_by_id = {42: "emacs-41-1"}

        shell.packages_short_cache_file = "/tmp/psc.f"
        shell.packages_long_cache_file = "/tmp/plc.f"
        shell.packages_by_id_cache_file = "/tmp/bic.f"

        tst = datetime.datetime(2019, 1, 1, 0, 0)
        shell.package_cache_expire = tst

        with patch("spacecmd.misc.save_cache", savecache) as savc:
            spacecmd.misc.save_package_caches(shell)

        assert shell.package_cache_expire == tst
        assert_args_expect(savecache.call_args_list,
                           [
                               (('/tmp/psc.f', {'emacs': ''}, tst), {}),
                               (('/tmp/plc.f', {'emacs-41-1': [42]}, tst), {}),
                               (('/tmp/bic.f', {42: 'emacs-41-1'}, tst), {}),
                           ])

    def test_user_confirm_bool_positive(self, shell):
        """
        Test interactive user confirmation UI. Boolean, positive.

        :return:
        """
        shell.options.yes = False
        for answer in ["yop", "yeah", "yes", "y", "Yes", "Yo"]:
            pmt = MagicMock(return_value=answer)
            with patch("spacecmd.misc.prompt_user", pmt) as prompter:
                out = spacecmd.misc.user_confirm(shell)

            assert isinstance(out, bool)
            assert out

    def test_user_confirm_bool_negative(self, shell):
        """
        Test interactive user confirmation UI. Boolean, negative.

        :return:
        """
        shell.options.yes = False
        for answer in ["whatever", "nope", "no", "n", "Nada", "go away"]:
            pmt = MagicMock(return_value=answer)
            with patch("spacecmd.misc.prompt_user", pmt) as prompter:
                out = spacecmd.misc.user_confirm(shell)

            assert isinstance(out, bool)
            assert not out

    def test_user_confirm_int_positive(self, shell):
        """
        Test interactive user confirmation UI. Integer, positive.

        :return:
        """
        shell.options.yes = False
        for answer in ["yop", "yeah", "yes", "y", "Yes", "Yo"]:
            pmt = MagicMock(return_value=answer)
            with patch("spacecmd.misc.prompt_user", pmt) as prompter:
                out = spacecmd.misc.user_confirm(shell, integer=True)

            assert isinstance(out, int)
            assert out == 1

    def test_user_confirm_int_negative(self, shell):
        """
        Test interactive user confirmation UI. Integer, negative.

        :return:
        """
        shell.options.yes = False
        for answer in ["whatever", "nope", "no", "n", "Nada", "go away"]:
            pmt = MagicMock(return_value=answer)
            with patch("spacecmd.misc.prompt_user", pmt) as prompter:
                out = spacecmd.misc.user_confirm(shell, integer=True)

            assert isinstance(out, int)
            assert out == 0

    def test_check_api_version(self, shell):
        """
        Test API version checker.

        :param shell:
        :return:
        """
        shell.api_version = "10.5"

        for smaller in ["10", "9", "10.1", "10.5"]:
            assert spacecmd.misc.check_api_version(shell, smaller)

        for bigger in ["11", "10.6"]:
            assert not spacecmd.misc.check_api_version(shell, bigger)

    def test_get_system_id_no_duplicates(self, shell):
        """
        Test getting system ID without duplicates (normal run).

        :param shell:
        :return:
        """
        shell.all_systems = {100100: "douchebox", 100200: "sloppy"}

        assert spacecmd.misc.get_system_id(shell, "douchebox") == 100100
        assert spacecmd.misc.get_system_id(shell, "sloppy") == 100200
        assert spacecmd.misc.get_system_id(shell, "100100") == 100100
        assert spacecmd.misc.get_system_id(shell, "100200") == 100200

    def test_get_system_id_handle_duplicates(self, shell):
        """
        Test getting system ID with duplicates.

        :param shell:
        :return:
        """
        shell.all_systems = {100100: "douchebox", 100200: "sloppy",
                             100300: "douchebox"}

        logger = MagicMock()
        with patch("spacecmd.misc.logging", logger) as lgr:
            assert spacecmd.misc.get_system_id(shell, "douchebox") == 0

        assert_args_expect(logger.warning.call_args_list,
                           [(('Duplicate system profile names found!',), {}),
                            (('Please reference systems by ID or resolve the',), {}),
                            (("underlying issue with 'system_delete' or 'system_rename'",), {}),
                            (('',), {}),
                            (('douchebox = 100100, 100300',), {})])

        assert logger.warning.called
