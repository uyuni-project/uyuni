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

import re
import sys
import xmlrpclib
from tabulate import tabulate

from spacewalk.susemanager.mgr_sync.channel import parse_channels
from spacewalk.susemanager.mgr_sync.channel import Channel
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

    def _listChannels(self):
        """
        List channels.
        """

        data = self._execute_xmlrpc_method("listChannels", self.auth.token)
        if not data:
            cli_msg("No channels found.")
            return

        base_channels = parse_channels(data)

        table = []
        for _, base_channel in base_channels.items():
            table.append(base_channel.to_table_row())
            if base_channel.status in (Channel.Status.INSTALLED,
                                       Channel.Status.AVAILABLE):
                for child in base_channel.children:
                    table.append(child.to_table_row())

        print("Available Channels:\n")
        print("Status:")
        print("- P - channel is installed (provided)")
        print("- . - channel is not installed, but is available")
        print("- X - channel is not available\n")
        print(tabulate(table, headers=("Status", "Name", "Description")))

    def _listProducts(self):
        """
        List products on the channel.
        """

        return self._execute_xmlrpc_method("listProducts", self.auth.token)

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

        for operation, method in actions:
            sys.stdout.write("Refreshing %s\t" % operation)
            sys.stdout.flush()
            try:
                self._execute_xmlrpc_method(method, self.auth.token)
                sys.stdout.write("[DONE]\n")
                sys.stdout.flush()
            except Exception, ex:
                sys.stdout.write("[FAIL]\n")
                sys.stdout.flush()
                sys.stderr.write("\tError: %s\n\n" % ex)
                sys.exit(1)

    def _execute_xmlrpc_method(self, method, auth_token, *params, **opts):
        """
        Invokes the remote method specified by the user. Repeats the operation
        once if there's a failure caused by the expiration of the sessions
        token.

        Retry on token expiration happens only when the
        'retry_on_session_failure' parameter is set to True.
        """

        retry_on_session_failure = opts.get("retry_on_session_failure", True)

        try:
            return getattr(self.conn.sync.content, method)(auth_token, *params)
        except xmlrpclib.Fault, ex:
            if retry_on_session_failure and self._check_session_fail(ex):
                self.auth.discard_token()
                auth_token = self.auth.token
                return self._execute_xmlrpc_method(method, auth_token, *params,
                                                   retry_on_session_failure=False)
            else:
                raise ex

    def run(self, options):
        """
        Run the app.
        """
        self.quiet = options.quiet
        self.auth.persist = options.saveconfig
        if options.listchannels:
            self._listChannels()
        elif options.listproducts:
            products = self._listProducts()
            if products:
                self._format(products, title="Available products")
            else:
                cli_msg("No products found.")
        elif options.refresh:
            self._refresh()

    def _check_session_fail(self, exception):
        """
        Check session failure.
        """

        fault = str(exception).lower()
        relevant_errors = (
            'could not find session',
            'session id.*is not valid'
        )

        for error_string in relevant_errors:
            if re.search(error_string, fault):
                return True

        return False

    def _format(self, data, title=""):
        """
        Format the output.
        """

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
