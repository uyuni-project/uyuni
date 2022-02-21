#
# Licensed under the GNU General Public License Version 3
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Copyright 2013 Aron Parsons <aronparsons@gmail.com>
#

# NOTE: the 'self' variable is an instance of SpacewalkShell

# wildcard import
# pylint: disable=W0401,W0614

# unused argument
# pylint: disable=W0613

import gettext
try:
    from xmlrpc import client as xmlrpclib
except ImportError:
    import xmlrpclib
from spacecmd.i18n import _N
from spacecmd.utils import *

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

def help_cryptokey_create(self):
    print(_('cryptokey_create: Create a cryptographic key'))
    print(_('''usage: cryptokey_create [options])

options:
  -t GPG or SSL
  -d DESCRIPTION
  -f KEY_FILE'''))


def do_cryptokey_create(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-t', '--type')
    arg_parser.add_argument('-d', '--description')
    arg_parser.add_argument('-f', '--file')

    (args, options) = parse_command_arguments(args, arg_parser)
    options.contents = None

    if is_interactive(options):
        options.type = prompt_user(_('GPG or SSL [G/S]:'))

        options.description = ''
        while options.description == '':
            options.description = prompt_user(_('Description:'))

        if self.user_confirm(_('Read an existing file [y/N]:'),
                             nospacer=True, ignore_yes=True):
            options.file = prompt_user('File:')
        else:
            options.contents = editor(delete=True)
    else:
        if not options.type:
            logging.error(_N('The key type is required'))
            return 1

        if not options.description:
            logging.error(_N('A description is required'))
            return 1

        if not options.file:
            logging.error(_N('A file containing the key is required'))
            return 1

    # read the file the user specified
    if options.file:
        options.contents = read_file(options.file)

    if not options.contents:
        logging.error(_N('No contents of the file'))
        return 1

    # translate the key type to what the server expects
    if re.match('G', options.type, re.I):
        options.type = 'GPG'
    elif re.match('S', options.type, re.I):
        options.type = 'SSL'
    else:
        logging.error(_N('Invalid key type'))
        return 1

    self.client.kickstart.keys.create(self.session,
                                      options.description,
                                      options.type,
                                      options.contents)

    return 0

####################


def help_cryptokey_delete(self):
    print(_('cryptokey_delete: Delete a cryptographic key'))
    print(_('usage: cryptokey_delete NAME'))


def complete_cryptokey_delete(self, text, line, beg, end):
    if len(line.split(' ')) <= 2:
        return tab_completer(self.do_cryptokey_list('', True),
                             text)

    return None

def do_cryptokey_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_cryptokey_delete()
        return 1

    # allow globbing of cryptokey names
    keys = filter_results(self.do_cryptokey_list('', True), args)
    logging.debug("cryptokey_delete called with args %s, keys=%s" %
                  (args, keys))

    if not keys:
        logging.error(_N("No keys matched argument %s") % args)
        return 1

    # Print the keys prior to the confirmation
    print('\n'.join(sorted(keys)))

    if self.user_confirm(_('Delete key(s) [y/N]:')):
        for key in keys:
            self.client.kickstart.keys.delete(self.session, key)
        return 0
    else:
        return 1

####################


def help_cryptokey_list(self):
    print(_('cryptokey_list: List all cryptographic keys (SSL, GPG)'))
    print(_('usage: cryptokey_list'))


def do_cryptokey_list(self, args, doreturn=False):
    keys = self.client.kickstart.keys.listAllKeys(self.session)
    keys = [k.get('description') for k in keys]

    if doreturn:
        return keys
    if keys:
        print('\n'.join(sorted(keys)))

    return None

####################


def help_cryptokey_details(self):
    print(_('cryptokey_details: Show the contents of a cryptographic key'))
    print(_('usage: cryptokey_details KEY ...'))


def complete_cryptokey_details(self, text, line, beg, end):
    return tab_completer(self.do_cryptokey_list('', True), text)


def do_cryptokey_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_cryptokey_details()
        return 1

    # allow globbing of cryptokey names
    keys = filter_results(self.do_cryptokey_list('', True), args)
    logging.debug("cryptokey_details called with args %s, keys=%s" %
                  (args, keys))

    if not keys:
        logging.error(_N("No keys matched argument %s") % args)
        return 1

    add_separator = False

    for key in keys:
        try:
            details = self.client.kickstart.keys.getDetails(self.session,
                                                            key)
        except xmlrpclib.Fault:
            logging.warning(_N('%s is not a valid crypto key') % key)
            return 1

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print(_('Description: %s') % details.get('description'))
        print(_('Type:        %s') % details.get('type'))

        print('')
        print(details.get('content'))

    return 0
