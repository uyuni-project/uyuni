# coding: utf-8
"""
Oracle ULN (Unbreakable Linux Network) authentication library.
"""
import os
import configparser
import urllib.parse
from spacewalk.satellite_tools.syncLib import RhnSyncException


class ULNAuth:
    """
    ULN Authentication.
    """
    ULN_CONF_PATH = "/etc/rhn/spacewalk-repo-sync/uln.conf"
    ULN_DEFAULT_HOST = "linux-update.oracle.com"

    def __init__(self):
        self._uln_token = None
        self._uln_url = None

    def get_hostname(self, url: str) -> tuple:
        """
        Get label from the URL (a hostname).

        :raises RhnSyncException: if URL is wrongly formatted.
        :returns: tuple (hostname, label)
        """
        if not url.startswith("uln://"):
            raise RhnSyncException("URL must start with 'uln://'.")
        p_url = urllib.parse.urlparse(url)
        return p_url.netloc or self.ULN_DEFAULT_HOST, p_url.path

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

    def authenticate_uln(self, url):
        """
        Get ULN token.

        :raises RhnSyncException: if configuration does not contain required sections.
        :returns: ULN token
        """
        usr, pwd = self.get_credentials()
        px_url, px_usr, px_pwd = get_proxy(url)
        hostname, label = self.get_hostname(url)
        self._uln_url = "https://{}/XMLRPC/GET-REQ{}".format(hostname, label)
        server_list = ServerList(["https://{}/rpc/api".format(hostname)])
        retry_server = RetryServer(server_list.server(), refreshCallback=None, proxy=None,
                                   username=px_usr, password=px_pwd, timeout=5)
        retry_server.addServerList(server_list)
        self._uln_token = retry_server.auth.login(usr, pwd)

        return self._uln_token
