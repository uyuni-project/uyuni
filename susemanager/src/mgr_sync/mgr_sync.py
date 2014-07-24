# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.

import sys
import xmlrpclib
from tabulate import tabulate

from spacewalk.susemanager.mgr_sync.config import Config
from spacewalk.susemanager.mgr_sync.authenticator import Authenticator
from spacewalk.susemanager.mgr_sync.helpers import cli_msg


class MgrSync(object):
    """
    App, which utilizes the XML-RPC API.
    """

    def __init__(self):
        self.config = Config()
        url = "http://%s:%s%s" % (self.config.host,
                                  self.config.port,
                                  self.config.uri)
        self.conn = xmlrpclib.ServerProxy(url)
        self.auth = Authenticator(self.conn, self.config)
        self.quiet = False

    def _addProduct(self):
        """
        Add product.
        """
        return self.conn.sync.content.addProduct(self.auth.token, "xxx")

    def _addChannel(self):
        """
        Add channel.
        """
        return self.conn.sync.content.addChannel(self.auth.token, "xxx")

    def _listChannels(self):
        """
        List channels.
        """
        return self.conn.sync.content.listChannels(self.auth.token)

    def _listProducts(self):
        """
        List products on the channel.
        """
        return self.conn.sync.content.listProducts(self.auth.token)

    def _refresh(self):
        """
        Refresh the SCC data in the SUSE Manager database.
        """

        actions = (
            ("Channels             ", "synchronizeChannels"),
            ("Channel families     ", "synchronizeChannelFamilies"),
            ("SUSE products        ", "synchronizeProducts"),
            ("SUSE Product channels", "synchronizeProductChannels"),
            ("Subscriptions        ", "synchronizeSubscriptions"),
            ("Upgrade paths        ", "synchronizeUpgradePaths")
        )

        for action in actions:
            operation = action[0]
            method = action[1]
            sys.stdout.write("Refreshing %s\t" % operation)
            sys.stdout.flush()
            try:
                self._execute_xmlrpc_method(method=method)
                sys.stdout.write("[DONE]\n")
                sys.stdout.flush()
            except Exception, ex:
                sys.stdout.write("[FAIL]\n")
                sys.stdout.flush()
                sys.stderr.write("\tError: %s\n\n" % ex)
                sys.exit(1)

    def _execute_xmlrpc_method(self, method, retry_on_session_failure=True):
        """
        Invokes the remote method specified by the user. Repeats the operation
        once if there's a failure caused by the expiration of the sessions
        token.

        Retry on token expiration happens only when the
        'retry_on_session_failure' parameter is set to True.
        """
        try:
            getattr(self.conn.sync.content, method)(self.auth.token)
        except Exception, ex:
            if self._check_session_fail(ex) and retry_on_session_failure:
                self.auth.discard_token()
                self._execute_xmlrpc_method(method, retry_on_session_failure=False)
            else:
                raise ex

    def run(self, options, attempted=False):
        """
        Run the app.
        """
        self.quiet = options.quiet
        try:
            self.auth.persist = options.saveconfig
            if options.listchannels:
                self.format(self._listChannels(), title="Available channels")
            elif options.listproducts:
                self.format(self._listProducts(), title="Available products")
            elif options.addproduct:
                self._addProduct()
            elif options.addchannel:
                self._addChannel()
            elif options.refresh:
                self._refresh()
        except xmlrpclib.Fault, ex:
            # Try to figure out it is an outdated session
            if self._check_session_fail(ex):
                if not attempted:
                    if options.verbose:
                        print "Cached session is outdated. Trying to connect..."
                    self.auth.discard_token()
                    self.run(options, attempted=True)
                else:
                    raise ex
            else:
                # The error does not seems like a failed session
                raise ex

    def _check_session_fail(self, exception):
        """
        Check session failure.
        """
        fault = str(exception).lower()
        return 'session' in fault and ('is not valid.' in fault or 'could not find' in fault)

    def format(self, data, title=""):
        """
        Format the output.
        """
        if not data:
            cli_msg("No products found.")
            sys.exit(1)

        if data[0].has_key('extensions'):
            self._format_tree(data)
        else:
            self._format_flat(data, title=title)

    def _format_tree(self, data):
        """
        Format the tree output within the table.
        """
        table = []
        for p in data:
            table.append((p["status"], p["title"], p["label"], p["version"], p["arch"],))
            for c in p.get("extensions", []):
                table.append((p["status"], "  \\_" + p["title"], p["label"], p["version"], p["arch"] or "N/A",))
        print tabulate(table, headers=("Status", "Title", "Label", "Version", "Arch"))

    def _format_flat(self, data, title=""):
        """
        Format the tabular output.
        """
        table = []
        idx = 1
        for p in data:
            descr = (p["description"] + "").strip() or p["url"] or "N/A"
            table.append((str(idx).zfill(2), p["name"], p["target"], descr),)
            idx += 1
        table = sorted(table)
        print (title and (title + ":\n") or "") + tabulate(table, headers=("No.", "Name", "OS", "Description"))
