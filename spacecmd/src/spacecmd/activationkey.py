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
# Copyright (c) 2011--2018 Red Hat, Inc.
#

# NOTE: the 'self' variable is an instance of SpacewalkShell

# wildcard import
# pylint: disable=W0401,W0614

# unused argument
# pylint: disable=W0613

# invalid function name
# pylint: disable=C0103

import re
import shlex
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

def help_activationkey_addpackages(self):
    print(_('activationkey_addpackages: Add packages to an activation key'))
    print(_('usage: activationkey_addpackages KEY <PACKAGE ...>'))


def complete_activationkey_addpackages(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True),
                             text)
    elif len(parts) > 2:
        return tab_completer(self.get_package_names(), text)

    return None


def do_activationkey_addpackages(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_addpackages()
        return 1

    key = args.pop(0)
    packages = [{'name': a} for a in args]

    self.client.activationkey.addPackages(self.session, key, packages)

    return 0

####################


def help_activationkey_removepackages(self):
    print(_('activationkey_removepackages: Remove packages from an ' +
            'activation key'))
    print(_('usage: activationkey_removepackages KEY <PACKAGE ...>'))


def complete_activationkey_removepackages(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True),
                             text)
    elif len(parts) > 2:
        details = self.client.activationkey.getDetails(self.session,
                                                       parts[1])
        packages = [p['name'] for p in details.get('packages')]
        return tab_completer(packages, text)

    return None


