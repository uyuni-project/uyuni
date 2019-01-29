# coding: utf-8
"""
Oracle ULN (Unbreakable Linux Network) authentication library.
"""
import os
import configparser
import urllib.parse


class ULNAuth:
    """
    ULN Authentication.
    """
    ULN_CONF_PATH = "/etc/rhn/spacewalk-repo-sync/uln.conf"
    ULN_DEFAULT_HOST = "linux-update.oracle.com"

    def __init__(self):
        self._uln_token = None
        self._uln_url = None

    def _get_hostname(self, url: str) -> tuple:
        """
        Get label from the URL (a hostname).

        :raises RhnSyncException: if URL is wrongly formatted.
        :returns: tuple (hostname, label)
        """
        if not url.startswith("uln://"):
            raise RhnSyncException("URL must start with 'uln://'.")
        p_url = urllib.parse.urlparse(url)
        return p_url.netloc or self.ULN_DEFAULT_HOST, p_url.path

    def _get_credentials(self) -> tuple:
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
