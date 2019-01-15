# coding: utf-8
"""
Reposync via Salt library.
"""
from __future__ import absolute_import, unicode_literals, print_function

import salt.client
import salt.config


class ZyppoSync(object):
    """
    Wrapper for underlying package manager for the reposync via Salt.

    Example usage:

    >>> zyppo = ZyppoSync()
    >>> for idx, repo_meta in enumerate(zyppo.list_repos().values()):
    >>>    print(idx + 1, repo_meta["name"])
    >>>    print("  ", repo_meta["baseurl"])

    """
    def __init__(self, cfg_path="/etc/salt/minion"):
        self._conf = salt.config.minion_config(cfg_path)
        self._conf["file_client"] = "local"
        self._caller = salt.client.Caller(mopts=self._conf)

    def _get_call(self, key):
        """
        Prepare a call to the pkg module.
        """
        def make_call(*args, **kwargs):
            """
            Makes a call to the underlying package.
            """
            return self._caller.cmd("pkg.{}".format(key), *args, **kwargs)
        return make_call

    def __getattr__(self, attr):
        """
        Prepare a callable on the requested
        attribute name.
        """
        return self._get_call(attr)
    