def do_activationkey_removepackages(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_removepackages()
        return 1

    key = args.pop(0)
    packages = [{'name': a} for a in args]

    self.client.activationkey.removePackages(self.session, key, packages)

    return 0

####################


def help_activationkey_addgroups(self):
    print(_('activationkey_addgroups: Add groups to an activation key'))
    print(_('usage: activationkey_addgroups KEY <GROUP ...>'))


def complete_activationkey_addgroups(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        return tab_completer(self.do_group_list('', True), parts[-1])

    return None


def do_activationkey_addgroups(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_addgroups()
        return 1

    key = args.pop(0)

    groups = []
    for a in args:
        details = self.client.systemgroup.getDetails(self.session, a)
        groups.append(details.get('id'))

    self.client.activationkey.addServerGroups(self.session, key, groups)

    return 0

####################


def help_activationkey_removegroups(self):
    print(_('activationkey_removegroups: Remove groups from an activation key'))
    print(_('usage: activationkey_removegroups KEY <GROUP ...>'))


def complete_activationkey_removegroups(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        key_details = self.client.activationkey.getDetails(self.session,
                                                           parts[-1])

        groups = []
        for group in key_details.get('server_group_ids'):
            details = self.client.systemgroup.getDetails(self.session,
                                                         group)
            groups.append(details.get('name'))
        return tab_completer(groups, text)

    return None


def do_activationkey_removegroups(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_removegroups()
        return 1

    key = args.pop(0)

    groups = []
    for a in args:
        details = self.client.systemgroup.getDetails(self.session, a)
        groups.append(details.get('id'))

    self.client.activationkey.removeServerGroups(self.session, key, groups)

    return 0

####################


def help_activationkey_addentitlements(self):
    print(_('activationkey_addentitlements: Add entitlements to an ' +
            'activation key'))
    print(_('usage: activationkey_addentitlements KEY <ENTITLEMENT ...>'))


def complete_activationkey_addentitlements(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True),
                             text)
    elif len(parts) > 2:
        return tab_completer(self.ENTITLEMENTS, text)

    return None


def do_activationkey_addentitlements(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_addentitlements()
        return 1

    key = args.pop(0)
    entitlements = args

    self.client.activationkey.addEntitlements(self.session,
                                              key,
                                              entitlements)

    return 0

####################


def help_activationkey_removeentitlements(self):
    print(_('activationkey_removeentitlements: Remove entitlements from an ' +
            'activation key'))
    print(_('usage: activationkey_removeentitlements KEY <ENTITLEMENT ...>'))


def complete_activationkey_removeentitlements(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        details = \
            self.client.activationkey.getDetails(self.session, parts[1])
        entitlements = details.get('entitlements')
        return tab_completer(entitlements, text)

    return None


def do_activationkey_removeentitlements(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_removeentitlements()
        return 1

    key = args.pop(0)
    entitlements = args

    self.client.activationkey.removeEntitlements(self.session,
                                                 key,
                                                 entitlements)

    return 0

####################


def help_activationkey_addchildchannels(self):
    print(_('activationkey_addchildchannels: Add child channels to an ' +
            'activation key'))
    print(_('usage: activationkey_addchildchannels KEY <CHANNEL ...>'))


def complete_activationkey_addchildchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True),
                             text)
    elif len(parts) > 2:
        key_details = \
            self.client.activationkey.getDetails(self.session, parts[1])
        base_channel = key_details.get('base_channel_label')

        all_channels = \
            self.client.channel.listSoftwareChannels(self.session)

        child_channels = []
        for c in all_channels:
            if base_channel == 'none':
                # this gets all child channels
                if c.get('parent_label'):
                    child_channels.append(c.get('label'))
            else:
                if c.get('parent_label') == base_channel:
                    child_channels.append(c.get('label'))

        return tab_completer(child_channels, text)

    return None


def do_activationkey_addchildchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_addchildchannels()
        return 1

    key = args.pop(0)
    channels = args

    self.client.activationkey.addChildChannels(self.session, key, channels)

    return 0

####################


def help_activationkey_removechildchannels(self):
    print(_('activationkey_removechildchannels: Remove child channels from ' +
            'an activation key'))
    print(_('usage: activationkey_removechildchannels KEY <CHANNEL ...>'))


def complete_activationkey_removechildchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        key_details = \
            self.client.activationkey.getDetails(self.session, parts[1])
        return tab_completer(key_details.get('child_channel_labels'), text)

    return None


def do_activationkey_removechildchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_removechildchannels()
        return 1

    key = args.pop(0)
    channels = args

    self.client.activationkey.removeChildChannels(self.session,
                                                  key,
                                                  channels)

    return 0

####################


def help_activationkey_listchildchannels(self):
    print(_('activationkey_listchildchannels: List the child channels ' +
            'for an activation key'))
    print(_('usage: activationkey_listchildchannels KEY'))


def complete_activationkey_listchildchannels(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listchildchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listchildchannels()
        return 1

    key = args[0]

    details = self.client.activationkey.getDetails(self.session, key)

    if details.get('child_channel_labels'):
        print('\n'.join(sorted(details.get('child_channel_labels'))))

    return 0

####################


def help_activationkey_listbasechannel(self):
    print(_('activationkey_listbasechannel: List the base channels ' +
            'for an activation key'))
    print(_('usage: activationkey_listbasechannel KEY'))


def complete_activationkey_listbasechannel(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listbasechannel(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listbasechannel()
        return 1

    key = args[0]

    details = self.client.activationkey.getDetails(self.session, key)

    print(details.get('base_channel_label'))

    return 0

####################


def help_activationkey_listgroups(self):
    print(_('activationkey_listgroups: List the groups for an ' +
            'activation key'))
    print(_('usage: activationkey_listgroups KEY'))


def complete_activationkey_listgroups(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listgroups(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listgroups()
        return 1

    key = args[0]

    details = self.client.activationkey.getDetails(self.session, key)

    for group in details.get('server_group_ids'):
        group_details = self.client.systemgroup.getDetails(self.session,
                                                           group)
        print(group_details.get('name'))

    return 0

####################


def help_activationkey_listentitlements(self):
    print(_('activationkey_listentitlements: List the entitlements ' +
            'for an activation key'))
    print(_('usage: activationkey_listentitlements KEY'))


def complete_activationkey_listentitlements(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listentitlements(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listentitlements()
        return 1

    key = args[0]

    details = self.client.activationkey.getDetails(self.session, key)

    if details.get('entitlements'):
        print('\n'.join(details.get('entitlements')))

    return 0

####################


def help_activationkey_listpackages(self):
    print(_('activationkey_listpackages: List the packages for an ' +
            'activation key'))
    print(_('usage: activationkey_listpackages KEY'))


def complete_activationkey_listpackages(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listpackages(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listpackages()
        return 1

    key = args[0]

    details = self.client.activationkey.getDetails(self.session, key)

    for package in sorted(details.get('packages'), key=lambda x: (x['name']), reverse=True):
        if 'arch' in package:
            print('%s.%s' % (package['name'], package['arch']))
        else:
            print(package['name'])

    return 0

####################


def help_activationkey_listconfigchannels(self):
    print(_('activationkey_listconfigchannels: List the configuration ' +
            'channels for an activation key'))
    print(_('usage: activationkey_listconfigchannels KEY'))


def complete_activationkey_listconfigchannels(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listconfigchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listconfigchannels()
        return 1

    key = args[0]

    channels = \
        self.client.activationkey.listConfigChannels(self.session,
                                                     key)

    channels = sorted([c.get('label') for c in channels])

    if channels:
        print('\n'.join(channels))

    return 0

####################


def help_activationkey_addconfigchannels(self):
    print(_('activationkey_addconfigchannels: Add config channels ' +
            'to an activation key'))
    print(_('''usage: activationkey_addconfigchannels KEY <CHANNEL ...> [options])

options:
  -t add channels to the top of the list
  -b add channels to the bottom of the list'''))


def complete_activationkey_addconfigchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        return tab_completer(self.do_configchannel_list('', True), text)

    return None


def do_activationkey_addconfigchannels(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-t', '--top', action='store_true')
    arg_parser.add_argument('-b', '--bottom', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_activationkey_addconfigchannels()
        return 1

    key = [args.pop(0)]
    channels = args

    if is_interactive(options):
        answer = prompt_user('Add to top or bottom? [T/b]:')
        if re.match('b', answer, re.I):
            options.top = False
        else:
            options.top = True
    else:
        if options.bottom:
            options.top = False
        else:
            options.top = True

    self.client.activationkey.addConfigChannels(self.session,
                                                key,
                                                channels,
                                                options.top)

    return 0

####################


def help_activationkey_removeconfigchannels(self):
    print(_('activationkey_removeconfigchannels: Remove config channels ' +
            'from an activation key'))
    print(_('usage: activationkey_removeconfigchannels KEY <CHANNEL ...>'))


def complete_activationkey_removeconfigchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        key_channels = \
            self.client.activationkey.listConfigChannels(self.session,
                                                         parts[1])

        config_channels = [c.get('label') for c in key_channels]
        return tab_completer(config_channels, text)

    return None


def do_activationkey_removeconfigchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_removeconfigchannels()
        return 1

    key = [args.pop(0)]
    channels = args

    self.client.activationkey.removeConfigChannels(self.session,
                                                   key,
                                                   channels)

    return 0

####################


def help_activationkey_setconfigchannelorder(self):
    print(_('activationkey_setconfigchannelorder: Set the ranked order of ' +
            'configuration channels'))
    print(_('usage: activationkey_setconfigchannelorder KEY'))


def complete_activationkey_setconfigchannelorder(self, text, line, beg,
                                                 end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_setconfigchannelorder(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 1:
        self.help_activationkey_setconfigchannelorder()
        return 1

    key = args[0]

    # get the current configuration channels from the first activationkey
    # in the list
    new_channels = \
        self.client.activationkey.listConfigChannels(self.session, key)
    new_channels = [c.get('label') for c in new_channels]

    # call an interface for the user to make selections
    all_channels = self.do_configchannel_list('', True)
    new_channels = config_channel_order(all_channels, new_channels)

    print('')
    print(_('New Configuration Channels:'))
    for i, new_channel in enumerate(new_channels, 1):
        print('[%i] %s' % (i, new_channel))

    self.client.activationkey.setConfigChannels(self.session,
                                                [key],
                                                new_channels)

    return 0

####################


def help_activationkey_create(self):
    print(_('activationkey_create: Create an activation key'))
    print(_('''usage: activationkey_create [options])

options:
  -n NAME
  -d DESCRIPTION
  -b BASE_CHANNEL
  -u set key as universal default
  -e [enterprise_entitled,virtualization_host]'''))


def do_activationkey_create(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-n', '--name')
    arg_parser.add_argument('-d', '--description')
    arg_parser.add_argument('-b', '--base-channel')
    arg_parser.add_argument('-e', '--entitlements')
    arg_parser.add_argument('-u', '--universal', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    if is_interactive(options):
        options.name = prompt_user(_('Name (blank to autogenerate):'))
        options.description = prompt_user(_('Description [None]:'))

        print('')
        print(_('Base Channels'))
        print('-------------')
        print('\n'.join(sorted(self.list_base_channels())))
        print('')

        options.base_channel = prompt_user(_('Base Channel (blank for default):'))

        options.entitlements = []

        for e in self.ENTITLEMENTS:
            if e == 'enterprise_entitled':
                continue

            if self.user_confirm(_('%s Entitlement [y/N]:') % e,
                                 ignore_yes=True):
                options.entitlements.append(e)

        options.universal = self.user_confirm(_('Universal Default [y/N]:'),
                                              ignore_yes=True)
    else:
        if not options.name:
            options.name = ''
        if not options.description:
            options.description = ''
        if not options.base_channel:
            options.base_channel = ''
        if not options.universal:
            options.universal = False
        if options.entitlements:
            options.entitlements = options.entitlements.split(',')

            # remove empty strings from the list
            if '' in options.entitlements:
                options.entitlements.remove('')
        else:
            options.entitlements = []

    new_key = self.client.activationkey.create(self.session,
                                               options.name,
                                               options.description,
                                               options.base_channel,
                                               options.entitlements,
                                               options.universal)

    logging.info(_N('Created activation key %s') % new_key)

    return 0

####################


def help_activationkey_delete(self):
    print(_('activationkey_delete: Delete an activation key'))
    print(_('usage: activationkey_delete KEY'))


def complete_activationkey_delete(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_delete()
        return 1

    # allow globbing of activationkey names
    keys = filter_results(self.do_activationkey_list('', True), args)
    logging.debug("activationkey_delete called with args %s, keys=%s" %
                  (args, keys))

    if not keys:
        logging.error(_N("No keys matched argument %s") % args)
        return 1

    # Print the keys prior to the confimation
    print('\n'.join(sorted(keys)))

    if not self.user_confirm(_('Delete activation key(s) [y/N]:')):
        return 1

    for key in keys:
        logging.debug("Deleting key %s" % key)
        self.client.activationkey.delete(self.session, key)

    return 0

####################


def help_activationkey_list(self):
    print(_('activationkey_list: List all activation keys'))
    print(_('usage: activationkey_list'))


def do_activationkey_list(self, args, doreturn=False):
    all_keys = self.client.activationkey.listActivationKeys(self.session)

    keys = []
    for k in all_keys:
        # don't list auto-generated re-activation keys
        if not re.match('Kickstart re-activation', k.get('description')):
            keys.append(k.get('key'))

    if doreturn:
        return keys
    else:
        if keys:
            print('\n'.join(sorted(keys)))

    return 0

####################


def help_activationkey_listsystems(self):
    print(_('activationkey_listsystems: List systems registered with a key'))
    print(_('usage: activationkey_listsystems KEY'))


def complete_activationkey_listsystems(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_listsystems(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_listsystems()
        return 1

    key = args[0]

    try:
        systems = \
            self.client.activationkey.listActivatedSystems(self.session,
                                                           key)
    except xmlrpclib.Fault:
        logging.warning(_N('%s is not a valid activation key') % key)
        return 1

    systems = sorted([s.get('hostname') for s in systems])

    if systems:
        print('\n'.join(systems))

    return 0

####################


def help_activationkey_details(self):
    print(_('activationkey_details: Show the details of an activation key'))
    print(_('usage: activationkey_details KEY ...'))


def complete_activationkey_details(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_details()
        return None

    add_separator = False

    result = []
    for key in args:
        try:
            details = self.client.activationkey.getDetails(self.session,
                                                           key)
            config_channels = \
                self.client.activationkey.listConfigChannels(
                    self.session, key)

            config_channel_deploy = \
                self.client.activationkey.checkConfigDeployment(
                    self.session, key)

            # API returns 0/1 instead of boolean
            config_channel_deploy = config_channel_deploy == 1
        except xmlrpclib.Fault:
            logging.warning(_N('%s is not a valid activation key') % key)
            return None

        groups = []
        for group in details.get('server_group_ids'):
            group_details = self.client.systemgroup.getDetails(self.session,
                                                               group)
            groups.append(group_details.get('name'))

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        result.append(_('Key:                    %s') % details.get('key'))
        result.append(_('Description:            %s') % details.get('description'))
        result.append(_('Universal Default:      %s') % details.get('universal_default'))
        result.append(_('Usage Limit:            %s') % details.get('usage_limit'))
        result.append(_('Deploy Config Channels: %s') % config_channel_deploy)
        if 'contact_method' in details:
            result.append(_('Contact Method:         %s') % details.get('contact_method'))

        result.append('')
        result.append(_('Software Channels'))
        result.append('-----------------')
        result.append(details.get('base_channel_label'))

        for channel in sorted(details.get('child_channel_labels')):
            result.append(' |-- %s' % channel)

        result.append('')
        result.append(_('Configuration Channels'))
        result.append('----------------------')
        for channel in config_channels:
            result.append(channel.get('label'))

        result.append('')
        result.append(_('Entitlements'))
        result.append('------------')
        result.append('\n'.join(sorted(details.get('entitlements'))))

        result.append('')
        result.append(_('System Groups'))
        result.append('-------------')
        result.append('\n'.join(sorted(groups)))

        result.append('')
        result.append(_('Packages'))
        result.append('--------')
        for package in sorted(details.get('packages'), key=lambda x: (x['name']), reverse=True):
            name = package.get('name')

            if package.get('arch'):
                name += '.%s' % package.get('arch')

            result.append(name)
    return result

####################


def help_activationkey_enableconfigdeployment(self):
    print(_('activationkey_enableconfigdeployment: Enable config ' +
            'channel deployment'))
    print(_('usage: activationkey_enableconfigdeployment KEY'))


def complete_activationkey_enableconfigdeployment(self, text, line, beg,
                                                  end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_enableconfigdeployment(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_enableconfigdeployment()
        return 1

    for key in args:
        logging.debug('Enabling config file deployment for %s' % key)
        self.client.activationkey.enableConfigDeployment(self.session, key)

    return 0

####################


def help_activationkey_disableconfigdeployment(self):
    print(_('activationkey_disableconfigdeployment: Disable config ' +
            'channel deployment'))
    print(_('usage: activationkey_disableconfigdeployment KEY'))


def complete_activationkey_disableconfigdeployment(self, text, line, beg,
                                                   end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_disableconfigdeployment(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_disableconfigdeployment()
        return 1

    for key in args:
        logging.debug('Disabling config file deployment for %s' % key)
        self.client.activationkey.disableConfigDeployment(self.session, key)

    return 0

####################


def help_activationkey_setbasechannel(self):
    print(_('activationkey_setbasechannel: Set the base channel of an ' +
            'activation key'))
    print(_('usage: activationkey_setbasechannel KEY CHANNEL'))


def complete_activationkey_setbasechannel(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        return tab_completer(self.list_base_channels(), text)

    return None


def do_activationkey_setbasechannel(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_setbasechannel()
        return 1

    key = args.pop(0)
    channel = args[0]

    current_details = self.client.activationkey.getDetails(self.session,
                                                           key)

    details = {'description': current_details.get('description'),
               'base_channel_label': channel,
               'usage_limit': current_details.get('usage_limit'),
               'universal_default':
               current_details.get('universal_default')}

    # getDetails returns a usage_limit of 0 unlimited, which is then
    # interpreted literally as zero when passed into setDetails, doh!
    # Setting it to -1 seems to keep the usage limit unlimited
    if details['usage_limit'] == 0:
        details['usage_limit'] = -1

    self.client.activationkey.setDetails(self.session, key, details)

    return 0

####################


def help_activationkey_setusagelimit(self):
    print(_('activationkey_setusagelimit: Set the usage limit of an ' +
            'activation key, can be a number or \"unlimited\"'))
    print(_('usage: activationkey_setusagelimit KEY <usage limit>'))
    print(_('usage: activationkey_setusagelimit KEY unlimited '))


def complete_activationkey_setusagelimit(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    elif len(parts) > 2:
        return "unlimited"

    return None


def do_activationkey_setusagelimit(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_setusagelimit()
        return 1

    key = args.pop(0)
    usage_limit = -1
    if args[0] == 'unlimited':
        logging.debug("Setting usage for key %s unlimited" % key)
    else:
        try:
            usage_limit = int(args[0])
            logging.debug("Setting usage for key %s to %d" % (key, usage_limit))
        except ValueError:
            logging.error(_N("Couldn't convert argument %s to an integer") %
                          args[0])
            self.help_activationkey_setusagelimit()
            return 1

    current_details = self.client.activationkey.getDetails(self.session,
                                                           key)
    details = {'description': current_details.get('description'),
               'base_channel_label':
               current_details.get('base_channel_label'),
               'usage_limit': usage_limit,
               'universal_default':
               current_details.get('universal_default')}

    self.client.activationkey.setDetails(self.session, key, details)

    return 0

####################


def help_activationkey_setuniversaldefault(self):
    print(_('activationkey_setuniversaldefault: Set this key as the ' +
            'universal default'))
    print(_('usage: activationkey_setuniversaldefault KEY'))


def complete_activationkey_setuniversaldefault(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_setuniversaldefault(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_activationkey_setuniversaldefault()
        return 1

    key = args.pop(0)

    current_details = self.client.activationkey.getDetails(self.session,
                                                           key)

    details = {'description': current_details.get('description'),
               'base_channel_label':
               current_details.get('base_channel_label'),
               'usage_limit': current_details.get('usage_limit'),
               'universal_default': True}

    # getDetails returns a usage_limit of 0 unlimited, which is then
    # interpreted literally as zero when passed into setDetails, doh!
    # Setting it to -1 seems to keep the usage limit unlimited
    if details['usage_limit'] == 0:
        details['usage_limit'] = -1

    self.client.activationkey.setDetails(self.session, key, details)

    return 0

####################


def help_activationkey_export(self):
    print(_('activationkey_export: Export activation key(s) to JSON format file'))
    print(_('''usage: activationkey_export [options] [<KEY> ...])

options:
    -f outfile.json : specify an output filename, defaults to <KEY>.json
                      if exporting a single key, akeys.json for multiple keys,
                      or akey_all.json if no KEY specified (export ALL)

Note : KEY list is optional, default is to export ALL keys '''))


def complete_activationkey_export(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def export_activationkey_getdetails(self, key):
    # Get the key details
    logging.info(_N("Getting activation key details for %s" % key))
    details = self.client.activationkey.getDetails(self.session, key)

    # Get the key config-channel data, add it to the existing details
    logging.debug("activationkey.listConfigChannels %s" % key)
    ccdlist = []
    try:
        ccdlist = self.client.activationkey.listConfigChannels(self.session,
                                                               key)
    except xmlrpclib.Fault:
        logging.debug("activationkey.listConfigChannel threw an exeception, setting config_channels=False")

    cclist = [c['label'] for c in ccdlist]
    logging.debug("Got config channel label list of %s" % cclist)
    details['config_channels'] = cclist

    logging.debug("activationkey.checkConfigDeployment %s" % key)
    details['config_deploy'] = \
        self.client.activationkey.checkConfigDeployment(self.session, key)

    # Get group details, as the server group IDs are not necessarily the same
    # across servers, so we need the group name on import
    details['server_groups'] = []
    if details['server_group_ids']:
        grp_detail_list = []
        for grp in details['server_group_ids']:
            grp_details = self.client.systemgroup.getDetails(self.session, grp)

            if grp_details:
                grp_detail_list.append(grp_details)

        details['server_groups'] = [g['name'] for g in grp_detail_list]

    # Now append the details dict describing the key to the specified file
    return details


def do_activationkey_export(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-f', '--file')

    (args, options) = parse_command_arguments(args, arg_parser)

    filename = ""
    if options.file is not None:
        logging.debug("Passed filename do_activationkey_export %s" %
                      options.file)
        filename = options.file

    # Get the list of keys to export and sort out the filename if required
    keys = []
    if not args:
        if not filename:
            filename = "akey_all.json"
        logging.info(_N("Exporting ALL activation keys to %s") % filename)
        keys = self.do_activationkey_list('', True)
    else:
        # allow globbing of activationkey names
        keys = filter_results(self.do_activationkey_list('', True), args)
        logging.debug("activationkey_export called with args %s, keys=%s" %
                      (args, keys))

        if not keys:
            logging.error(_N("Invalid activation key passed"))
            return 1

        if not filename:
            # No filename arg, so we try to do something sensible:
            # If we are exporting exactly one key, we default to keyname.json
            # otherwise, generic akeys.json name
            if len(keys) == 1:
                filename = "%s.json" % keys[0]
            else:
                filename = "akeys.json"

    # Dump as a list of dict
    keydetails_list = []
    for k in keys:
        logging.info(_N("Exporting key %s to %s") % (k, filename))
        keydetails_list.append(self.export_activationkey_getdetails(k))

    logging.debug("About to dump %d keys to %s" %
                  (len(keydetails_list), filename))

    # Check if filepath exists, if it is an existing file
    # we prompt the user for confirmation
    if os.path.isfile(filename):
        if not self.user_confirm(_("File %s exists, confirm overwrite file? (y/n)") %
                                 filename):
            return 1

    if json_dump_to_file(keydetails_list, filename) is not True:
        logging.error(_N("Failed to save exported keys to file: {}").format(filename))
        return 1

    return 0

####################


def help_activationkey_import(self):
    print(_('activationkey_import: import activation key(s) from JSON file(s)'))
    print(_('''usage: activationkey_import <JSONFILE ...>'''))


def do_activationkey_import(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        logging.error(_N("No filename passed"))
        self.help_activationkey_import()
        return 1

    for filename in args:
        logging.debug("Passed filename do_activationkey_import %s" % filename)
        keydetails_list = json_read_from_file(filename)

        if not keydetails_list:
            logging.error(_N("Could not read json data from %s") % filename)
            return 1

        for keydetails in keydetails_list:
            if self.import_activationkey_fromdetails(keydetails) is not True:
                logging.error(_N("Failed to import key %s") %
                              keydetails['key'])
                return 1

    return 0

# create a new key based on the dict from export_activationkey_getdetails


def import_activationkey_fromdetails(self, keydetails):
    # First we check that an existing key with the same name does not exist
    existing_keys = self.do_activationkey_list('', True)

    if keydetails['key'] in existing_keys:
        logging.warning(_N("%s already exists! Skipping!") % keydetails['key'])
        return False
    else:
        # create the key, we need to drop the org prefix from the key name
        keyname = re.sub('^[0-9]-', '', keydetails['key'])
        logging.debug("Found key %s, importing as %s" %
                      (keydetails['key'], keyname))

        # Channel label must be an empty-string for "Red Hat Satellite Default"
        # The export to json maps this to a unicode string "none"
        # To avoid changing the json format now, just fix it up here...
        if keydetails['base_channel_label'] == "none":
            keydetails['base_channel_label'] = ''

        if keydetails['usage_limit'] != 0:
            newkey = self.client.activationkey.create(self.session,
                                                      keyname,
                                                      keydetails['description'],
                                                      keydetails['base_channel_label'],
                                                      keydetails['usage_limit'],
                                                      keydetails['entitlements'],
                                                      keydetails['universal_default'])
        else:
            newkey = self.client.activationkey.create(self.session,
                                                      keyname,
                                                      keydetails['description'],
                                                      keydetails['base_channel_label'],
                                                      keydetails['entitlements'],
                                                      keydetails['universal_default'])
        if not newkey:
            logging.error(_N("Failed to import key %s") %
                          keyname)
            return False

        # add child channels
        self.client.activationkey.addChildChannels(self.session, newkey,
                                                   keydetails['child_channel_labels'])

        # set config channel options and channels (missing are skipped)
        if keydetails['config_deploy'] != 0:
            self.client.activationkey.enableConfigDeployment(self.session,
                                                             newkey)
        else:
            self.client.activationkey.disableConfigDeployment(self.session,
                                                              newkey)

        if keydetails['config_channels']:
            self.client.activationkey.addConfigChannels(self.session, [newkey],
                                                        keydetails['config_channels'], False)

        # set groups (missing groups are created)
        gids = []
        for grp in keydetails['server_groups']:
            grpdetails = self.client.systemgroup.getDetails(self.session, grp)
            if grpdetails is None:
                logging.info(_N("System group %s doesn't exist, creating") % grp)
                grpdetails = self.client.systemgroup.create(self.session, grp,
                                                            grp)
            gids.append(grpdetails.get('id'))

        if gids:
            logging.debug("Adding groups %s to key %s" % (gids, newkey))
            self.client.activationkey.addServerGroups(self.session, newkey,
                                                      gids)

        # Finally add the package list
        if keydetails['packages']:
            self.client.activationkey.addPackages(self.session, newkey,
                                                  keydetails['packages'])

        return True

####################


def help_activationkey_clone(self):
    print(_('activationkey_clone: Clone an activation key'))
    print(_('''usage examples:
                 activationkey_clone foo_key -c bar_key
                 activationkey_clone foo_key1 foo_key2 -c prefix
                 activationkey_clone foo_key -x "s/foo/bar"
                 activationkey_clone foo_key1 foo_key2 -x "s/foo/bar"

options:
  -c CLONE_NAME  : Name of the resulting key, treated as a prefix for multiple
                   keys
  -x "s/foo/bar" : Optional regex replacement, replaces foo with bar in the
                   clone description, base-channel label, child-channel
                   labels, config-channel names '''))


def complete_activationkey_clone(self, text, line, beg, end):
    return tab_completer(self.do_activationkey_list('', True), text)


def do_activationkey_clone(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-c', '--clonename')
    arg_parser.add_argument('-x', '--regex')

    (args, options) = parse_command_arguments(args, arg_parser)
    allkeys = self.do_activationkey_list('', True)

    if is_interactive(options):
        print('')
        print(_('Activation Keys'))
        print('------------------')
        print('\n'.join(sorted(allkeys)))
        print('')

        if len(args) == 1:
            print(_("Key to clone: %s") % args[0])
        else:
            # Clear out any args as interactive doesn't handle multiple keys
            args = []
            args.append(prompt_user(_('Original Key:'), noblank=True))

        options.clonename = prompt_user(_('Cloned Key:'), noblank=True)
    else:
        if not options.clonename and not options.regex:
            logging.error(_N("Error - must specify either -c or -x options!"))
            self.help_activationkey_clone()
            return 1

    if options.clonename in allkeys:
        logging.error(_N("Key %s already exists") % options.clonename)
        return 1

    if not args:
        logging.error(_N("Error no activationkey to clone passed!"))
        self.help_activationkey_clone()
        return 1

    logging.debug("Got args=%s %d" % (args, len(args)))
    # allow globbing of configchannel channel names
    akeys = filter_results(allkeys, args)
    logging.debug("Filtered akeys %s" % akeys)
    logging.debug("all akeys %s" % allkeys)
    for ak in akeys:
        logging.debug("Cloning %s" % ak)
        # Replace the key-name with the clonename specified by the user
        keydetails = self.export_activationkey_getdetails(ak)

        # If the -x/--regex option is passed, do a sed-style replacement over
        # everything contained by the key.  This makes it easier to clone when
        # content is based on a known naming convention
        if options.regex:
            # formatted like a sed-replacement, s/foo/bar
            findstr = options.regex.split("/")[1]
            replacestr = options.regex.split("/")[2]
            logging.debug("Regex option with %s, replacing %s with %s" %
                          (options.regex, findstr, replacestr))

            # First we do the key name
            newkey = re.sub(findstr, replacestr, keydetails['key'])
            keydetails['key'] = newkey

            # Then the description
            newdesc = re.sub(findstr, replacestr, keydetails['description'])
            keydetails['description'] = newdesc

            # Then the base-channel label
            newbasech = re.sub(findstr, replacestr,
                               keydetails['base_channel_label'])
            if newbasech in self.list_base_channels():
                keydetails['base_channel_label'] = newbasech
                # Now iterate over any child-channel labels
                # we have the new base-channel, we can check if the new child
                # label exists under the new base-channel:
                # If it doesn't we can only skip it and print(a warning)
                all_childch = self.list_child_channels(system=None,
                                                       parent=newbasech, subscribed=False)

                new_child_channel_labels = []
                for c in keydetails['child_channel_labels']:
                    newc = re.sub(findstr, replacestr, c)
                    if newc in all_childch:
                        logging.debug("Found child channel %s for key %s, " %
                                      (c, keydetails['key']) +
                                      "replacing with %s" % newc)

                        new_child_channel_labels.append(newc)
                    else:
                        logging.warning(_N("Found child channel %s key %s, %s") %
                                        (c, keydetails['key'], newc) +
                                        _N(" does not exist, skipping!"))

                logging.debug("Processed all child channels, " +
                              "new_child_channel_labels=%s" % new_child_channel_labels)

                keydetails['child_channel_labels'] = new_child_channel_labels
            else:
                logging.error(_N("Regex-replacement results in new " +
                                 "base-channel %s which does not exist!") % newbasech)

            # Finally, any config-channels
            new_config_channels = []
            allccs = self.do_configchannel_list('', True)
            for cc in keydetails['config_channels']:
                newcc = re.sub(findstr, replacestr, cc)

                if newcc in allccs:
                    logging.debug("Found config channel %s for key %s, " %
                                  (cc, keydetails['key']) +
                                  "replacing with %s" % newcc)

                    new_config_channels.append(newcc)
                else:
                    logging.warning(_N("Found config channel %s for key %s, %s ")
                                    % (cc, keydetails['key'], newcc) +
                                    _N("does not exist, skipping!"))

            logging.debug("Processed all config channels, " +
                          "new_config_channels = %s" % new_config_channels)

            keydetails['config_channels'] = new_config_channels

        # Not regex mode
        elif options.clonename:
            if len(akeys) > 1:
                # We treat the clonename as a prefix for multiple keys
                # However we need to insert the prefix after the org-
                newkey = re.sub(r'^([0-9]-)', r'\1' + options.clonename,
                                keydetails['key'])
                keydetails['key'] = newkey
            else:
                keydetails['key'] = options.clonename

        logging.info(_N("Cloning key %s as %s") % (ak, keydetails['key']))

        # Finally : import the key from the modified keydetails dict
        if self.import_activationkey_fromdetails(keydetails) is not True:
            logging.error(_N("Failed to clone %s to %s") %
                          (ak, keydetails['key']))
            return 1

    return 0

####################
# activationkey helper


def is_activationkey(self, name):
    if not name:
        return None
    return name in self.do_activationkey_list(name, True)


def check_activationkey(self, name):
    if not name:
        logging.error(_N("no activationkey label given"))
        return False
    if not self.is_activationkey(name):
        logging.error(_N("invalid activationkey label ") + name)
        return False
    return True


def dump_activationkey(self, name, replacedict=None, excludes=None):
    content = self.do_activationkey_details(name)
    if not excludes:
        excludes = ["Universal Default:"]
    content = get_normalized_text(content, replacedict=replacedict, excludes=excludes)

    return content

####################


def help_activationkey_diff(self):
    print(_('activationkey_diff: Diff activation keys'))
    print('')
    print(_('usage: activationkey_diff SOURCE_KEY TARGET_KEY'))


def complete_activationkey_diff(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')
    args = len(parts)

    if args == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    if args == 3:
        return tab_completer(self.do_activationkey_list('', True), text)
    return []


def do_activationkey_diff(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) not in [1, 2]:
        self.help_activationkey_diff()
        return None

    source_channel = args[0]
    if not self.check_activationkey(source_channel):
        return None

    target_channel = None
    if len(args) == 2:
        target_channel = args[1]
    elif hasattr(self, "do_activationkey_getcorresponding"):
        # can a corresponding channel name be found automatically?
        target_channel = self.do_activationkey_getcorresponding(source_channel)
    if not self.check_activationkey(target_channel):
        return None

    source_replacedict, target_replacedict = get_string_diff_dicts(source_channel, target_channel)

    source_data = self.dump_activationkey(source_channel, source_replacedict)
    target_data = self.dump_activationkey(target_channel, target_replacedict)

    return diff(source_data, target_data, source_channel, target_channel)

####################


def help_activationkey_disable(self):
    print(_('activationkey_disable: Disable an activation key'))
    print('')
    print(_('usage: activationkey_disable KEY [KEY ...]'))


def complete_activationkey_disable(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) >= 2:
        return tab_completer(self.do_activationkey_list('', True), text)

    return None


def do_activationkey_disable(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 1:
        self.help_activationkey_disable()
        return 1

    keys = filter_results(self.do_activationkey_list('', True), args)

    details = {'disabled': True}

    for akey in keys:
        self.client.activationkey.setDetails(self.session, akey, details)

    return 0

####################


def help_activationkey_enable(self):
    print(_('activationkey_enable: Enable an activation key'))
    print('')
    print(_('usage: activationkey_enable KEY [KEY ...]'))


def complete_activationkey_enable(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) >= 2:
        return tab_completer(self.do_activationkey_list('', True), text)

    return None


def do_activationkey_enable(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 1:
        self.help_activationkey_enable()
        return 1

    keys = filter_results(self.do_activationkey_list('', True), args)

    details = {'disabled': False}

    for akey in keys:
        self.client.activationkey.setDetails(self.session, akey, details)

    return 0

####################


def help_activationkey_setdescription(self):
    print(_('activationkey_setdescription: Set the activation key description'))
    print('')
    print(_('usage: activationkey_setdescription KEY DESCRIPTION'))


def complete_activationkey_setdescription(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) <= 2:
        return tab_completer(self.do_activationkey_list('', True), text)

    return None


def do_activationkey_setdescription(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) >= 2:
        self.help_activationkey_setdescription()
        return 1

    akey = args.pop(0)
    description = ' '.join(args)

    details = {'description': description}

    self.client.activationkey.setDetails(self.session, akey, details)

    return 0

####################


def help_activationkey_setcontactmethod(self):
    print(_('activationkey_setcontactmethod: Set the contact method to use for ' \
          'systems registered with this key.'))
    print(_('Available contact methods: ') + str(self.CONTACT_METHODS))
    print(_('usage: activationkey_setcontactmethod KEY CONTACT_METHOD'))


def complete_activationkey_setcontactmethod(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_activationkey_list('', True), text)
    else:
        return tab_completer(self.CONTACT_METHODS, text)


def do_activationkey_setcontactmethod(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not len(args) == 2:
        self.help_activationkey_setcontactmethod()
        return 1

    details = {'contact_method': args.pop()}
    akey = args.pop()

    self.client.activationkey.setDetails(self.session, akey, details)

    return 0
