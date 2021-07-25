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
from spacecmd.i18n import _N
from spacecmd.utils import *

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

def help_ssm(self):
    print(_('The System Set Manager (SSM) is a group of systems that you '))
    print(_('can perform tasks on as a whole.'))
    print('')
    print(_('Adding Systems:'))
    print('> ssm_add group:rhel5-x86_64')
    print('> ssm_add channel:rhel-x86_64-server-5')
    print('> ssm_add search:device:vmware')
    print('> ssm_add host.example.com')
    print('')
    print(_('Intersections:'))
    print('> ssm_add group:rhel5-x86_64')
    print('> ssm_intersect group:web-servers')
    print('')
    print(_('Using the SSM:'))
    print('> system_installpackage ssm zsh')
    print('> system_runscript ssm')

####################


def help_ssm_add(self):
    print(_('ssm_add: Add systems to the SSM'))
    print(_('usage: ssm_add <SYSTEMS>'))
    print('')
    print(_("see 'help ssm' for more details"))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_ssm_add(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_ssm_add(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_ssm_add()
        return 1

    systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems found'))
        return 1

    for system in systems:
        if system in self.ssm:
            logging.warning(_N('%s is already in the list') % system)
            continue
        self.ssm[system] = self.get_system_id(system)
        logging.debug('Added %s' % system)

    if self.ssm:
        logging.debug('Systems Selected: %i' % len(self.ssm))

    # save the SSM for use between sessions
    save_cache(self.ssm_cache_file, self.ssm)

    return 0

####################


def help_ssm_intersect(self):
    print(_('ssm_intersect: Replace the current SSM with the intersection'))
    print(_('               of the current list of systems and the list of'))
    print(_('               systems passed as arguments'))
    print(_('usage: ssm_intersect <SYSTEMS>'))
    print('')
    print(_("see 'help ssm' for more details"))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_ssm_intersect(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_ssm_intersect(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_ssm_intersect()
        return 1

    systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems found'))
        return 1

    # tmp_ssm placeholder to gather systems that are both in original ssm
    # selection and newly selected group
    tmp_ssm = {}
    for system in systems:
        if system in self.ssm:
            logging.debug('%s is in both groups: leaving in SSM' % system)
            tmp_ssm[system] = self.ssm[system]

    # set self.ssm to tmp_ssm, which now holds the intersection
    self.ssm = tmp_ssm

    # save the SSM for use between sessions
    save_cache(self.ssm_cache_file, self.ssm)

    if self.ssm:
        logging.debug('Systems Selected: %i' % len(self.ssm))

    return 0

####################


def help_ssm_remove(self):
    print(_('ssm_remove: Remove systems from the SSM'))
    print(_('usage: ssm_remove <SYSTEMS>'))
    print('')
    print(_("see 'help ssm' for more details"))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_ssm_remove(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_ssm_remove(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_ssm_remove()
        return 1

    systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems found'))
        return 1

    for system in systems:
        # double-check for existance in case of duplicate names
        if system in self.ssm:
            logging.debug('Removed %s' % system)
            del self.ssm[system]

    logging.debug('Systems Selected: %i' % len(self.ssm))

    # save the SSM for use between sessions
    save_cache(self.ssm_cache_file, self.ssm)

    return 0

####################


def help_ssm_list(self):
    print(_('ssm_list: List the systems currently in the SSM'))
    print(_('usage: ssm_list'))
    print('')
    print(_("see 'help ssm' for more details"))


def do_ssm_list(self, args):
    systems = sorted(self.ssm)

    if systems:
        print('\n'.join(systems))
        logging.debug('Systems Selected: %i' % len(systems))
        return 0
    else:
        return 1

####################


def help_ssm_clear(self):
    print(_('ssm_clear: Remove all systems from the SSM'))
    print(_('usage: ssm_clear'))


def do_ssm_clear(self, args):
    self.ssm = {}

    # save the SSM for use between sessions
    save_cache(self.ssm_cache_file, self.ssm)

    return 0
