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

from spacewalk.susemanager.content_sync_helper import current_backend, switch_to_scc, BackendType
from spacewalk.susemanager.mgr_sync.channel import parse_channels, Channel, find_channel_by_label
from spacewalk.susemanager.mgr_sync.product import parse_products, Product
from spacewalk.susemanager.mgr_sync.config import Config
from spacewalk.susemanager.authenticator import Authenticator
from spacewalk.susemanager.helpers import cli_ask


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
        self.auth = Authenticator(connection=self.conn,
                                  user=self.config.user,
                                  password=self.config.password,
                                  token=self.config.token)
        self.quiet = False

    def run(self, options):
        """
        Run the app.
        """
        if not current_backend() == BackendType.SCC \
           and not vars(options).has_key('enable_scc'):
            msg = """Error: the Novell Customer Center (NCC) backend is currently in use.
mgr-sync requires the SUSE Customer Center (SCC) backend to be activated.

This can be done using the following commmand:
    mgr-sync enable-scc

Note well: there is no way to revert the migration from Novell Customer Center (NCC) to SUSE Customer Center (SCC).
"""
            sys.stderr.write(msg)
            sys.exit(1)

        self.quiet = not options.verbose
        if vars(options).has_key('list_target'):
            if 'channel' in options.list_target:
                self._list_channels(expand=options.expand,
                                    filter=options.filter,
                                    no_optionals=options.no_optionals,
                                    compact=options.compact)
            elif 'product' in options.list_target:
                self._list_products(filter=options.filter)
        elif vars(options).has_key('add_target'):
            if 'channel' in options.add_target:
                self._add_channels(options.target)
            elif 'product' in options.add_target:
                self._add_products()
        elif vars(options).has_key('refresh'):
            self._refresh(enable_reposync=options.refresh_channels)
        elif vars(options).has_key('enable_scc'):
            if current_backend() == BackendType.SCC:
                print("The SUSE Customer Center (SCC) backend is already "
                      "active, nothing to do.")
            else:
                self._enable_scc()

        if options.saveconfig and self.auth.has_credentials():
            self.config.user = self.auth.user
            self.config.password = self.auth.password
            self.config.write()
            print("credentials has been saved to the {0} file.".format(
                self.config.dotfile))

        if self.auth.token(connect=False):
            self.config.token = self.auth.token(connect=False)
            self.config.write()

    ###########################
    #                         #
    # Channel related methods #
    #                         #
    ###########################

    def _list_channels(self, expand, filter, no_optionals,
                       compact=False, show_interactive_numbers=False):
        """
        List channels.
        """

        interactive_number = 0
        available_channels = []

        if filter:
            filter = filter.lower()

        base_channels = self._fetch_remote_channels()

        if not base_channels:
            print("No channels found.")
            return

        print "Available Channels%s:\n" % (expand and " (full)" or "")
        print("\nStatus:")
        print("  - [I] - channel is installed")
        print("  - [ ] - channel is not installed, but is available")
        print("  - [U] - channel is unavailable\n")

        for bc_label in sorted(base_channels.keys()):
            base_channel = base_channels[bc_label]

            prefix = ""
            parent_output = base_channel.to_ascii_row(compact)
            children_output = []

            if base_channel.status in (Channel.Status.INSTALLED,
                                       Channel.Status.AVAILABLE):
                for child in base_channel.children:
                    prefix = ""
                    if base_channel.status == Channel.Status.INSTALLED or expand:
                        output = child.to_ascii_row(compact)
                        if not filter or filter in output.lower():
                            if child.status == Channel.Status.AVAILABLE:
                                interactive_number += 1
                                if show_interactive_numbers:
                                    prefix = "%.2d) " % interactive_number
                                available_channels.append(child.label)
                            elif show_interactive_numbers:
                                prefix = "    "

                            children_output.append("    " + prefix + output)

            if not filter or filter in parent_output.lower() or children_output:
                prefix = ""

                if base_channel.status == Channel.Status.AVAILABLE:
                    interactive_number += 1
                    if show_interactive_numbers:
                        prefix = "%.2d) " % interactive_number
                    available_channels.append(base_channel.label)
                elif show_interactive_numbers:
                    prefix = "    "
                print(prefix + parent_output)

                for child_output in children_output:
                    print(child_output)

        return available_channels

    def _fetch_remote_channels(self):
        """ Returns the list of channels as reported by the remote server """
        return parse_channels(
            self._execute_xmlrpc_method(self.conn.sync.content,
                                        "listChannels", self.auth.token()))

    def _add_channels(self, channels):
        """ Add a list of channels.

        If the channel list is empty the interactive mode is started.
        """

        exit_with_error = False
        enable_checks = True
        current_channels = []

        if not channels:
            channels = [self._select_channel_interactive_mode()]
            enable_checks = False

        if enable_checks:
            current_channels = self._fetch_remote_channels()

        for channel in channels:
            add_channel = True
            if enable_checks:
                match = find_channel_by_label(channel, current_channels)
                if match:
                    if match.status == Channel.Status.INSTALLED:
                        add_channel = False
                        print("Channel '{0}' has already been added".format(
                            channel))
                    elif match.status == Channel.Status.UNAVAILABLE:
                        print("Channel '{0}' is not available, skipping".format(
                            channel))
                        exit_with_error = 1
                        continue

                    if not match.base_channel:
                        parent = current_channels[match.parent]
                        if parent.status == Channel.Status.UNAVAILABLE:
                            sys.stderr.write(
                                "Error, '{0}' depends on channel '{1}' which is not available\n".format(
                                    channel, parent.label))
                            sys.stderr.write(
                                "'{0}' has not been added\n".format(channel))
                            exit_with_error = True
                            continue
                        if parent.status == Channel.Status.AVAILABLE:
                            print("'{0}' depends on channel '{1}' which has not been added yet".format(
                                channel, parent.label))
                            print("Going to add '{0}'".format(
                                parent.label))
                            self._add_channels([parent.label])

            if add_channel:
                print("Adding '{0}' channel".format(channel))
                self._execute_xmlrpc_method(self.conn.sync.content,
                                            "addChannel",
                                            self.auth.token(),
                                            channel)

            print("Scheduling reposync for '{0}' channel".format(channel))
            self._schedule_channel_reposync(channel)

        if exit_with_error:
            sys.exit(1)

    def _schedule_channel_reposync(self, channel):
        """ Schedules a reposync for the given channel.

        :param channel: the label identifying the channel
        """

        self._execute_xmlrpc_method(self.conn.channel.software,
                                    "syncRepo",
                                    self.auth.token(),
                                    channel)

    def _select_channel_interactive_mode(self):
        """Show not installed channels prefixing a number, then reads
        user input and returns the label of the chosen channel

        """
        channels = self._list_channels(
            expand=False, filter=None, compact=False,
            no_optionals=True, show_interactive_numbers=True)

        validator = lambda i: re.search("\d+", i) and \
            int(i) in range(1, len(channels)+1)
        choice = cli_ask(
            msg=("Enter channel number (1-{0})".format(len(channels))),
            validator=validator)

        return channels[int(choice)-1]

    ############################
    #                          #
    # Products related methods #
    #                          #
    ############################

    def _fetch_remote_products(self):
        """ Returns the list of products as reported by the remote server """
        return parse_products(
            self._execute_xmlrpc_method(self.conn.sync.content,
                                        "listProducts", self.auth.token()))

    def _list_products(self, filter, show_interactive_numbers=False):
        """
        List products
        """

        interactive_data = None
        if show_interactive_numbers:
            interactive_data = {
                'counter': 1,
                'num_prod': {}
            }

        if filter:
            filter = filter.lower()

        products = self._fetch_remote_products()

        if not products:
            print("No products found.")
            return

        print("Available Products:\n")
        print("\nStatus:")
        print("  - [I] - product is installed")
        print("  - [ ] - product is not installed, but is available\n")

        for product in products:
            product.to_stdout(filter=filter,
                              interactive_data=interactive_data)

        return interactive_data

    def _add_products(self):
        """ Add a list of products.

        If the products list is empty the interactive mode is started.
        """

        product = self._select_product_interactive_mode()

        if product.status == Product.Status.INSTALLED:
            print("Product '{0}' has already been added".format(
                product.friendly_name))
            return

        mandatory_channels = [c for c in product.channels
                              if not c.optional]
        missing_channels = [c for c in mandatory_channels
                            if c.status == Channel.Status.UNAVAILABLE]

        if missing_channels:
            sys.stderr.write(
                "Cannot add product '{0}' because the "
                "following channels are not available:\n".format(
                    product.friendly_name))
            for c in missing_channels:
                sys.stderr.write("  - {0}\n".format(c.label))
            sys.exit(1)

        print("Adding channels required by '{0}' product".format(
            product.friendly_name))
        for channel in mandatory_channels:
            sys.stdout.write("  * {0}: ".format(channel.label))
            if channel.status == Channel.Status.INSTALLED:
                sys.stdout.write("already added, ")
            else:
                self._execute_xmlrpc_method(self.conn.sync.content,
                                            "addChannel",
                                            self.auth.token(),
                                            channel.label)
                sys.stdout.write("added, ")
            self._schedule_channel_reposync(channel.label)
            sys.stdout.write("reposync scheduled\n")
            sys.stdout.flush()
        print("Product successfully added")

    def _select_product_interactive_mode(self):
        """Show not installed products prefixing a number, then reads
        user input and returns the label of the chosen product

        """
        interactive_data = self._list_products(
            filter=None, show_interactive_numbers=True)

        num_prod = interactive_data['num_prod']
        if num_prod:
            validator = lambda i: re.search("\d+", i) and \
                int(i) in range(1, len(num_prod.keys()))
            choice = cli_ask(
                msg=("Enter product number (1-{0})".format(
                    len(num_prod.keys()))),
                validator=validator)
            return num_prod[int(choice)]
        else:
            print("All the available products have already been installed, "
                  "nothing to do")
            sys.exit(0)

    #################
    #               #
    # Other methods #
    #               #
    #################

    def _refresh(self, enable_reposync):
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
        text_width = len("Refreshing ") + 8 + \
                     len(sorted(actions, key=lambda t: t[0], reverse=True)[0])

        for operation, method in actions:
            sys.stdout.write("Refreshing %s" % operation)
            sys.stdout.flush()
            try:
                self._execute_xmlrpc_method(self.conn.sync.content, method,
                                            self.auth.token())
                sys.stdout.write("[DONE]".rjust(text_width) + "\n")
                sys.stdout.flush()
            except Exception, ex:
                sys.stdout.write("[FAIL]".rjust(text_width) + "\n")
                sys.stdout.flush()
                sys.stderr.write("\tError: %s\n\n" % ex)
                sys.exit(1)

        if enable_reposync:
            print("\nScheduling refresh of all the available channels")

            base_channels = self._fetch_remote_channels()
            for bc_label in sorted(base_channels.keys()):
                bc = base_channels[bc_label]

                if bc.status != Channel.Status.INSTALLED:
                    continue

                print("Scheduling reposync for '{0}' channel".format(bc.label))
                self._schedule_channel_reposync(bc.label)
                for child in bc.children:
                    if child.status == Channel.Status.INSTALLED:
                        print("Scheduling reposync for '{0}' channel".format(
                            child.label))
                        self._schedule_channel_reposync(child.label)

    def _enable_scc(self, retry_on_session_failure=True):
        """ Enable the SCC backend """

        if current_backend() == BackendType.NCC:

            try:
                switch_to_scc(self.conn, self.auth.token())
            except xmlrpclib.Fault, ex:
                if retry_on_session_failure and self._check_session_fail(ex):
                    self.auth.discard_token()
                    return self._enable_scc(retry_on_session_failure=False)
                else:
                    raise ex
            self._refresh(enable_reposync=False)
            print("SCC backend successfully migrated.")
        else:
            print("SUSE Manager is already using the SCC backend.")

    def _execute_xmlrpc_method(self, endoint, method, auth_token, *params, **opts):
        """
        Invokes the remote method specified by the user. Repeats the operation
        once if there's a failure caused by the expiration of the sessions
        token.

        Retry on token expiration happens only when the
        'retry_on_session_failure' parameter is set to True.
        """

        retry_on_session_failure = opts.get("retry_on_session_failure", True)

        try:
            return getattr(endoint, method)(auth_token, *params)
        except xmlrpclib.Fault, ex:
            if retry_on_session_failure and self._check_session_fail(ex):
                self.auth.discard_token()
                auth_token = self.auth.token()
                return self._execute_xmlrpc_method(
                    endoint, method, auth_token, *params,
                    retry_on_session_failure=False)
            else:
                raise ex

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

