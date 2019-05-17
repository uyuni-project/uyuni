# coding: utf-8
"""
Oracle ULN (Unbreakable Linux Network) authentication library.
"""
import os
import configparser
import urllib.parse

from spacewalk.common.suseLib import get_proxy
from spacewalk.satellite_tools.syncLib import RhnSyncException
from up2date_client.rpcServer import RetryServer, ServerList

from spacewalk.common.rhnConfig import initCFG

import logging
log = logging.getLogger(__name__)


class ULNTokenException(Exception):
    """
    This class represent an exception getting the ULN token
    """
    pass


class ULNAuth:
    """
    ULN Authentication.
    """
    ULN_CONF_PATH = "/etc/rhn/spacewalk-repo-sync/uln.conf"
    ULN_DEFAULT_HOST = "linux-update.oracle.com"

    def __init__(self):
        initCFG("server.satellite")
        self._uln_token = None
        self._uln_url = None

    @property
    def token(self):
        """
        Return ULN token, if authorised.
        """
        return self._uln_token

    @property
    def url(self):
        """
        Return ULN URL for the access.
        """
        return self._uln_url

    def get_hostname(self, url: str) -> tuple:
        """
        Get label from the URL (a hostname).

        :raises RhnSyncException: if URL is wrongly formatted.
        :returns: tuple (hostname, label)
        """
        if url.startswith("uln:///"):
            return "https://" + self.ULN_DEFAULT_HOST, url[7:]
        elif url.startswith("uln://"):
            parts = url[6:].split("/")
            return "https://" + parts[0], "/".join(parts[1:])
        else:
            raise RhnSyncException("URL must start with 'uln://'.")


    def get_credentials(self) -> tuple:
        """
        Get credentials from the uln.conf

        :raises AssertionError: if configuration does not contain required sections.
        :returns: tuple of username and password
        """
        if not os.path.exists(self.ULN_CONF_PATH):
            raise RhnSyncException("'{}' does not exists".format(self.ULN_CONF_PATH))
        elif not os.access(self.ULN_CONF_PATH, os.R_OK):
            raise RhnSyncException("Permission denied to '{}'".format(self.ULN_CONF_PATH))

        config = configparser.ConfigParser()
        config.read(self.ULN_CONF_PATH)
        if "main" in config:
            sct = config["main"]
            username, password = sct.get("username"), sct.get("password")
        else:
            username = password = None
        assert username is not None and password is not None, "Credentials were not found in the configuration"

        return username, password

    def authenticate(self, url):
        """
        Get ULN token.

        :raises RhnSyncException: if configuration does not contain required sections.
        :returns: ULN token
        """
        err_msg = ''
        if self._uln_token is None:
            try:
                usr, pwd = self.get_credentials()
                self._uln_url, label = self.get_hostname(url)
                px_url, px_usr, px_pwd = get_proxy(self._uln_url)
                server_list = ServerList([self._uln_url + "/rpc/api"])
                retry_server = RetryServer(server_list.server(),
                                           refreshCallback=None,
                                           proxy=px_url,
                                           username=px_usr,
                                           password=px_pwd,
                                           timeout=5)
                retry_server.addServerList(server_list)
                self._uln_token = retry_server.auth.login(usr, pwd)
            except Exception as exc:
                err_msg = exc

        if not self.token or err_msg:
            raise ULNTokenException("Authentication failure: token was not obtained. {}".format(err_msg))

        return self.token
