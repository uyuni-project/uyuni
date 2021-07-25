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
from spacecmd.i18n import _N
from spacecmd.utils import *

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

def help_custominfo_createkey(self):
    print(_('custominfo_createkey: Create a custom key'))
    print(_('usage: custominfo_createkey [NAME] [DESCRIPTION]'))


def do_custominfo_createkey(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if args:
        key = args[0]
    else:
        key = ''

    while key == '':
        key = prompt_user(_('Name:'))

    if len(args) > 1:
        description = ' '.join(args[1:])
    else:
        description = prompt_user(_('Description:'))
        if description == '':
            description = key

    self.client.system.custominfo.createKey(self.session,
                                            key,
                                            description)

    return 0

####################


def help_custominfo_deletekey(self):
    print(_('custominfo_deletekey: Delete a custom key'))
    print(_('usage: custominfo_deletekey KEY ...'))


def complete_custominfo_deletekey(self, text, line, beg, end):
    return tab_completer(self.do_custominfo_listkeys('', True), text)


def do_custominfo_deletekey(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_custominfo_deletekey()
        return 1

    # allow globbing of custominfo key names
    keys = filter_results(self.do_custominfo_listkeys('', True), args)
    logging.debug("customkey_deletekey called with args %s, keys=%s" %
                  (args, keys))

    if not keys:
        logging.error(_N("No keys matched argument %s") % args)
        return 1

    # Print the keys prior to the confirmation
    print('\n'.join(sorted(keys)))

    if not self.user_confirm('Delete these keys [y/N]:'):
        return 1

    for key in keys:
        self.client.system.custominfo.deleteKey(self.session, key)

    return 0

####################


def help_custominfo_listkeys(self):
    print(_('custominfo_listkeys: List all custom keys'))
    print(_('usage: custominfo_listkeys'))


def do_custominfo_listkeys(self, args, doreturn=False):
    keys = self.client.system.custominfo.listAllKeys(self.session)
    keys = [k.get('label') for k in keys]

    if doreturn:
        return keys
    if keys:
        print('\n'.join(sorted(keys)))

    return None

####################


def help_custominfo_details(self):
    print(_('custominfo_details: Show the details of a custom key'))
    print(_('usage: custominfo_details KEY ...'))


def complete_custominfo_details(self, text, line, beg, end):
    return tab_completer(self.do_custominfo_listkeys('', True), text)


def do_custominfo_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_custominfo_details()
        return 1

    # allow globbing of custominfo key names
    keys = filter_results(self.do_custominfo_listkeys('', True), args)
    logging.debug("customkey_details called with args: '{}', keys: '{}'.".format(
        ", ".join(args), ", ".join(keys)))

    if not keys:
        logging.error(_N("No keys matched argument '{}'.").format(", ".join(args)))
        return 1

    add_separator = False

    all_keys = self.client.system.custominfo.listAllKeys(self.session)

    for key in keys:
        details = {}
        for key_details in all_keys:
            if key_details.get('label') == key:
                details = key_details

        if details:
            if add_separator:
                print(self.SEPARATOR)
            add_separator = True
            print(_('Label:        %s') % (details.get('label') or "N/A"))
            print(_('Description:  %s') % (details.get('description') or "N/A"))
            print(_('Modified:     %s') % (details.get('last_modified') or "N/A"))
            print(_('System Count: %i') % (details.get('system_count') or 0))

    return 0

####################


def help_custominfo_updatekey(self):
    print(_('custominfo_updatekey: Update a custom key'))
    print(_('usage: custominfo_updatekey [NAME] [DESCRIPTION]'))


def do_custominfo_updatekey(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if args:
        key = args[0]
    else:
        key = ''

    while key == '':
        key = prompt_user(_('Name:'))

    if len(args) > 1:
        description = ' '.join(args[1:])
    else:
        description = prompt_user(_('Description:'))
        if description == '':
            description = key

    self.client.system.custominfo.updateKey(self.session,
                                            key,
                                            description)

    return 0
