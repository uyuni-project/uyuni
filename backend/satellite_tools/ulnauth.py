# coding: utf-8
"""
Oracle ULN (Unbreakable Linux Network) authentication library.
"""
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
            RhnSyncException("URL must start with 'uln://'.")
        p_url = urllib.parse.urlparse(url)
        return p_url.netloc or self.DEFAULT_ULN_HOST, p_url.path

    
