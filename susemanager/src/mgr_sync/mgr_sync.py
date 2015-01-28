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
import os.path
import xmlrpclib

from spacewalk.common.suseLib import current_cc_backend, BackendType, hasISSMaster
from spacewalk.susemanager.content_sync_helper import switch_to_scc
from spacewalk.susemanager.mgr_sync.channel import parse_channels, Channel, find_channel_by_label
from spacewalk.susemanager.mgr_sync.product import parse_products, Product
from spacewalk.susemanager.mgr_sync.config import Config
from spacewalk.susemanager.mgr_sync import logger
from spacewalk.susemanager.authenticator import Authenticator, MaximumNumberOfAuthenticationFailures
from spacewalk.susemanager.helpers import cli_ask

# see TaskoXmlRpcHandler.java for available methods
TASKOMATIC_XMLRPC_URL = 'http://localhost:2829/RPC2'

DEFAULT_LOG_LOCATION = "/var/log/rhn/mgr-sync.log"

class MgrSync(object):
    """
    App, which utilizes the XML-RPC API.
    """

    def __init__(self):
        self.config = Config()
        url = "http://{0}:{1}{2}".format(self.config.host,
                                  self.config.port,
                                  self.config.uri)
        self.conn = xmlrpclib.ServerProxy(url)
        self.auth = Authenticator(connection=self.conn,
                                  user=self.config.user,
                                  password=self.config.password,
                                  token=self.config.token)
        self.quiet = False

    def __init__logger(self, debug_level, logfile=DEFAULT_LOG_LOCATION):
        if debug_level == 1:
            debug_level = self.config.debug or 1

        return logger.Logger(debug_level, logfile)

    def run(self, options):
        """
        Run the app.
        Returns an integer with the exit status of mgr-sync.
        """
        self.log = self.__init__logger(options.debug)
        self.log.info("Executing mgr-sync {0}".format(options))

        if not self._is_scc_allowed():
            msg = """Support for SUSE Customer Center (SCC) is not yet available.\n"""
            self.log.error(msg)
            sys.stderr.write(msg)
            return 1

        if not current_cc_backend() == BackendType.SCC \
           and not vars(options).has_key('enable_scc'):
            msg = """Error: the Novell Customer Center (NCC) backend is currently in use.
mgr-sync requires the SUSE Customer Center (SCC) backend to be activated.

This can be done using the following commmand:
    mgr-sync enable-scc

Note: there is no way to revert the migration from Novell Customer Center (NCC) to SUSE Customer Center (SCC).
"""
            self.log.error(msg)
            sys.stderr.write(msg)
            return 1

        if hasISSMaster() and not (vars(options).has_key('enable_scc') or vars(options).has_key('refresh')):
            msg = """SUSE Manager is configured as slave server. Please use 'mgr-inter-sync' command.\n"""
            self.log.error(msg)
            sys.stderr.write(msg)
            return 1

        if options.store_credentials and not self.auth.has_credentials():
            # Ensure credentials are asked to the user, even though
            # there's a token already store inside of the local
            # configuration
            self.auth.discard_token()

        # Now we can process the user request
        exit_code = 0
        try:
            exit_code = self._process_user_request(options)
        except MaximumNumberOfAuthenticationFailures:
            msg = "mgr-sync: Authentication failure"
            sys.stderr.write(msg + "\n")
            self.log.error(msg)
            return 1

        # Ensure the latest valid token is saved to the local configuration
        self.config.token = self.auth.token()
        if options.store_credentials and self.auth.has_credentials():
            # Save user credentials only with explicitly asked by the user
            self.config.user = self.auth.user
            self.config.password = self.auth.password

        self.config.write()
        if options.store_credentials and self.auth.has_credentials():
            print("Credentials have been saved to the {0} file.".format(
                self.config.dotfile))
            self.log.info("Credentials have been saved to the {0} file.".format(
                self.config.dotfile))

        return exit_code

    def _process_user_request(self, options):
        """
        Execute the user request.
        Returns an integer with the exit status of mgr-sync.
        """
        self.quiet = not options.verbose
        self.exit_with_error = False

        if vars(options).has_key('list_target'):
            if 'channel' in options.list_target:
                self.log.info("Listing channels...")
                self._list_channels(expand=options.expand,
                                    filter=options.filter,
                                    no_optionals=options.no_optionals,
                                    compact=options.compact)
            elif 'credentials' in options.list_target:
                self.log.info("Listing credentials...")
                self._list_credentials()
            elif 'product' in options.list_target:
                self.log.info("Listing products...")
                self._list_products(expand=options.expand,
                                    filter=options.filter)
        elif vars(options).has_key('add_target'):
            if 'channel' in options.add_target:
                self._add_channels(channels=options.target, mirror=options.mirror)
            elif 'credentials' in options.add_target:
                self._add_credentials(options.primary, options.target)
            elif 'product' in options.add_target:
                self._add_products(mirror="")
        elif vars(options).has_key('refresh'):
            self._refresh(enable_reposync=options.refresh_channels, mirror=options.mirror,
                          schedule=options.schedule)
        elif vars(options).has_key('enable_scc'):
            if current_cc_backend() == BackendType.SCC:
                self.log.info("The SUSE Customer Center (SCC) backend is already "
                          "active, nothing to do.")
                print("The SUSE Customer Center (SCC) backend is already "
                      "active, nothing to do.")
            else:
                self._enable_scc()
        elif vars(options).has_key('delete_target'):
            if 'credentials' in options.delete_target:
                self._delete_credentials(options.target)

        if self.exit_with_error:
            return 1
        else:
            return 0

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
            self.log.info("No channels found.")
            print("No channels found.")
            return

        print "Available Channels{0}:\n".format(expand and " (full)" or "")
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
                                    prefix = "{0:02}) ".format(interactive_number)
                                available_channels.append(child.label)
                            elif show_interactive_numbers:
                                prefix = "    "

                            children_output.append("    " + prefix + output)

            if not filter or filter in parent_output.lower() or children_output:
                prefix = ""

                if base_channel.status == Channel.Status.AVAILABLE:
                    interactive_number += 1
                    if show_interactive_numbers:
                        prefix = "{0:02}) ".format(interactive_number)
                    available_channels.append(base_channel.label)
                elif show_interactive_numbers:
                    prefix = "    "
                print(prefix + parent_output)

                for child_output in children_output:
                    print(child_output)
        self.log.info(available_channels)
        return available_channels

    def _fetch_remote_channels(self):
        """ Returns the list of channels as reported by the remote server """
        return parse_channels(
            self._execute_xmlrpc_method(self.conn.sync.content,
                                        "listChannels", self.auth.token()), self.log)

    def _add_channels(self, channels, mirror=""):
        """ Add a list of channels.

        If the channel list is empty the interactive mode is started.
        """

        enable_checks = True
        current_channels = []

        if not channels:
            channels = [self._select_channel_interactive_mode()]
            enable_checks = False

        current_channels = self._fetch_remote_channels()

        for channel in channels:
            add_channel = True
            if enable_checks:
                match = find_channel_by_label(channel, current_channels, self.log)
                if match:
                    if match.status == Channel.Status.INSTALLED:
                        add_channel = False
                        self.log.info("Channel '{0}' has already been added".format(
                            channel))
                        print("Channel '{0}' has already been added".format(
                            channel))
                    elif match.status == Channel.Status.UNAVAILABLE:
                        self.log.error("Channel '{0}' is not available, skipping".format(
                            channel))
                        print("Channel '{0}' is not available, skipping".format(
                            channel))
                        self.exit_with_error = True
                        continue

                    if not match.base_channel:
                        parent = current_channels[match.parent]
                        if parent.status == Channel.Status.UNAVAILABLE:
                            self.log.error("Error, '{0}' depends on channel '{1}' which is not available".format(
                                    channel, parent.label))
                            self.log.error("'{0}' has not been added".format(channel))
                            sys.stderr.write(
                                "Error, '{0}' depends on channel '{1}' which is not available\n".format(
                                    channel, parent.label))
                            sys.stderr.write(
                                "'{0}' has not been added\n".format(channel))
                            self.exit_with_error = True
                            continue
                        if parent.status == Channel.Status.AVAILABLE:
                            self.log.info("'{0}' depends on channel '{1}' which has not been added yet".format(
                                channel, parent.label))
                            self.log.info("Going to add '{0}'".format(
                                parent.label))
                            print("'{0}' depends on channel '{1}' which has not been added yet".format(
                                channel, parent.label))
                            print("Going to add '{0}'".format(
                                parent.label))
                            self._add_channels([parent.label])

            if add_channel:
                self.log.debug("Adding channel '{0}'".format(channel))
                print("Adding '{0}' channel".format(channel))
                self._execute_xmlrpc_method(self.conn.sync.content,
                                            "addChannel",
                                            self.auth.token(),
                                            channel,
                                            mirror)
                match = find_channel_by_label(channel, current_channels, self.log)
                if match:
                    match.status = Channel.Status.INSTALLED

            print("Scheduling reposync for '{0}' channel".format(channel))
            self._schedule_channel_reposync(channel)

    def _schedule_channel_reposync(self, channel):
        """ Schedules a reposync for the given channel.

        :param channel: the label identifying the channel
        """

        try:
            self.log.info("Scheduling reposync for '{0}' channel".format(
                channel))
            self._execute_xmlrpc_method(self.conn.channel.software,
                                        "syncRepo",
                                        self.auth.token(),
                                        channel)
        except xmlrpclib.Fault, ex:
            if ex.faultCode == 2802:
                self.log.error("Error, unable to schedule channel reposync: Taskomatic is not responding.")
                sys.stderr.write("Error, unable to schedule channel reposync: Taskomatic is not responding.\n")
                sys.exit(1)

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

        self.log.info("Selecting channel '{0}' from choice '{1}'".format(
            channels[int(choice)-1], choice))
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
                                        "listProducts", self.auth.token()), self.log)

    def _list_products(self, filter, expand=False,
                       show_interactive_numbers=False):
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
            expand = True

        products = self._fetch_remote_products()

        if not products:
            self.log.info("No products found.")
            print("No products found.")
            return

        print("Available Products:\n")
        print("\nStatus:")
        print("  - [I] - product is installed")
        print("  - [ ] - product is not installed, but is available")
        print("  - [U] - product is unavailable\n")

        for product in products:
            product.to_stdout(filter=filter,
                              expand=expand,
                              interactive_data=interactive_data)
            self.log.info("{0} {1}".format(product.friendly_name, product.arch))

        return interactive_data

    def _add_products(self, mirror=""):
        """ Add a list of products.

        If the products list is empty the interactive mode is started.
        """

        product = self._select_product_interactive_mode()
        if not product:
            return

        if product.status == Product.Status.INSTALLED:
            self.log.info("Product '{0}' has already been added".format(
                product.friendly_name))
            print("Product '{0}' has already been added".format(
                product.friendly_name))
            return

        mandatory_channels = [c.label for c in product.channels
                              if not c.optional]

        self.log.debug("Adding channels required by '{0}' product".format(
            product.friendly_name))
        print("Adding channels required by '{0}' product".format(
            product.friendly_name))
        self._add_channels(channels=mandatory_channels, mirror=mirror)
        self.log.info("Product successfully added")
        print("Product successfully added")

    def _select_product_interactive_mode(self):
        """Show not installed products prefixing a number, then reads
        user input and returns the label of the chosen product

        """
        interactive_data = self._list_products(
            filter=None, expand=False, show_interactive_numbers=True)

        num_prod = interactive_data['num_prod']
        if num_prod:
            validator = lambda i: re.search("\d+", i) and \
                int(i) in range(1, len(num_prod.keys()) + 1)
            choice = cli_ask(
                msg=("Enter product number (1-{0})".format(
                    len(num_prod.keys()))),
                validator=validator)

            self.log.info("Selecting product '{0} {1}' from choice '{2}'".format(
                num_prod[int(choice)].friendly_name, num_prod[int(choice)].arch,
                choice))
            return num_prod[int(choice)]
        else:
            self.log.info("All the available products have already been "
                      "installed, nothing to do")
            print("All the available products have already been installed, "
                  "nothing to do")
            return None

    ##############################
    #                            #
    # Credential related methods #
    #                            #
    ##############################

    def _fetch_credentials(self):
        """ Returns the list of credentials as reported by the remote server """
        return self._execute_xmlrpc_method(self.conn.sync.content,
                                    "listCredentials", self.auth.token())

    def _list_credentials(self, show_interactive_numbers=False):
        """
        List credentials in the SUSE Manager database.
        """
        credentials = self._fetch_credentials()
        interactive_number = 0

        if credentials:
            print("Credentials:")
            for credential in credentials:
                msg=credential['user']
                self.log.info(credential['user'])
                if show_interactive_numbers:
                    interactive_number += 1
                    msg = "{0:02}) {1}".format(interactive_number, msg)
                if credential['isPrimary']:
                    msg += " (primary)"
                print(msg)
        else:
            self.log.info("No credentials found")
            print("No credentials found")

    def _add_credentials(self, primary, credentials):
        """
        Add credentials to the SUSE Manager database.
        """
        if not credentials:
            user = cli_ask(
                msg=("User to add"))
            pw = cli_ask(
                msg=("Password to add"),
                password=True)
            if not pw == cli_ask(msg=("Confirm password"),password=True):
                self.log.error("Passwords do not match")
                print("Passwords do not match")
                self.exit_with_error = True
                return
        else:
            user = credentials[0]
            pw = credentials[1]

        saved_users = self._fetch_credentials()
        if any(user == saved_user['user'] for saved_user in saved_users):
            self.log.error("Credentials already exist")
            print("Credentials already exist")
            self.exit_with_error = True
        else:
            self._execute_xmlrpc_method(self.conn.sync.content,
                                        "addCredentials",
                                        self.auth.token(),
                                        user,
                                        pw,
                                        primary)
            self.log.info("Successfully added credentials.")
            print("Successfully added credentials.")

    def _delete_credentials(self, credentials):
        """
        Delete credentials from the SUSE Manager database.

        If the credentials list is empty interactive mode is used.
        """
        interactive = False

        if not credentials:
            credentials = self._delete_credentials_interactive_mode()
            interactive = True

        saved_credentials = self._fetch_credentials()
        for user in credentials:
            if any(user == saved_user['user'] for saved_user in  saved_credentials):
                if interactive:
                    confirm = cli_ask(
                        msg=("Really delete credentials '{0}'? (y/n)".format(user)))
                    if not re.search("[yY]", confirm):
                        return
                self._execute_xmlrpc_method(self.conn.sync.content,
                                            "deleteCredentials",
                                            self.auth.token(),
                                            user)

                self.log.info("Successfully deleted credentials: {0}".format( user))
                print("Successfully deleted credentials: {0}".format( user))
            else:
                self.log.error("Credentials not found in database: {0}".format(user))
                print("Credentials not found in database: {0}".format(user))
                self.exit_with_error = True

    def _delete_credentials_interactive_mode(self):
        """
        Show saved credentials prefixed with a number, read the user input
        and return the chosen credential.
        """
        credentials = [];
        saved_credentials = self._fetch_credentials()
        validator = lambda i: re.search("\d+", i) and \
            int(i) in range(1, len(saved_credentials)+1)

        self._list_credentials(True);
        number = cli_ask(
            msg=("Enter credentials number (1-{0})".format(len(saved_credentials))),
                 validator=validator)

        self.log.info("Selecting credentials '{0}' from choice '{1}'".format(
            saved_credentials[int(number)-1]['user'], number))
        return [saved_credentials[int(number)-1]['user']]


    #################
    #               #
    # Other methods #
    #               #
    #################

    def _refresh(self, enable_reposync, mirror="", schedule=False):
        """
        Refresh the SCC data in the SUSE Manager database.
        """
        self.log.info("Refreshing SCC data...")
        token = self.auth.token()
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

        if hasISSMaster() or schedule:
            client = xmlrpclib.Server(TASKOMATIC_XMLRPC_URL)
            params = {}
            params['noRepoSync'] = not enable_reposync

            try:
                self.log.debug("Calling Taskomatic refresh with '{0}'".format(
                    params))
                client.tasko.scheduleSingleSatBunchRun('mgr-sync-refresh-bunch', params)
            except xmlrpclib.Fault, e:
                self.log.error("Error scheduling refresh: {0}".format(e))
                sys.stderr.write("Error scheduling refresh: {0}\n".format(e))
                return
            self.log.info("Refresh successfully scheduled")
            sys.stdout.write("Refresh successfully scheduled\n")
            sys.stdout.flush()
            return

        for operation, method in actions:
            sys.stdout.write("Refreshing {0}".format(operation))
            sys.stdout.flush()
            try:
                if method == "synchronizeChannels":
                    self._execute_xmlrpc_method(self.conn.sync.content, method, token, mirror)
                else:
                    self._execute_xmlrpc_method(self.conn.sync.content, method,
                                                token)
                self.log.info("Refreshing {0} succeeded".format(operation.rstrip()))
                sys.stdout.write("[DONE]".rjust(text_width) + "\n")
                sys.stdout.flush()
            except Exception, ex:
                self.log.error("Refreshing {0} failed".format(operation.rstrip()))
                self.log.error("Error: {0}".format(ex))
                sys.stdout.write("[FAIL]".rjust(text_width) + "\n")
                sys.stdout.flush()
                sys.stderr.write("\tError: {0}\n\n".format(ex))
                self.exit_with_error = True
                return False

        if enable_reposync:
            self.log.info("Scheduling refresh of all the available channels")
            print("\nScheduling refresh of all the available channels")

            base_channels = self._fetch_remote_channels()
            for bc_label in sorted(base_channels.keys()):
                bc = base_channels[bc_label]

                if bc.status != Channel.Status.INSTALLED:
                    continue

                self.log.debug("Scheduling reposync for '{0}' channel".format(bc.label))
                print("Scheduling reposync for '{0}' channel".format(bc.label))
                self._schedule_channel_reposync(bc.label)
                for child in bc.children:
                    if child.status == Channel.Status.INSTALLED:
                        self.log.debug("Scheduling reposync for '{0}' channel".format(
                            child.label))
                        print("Scheduling reposync for '{0}' channel".format(
                            child.label))
                        self._schedule_channel_reposync(child.label)
        return True

    def _enable_scc(self, retry_on_session_failure=True):
        """ Enable the SCC backend """

        self.log.info("Enabling SCC...")
        if current_cc_backend() == BackendType.NCC:

            try:
                switch_to_scc(self.conn, self.auth.token())
            except xmlrpclib.Fault, ex:
                if retry_on_session_failure and self._check_session_fail(ex):
                    self.log.debug("Retrying after session failure: {0}".format(ex))
                    self.auth.discard_token()
                    return self._enable_scc(retry_on_session_failure=False)
                else:
                    self.log.error("Error: {0}".format(ex))
                    raise ex
            if not self._refresh(enable_reposync=False):
                sys.exit(1)
            self.log.info("SCC backend successfully migrated.")
            print("SCC backend successfully migrated.")
        else:
            self.log.info("SUSE Manager is already using the SCC backend.")
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
            self.log.debug("Invoking remote method {0} with auth_token {1}".format(
                method, auth_token))
            return getattr(endoint, method)(auth_token, *params)
        except xmlrpclib.Fault, ex:
            if retry_on_session_failure and self._check_session_fail(ex):
                self.log.info("Retrying after session failure: {0}".format(ex))
                self.auth.discard_token()
                auth_token = self.auth.token()
                return self._execute_xmlrpc_method(
                    endoint, method, auth_token, *params,
                    retry_on_session_failure=False)
            else:
                self.log.error("Error: {0}".format(ex))
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

    def _is_scc_allowed(self):
        return os.path.isfile("/var/lib/spacewalk/scc/default_scc")
