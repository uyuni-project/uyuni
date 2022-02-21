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

# invalid function name
# pylint: disable=C0103

import gettext
from spacecmd.utils import *

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

def help_filepreservation_list(self):
    print(_('filepreservation_list: List all file preservations'))
    print(_('usage: filepreservation_list'))


def do_filepreservation_list(self, args, doreturn=False):
    lists = [l.get('name') for l in self.client.kickstart.filepreservation.listAllFilePreservations(self.session)]

    if not doreturn:
        print('\n'.join(sorted(lists)))
        return None

    return lists

####################


def help_filepreservation_create(self):
    print(_('filepreservation_create: Create a file preservation list'))
    print(_('usage: filepreservation_create [NAME] [FILE ...]'))


def do_filepreservation_create(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if args:
        name = args[0]
    else:
        name = prompt_user(_('Name:'), noblank=True)

    if len(args) > 1:
        files = args[1:]
    else:
        files = []

        while True:
            print(_('File List'))
            print('---------')
            print('\n'.join(sorted(files)))
            print('')

            userinput = prompt_user(_('File [blank to finish]:'))

            if userinput == '':
                break
            if userinput not in files:
                files.append(userinput)

    print('')
    print(_('File List'))
    print('---------')
    print('\n'.join(sorted(files)))

    if not self.user_confirm():
        return 1

    self.client.kickstart.filepreservation.create(self.session,
                                                  name,
                                                  files)

    return 0

####################


def help_filepreservation_delete(self):
    print(_('filepreservation_delete: Delete a file preservation list'))
    print(_('usage: filepreservation_delete NAME'))


def complete_filepreservation_delete(self, text, line, beg, end):
    return tab_completer(self.do_filepreservation_list('', True), text)


def do_filepreservation_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_filepreservation_delete()
        return 1

    name = args[0]

    if not self.user_confirm(_('Delete this list [y/N]:')):
        return 1

    self.client.kickstart.filepreservation.delete(self.session, name)

    return 0

####################


def help_filepreservation_details(self):
    print(_('''filepreservation_details: Show the details of a file
'preservation list'''))
    print(_('usage: filepreservation_details NAME'))


def complete_filepreservation_details(self, text, line, beg, end):
    return tab_completer(self.do_filepreservation_list('', True), text)


def do_filepreservation_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_filepreservation_details()
        return 1

    name = args[0]

    details = \
        self.client.kickstart.filepreservation.getDetails(self.session,
                                                          name)

    print('\n'.join(sorted(details.get('file_names'))))

    return 0
