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
import os
import re
import shlex
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


def help_group_addsystems(self):
    print(_('group_addsystems: Add systems to a group'))
    print(_('usage: group_addsystems GROUP <SYSTEMS>'))
    print(_('       group_addsystems GROUP <ssm>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_group_addsystems(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return tab_completer(self.do_group_list('', True), text)
    elif len(parts) > 2:
        return self.tab_complete_systems(parts[-1])

    return None


def do_group_addsystems(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_addsystems()
        return 1

    group_name = args.pop(0)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    system_ids = []
    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue
        system_ids.append(system_id)

    if system_ids:
        self.client.systemgroup.addOrRemoveSystems(self.session, group_name,
                                                   system_ids, True)
        return 0
    else:
        return 1

####################


def help_group_removesystems(self):
    print(_('group_removesystems: Remove systems from a group'))
    print(_('usage: group_removesystems GROUP <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_group_removesystems(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return tab_completer(self.do_group_list('', True), text)
    elif len(parts) > 2:
        return self.tab_complete_systems(parts[-1])

    return None


def do_group_removesystems(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_removesystems()
        return 1

    group_name = args.pop(0)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    system_ids = []
    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue
        system_ids.append(system_id)

    if system_ids:
        print(_('Systems'))
        print('-------')
        print('\n'.join(sorted(systems)))

        if not self.user_confirm(_('Remove these systems [y/N]:')):
            return None

        self.client.systemgroup.addOrRemoveSystems(self.session,
                                                   group_name,
                                                   system_ids,
                                                   False)
        return 0
    else:
        print(_("No systems found"))
        return 1

    return None

####################


def help_group_create(self):
    print(_('group_create: Create a system group'))
    print(_('usage: group_create [NAME] [DESCRIPTION]'))


def do_group_create(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if args:
        name = args[0]
    else:
        name = prompt_user(_('Name:'))

    if len(args) > 1:
        description = ' '.join(args[1:])
    else:
        description = prompt_user(_('Description:'))

    self.client.systemgroup.create(self.session, name, description)

    return 0

####################


def help_group_delete(self):
    print(_('group_delete: Delete a system group'))
    print(_('usage: group_delete NAME ...'))


def complete_group_delete(self, text, line, beg, end):
    return tab_completer(self.do_group_list('', True), text)


def do_group_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_delete()
        return 1

    groups = args

    self.do_group_details('', True)
    if not self.user_confirm(_('Delete these groups [y/N]:')):
        return 1

    for group in groups:
        self.client.systemgroup.delete(self.session, group)

    return 0

####################


def help_group_backup(self):
    print(_('group_backup: backup a system group'))
    print(_('''usage: group_backup <NAME> [OUTDIR])
                    group_backup ALL

"OUTDIR" defaults to $HOME/spacecmd-backup/group/YYYY-MM-DD/NAME
"ALL" is a keyword and collects all groups
'''))


def complete_group_backup(self, text, line, beg, end):
    List = self.do_group_list('', True)
    List.append('ALL')
    return tab_completer(List, text)


def do_group_backup(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_backup()
        return 1

    groups = args
    if len(args) > 0 and args[0] == 'ALL':      # pylint: disable=len-as-condition
        groups = self.do_group_list('', True)

    # use an output base from the user if it was passed
    if len(args) > 1:
        outputpath_base = datetime.now().strftime(os.path.expanduser(args[1]))
    else:
        outputpath_base = os.path.expanduser('~/spacecmd-backup/group')

        # make the final output path be <base>/date
        outputpath_base = os.path.join(outputpath_base,
                                       datetime.now().strftime("%Y-%m-%d"))

    try:
        if not os.path.isdir(outputpath_base):
            os.makedirs(outputpath_base)
    except OSError:
        logging.error(_N('Could not create output directory: %s'), outputpath_base)
        return 1

    for group in groups:
        print(_("Backup Group: %s") % group)
        details = self.client.systemgroup.getDetails(self.session, group)
        formulas = self.client.formula.getFormulasByGroupId(self.session, details['id'])
        formula_data = {
            f: self.client.formula.getGroupFormulaData(self.session, details['id'], f)
            for f in formulas
        }

        outputpath = outputpath_base + "/" + group
        print(_("Output File: %s") % outputpath)

        backup = {
            'description': details['description'],
            'formulas': formula_data
        }

        json_dump_to_file(backup, outputpath)

    return 0

####################


def help_group_restore(self):
    print(_('group_restore: restore a system group'))
    print(_('''
usage: group_restore INPUTDIR [NAME] ...
       group_restore INPUTDIR ALL
       group_restore INPUTDIR
       group_restore .

Specifying only INPUTDIR will default to ALL groups.
Setting dot (.) instead of full INPUTDIR will imply current directory.
    '''))


def complete_group_restore(self, text, line, beg, end):
    parts = shlex.split(line)

    if len(parts) > 1:
        groups = self.do_group_list('', True)
        groups.append('ALL')
        return tab_completer(groups, text)

    return None


def do_group_restore(self, args):
    args, options = parse_command_arguments(args, get_argument_parser())

    files = {}
    current = {}

    if args:
        inputdir = os.getcwd() if args[0] == "." else args[0]
        groups = args[1:] or ["ALL"]
    else:
        self.help_group_restore()
        return 1

    inputdir = os.path.abspath(inputdir)
    logging.debug("Input Directory: %s" % (inputdir))

    # make a list of file items in the input dir
    if os.path.isdir(inputdir):
        d_content = os.listdir(inputdir)
        for d_item in d_content:
            if os.path.isfile(inputdir + "/" + d_item):
                logging.debug("Found file %s" % inputdir + "/" + d_item)
                files[d_item] = inputdir + "/" + d_item
    else:
        logging.error(_N("Restore dir %s does not exits or is not a directory") % inputdir)
        return 1

    if not files:
        logging.error(_N("Restore dir %s has no restore items") % inputdir)
        return 1

    missing_groups = 0
    if groups and next(iter(groups)) != 'ALL':
        for group in groups:
            if group not in files:
                logging.error(_N("Group %s was not found in backup") % (group))
                missing_groups += 1
    if missing_groups:
        logging.error(_N("Found %s missing groups, terminating"), missing_groups)
        return 1

    for groupname in self.do_group_list('', True):
        details = self.client.systemgroup.getDetails(self.session, groupname)
        formulas = self.client.formula.getFormulasByGroupId(self.session, details['id'])
        formula_data = {}
        for f in formulas:
            formula_data[f] = self.client.formula.getGroupFormulaData(self.session, details['id'], f)

        current[groupname] = {
            'description': details['description'],
            'id': details['id'],
            'formulas': formula_data
        }

    for groupname in files:
        backup = json_read_from_file(files[groupname])
        if backup is None:
            # assume old backup, not in json format. Read complete file as string
            logging.info(_("Assuming group to be in old plain text format"))
            with open(files[groupname], 'r') as fh:
                details = fh.read()
            backup = {
                'description': details.rstrip('\n'),
                'formulas': {}
            }

        if groupname in current:
            # pass id to the backup structure, so that comparison works
            backup['id'] = current[groupname]['id']
            if current[groupname] == backup:
                logging.error(_N("Group %s already restored") % groupname)
            else:
                logging.debug("Already have %s but the data have changed" % groupname)

                if is_interactive(options):
                    if current[groupname]['description'] != backup['description']:
                        print(_("Changing description from:"))
                        print('\n"%s"\nto\n"%s"\n' % (current[groupname]['description'], backup['description']))
                        userinput = prompt_user(_('Continue [y/N]:'))

                        if userinput.lower() == 'y':
                            logging.info(_N("Updating description for group: %s") % groupname)
                            self.client.systemgroup.update(self.session, groupname, backup['description'])
                    if current[groupname]['formulas'] != backup['formulas']:
                        userinput = prompt_user(_('Update formula data from backup? [y/N]:'))
                        if userinput.lower() == 'y':
                            formulas = list(backup['formulas'].keys())
                            self.client.formula.setFormulasOfGroup(self.session, current[groupname]['id'], formulas)
                            for f, v in backup['formulas'].items():
                                self.client.formula.setGroupFormulaData(self.session, current[groupname]['id'], f, v)
                else:
                    logging.info(_N("Updating data for group: %s") % groupname)
                    self.client.systemgroup.update(self.session, groupname, backup['description'])
                    formulas = list(backup['formulas'].keys())
                    self.client.formula.setFormulasOfGroup(self.session, current[groupname]['id'], formulas)
                    for f, v in backup['formulas'].items():
                        self.client.formula.setGroupFormulaData(self.session, current[groupname]['id'], f, v)

        else:
            logging.info(_N("Creating new group %s") % groupname)
            details = self.client.systemgroup.create(self.session, groupname, backup['description'])
            formulas = list(backup['formulas'].keys())
            self.client.formula.setFormulasOfGroup(self.session, details['id'], formulas)
            for f, v in backup['formulas'].items():
                self.client.formula.setGroupFormulaData(self.session, details['id'], f, v)

    return 0

####################


def help_group_list(self):
    print(_('group_list: List available system groups'))
    print(_('usage: group_list'))


def do_group_list(self, args, doreturn=False):
    groups = self.client.systemgroup.listAllGroups(self.session)
    groups = [g.get('name') for g in groups]

    if doreturn:
        return groups
    if groups:
        print('\n'.join(sorted(groups)))
    return None

####################


def help_group_listsystems(self):
    print(_('group_listsystems: List the members of a group'))
    print(_('usage: group_listsystems GROUP'))


def complete_group_listsystems(self, text, line, beg, end):
    return tab_completer(self.do_group_list('', True), text)


def do_group_listsystems(self, args, doreturn=False):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 1:
        self.help_group_listsystems()
        return None

    group = args[0]

    try:
        systems = self.client.systemgroup.listSystems(self.session, group)
        systems = [s.get('profile_name') for s in systems]
    except xmlrpclib.Fault:
        logging.warning(_N('%s is not a valid group') % group)
        return []

    if doreturn:
        return systems
    if systems:
        print('\n'.join(sorted(systems)))

    return None

####################


def help_group_details(self):
    print(_('group_details: Show the details of a system group'))
    print(_('usage: group_details GROUP ...'))


def complete_group_details(self, text, line, beg, end):
    return tab_completer(self.do_group_list('', True), text)


def do_group_details(self, args, short=False):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_details()
        return 1

    add_separator = False

    for group in args:
        try:
            details = self.client.systemgroup.getDetails(self.session, group)
            systems = [stm.get('profile_name') for stm in self.client.systemgroup.listSystems(self.session, group)]
        except xmlrpclib.Fault:
            logging.warning(_N('The group "%s" is invalid') % group)
            return 1

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print(_('ID:                %i') % details.get('id'))
        print(_('Name:              %s') % details.get('name'))
        print(_('Description:       %s') % details.get('description'))
        print(_('Number of Systems: %i') % details.get('system_count'))

        if not short:
            print('')
            print(_('Members'))
            print('-------')
            print('\n'.join(sorted(systems)))

    return 0

####################

def help_group_listconfigchannels(self):
    print(_('group_listconfigchannels: List configuration channels assigned to a system group'))
    print(_('usage: group_listconfigchannels GROUP'))

def complete_group_listconfigchannels(self, text, line, beg, end):
    return tab_completer(self.do_group_list('', True), text)

def do_group_listconfigchannels(self, args):
    if not self.check_api_version('25.0'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_listconfigchannels()
        return 1

    add_separator = False

    for group in args:
        try:
            channels = self.client.systemgroup.listAssignedConfigChannels(self.session, group)
        except xmlrpclib.Fault:
            logging.warning(_N('The group "%s" is invalid') % group)
            return 1
        for details in channels:
            if add_separator:
                print(self.SEPARATOR)
            add_separator = True

            print(_('Label:       %s') % details.get('label'))
            print(_('Name:        %s') % details.get('name'))
            print(_('Description: %s') % details.get('description'))
            print(_('Type:        %s') % details.get('configChannelType', {}).get('label'))

    return 0

####################


def help_group_addconfigchannels(self):
    print(_('group_addconfigchannels: Add config channels to a group'))
    print(_('''usage: group_addconfigchannels <GROUP> <CHANNEL ...>'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_group_addconfigchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_completer(self.do_group_list('', True), text)
    elif len(parts) > 2:
        return tab_completer(self.do_configchannel_list('', True),
                             text)
    return None


def do_group_addconfigchannels(self, args):
    if not self.check_api_version('25.0'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()
    (args,_) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_addconfigchannels()
        return 1

    group = args.pop(0)
    channels = args
    self.client.systemgroup.subscribeConfigChannel(self.session, group, channels)
    return 0

####################


def help_group_removeconfigchannels(self):
    print(_('group_removeconfigchannels: Remove config channels from a group'))
    print(_('''usage: group_removeconfigchannels <GROUP> <CHANNEL ...>'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_group_removeconfigchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_completer(self.do_group_list('', True), text)
    elif len(parts) > 2:
        return tab_completer(self.do_configchannel_list('', True),
                             text)
    return None


def do_group_removeconfigchannels(self, args):
    if not self.check_api_version('25.0'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()
    (args,_) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_group_removeconfigchannels()
        return 1

    group = args.pop(0)
    channels = args
    self.client.systemgroup.unsubscribeConfigChannel(self.session, group, channels)
    return 0
