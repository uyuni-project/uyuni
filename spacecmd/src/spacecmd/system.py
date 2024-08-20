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
# Copyright (c) 2013--2018 Red Hat, Inc.
#

# NOTE: the 'self' variable is an instance of SpacewalkShell

# unused argument
# pylint: disable=W0613

# wildcard import
# pylint: disable=W0401,W0614

# invalid function name
# pylint: disable=C0103

import gettext
import shlex
from datetime import datetime
try:
    from xmlrpc import client as xmlrpclib
except ImportError:
    import xmlrpclib
from getpass import getpass
from operator import itemgetter
from xml.parsers.expat import ExpatError
from spacecmd.i18n import _N
from spacecmd.utils import *

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

__PKG_COMPARISONS = {0: _('Same'),
                     1: _('Only here'),
                     2: _('Newer here'),
                     3: _('Only there'),
                     4: _('Newer there')}


def print_package_comparison(self, results):
    max_name = max_length(map(itemgetter('package_name'), results), minimum=7)

    # sometimes 'this_system' or 'other_system' can be None
    tmp_this = []
    tmp_other = []
    for item in results:
        tmp_this.append(str(item.get('this_system')))
        tmp_other.append(str(item.get('other_system')))

    max_this = max_length(tmp_this, minimum=11)
    max_other = max_length(tmp_other, minimum=12)

    max_comparison = 10

    # print(headers)
    print('%s  %s  %s  %s' % (
        _('Package').ljust(max_name),
        _('This System').ljust(max_this),
        _('Other System').ljust(max_other),
        _('Difference').ljust(max_comparison)))

    print('%s  %s  %s  %s' % (
        '-' * max_name,
        '-' * max_this,
        '-' * max_other,
        '-' * max_comparison))

    for item in results:
        # don't show packages that are the same
        if item.get('comparison') == 0:
            continue

        print('%s  %s  %s  %s' % (
            item.get('package_name').ljust(max_name),
            str(item.get('this_system')).ljust(max_this),
            str(item.get('other_system')).ljust(max_other),
            __PKG_COMPARISONS[item.get('comparison')]))

####################


def manipulate_child_channels(self, args, remove=False):
    arg_parser = get_argument_parser()
    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        if remove:
            self.help_system_removechildchannels()
        else:
            self.help_system_addchildchannels()
        return

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    new_channels = args

    print(_('Systems'))
    print('-------')
    print('\n'.join(sorted(["%s" % x for x in systems])))
    print('')

    if remove:
        print(_('Removing Channels'))
        print('-----------------')
    else:
        print(_('Adding Channels'))
        print('---------------')

    print('\n'.join(sorted(new_channels)))

    if not self.user_confirm():
        return

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        child_channels = \
            self.client.system.listSubscribedChildChannels(self.session,
                                                           system_id)

        child_channels = [c.get('label') for c in child_channels]

        if remove:
            for channel in new_channels:
                if channel in child_channels:
                    child_channels.remove(channel)
        else:
            for channel in new_channels:
                if channel not in child_channels:
                    child_channels.append(channel)

        self.client.system.setChildChannels(self.session,
                                            system_id,
                                            child_channels)

    return

####################


def help_system_list(self):
    print(_('system_list: List all system profiles'))
    print(_('usage: system_list'))


def do_system_list(self, args, doreturn=False):
    if doreturn:
        return self.get_system_names()
    else:
        if self.get_system_names():
            print('\n'.join(sorted(['%s : %s' % (v, k) for k, v in self.get_system_names_ids().items()])))

    return 0

####################


def help_system_reboot(self):
    print(_('system_reboot: Reboot a system'))
    print(_('''usage: system_reboot <SYSTEMS> [options])

options:
  -s START_TIME'''))

    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_reboot(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_reboot(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_reboot()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user('Start Time [now]:')
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    print('')

    print(_('Start Time: %s') % options.start_time)
    print('')
    print(_('Systems'))
    print('-------')
    print('\n'.join(sorted(systems)))

    if not self.user_confirm(_('Reboot these systems [y/N]:')):
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.scheduleReboot(self.session, system_id, options.start_time)

    return 0

####################


def help_system_search(self):
    print(_('system_search: List systems that match the given criteria'))
    print(_('usage: system_search QUERY'))
    print('')
    print(_('Available Fields:'))
    print('\n'.join(self.SYSTEM_SEARCH_FIELDS))
    print('')
    print(_('Examples:'))
    print(_('> system_search device:vmware'))
    print(_('> system_search ip:192.168.82'))


def do_system_search(self, args, doreturn=False):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 1:
        self.help_system_search()
        return 1

    query = args[0]

    if re.search(':', query):
        try:
            (field, value) = query.split(':')
        except ValueError:
            logging.error(_N('Invalid query'))
            return []
    else:
        field = 'name'
        value = query

    if not value:
        logging.warning(_N('Invalid query'))
        return []

    results = []
    if field == 'name':
        results = self.client.system.search.nameAndDescription(self.session,
                                                               value)
        key = 'name'
    elif field == 'id':
        # build an array of key/value pairs from our local system cache
        self.generate_system_cache()
        results = [{'id': k, 'name': self.all_systems[k]}
                   for k in self.all_systems]
        key = 'id'
    elif field == 'ip':
        results = self.client.system.search.ip(self.session, value)
        key = 'ip'
    elif field == 'hostname':
        results = self.client.system.search.hostname(self.session, value)
        key = 'hostname'
    elif field == 'device':
        results = self.client.system.search.deviceDescription(self.session,
                                                              value)
        key = 'hw_description'
    elif field == 'vendor':
        results = self.client.system.search.deviceVendorId(self.session,
                                                           value)
        key = 'hw_vendor_id'
    elif field == 'driver':
        results = self.client.system.search.deviceDriver(self.session,
                                                         value)
        key = 'hw_driver'
    elif field == 'uuid':
        results = self.client.system.search.uuid(self.session, value)
        key = 'uuid'
    else:
        logging.warning(_N('Invalid search field'))
        return []

    systems = []
    max_size = 0
    for s in results:
        # only use real matches, not the fuzzy ones we get back
        if re.search(value, "%s" % s.get(key), re.I):
            if len(s.get('name')) > max_size:
                max_size = len(s.get('name'))

            systems.append((s.get('name'), s.get(key), s.get('id')))

    if doreturn:
        return [s[2] for s in systems]
    else:
        if systems:
            for s in sorted(systems):
                if key == 'name':
                    print(s[0])
                else:
                    print('%s  %s' % (s[0].ljust(max_size),
                                      str(s[1]).strip()))

    return 0

####################


def help_system_runscript(self):
    print(_('system_runscript: Schedule a script to run on the list of'))
    print(_('                  systems provided'))
    print(_('''usage: system_runscript <SYSTEMS> [options])

options:
  -u USER
  -g GROUP
  -t TIMEOUT
  -s START_TIME
  -l LABEL
  -f FILE'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_runscript(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_runscript(self, args): # pylint: disable=too-many-return-statements
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-u', '--user')
    arg_parser.add_argument('-g', '--group')
    arg_parser.add_argument('-t', '--timeout')
    arg_parser.add_argument('-s', '--start-time')
    arg_parser.add_argument('-l', '--label')
    arg_parser.add_argument('-f', '--file')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_runscript()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    if is_interactive(options):
        options.user = prompt_user(_('User [root]:'))
        options.group = prompt_user(_('Group [root]:'))

        # defaults
        if not options.user:
            options.user = 'root'
        if not options.group:
            options.group = 'root'

        try:
            options.timeout = prompt_user(_('Timeout (in seconds) [600]:'))
            if options.timeout:
                options.timeout = int(options.timeout)
            else:
                options.timeout = 600
        except ValueError:
            logging.error(_N('Invalid timeout'))
            return 1

        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)

        options.label = prompt_user(_('Label/Short Description [default]:'))
        if options.label == "":
            options.label = None

        options.file = prompt_user(_('Script File [create]:'))

        # read the script provided by the user
        if options.file:
            keep_script_file = True

            script_contents = read_file(os.path.abspath(options.file))
        else:
            # have the user write their script
            (script_contents, options.file) = editor('#!/bin/bash')
            keep_script_file = False

        if not script_contents:
            logging.error(_N('No script provided'))
            return 1
    else:
        if not options.user:
            options.user = 'root'
        if not options.group:
            options.group = 'root'
        if not options.label:
            options.label = None
        if not options.timeout:
            options.timeout = 600
        else:
            options.timeout = int(options.timeout)
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

        if not options.file:
            logging.error(_N('A script file is required'))
            return 1

        script_contents = read_file(options.file)
        keep_script_file = True

    # display a summary
    print('')
    print(_('User:       %s') % options.user)
    print(_('Group:      %s') % options.group)
    print(_('Timeout:    %i seconds') % options.timeout)
    print(_('Start Time: %s') % options.start_time)
    print('')
    if options.label:
        print(_('Label:      %s') % options.label)
    print(_('Script Contents'))
    print('---------------')
    print(script_contents)

    print(_('Systems'))
    print('-------')
    print('\n'.join(sorted(systems)))

    # have the user confirm
    if not self.user_confirm():
        return 1

    scheduled = 0

    if self.check_api_version('10.11'):
        logging.debug('Scheduling all systems for the same action')

        # schedule all systems for the same action
        system_ids = [self.get_system_id(s) for s in systems]
        if not options.label:
            action_id = self.client.system.scheduleScriptRun(self.session,
                                                             system_ids,
                                                             options.user,
                                                             options.group,
                                                             options.timeout,
                                                             script_contents,
                                                             options.start_time)
        else:
            action_id = self.client.system.scheduleScriptRun(self.session,
                                                             options.label,
                                                             system_ids,
                                                             options.user,
                                                             options.group,
                                                             options.timeout,
                                                             script_contents,
                                                             options.start_time)


        logging.info(_N('Action ID: %i') % action_id)
        scheduled = len(system_ids)
    else:
        # older versions of the API require each system to be
        # scheduled individually
        for system in systems:
            system_id = self.get_system_id(system)
            if not system_id:
                continue

            try:
                action_id = \
                    self.client.system.scheduleScriptRun(self.session,
                                                         system_id,
                                                         options.user,
                                                         options.group,
                                                         options.timeout,
                                                         script_contents,
                                                         options.start_time)

                logging.info(_N('Action ID: %i') % action_id)
                scheduled += 1
            except xmlrpclib.Fault as detail:
                logging.debug(detail)
                logging.error(_N('Failed to schedule %s') % system)
                return 1

    logging.info(_N('Scheduled: %i system(s)') % scheduled)

    # don't delete a pre-existing script that the user provided
    if not keep_script_file:
        try:
            os.remove(options.file)
        except OSError:
            logging.error(_N('Could not remove %s') % options.file)
            return 1

    return 0

####################


def help_system_listhardware(self):
    print(_('system_listhardware: List the hardware details of a system'))
    print(_('usage: system_listhardware <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listhardware(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listhardware(self, args):
    arg_parser = get_argument_parser()
    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listhardware()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        cpu = self.client.system.getCpu(self.session, system_id)
        memory = self.client.system.getMemory(self.session, system_id)
        devices = self.client.system.getDevices(self.session, system_id)
        network = self.client.system.getNetworkDevices(self.session,
                                                       system_id)

        # Solaris systems don't have these value s
        for v in ('cache', 'vendor', 'family', 'stepping'):
            if not cpu.get(v):
                cpu[v] = ''

        try:
            dmi = self.client.system.getDmi(self.session, system_id)
        except ExpatError:
            dmi = None

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)
            print('')

        if network:
            print(_('Network'))
            print('-------')

            count = 0
            for device in network:
                if count:
                    print('')
                count += 1

                print(_('Interface:   %s') % device.get('interface'))
                print(_('MAC Address: %s') % device.get('hardware_address').upper())
                print(_('IP Address:  %s') % device.get('ip'))
                print(_('Netmask:     %s') % device.get('netmask'))
                print(_('Broadcast:   %s') % device.get('broadcast'))
                print(_('Module:      %s') % device.get('module'))

            print('')

        print(_('CPU'))
        print('---')
        print(_('Count:    %i') % cpu.get('count'))
        print(_('Arch:     %s') % cpu.get('arch'))
        print(_('MHz:      %s') % cpu.get('mhz'))
        print(_('Cache:    %s') % cpu.get('cache'))
        print(_('Vendor:   %s') % cpu.get('vendor'))
        print(_('Model:    %s') % re.sub(r'\s+', ' ', cpu.get('model')))

        print('')
        print(_('Memory'))
        print('------')
        print(_('RAM:  %i') % memory.get('ram'))
        print(_('Swap: %i') % memory.get('swap'))

        if dmi:
            print('')
            print(_('DMI'))
            print(_('Vendor:       %s') % dmi.get('vendor'))
            print(_('System:       %s') % dmi.get('system'))
            print(_('Product:      %s') % dmi.get('product'))
            print(_('Board:        %s') % dmi.get('board'))

            print('')
            print(_('Asset'))
            print('-----')
            for asset in dmi.get('asset').split(') ('):
                print(re.sub(r'\)|\(', '', asset))

            print('')
            print(_('BIOS Release: %s') % dmi.get('bios_release'))
            print(_('BIOS Vendor:  %s') % dmi.get('bios_vendor'))
            print(_('BIOS Version: %s') % dmi.get('bios_version'))

        if devices:
            print('')
            print(_('Devices'))
            print('-------')

            count = 0
            for device in devices:
                if count:
                    print('')
                count += 1

                if device.get('description') is None:
                    print(_('Description: None'))
                else:
                    print(_('Description: %s') % (
                        wrap(device.get('description'), 60)[0]))
                print(_('Driver:      %s') % device.get('driver'))
                print(_('Class:       %s') % device.get('device_class'))
                print(_('Bus:         %s') % device.get('bus'))

    return 0

####################


def help_system_installpackage(self):
    print(_('system_installpackage: Install a package on a system'))
    print(_('''usage: system_installpackage <SYSTEMS> <PACKAGE ...> [options])

options:
    -s START_TIME'''))

    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_installpackage(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.get_package_names(), text)

    return None

def do_system_installpackage(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_installpackage()
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()

        # remove 'ssm' from the argument list
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    packages_to_install = args

    # get the ID for each system
    system_ids = []
    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue
        system_ids.append(system_id)

    jobs = {}

    if self.check_api_version('10.11'):
        for package in packages_to_install:
            logging.debug('Finding the latest version of %s' % package)

            avail_packages = \
                self.client.system.listLatestAvailablePackage(self.session,
                                                              system_ids,
                                                              package)

            for system in avail_packages:
                system_id = system.get('id')
                if system_id not in jobs:
                    jobs[system_id] = []

                # add this package to the system's queue
                jobs[system_id].append(system.get('package').get('id'))
    else:
        # XXX: Satellite 5.3 compatibility
        for system_id in system_ids:
            logging.debug('Getting available packages for %s' %
                          self.get_system_name(system_id))

            avail_packages = \
                self.client.system.listLatestInstallablePackages(self.session,
                                                                 system_id)

            for package in avail_packages:
                if package.get('name') in packages_to_install:
                    if system_id not in jobs:
                        jobs[system_id] = []

                    jobs[system_id].append(package.get('id'))

    if not jobs:
        logging.warning(_N('No packages to install'))
        return 1

    add_separator = False

    warnings = []
    for system_id in jobs:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        # warn the user if the request can not be 100% fulfilled
        if len(jobs[system_id]) != len(packages_to_install):
            # stash the warnings and show at the end so the user can see them
            warnings.append(system_id)

        print('%s:' % self.get_system_name(system_id))
        for package_id in jobs[system_id]:
            print(self.get_package_name(package_id))

    # show the warnings to the user
    if warnings:
        print('')
    for system_id in warnings:
        logging.warning(_N('%s does not have access to all requested packages') %
                        self.get_system_name(system_id))

    print('')
    print(_('Start Time: %s') % options.start_time)

    if not self.user_confirm(_('Install these packages [y/N]:')):
        return 1

    scheduled = 0
    for system_id in jobs:
        try:
            self.client.system.schedulePackageInstall(self.session,
                                                      system_id,
                                                      jobs[system_id],
                                                      options.start_time)

            scheduled += 1
        except xmlrpclib.Fault:
            logging.error(_N('Failed to schedule %s') % self.get_system_name(system_id))

    logging.info(_N('Scheduled %i system(s)') % scheduled)

    return 0

####################


def help_system_removepackage(self):
    print(_('system_removepackage: Remove a package from a system'))
    print(_('''usage: system_removepackage <SYSTEMS> <PACKAGE ...> [options])

options:
    -s START_TIME'''))

    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_removepackage(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.get_package_names(), text)

    return None

def do_system_removepackage(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_removepackage()
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()

        # remove 'ssm' from the argument list
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    package_list = args

    # get all matching package names
    logging.debug('Finding matching packages')
    matching_packages = \
        filter_results(self.get_package_names(True), package_list, True)

    jobs = {}
    for package_name in matching_packages:
        logging.debug('Finding systems with %s' % package_name)

        installed_systems = {}
        for package_id in self.get_package_id(package_name):
            for system in self.client.system.listSystemsWithPackage(self.session, package_id):
                installed_systems[system.get('name')] = package_id

        # each system has a list of packages to remove so that only one
        # API call needs to be made to schedule all the package removals
        # for each system
        for system in systems:
            if system in installed_systems.keys():
                if system not in jobs:
                    jobs[system] = []

                jobs[system].append(installed_systems[system])

    add_separator = False

    for system in jobs:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print('%s:' % system)
        for package in jobs[system]:
            print(self.get_package_name(package))

    if not jobs:
        return 1

    print('')
    print(_('Start Time: %s') % options.start_time)

    if not self.user_confirm(_('Remove these packages [y/N]:')):
        return 1

    scheduled = 0
    for system in jobs:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        try:
            action_id = self.client.system.schedulePackageRemove(self.session,
                                                                 system_id,
                                                                 jobs[system],
                                                                 options.start_time)

            logging.info(_N('Action ID: %i') % action_id)
            scheduled += 1
        except xmlrpclib.Fault:
            logging.error(_N('Failed to schedule %s') % system)

    logging.info(_N('Scheduled %i system(s)') % scheduled)

    return 0

####################


def help_system_upgradepackage(self):
    print(_('system_upgradepackage: Upgrade a package on a system'))
    print(_('''usage: system_upgradepackage <SYSTEMS> <PACKAGE ...>|* [options]')

options:
    -s START_TIME'''))

    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_upgradepackage(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.get_package_names(), text)

    return None


def do_system_upgradepackage(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    # this will come handy for individual packages, as we call
    # self.do_system_installpackage anyway
    orig_args = args

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_upgradepackage()
        return 1

    # install and upgrade for individual packages are the same
    if not '.*' in args[1:]:
        return self.do_system_installpackage(orig_args)

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()

        # remove 'ssm' from the argument list
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    # make a dictionary of each system and the package IDs to install
    jobs = {}
    minions = {}
    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue
        details = self.client.system.getDetails(self.session, system_id)
        if self.check_api_version('25.0') and \
           details.get('base_entitlement', '') == 'salt_entitled':
            minions[system] = system_id
        else:
            packages = \
                self.client.system.listLatestUpgradablePackages(self.session,
                                                                system_id)

            if packages:
                package_ids = [p.get('to_package_id') for p in packages]
                jobs[system] = package_ids
            else:
                logging.warning(_N('No upgrades available for %s') % system)

    if not jobs and not minions:
        return 1

    add_separator = False

    for system in jobs:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print(system)
        print('-' * len(system))

        # build a temporary list so we can sort by package name
        package_names = []
        for package in jobs[system]:
            name = self.get_package_name(package)

            if name:
                package_names.append(name)
            else:
                logging.error(_N("Couldn't get name for package %i") % package)

        print('\n'.join(sorted(package_names)))

    for system in minions:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        hdr = _('Full package update on systems:')
        print(hdr)
        print('-' * len(hdr))
        print('- {}'.format(system))


    print('')
    print(_('Start Time: %s') % options.start_time)

    if not self.user_confirm(_('Upgrade these systems/packages [y/N]:')):
        return 1

    scheduled = 0
    for system in jobs:
        system_id = self.get_system_id(system)

        try:
            self.client.system.schedulePackageInstall(self.session,
                                                      system_id,
                                                      jobs[system],
                                                      options.start_time)

            scheduled += 1
        except xmlrpclib.Fault:
            logging.error(_N('Failed to schedule %s') % system)

    if minions:
        try:
            sids = list(minions.values())
            self.client.system.schedulePackageUpdate(self.session,
                                                     sids,
                                                     options.start_time)
            scheduled += len(sids)
        except xmlrpclib.Fault:
             logging.error(_N('Failed to schedule %s') % system)


    logging.info(_N('Scheduled %i system(s)') % scheduled)

    return 0

####################


def help_system_listupgrades(self):
    print(_('system_listupgrades: List the available upgrades for a system'))
    print(_('usage: system_listupgrades <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listupgrades(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listupgrades(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listupgrades()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        packages = \
            self.client.system.listLatestUpgradablePackages(self.session,
                                                            system_id)

        if not packages:
            logging.warning(_N('No upgrades available for %s') % system)
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(system)
            print('-' * len(system))

        latest_packages = filter_latest_packages(packages, 'to_version', 'to_release', 'to_epoch')

        for package in sorted(latest_packages.values(), key=itemgetter('name')):
            print(build_package_names({
                'name': package['name'],
                'version': package['to_version'],
                'release': package['to_release'],
                'epoch': package['to_epoch'],
                'arch': package['to_arch']
            }))

    return 0

####################


def help_system_listinstalledpackages(self):
    print(_('system_listinstalledpackages: List the installed packages on a'))
    print(_('                              system'))
    print(_('usage: system_listinstalledpackages <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listinstalledpackages(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listinstalledpackages(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listinstalledpackages()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        packages = self.client.system.listPackages(self.session,
                                                   system_id)

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)
            print('')

        print('\n'.join(build_package_names(packages)))

    return 0

####################


def help_system_listconfigchannels(self):
    print(_('system_listconfigchannels: List the config channels of a system'))
    print(_('usage: system_listconfigchannels <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listconfigchannels(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listconfigchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listconfigchannels()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        try:
            channels = self.client.system.config.listChannels(self.session,
                                                              system_id)
        except xmlrpclib.Fault:
            logging.warning(_N('%s does not support configuration channels') %
                            system)
            continue

        print('\n'.join([c.get('label') for c in channels]))

    return 0

####################


def print_configfiles(self, quiet, filelist):

    # Figure out correct indentation to allow pretty table output
    max_path = max_length([f['path'] for f in filelist], minimum=10)
    max_type = max_length(["file", "directory", "symlink"], minimum=10)
    max_label = max_length([f['channel_label'] for f in filelist], minimum=15)

    # print(header when not in quiet mode)
    if not quiet:
        print('%s  %s  %s' % (
            'path'.ljust(max_path),
            'type'.ljust(max_type),
            'label/type'.ljust(max_label)))

        print('%s  %s  %s' % (
            '-' * max_path,
            '-' * max_type,
            '-' * max_label))

    for f in filelist:
        print('%s  %s  %s' % (f['path'].ljust(max_path),
                              f['type'].ljust(max_type),
                              f['channel_label'].ljust(max_label)))


def help_system_listconfigfiles(self):
    print(_('system_listconfigfiles: List the managed config files of a system'))
    print(_('''usage: system_listconfigfiles <SYSTEMS>')
options:
  -s/--sandbox : list only system-sandbox files
  -l/--local   : list only locally managed files
  -c/--central : list only centrally managed files
  -q/--quiet   : quiet mode (omits the header)'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listconfigfiles(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listconfigfiles(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--sandbox', action='store_true')
    arg_parser.add_argument('-l', '--local', action='store_true')
    arg_parser.add_argument('-c', '--central', action='store_true')
    arg_parser.add_argument('-q', '--quiet', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not options.sandbox and not options.local and not options.central:
        logging.debug("No sandbox/local/central option specified, listing ALL")
        options.sandbox = True
        options.local = True
        options.central = True

    if not args:
        self.help_system_listconfigfiles()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        try:
            # Pass 0 for system-sandbox files
            # Pass 1 for locally managed or centrally managed
            files = self.client.system.config.listFiles(self.session,
                                                        system_id, 0)
            files += self.client.system.config.listFiles(self.session,
                                                         system_id, 1)
        except xmlrpclib.Fault:
            logging.warning(_N('%s does not support configuration channels') %
                            system)
            continue

        # For system sandbox or locally managed files, there is no
        # channel_label so we add a descriptive label for these files
        toprint = []
        for f in files:
            if f['channel_type']['label'] == 'server_import':
                f['channel_label'] = "system_sandbox"
                if options.sandbox:
                    toprint.append(f)

            elif f['channel_type']['label'] == 'local_override':
                f['channel_label'] = "locally_managed"
                if options.local:
                    toprint.append(f)

            elif f['channel_type']['label'] == 'normal':
                if options.central:
                    toprint.append(f)

            else:
                logging.error(_N("Error, unexpected channel type label %s") %
                              f['channel_type']['label'])
                return 1

        self.print_configfiles(options.quiet, toprint)

    return 0

####################


def help_system_addconfigfile(self):
    print(_('system_addconfigfile: Create a configuration file'))
    print(_('Note this is only for system sandbox or locally-managed files'))
    print(_('Centrally managed files should be created via configchannel_addfile'))
    print(_('''usage: system_addconfigfile [SYSTEM] [options]

options:
  -S/--sandbox : list only system-sandbox files
  -L/--local   : list only locally managed files
  -p PATH
  -r REVISION
  -o OWNER [default: root]
  -g GROUP [default: root]
  -m MODE [defualt: 0644]
  -x SELINUX_CONTEXT
  -d path is a directory
  -s path is a symlink
  -b path is a binary (or other file which needs base64 encoding)
  -t SYMLINK_TARGET
  -f local path to file contents

  Note re binary/base64: Some text files, notably those containing trailing
  newlines, those containing ASCII escape characters (or other charaters not
  allowed in XML) need to be sent as binary (-b).  Some effort is made to auto-
  detect files which require this, but you may need to explicitly specify.
'''))


def complete_system_addconfigfile(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_addconfigfile(self, args, update_path=''):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-S', '--sandbox', action='store_true')
    arg_parser.add_argument('-L', '--local', action='store_true')
    arg_parser.add_argument('-p', '--path')
    arg_parser.add_argument('-o', '--owner')
    arg_parser.add_argument('-g', '--group')
    arg_parser.add_argument('-m', '--mode')
    arg_parser.add_argument('-x', '--selinux-ctx')
    arg_parser.add_argument('-t', '--target-path')
    arg_parser.add_argument('-f', '--file')
    arg_parser.add_argument('-r', '--revision')
    arg_parser.add_argument('-s', '--symlink', action='store_true')
    arg_parser.add_argument('-b', '--binary', action='store_true')
    arg_parser.add_argument('-d', '--directory', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    file_info = None

    # the system name can be passed in
    if args:
        options.system = args[0]

    interactive = is_interactive(options)
    if interactive:
        if not options.system:
            while True:
                print(_('Systems'))
                print('----------------------')
                print('\n'.join(sorted(self.do_system_list('', True))))
                print('')

                options.system = prompt_user('Select:', noblank=True)

                # ensure the user enters a valid system
                if options.system in self.do_system_list('', True):
                    break
                print('')
                logging.warning(_N('%s is not a valid system') %
                                options.system)
                print('')

        if update_path:
            options.path = update_path
        else:
            options.path = prompt_user('Path:', noblank=True)

        while not options.local and not options.sandbox:
            answer = prompt_user(_('System-Sandbox or Locally-Managed? [S/L]:'))
            if re.match('L', answer, re.I):
                options.local = True
                localopt = 1
            elif re.match('S', answer, re.I):
                options.sandbox = True
                localopt = 0

    # Set the int variable (required by the API calls) for sandbox/local
    localopt = 0
    if options.local:
        logging.debug("Selected locally-managed")
        localopt = 1
    elif options.sandbox:
        logging.debug("Selected system-sandbox")
    else:
        logging.error(_N("Must choose system-sandbox or locally-managed option"))
        self.help_system_addconfigfile()
        return 1

    if not options.system:
        logging.error(_N("Must provide system"))
        self.help_system_addconfigfile()
        return 1

    system_id = self.get_system_id(options.system)
    logging.debug("Got ID %s for system %s" % (system_id, options.system))

    # check if this file already exists
    try:
        file_info = self.client.system.config.lookupFileInfo(self.session,
                                                             system_id, [options.path], localopt)
        if file_info:
            logging.debug("Found existing file_info %s" % file_info)
    except xmlrpclib.Fault:
        logging.debug("No existing file information found for %s" %
                      options.path)

    file_info = self.configfile_getinfo(args, options, file_info, interactive)

    if self.user_confirm():
        if options.symlink:
            self.client.system.config.createOrUpdateSymlink(self.session,
                                                            system_id, options.path, file_info, localopt)
        else:
            self.client.system.config.createOrUpdatePath(self.session,
                                                         system_id, options.path, options.directory, file_info,
                                                         localopt)

    return 0

####################


def help_system_addconfigchannels(self):
    print(_('system_addconfigchannels: Add config channels to a system'))
    print(_('''usage: system_addconfigchannels <SYSTEMS> <CHANNEL ...> [options]

options:
  -t add channels to the top of the list
  -b add channels to the bottom of the list'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)

def complete_system_addconfigchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.do_configchannel_list('', True),
                             text)

    return None

def do_system_addconfigchannels(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-t', '--top', action='store_true')
    arg_parser.add_argument('-b', '--bottom', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_addconfigchannels()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    channels = args

    if is_interactive(options):
        answer = prompt_user(_('Add to top or bottom? [T/b]:'))
        if re.match('b', answer, re.I):
            options.top = False
        else:
            options.top = True
    else:
        if options.bottom:
            options.top = False
        else:
            options.top = True

    system_ids = [self.get_system_id(s) for s in systems]

    self.client.system.config.addChannels(self.session,
                                          system_ids,
                                          channels,
                                          options.top)

    return 0

####################


def help_system_removeconfigchannels(self):
    print(_('system_removeconfigchannels: Remove config channels from a system'))
    print(_('usage: system_removeconfigchannels <SYSTEMS> <CHANNEL ...>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_removeconfigchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.do_configchannel_list('', True),
                             text)

    return None

def do_system_removeconfigchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_removeconfigchannels()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    channels = args

    system_ids = [self.get_system_id(s) for s in systems]

    self.client.system.config.removeChannels(self.session,
                                             system_ids,
                                             channels)

    return 0

####################


def help_system_setconfigchannelorder(self):
    print(_('system_setconfigchannelorder: Set the ranked order of configuration channels'))
    print(_('usage: system_setconfigchannelorder <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_setconfigchannelorder(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_setconfigchannelorder(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_setconfigchannelorder()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = list(self.ssm.keys())
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    # get the current configuration channels from the first system
    # in the list
    system_id = self.get_system_id(systems[0])
    new_channels = self.client.system.config.listChannels(self.session,
                                                          system_id)
    new_channels = [c.get('label') for c in new_channels]

    # call an interface for the user to make selections
    all_channels = self.do_configchannel_list('', True)
    new_channels = config_channel_order(all_channels, new_channels)

    print('')
    print(_('New Configuration Channels'))
    print('--------------------------')
    for i, new_channel in enumerate(new_channels, 1):
        print('[%i] %s' % (i, new_channel))

    if not self.user_confirm():
        return 1

    system_ids = [self.get_system_id(s) for s in systems]

    self.client.system.config.setChannels(self.session,
                                          system_ids,
                                          new_channels)

    return 0

####################


def help_system_deployconfigfiles(self):
    print(_('system_deployconfigfiles: Deploy all configuration files for a system'))
    print(_('''usage: system_deployconfigfiles <SYSTEMS> [options]

options:
    -s START_TIME'''))

    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_deployconfigfiles(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_deployconfigfiles(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_deployconfigfiles()
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    print('')
    print(_('Start Time: %s') % options.start_time)
    print('')
    print(_('Systems'))
    print('-------')
    print('\n'.join(sorted(systems)))

    message = _('Deploy ALL configuration files to these systems [y/N]:')
    if not self.user_confirm(message):
        return 1

    system_ids = [self.get_system_id(s) for s in systems]

    self.client.system.config.deployAll(self.session,
                                        system_ids,
                                        options.start_time)

    logging.info(_N('Scheduled deployment for %i system(s)') % len(system_ids))

    return 0

####################


def help_system_delete(self):
    print(_('system_delete: Delete a system profile'))
    print(_('''usage: system_delete [options] <SYSTEMS>

    options:
          -c TYPE - Possible values:
             *  'FAIL_ON_CLEANUP_ERR' - fail in case of cleanup error,
             *  'NO_CLEANUP' - do not cleanup, just delete,
             *  'FORCE_DELETE' - Try cleanup first but delete server anyway in case of error
    '''))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_delete(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_delete(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-c', '--cleanuptype', default='NO_CLEANUP',
                            choices=['FAIL_ON_CLEANUP_ERR', 'NO_CLEANUP', 'FORCE_DELETE'])

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_delete()
        return 1

    system_ids = []

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    # get the system ID for each system
    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        system_ids.append(system_id)

    if not system_ids:
        logging.warning(_N('No systems to delete'))
        return 1

    # make the column the right size
    colsize = max_length([self.get_system_name(s) for s in system_ids])
    if colsize < 7:
        colsize = 7

    print(_('%s  System ID') % _('Profile').ljust(colsize))
    print('%s  ---------' % ('-' * colsize))

    # print(a summary for the user)
    for system_id in system_ids:
        print('%s  %i' %
              (self.get_system_name(system_id).ljust(colsize), system_id))

    if not self.user_confirm(_('Delete these systems [y/N]:')):
        return 1

    logging.debug("System IDs to remove: %s", system_ids)
    logging.debug("System names to IDs: %s", systems)

    self.client.system.deleteSystems(self.session, system_ids, options.cleanuptype)
    logging.info(_N('%i system(s) scheduled for removal'), len(system_ids))

    # regenerate the system name cache
    self.generate_system_cache(True, delay=1)

    # remove these systems from the SSM and update the cache
    for system_name in list(systems):
        if system_name in self.ssm:
            self.ssm.pop(system_name)
    logging.debug("SSM stack updated")

    save_cache(self.ssm_cache_file, self.ssm)
    logging.debug("SSM cache saved")

    return 0

####################


def help_system_lock(self):
    print(_('system_lock: Lock a system'))
    print(_('usage: system_lock <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_lock(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_lock(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_lock()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.setLockStatus(self.session, system_id, True)

    return 0

####################


def help_system_unlock(self):
    print(_('system_unlock: Unlock a system'))
    print(_('usage: system_unlock <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_unlock(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_unlock(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_unlock()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.setLockStatus(self.session, system_id, False)

    return 0

####################


def help_system_rename(self):
    print(_('system_rename: Rename a system profile'))
    print(_('usage: system_rename OLDNAME NEWNAME'))

def complete_system_rename(self, text, line, beg, end):
    if len(line.split(' ')) == 2:
        return tab_completer(self.get_system_names(), text)

    return None


def do_system_rename(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_system_rename()
        return 1

    (old_name, new_name) = args

    system_id = self.get_system_id(old_name)
    if not system_id:
        return 1

    print('%s (%s) -> %s' % (old_name, system_id, new_name))
    if not self.user_confirm():
        return 1

    self.client.system.setProfileName(self.session,
                                      system_id,
                                      new_name)

    # regenerate the cache of systems
    self.generate_system_cache(True)

    # update the SSM
    if old_name in self.ssm:
        self.ssm.remove(old_name)
        self.ssm.append(new_name)

    return 0

####################


def help_system_listcustomvalues(self):
    print(_('system_listcustomvalues: List the custom values for a system'))
    print(_('usage: system_listcustomvalues <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listcustomvalues(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listcustomvalues(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listcustomvalues()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    add_separator = False

    for system in systems:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)
            print('')

        system_id = self.get_system_id(system)
        if not system_id:
            continue

        values = self.client.system.getCustomValues(self.session,
                                                    system_id)

        for v in values:
            print('%s = %s' % (v, values[v]))

    return 0

####################


def help_system_addcustomvalue(self):
    print(_('system_addcustomvalue: Set a custom value for a system'))
    print(_('usage: system_addcustomvalue KEY VALUE <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_addcustomvalue(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return tab_completer(self.do_custominfo_listkeys('', True), text)
    elif len(parts) >= 4:
        return self.tab_complete_systems(text)

    return None

def do_system_addcustomvalue(self, args):
    if not isinstance(args, list):
        arg_parser = get_argument_parser()

        (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 3:
        self.help_system_addcustomvalue()
        return 1

    key = args[0]
    value = args[1]

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args[2:])

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.setCustomValues(self.session,
                                           system_id,
                                           {key: value})

    return 0

####################


def help_system_updatecustomvalue(self):
    print(_('system_updatecustomvalue: Update a custom value for a system'))
    print(_('usage: system_updatecustomvalue KEY VALUE <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)

def complete_system_updatecustomvalue(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return tab_completer(self.do_custominfo_listkeys('', True), text)
    elif len(parts) >= 4:
        return self.tab_complete_systems(text)

    return None


def do_system_updatecustomvalue(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 3:
        self.help_system_updatecustomvalue()
        return 1

    return self.do_system_addcustomvalue(args)

####################


def help_system_removecustomvalues(self):
    print(_('system_removecustomvalues: Remove a custom value for a system'))
    print(_('usage: system_removecustomvalues <SYSTEMS> <KEY ...>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)

def complete_system_removecustomvalues(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) == 3:
        return tab_completer(self.do_custominfo_listkeys('', True),
                             text)

    return None

def do_system_removecustomvalues(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_removecustomvalues()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    keys = args[1:]

    if not self.user_confirm(_('Delete these values [y/N]:')):
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.deleteCustomValues(self.session,
                                              system_id,
                                              keys)

    return 0

####################


def help_system_addnote(self):
    print(_('system_addnote: Set a note for a system'))
    print(_('''usage: system_addnote <SYSTEM> [options]

options:
  -s SUBJECT
  -b BODY'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_addnote(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_addnote(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--subject')
    arg_parser.add_argument('-b', '--body')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 1:
        self.help_system_addnote()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    if is_interactive(options):
        options.subject = prompt_user(_('Subject of the Note:'), noblank=True)

        message = _('Note Body (ctrl-D to finish):')
        options.body = prompt_user(message, noblank=True, multiline=True)
    else:
        if not options.subject:
            logging.error(_N('A subject is required'))
            return 1

        if not options.body:
            logging.error(_N('A body is required'))
            return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.addNote(self.session,
                                   system_id,
                                   options.subject,
                                   options.body)

    return 0

####################


def help_system_deletenotes(self):
    print(_('system_deletenotes: Delete notes from a system'))
    print(_('usage: system_deletenotes <SYSTEM> <ID|*>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_deletenotes(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_deletenotes(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listnotes()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    note_ids = args

    if not args:
        logging.warning(_N('No notes to delete'))
        return None

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if '.*' in note_ids:
            self.client.system.deleteNotes(self.session, system_id)
        else:
            for note_id in note_ids:
                try:
                    note_id = int(note_id)
                except ValueError:
                    logging.warning(_N('%s is not a valid note ID') % note_id)
                    continue

                # deleteNote does not throw an exception
                self.client.system.deleteNote(self.session, system_id, note_id)

    return 0

####################


def help_system_listnotes(self):
    print(_('system_listnotes: List the available notes for a system'))
    print(_('usage: system_listnotes <SYSTEM>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listnotes(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listnotes(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listnotes()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    add_separator = False

    for system in sorted(systems):
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)
            print('')

        system_id = self.get_system_id(system)
        if not system_id:
            continue

        notes = self.client.system.listNotes(self.session, system_id)

        for n in notes:
            print('%d. %s (%s)' % (n['id'], n['subject'], n['creator']))
            print(n['note'])
            print('')

    return 0

####################

####################


def help_system_listfqdns(self):
    print(_('system_listfqdns: List the associated FQDNs for a system'))
    print(_('usage: system_listfqdns <SYSTEM>'))
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listfqdns(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listfqdns(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listfqdns()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    add_separator = False

    for system in sorted(systems):
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)
            print('')

        system_id = self.get_system_id(system)
        if not system_id:
            continue

        fqdns = self.client.system.listFqdns(self.session, system_id)

        for f in fqdns:
            print(f)

    return 0

####################

def help_system_setbasechannel(self):
    print(_("system_setbasechannel: Set a system's base software channel"))
    print(_('usage: system_setbasechannel <SYSTEMS> CHANNEL'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_setbasechannel(self, text, line, beg, end):
    if len(line.split(' ')) == 2:
        return self.tab_complete_systems(text)
    elif len(line.split(' ')) == 3:
        return tab_completer(self.list_base_channels(), text)

    return None


def do_system_setbasechannel(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_system_setbasechannel()
        return 1

    new_channel = args.pop()

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    add_separator = False

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        old = self.client.system.getSubscribedBaseChannel(self.session,
                                                          system_id)

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print(_('System:           %s') % system)
        print(_('Old Base Channel: %s') % old.get('label'))
        print(_('New Base Channel: %s') % new_channel)

    if not self.user_confirm():
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.setBaseChannel(self.session,
                                          system_id,
                                          new_channel)

    return 0

####################

def help_system_schedulechangechannels(self):
    print(_("system_schedulechangechannels: Schedule changing a system's software channels"))
    print(_('''usage: system_setbasechannel <SYSTEMS> [options]

options:
  -b BASE_CHANNEL base channel label
  -c CHILD_CHANNEL child channel labels (allowed multiple times)
  -s START_TIME time defaults to now'''))
    print(self.HELP_SYSTEM_OPTS)


def complete_system_schedulechangechannels(self, text, line, beg, end):

    if len(line.split(' ')) == 2:
        return self.tab_complete_systems(text)

    return None

def do_system_schedulechangechannels(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-b', '--base')
    arg_parser.add_argument('-c', '--child', action='append', default=[])
    arg_parser.add_argument('-s', '--start-time', action='store')

    (args, options) = parse_command_arguments(args, arg_parser)
    # import pdb;
    # pdb.set_trace()
    if len(args) < 1:
        self.help_system_schedulechangechannels()
        return 1

    if not options.base:
        logging.error(_N('A base channel is required'))
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    if not options.start_time:
        options.start_time = parse_time_input('now')
    else:
        options.start_time = parse_time_input(options.start_time)

    baseChannel = options.base
    childChannels = options.child or []

    add_separator = False

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        oldBase = self.client.system.getSubscribedBaseChannel(self.session,
                                                              system_id)

        oldKids = self.client.system.listSubscribedChildChannels(self.session,
                                                                 system_id)
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print(_('System:           %s') % system)
        print(_('Old Base Channel: %s') % oldBase.get('label'))
        print(_('Old Child Channels: %s') % ', '.join([k.get('label') for k in oldKids]))
        print(_('New Base Channel: %s') % baseChannel)
        print(_('New Child channels %s') % ', '.join(childChannels))

    if not self.user_confirm():
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        actionId = self.client.system.scheduleChangeChannels(self.session,
                                                             system_id,
                                                             baseChannel,
                                                             childChannels,
                                                             options.start_time)
        print(_('Scheduled action id: %s') % actionId)

    return 0

####################

def help_system_listbasechannel(self):
    print(_('system_listbasechannel: List the base channel for a system'))
    print(_('usage: system_listbasechannel <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listbasechannel(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listbasechannel(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listbasechannel()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        channel = \
            self.client.system.getSubscribedBaseChannel(self.session,
                                                        system_id)

        print(channel.get('label'))

    return 0

####################


def help_system_listchildchannels(self):
    print(_('system_listchildchannels: List the child channels for a system'))
    print(_('usage: system_listchildchannels <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listchildchannels(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listchildchannels(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listchildchannels()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        channels = \
            self.client.system.listSubscribedChildChannels(self.session,
                                                           system_id)

        print('\n'.join(sorted([c.get('label') for c in channels])))

    return 0

####################


def help_system_addchildchannels(self):
    print(_("system_addchildchannels: Add child channels to a system"))
    print(_('usage: system_addchildchannels <SYSTEMS> <CHANNEL ...>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_addchildchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.list_child_channels(), text)

    return None

def do_system_addchildchannels(self, args):
    self.manipulate_child_channels(args)
    return 0

####################


def help_system_removechildchannels(self):
    print(_("system_removechildchannels: Remove child channels from a system"))
    print(_('usage: system_removechildchannels <SYSTEMS> <CHANNEL ...>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_removechildchannels(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return tab_completer(self.list_child_channels(), text)

    return None

def do_system_removechildchannels(self, args):
    self.manipulate_child_channels(args, True)
    return 0

####################


def help_system_details(self):
    print(_('system_details: Show the details of a system profile'))
    print(_('usage: system_details <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_details(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_details(self, args, short=False):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_details()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        last_checkin = \
            self.client.system.getName(self.session,
                                       system_id).get('last_checkin')

        details = self.client.system.getDetails(self.session, system_id)

        if self.check_api_version('10.16'):
            uuid = self.client.system.getUuid(self.session, system_id)
        else:
            uuid = None

        registered = self.client.system.getRegistrationDate(self.session,
                                                            system_id)

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print(_('Name:          %s') % details.get('profile_name'))
        print(_('System ID:     %i') % system_id)

        if uuid:
            print(_('UUID:          %s') % uuid)

        print(_('Locked:        %s') % details.get('lock_status'))
        print(_('Registered:    %s') % registered)
        print(_('Last Checkin:  %s') % last_checkin)
        print(_('OSA Status:    %s') % details.get('osa_status'))
        print(_('Last Boot:     %s') % details.get('last_boot'))
        if 'contact_method' in details:
            print(_('Contact Method:%s') % details.get('contact_method'))

        # only print(basic information if requested)
        if short:
            continue

        network = self.client.system.getNetwork(self.session, system_id)

        entitlements = self.client.system.getEntitlements(self.session,
                                                          system_id)

        base_channel = \
            self.client.system.getSubscribedBaseChannel(self.session,
                                                        system_id)

        child_channels = \
            self.client.system.listSubscribedChildChannels(self.session,
                                                           system_id)

        groups = self.client.system.listGroups(self.session,
                                               system_id)

        kernel = self.client.system.getRunningKernel(self.session,
                                                     system_id)

        keys = self.client.system.listActivationKeys(self.session,
                                                     system_id)

        ranked_config_channels = []

        try:
            config_channels = \
                self.client.system.config.listChannels(self.session, system_id)
        except xmlrpclib.Fault as exc:
            # 10003 - unsupported operation
            if exc.faultCode == 10003:
                logging.debug(exc.faultString)
            else:
                logging.warning(exc.faultString)
        else:
            for channel in config_channels:
                ranked_config_channels.append(channel.get('label'))

        print('')
        print(_('Hostname:      %s') % network.get('hostname'))
        print(_('IP Address:    %s') % network.get('ip'))
        print(_('Kernel:        %s') % kernel)

        if keys:
            print('')
            print(_('Activation Keys'))
            print('---------------')
            print('\n'.join(sorted(keys)))

        print('')
        print(_('Software Channels'))
        print('-----------------')
        print(base_channel.get('label'))

        for channel in child_channels:
            print('  |-- %s' % channel.get('label'))

        if ranked_config_channels:
            print('')
            print(_('Configuration Channels'))
            print('----------------------')
            print('\n'.join(ranked_config_channels))

        print('')
        print(_('Entitlements'))
        print('------------')
        print('\n'.join(sorted(entitlements)))

        if groups:
            print('')
            print(_('System Groups'))
            print('-------------')
            for group in groups:
                if group.get('subscribed') == 1:
                    print(group.get('system_group_name'))

    return 0

####################


def help_system_listerrata(self):
    print(_('system_listerrata: List available errata for a system'))
    print(_('usage: system_listerrata <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listerrata(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listerrata(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listerrata()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)
            print('')

        errata = self.client.system.getRelevantErrata(self.session,
                                                      system_id)

        print_errata_list(errata)

    return 0

####################


def help_system_applyerrata(self):
    print(_('system_applyerrata: Apply errata to a system'))
    print(_('''usage: system_applyerrata [options] <SYSTEMS>
[ERRATA|search:XXX ...]

options:
  -s START_TIME'''))
    print('')
    print(self.HELP_TIME_OPTS)
    print('')
    print(self.HELP_SYSTEM_OPTS)

def complete_system_applyerrata(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return self.tab_complete_errata(text)

    return None

def do_system_applyerrata(self, args):
    # this is really just an entry point to do_errata_apply
    # and the whole parsing of the start time needed is done
    # there; here we only make sure we accept this option
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_applyerrata()
        return 1

    # use the systems applyed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    # allow globbing and searching of errata
    errata_list = self.expand_errata(args)

    if not errata_list or not systems:
        return 1

    # reconstruct options so we can pass them to do_errata_apply
    opts = []
    if options.start_time:
        opts.append('-s ' + options.start_time)

    return self.do_errata_apply(' '.join(opts), errata_list, systems)

####################


def help_system_listevents(self):
    if self.check_api_version('25.0'):
        logging.warning(_('This method is deprecated and will be removed in a future API version. '
        'Please use system_listeventhistory instead.\n'))

    print(_('system_listevents: List the event history for a system'))
    print(_('usage: system_listevents <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listevents(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listevents(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listevents()
        return 1

    if self.check_api_version('25.0'):
        logging.warning(_('This method is deprecated and will be removed in a future API version. '
        'Please use system_listeventhistory instead.\n'))

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    add_separator = False

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        events = self.client.system.getEventHistory(self.session, system_id)

        for e in events:
            print('')
            print(_('Summary:   %s') % e.get('summary'))
            print(_('Completed: %s') % e.get('completed'))
            print(_('Details:   %s') % e.get('details'))

    return 0

####################


def help_system_listeventhistory(self):
    print(_('system_listeventhistory: List the event history for a system'))
    print(_('''usage: system_listeventhistory <SYSTEMS> [options]

options:
  -s START_TIME list only the events happened after the specified time. [Default: returns all events]
  -o OFFSET skip the first events. Ignored if -l is not specified as well. [Default: 0]
  -l LIMIT limit the results to the specified number of events. [Default: no limit]'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listeventhistory(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listeventhistory(self, args):
    if not self.check_api_version('25.0'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')
    arg_parser.add_argument('-o', '--offset')
    arg_parser.add_argument('-l', '--limit')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listeventhistory()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    if not options.start_time:
        options.start_time = datetime(1970, 1, 1)
    else:
        options.start_time = parse_time_input(options.start_time)
        if not options.start_time:
            return 1

    if not options.offset:
        options.offset = 0
    else:
        try:
            options.offset = int(options.offset)
        except ValueError:
            logging.error(_('Invalid offset'))
            return 1

    if options.limit:
        try:
            options.limit = int(options.limit)
        except ValueError:
            logging.error(_('Invalid limit'))
            return 1

    add_separator = False

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        if options.limit:
            events = self.client.system.getEventHistory(self.session, system_id,
                                                        options.start_time, options.offset, options.limit)
        else:
            events = self.client.system.getEventHistory(self.session, system_id, options.start_time)

        for e in events:
            print('')
            print(_('Id:           %s') % e.get('id'))
            print(_('History type: %s') % e.get('history_type'))
            print(_('Status:       %s') % e.get('status'))
            print(_('Summary:      %s') % e.get('summary'))
            print(_('Completed:    %s') % e.get('completed'))

    return 0

####################


def help_system_eventdetails(self):
    print(_('system_eventdetails: Retrieve the details of an event for a system'))
    print(_('usage: system_eventdetails <SYSTEM> <EVENT>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_eventdetails(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)

    return None


def do_system_eventdetails(self, args):
    if not self.check_api_version('25.0'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_eventdetails()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args.pop(0))

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    if not args:
        logging.warning(_N('No event specified'))
        return 1

    try:
        event_id = int(args.pop(0))
    except ValueError:
        logging.error(_('Invalid event id'))
        return 1

    add_separator = False

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        print('')

        try:
            detail = self.client.system.getEventDetails(self.session, system_id, event_id)
        except xmlrpclib.Fault:
            print(_('No event %s found in the history of system %s' % (event_id, system_id)))
            continue

        print(_('Id:              %s') % detail.get('id'))
        print('')
        print(_('History type:    %s') % detail.get('history_type'))
        print(_('Status:          %s') % detail.get('status'))
        print(_('Summary:         %s') % detail.get('summary'))
        print('')
        print(_('Created:         %s') % detail.get('created'))
        print(_('Picked up:       %s') % detail.get('picked_up'))
        print(_('Completed:       %s') % detail.get('completed'))

        if detail.get('history_type') != 'History Event':
            print('')
            print(_('Earliest action: %s') % detail.get('earliest_action'))
            print(_('Result message:  %s') % detail.get('result_msg'))
            print(_('Result code:     %s') % detail.get('result_code'))

            additional_info = detail.get('additional_info')
            if additional_info and additional_info:
                print('')
                print(_('Additional info:'))

                info_separator = False

                for info in additional_info:
                    if info_separator:
                        print('')
                    info_separator = True

                    print(_('    Result:          %s') % info.get('result'))
                    print(_('    Detail:          %s') % info.get('detail'))

    return 0

####################


def help_system_listentitlements(self):
    print(_('system_listentitlements: List the entitlements for a system'))
    print(_('usage: system_listentitlements <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_listentitlements(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_listentitlements(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_listentitlements()
        return 1

    add_separator = False

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(systems) > 1:
            print(_('System: %s') % system)

        entitlements = self.client.system.getEntitlements(self.session,
                                                          system_id)

        print('\n'.join(sorted(entitlements)))

    return 0

####################


def help_system_addentitlements(self):
    print(_('system_addentitlements: Add entitlements to a system'))
    print(_('usage: system_addentitlements <SYSTEMS> ENTITLEMENT'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_addentitlements(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)

    return tab_completer(self.ENTITLEMENTS, text)


def do_system_addentitlements(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_addentitlements()
        return 1

    entitlement = args.pop()

    for e in self.ENTITLEMENTS:
        if re.match(entitlement, e, re.I):
            entitlement = e
            break

    # use the systems applyed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.addEntitlements(self.session,
                                           system_id,
                                           [entitlement])

    return 0

####################


def help_system_removeentitlement(self):
    print(_('system_removeentitlement: Remove an entitlement from a system'))
    print(_('usage: system_removeentitlement <SYSTEMS> ENTITLEMENT'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_removeentitlement(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)

    return tab_completer(self.ENTITLEMENTS, text)


def do_system_removeentitlement(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_removeentitlement()
        return 1

    entitlement = args.pop()

    for e in self.ENTITLEMENTS:
        if re.match(entitlement, e, re.I):
            entitlement = e
            break

    # use the systems applyed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.removeEntitlements(self.session,
                                              system_id,
                                              [entitlement])

    return 0

####################


def help_system_listpackageprofiles(self):
    print(_('system_listpackageprofiles: List all package profiles'))
    print(_('usage: system_listpackageprofiles'))


def do_system_listpackageprofiles(self, args, doreturn=False):
    profiles = self.client.system.listPackageProfiles(self.session)
    profiles = [p.get('name') for p in profiles]

    if doreturn:
        return profiles
    else:
        if profiles:
            print('\n'.join(sorted(profiles)))

    return 0

####################


def help_system_deletepackageprofile(self):
    print(_('system_deletepackageprofile: Delete a package profile'))
    print(_('usage: system_deletepackageprofile PROFILE'))

def complete_system_deletepackageprofile(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return self.tab_complete_systems(
            self.do_system_listpackageprofiles('', True), text)

    return None

def do_system_deletepackageprofile(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_deletepackageprofile()
        return 1

    label = args[0]

    if not self.user_confirm(_('Delete this profile [y/N]:')):
        return 1

    all_profiles = self.client.system.listPackageProfiles(self.session)

    profile_id = 0
    for profile in all_profiles:
        if label == profile.get('name'):
            profile_id = profile.get('id')

    if not profile_id:
        logging.warning(_N('%s is not a valid profile') % label)
        return 1

    self.client.system.deletePackageProfile(self.session, profile_id)

    return 0

####################


def help_system_createpackageprofile(self):
    print(_('system_createpackageprofile: Create a package profile'))
    print(_('''usage: system_createpackageprofile SYSTEM [options]

options:
  -n NAME
  -d DESCRIPTION'''))


def complete_system_createpackageprofile(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)

    return None

def do_system_createpackageprofile(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-n', '--name')
    arg_parser.add_argument('-d', '--description')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) != 1:
        self.help_system_createpackageprofile()
        return 1

    system_id = self.get_system_id(args[0])
    if not system_id:
        return 1

    if is_interactive(options):
        options.name = prompt_user(_('Profile Label:'), noblank=True)
        options.description = prompt_user(_('Description:'), multiline=True)
    else:
        if not options.name:
            logging.error(_N('A profile name is required'))
            return 1

        if not options.description:
            logging.error(_N('A profile description is required'))
            return 1

    self.client.system.createPackageProfile(self.session,
                                            system_id,
                                            options.name,
                                            options.description)

    logging.info(_N("Created package profile '%s'") % options.name)

    return 0

####################


def help_system_comparepackageprofile(self):
    print(_('system_comparepackageprofile: Compare a system against a package profile'))
    print(_('usage: system_comparepackageprofile <SYSTEMS> PROFILE'))
    print('')
    print(self.HELP_SYSTEM_OPTS)

def complete_system_comparepackageprofile(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    elif len(parts) > 2:
        return self.tab_complete_systems(
            self.do_system_listpackageprofiles('', True), parts[-1])

    return None

def do_system_comparepackageprofile(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_comparepackageprofile()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
        args.pop(0)
    else:
        systems = self.expand_systems(args[:-1])

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    profile = args[-1]

    add_separator = False

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        results = self.client.system.comparePackageProfile(self.session,
                                                           system_id,
                                                           profile)

        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        print('%s:' % system)
        self.print_package_comparison(results)

    return 0

####################


def help_system_comparepackages(self):
    print(_('system_comparepackages: Compare the packages between two systems'))
    print(_('usage: system_comparepackages SOME_SYSTEM ANOTHER_SYSTEM'))


def complete_system_comparepackages(self, text, line, beg, end):
    return tab_completer(self.get_system_names(), text)


def do_system_comparepackages(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_system_comparepackages()
        return 1

    this_system = self.get_system_id(args[0])
    other_system = self.get_system_id(args[1])

    results = self.client.system.comparePackages(self.session,
                                                 this_system,
                                                 other_system)

    self.print_package_comparison(results)

    return 0

####################


def help_system_syncpackages(self):
    print(_('system_syncpackages: Sync packages between two systems'))
    print(_('''usage: system_syncpackages SOURCE TARGET [options]

options:
    -s START_TIME'''))
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_syncpackages(self, text, line, beg, end):
    return tab_completer(self.get_system_names(), text)


def do_system_syncpackages(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_system_syncpackages()
        return 1

    (source, target) = args

    source_id = self.get_system_id(source)
    target_id = self.get_system_id(target)

    if not source_id or not target_id:
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # show a comparison and ask for confirmation
    self.do_system_comparepackages('%s %s' % (source_id, target_id))

    print('')
    print(_('Start Time: %s') % options.start_time)

    if not self.user_confirm(_('Sync packages [y/N]:')):
        return 1

    # get package IDs
    packages = self.client.system.listPackages(self.session, source_id)

    package_names = build_package_names(packages)

    package_ids = []

    for name in package_names:
        p_ids = self.get_package_id(name)

        # filter out invalid package IDs
        if p_ids:
            package_ids += p_ids

    self.client.system.scheduleSyncPackagesWithSystem(self.session,
                                                      target_id,
                                                      source_id,
                                                      package_ids,
                                                      options.start_time)

    return 0

####################


def filter_latest_packages(pkglist, version_key='version',
                           release_key='release', epoch_key='epoch'):
    # Returns a dict, indexed by a compound (tuple) key based on
    # arch and name, so we can store the latest version of each package
    # for each arch.  This approach avoids nested loops :)
    latest = {}
    for p in pkglist:
        if 'arch_label' in p:
            tuplekey = p['name'], p['arch_label']
        elif 'arch' in p:
            # Fixup arch==AMD64 which is returned for some reason
            p['arch'] = re.sub('AMD64', 'x86_64', p['arch'])
            tuplekey = p['name'], p['arch']
        else:
            logging.error(_N("Failed to filter package list, package %s") % p
                          + _N("found with no arch or arch_label"))
            return None
        if not tuplekey in latest:
            latest[tuplekey] = p
        else:
            # Already have this package, is p newer?
            if p == latest_pkg(p, latest[tuplekey], version_key, release_key, epoch_key):
                latest[tuplekey] = p

    return latest


def print_comparison_withchannel(self, channelnewer, systemnewer,
                                 channelmissing, channel_latest):

    # Figure out correct indentation to allow pretty table output
    results = channelnewer + systemnewer + channelmissing

    tmp_names = []
    tmp_system = []
    tmp_channel = []
    for item in results:
        name_string = "%(name)s.%(arch)s" % item
        tmp_names.append(name_string)
        # Create two version-string lists, one for the version in the results
        # list, and another with the version string from the channel_latest
        # dict, if the channel contains a matching package
        version_string = "%(version)s-%(release)s" % item
        tmp_system.append(version_string)
        key = item['name'], item['arch']
        if key in channel_latest:
            version_string = "%(version)s-%(release)s" % channel_latest[key]
            tmp_channel.append(version_string)

    max_name = max_length(tmp_names, minimum=7)
    max_system = max_length(tmp_system, minimum=11)
    max_channel = max_length(tmp_channel, minimum=15)
    max_comparison = 25

    # print(headers)
    print('%s  %s  %s  %s' % (
        _('Package').ljust(max_name),
        _('System Version').ljust(max_system),
        _('Channel Version').ljust(max_channel),
        _('Difference').ljust(max_comparison)))

    print('%s  %s  %s  %s' % (
        '-' * max_name,
        '-' * max_system,
        '-' * max_channel,
        '-' * max_comparison))

    # Then print(the packages)
    for item in channelnewer:
        name_string = "%(name)s.%(arch)s" % item
        version_string = "%(version)s-%(release)s" % item
        key = item['name'], item['arch']
        if key in channel_latest:
            channel_version = "%(version)s-%(release)s" % channel_latest[key]
        else:
            channel_version = '-'
        print('%s  %s  %s  %s' % (
            name_string.ljust(max_name),
            version_string.ljust(max_system),
            channel_version.ljust(max_channel),
            _("Channel_newer_than_system").ljust(max_comparison)))
    for item in systemnewer:
        name_string = "%(name)s.%(arch)s" % item
        version_string = "%(version)s-%(release)s" % item
        key = item['name'], item['arch']
        if key in channel_latest:
            channel_version = "%(version)s-%(release)s" % channel_latest[key]
        else:
            channel_version = '-'
        print('%s  %s  %s  %s' % (
            name_string.ljust(max_name),
            version_string.ljust(max_system),
            channel_version.ljust(max_channel),
            _("System_newer_than_channel").ljust(max_comparison)))
    for item in channelmissing:
        name_string = "%(name)s.%(arch)s" % item
        version_string = "%(version)s-%(release)s" % item
        channel_version = '-'
        print('%s  %s  %s  %s' % (
            name_string.ljust(max_name),
            version_string.ljust(max_system),
            channel_version.ljust(max_channel),
            _("Missing_in_channel").ljust(max_comparison)))


def help_system_comparewithchannel(self):
    print(_('system_comparewithchannel: Compare the installed packages on a'))
    print(_('                           system with those in the channels it is'))
    print(_('                           registerd to, or optionally some other'))
    print(_('                           channel'))
    print(_('usage: system_comparewithchannel <SYSTEMS> [options]'))
    print(_('options:'))
    print(_('         -c/--channel : Specific channel to compare against,'))
    print(_('                        default is those subscribed to, including'))
    print(_('                        child channels'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_comparewithchannel(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_comparewithchannel(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-c', '--channel')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_comparewithchannel()
        return 1

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    channel_latest = {}
    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        instpkgs = self.client.system.listPackages(self.session,
                                                   system_id)
        logging.debug("Got %d packages installed in system %s" %
                      (len(instpkgs), system))
        # We need to filter to get only the latest installed packages,
        # because multiple versions (e.g kernel) can be installed
        packages = filter_latest_packages(instpkgs)
        logging.debug("Got latest %d packages installed in system %s" %
                      (len(packages.keys()), system))

        channels = []
        if options.channel:
            # User specified a specific channel, check it exists
            allch = self.client.channel.listSoftwareChannels(self.session)
            allch_labels = [c['label'] for c in allch]
            if not options.channel in allch_labels:
                logging.error(_N("Specified channel does not exist"))
                self.help_system_comparewithchannel()
                return None
            channels = [options.channel]
            logging.debug("User specified channel %s" % options.channel)
        else:
            # No specified channel, so we create a list of all channels the
            # system is subscribed to
            basech = self.client.system.getSubscribedBaseChannel(self.session,
                                                                 system_id)
            if not basech:
                logging.error(_N("system %s is not subscribed to any channel!")
                              % system)
                logging.error(_N("Please subscribe to a channel, or specify a" +
                                 "channel to compare with"))
                return 1
            logging.debug("base channel %s for %s" % (basech['name'], system))
            childch = self.client.system.listSubscribedChildChannels(
                self.session, system_id)
            channels = [basech['label']]
            for c in childch:
                channels.append(c['label'])

        # Get the latest packages in each channel
        latestpkgs = {}
        for c in channels:
            if not c in channel_latest:
                logging.debug("Getting packages for channel %s" % c)
                pkgs = self.client.channel.software.listAllPackages(
                    self.session, c)
                # filter_latest_packages Returns a dict of latest packages
                # indexed by name,arch tuple, which we add to the dict-of-dict
                # channel_latest, to avoid getting the same channel data
                # multiple times when processing more than one system
                channel_latest[c] = filter_latest_packages(pkgs)
            # Merge the channel latest dicts into one latestpkgs dict
            # We handle collisions and only store the latest version
            # We do this for every channel of every system, since the mix of
            # subscribed channels may be different
            for key in channel_latest[c].keys():
                if not key in latestpkgs:
                    latestpkgs[key] = channel_latest[c][key]
                else:
                    p_newest = latest_pkg(channel_latest[c][key], latestpkgs[key])
                    latestpkgs[key] = p_newest

        if len(systems) > 1:
            print(_('\nSystem: %s') % system)

        # Iterate over the installed packages
        channelnewer = []
        systemnewer = []
        channelmissing = []
        for key in packages:
            syspkg = packages.get(key)
            if key in latestpkgs:
                chpkg = latestpkgs.get(key)
                newest = latest_pkg(syspkg, chpkg)
                if syspkg == newest:
                    systemnewer.append(syspkg)
                elif chpkg == newest:
                    channelnewer.append(syspkg)
            else:
                channelmissing.append(syspkg)
        self.print_comparison_withchannel(channelnewer, systemnewer,
                                          channelmissing, latestpkgs)

    return 0

####################


def help_system_schedulehardwarerefresh(self):
    print(_('system_schedulehardwarerefresh: Schedule a hardware refresh for a system'))
    print(_('''usage: system_schedulehardwarerefresh <SYSTEMS> [options]

options:
  -s START_TIME'''))

    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_schedulehardwarerefresh(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_schedulehardwarerefresh(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_schedulehardwarerefresh()
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.scheduleHardwareRefresh(self.session,
                                                   system_id,
                                                   options.start_time)

    return 0

####################


def help_system_schedulepackagerefresh(self):
    print(_('system_schedulepackagerefresh: Schedule a software package refresh for a system'))
    print(_('''usage: system_schedulepackagerefresh <SYSTEMS> [options])

options:
  -s START_TIME'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def complete_system_schedulepackagerefresh(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_system_schedulepackagerefresh(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_schedulepackagerefresh()
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in systems:
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.schedulePackageRefresh(self.session,
                                                  system_id,
                                                  options.start_time)

    return 0

####################


def help_system_show_packageversion(self):
    print(_('system_show_packageversion: Shows version of installed package on given system(s)'))
    print(_('usage: system_show_packageversion <SYSTEM> <PACKAGE>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_show_packageversion(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)

    return tab_completer(self.get_package_names(), text)


def do_system_show_packageversion(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_system_show_packageversion()
        return 1

    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    print(_("Package\tVersion\tRelease\tEpoch\tArch\tSystem"))
    print("==============================================")
    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        instpkgs = self.client.system.listPackages(self.session, system_id)
        searchpkg = args[1]
        for pkg in instpkgs:
            if pkg.get('name') == searchpkg:
                print("%s\t%s\t%s\t%s\t%s\t%s" % (pkg.get('name'), pkg.get('version'), pkg.get('release'),
                                                  pkg.get('epoch'), pkg.get('arch_label'), system))

    return 0

####################


def help_system_setcontactmethod(self):
    print(_('system_setcontactmethod: Set the contact method for given system(s).'))
    print(_('Available contact methods: ' + str(self.CONTACT_METHODS)))
    print(_('usage: system_setcontactmethod <SYSTEMS> <CONTACT_METHOD>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_system_setcontactmethod(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return self.tab_complete_systems(text)
    else:
        return tab_completer(self.CONTACT_METHODS, text)


def do_system_setcontactmethod(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_system_setcontactmethod()
        return 1

    contact_method = args.pop()
    details = {'contact_method': contact_method}

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            continue

        self.client.system.setDetails(self.session, system_id, details)

    return 0

####################


def help_system_scheduleapplyconfigchannels(self):
    print(_("system_scheduleapplyconfigchannels: "
            "Schedule applying the assigned config channels to the System (Minion only)"))
    print(_('''usage: scheduleapplyconfigchannels <SYSTEMS> [options]

    options:
        -s START_TIME'''))
    print('')
    print(self.HELP_SYSTEM_OPTS)
    print('')
    print(self.HELP_TIME_OPTS)


def do_system_scheduleapplyconfigchannels(self, args):


    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')

    (args, options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_system_scheduleapplyconfigchannels()
        return 1

    # get the start time option
    # skip the prompt if we are running with --yes
    # use "now" if no start time was given
    if is_interactive(options) and self.options.yes is not True:
        options.start_time = prompt_user(_('Start Time [now]:'))
        options.start_time = parse_time_input(options.start_time)
    else:
        if not options.start_time:
            options.start_time = parse_time_input('now')
        else:
            options.start_time = parse_time_input(options.start_time)

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args)

    if not systems:
        logging.warning(_N('No systems selected'))
        return 1

    print('')
    print(_('Start Time: %s') % options.start_time)
    print('')
    print(_('Systems'))
    print('-------')
    print('\n'.join(sorted(systems)))

    message = _('Schedule applying config channels to these systems [y/N]:')
    if not self.user_confirm(message):
        return 1

    system_ids = [self.get_system_id(s) for s in systems]

    actionId = self.client.system.config.scheduleApplyConfigChannel(self.session,
                                                                    system_ids,
                                                                    options.start_time, False)
    print(_('Scheduled action id: %s') % actionId)

    return 0

####################


def help_system_listmigrationtargets(self):
    print(_('system_listmigrationtargets: List possible migration targets for given systems.'))
    print(_('usage: system_listmigrationtargets <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)

def do_system_listmigrationtargets(self, args):
    arg_parser = get_argument_parser()

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) != 1:
        self.help_system_listmigrationtargets()
        return

    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args[0])

    if not systems:
        print(_('No systems found'))
        return

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            print(_('WARN: Cannot find system ') + str(system))
            continue

        print(_('System ') + str(system))
        tgts = self.client.system.listMigrationTargets(self.session, system_id)

        if not tgts:
            print(_('  No migration targets'))

        for num, tgt in enumerate(tgts, start=1):
            print(_('  Target #') + str(num) + ":")
            print(_('    IDs: ') + tgt['ident'])
            print(_('    Friendly names: ') + tgt['friendly'])

    return

####################


def help_system_schedulespmigration(self):
    print(_('This method is deprecated and will be removed in a future API version. '
            'Please use system_scheduleproductmigration instead.'))
    logging.warning(_("This method is deprecated and will be removed in a future API version"))
    print(_('system_schedulespmigration: Schedule a Service Pack migration for systems.'))
    print(_('usage: system_schedulespmigration <SYSTEM> <BASE_CHANNEL_LABEL> <MIGRATION_TARGET> [options] \
\n    For MIGRATION_TARGET parameter see system_listmigrationtargets. \
\n    The MIGRATION_TARGET parameter must be passed in the following format: [3143,3146,3147,3145,3144,3148,3062]. \
\n    Options: \
\n        -s START_TIME \
\n        -d pass this flag, if you want to do a dry run \
\n        -c CHILD_CHANNELS (comma-separated child channels labels (with no spaces))'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def do_system_schedulespmigration(self, args):
    print(_('This method is deprecated and will be removed in a future API version. '
            'Please use system_scheduleproductmigration instead.'))
    logging.warning(_("This method is deprecated and will be removed in a future API version"))
    self.do_system_scheduleproductmigration(self, args)

####################


def help_system_scheduleproductmigration(self):
    print(_('system_scheduleproductmigration: Schedule a Product migration for systems.'))
    print(_('usage: system_scheduleproductmigration <SYSTEM> <BASE_CHANNEL_LABEL> <MIGRATION_TARGET> [options] \
\n    For MIGRATION_TARGET parameter see system_listmigrationtargets. \
\n    The MIGRATION_TARGET parameter must be passed in the following format: [3143,3146,3147,3145,3144,3148,3062]. \
\n    Options: \
\n        -s START_TIME \
\n        -d pass this flag, if you want to do a dry run \
\n        --allow-vendor-change pass this flag if you want to allow vendor change \
\n        -r pass this flag if you want to remove remove products which have no successors \
\n        -c CHILD_CHANNELS (comma-separated child channels labels (with no spaces))'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def do_system_scheduleproductmigration(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-s', '--start-time')
    arg_parser.add_argument('-d', '--dry-run', action='store_true', default=False)
    arg_parser.add_argument('-c', '--child-channels')
    arg_parser.add_argument('--allow-vendor-change', action='store_true', default=False)
    arg_parser.add_argument('-r', '--remove-products-without-successor', action='store_true', default=False)

    (args, options) = parse_command_arguments(args, arg_parser)

    if len(args) < 3:
        self.help_system_scheduleproductmigration()
        return

    # POSITIONAL ARGS
    # use the systems listed in the SSM
    if re.match('ssm', args[0], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args[0])

    if not systems:
        print(_('No systems found'))
        return

    base_channel_label = args[1]
    migration_target = args[2]

    # OPTIONAL NAMED ARGS
    if options.start_time:
        options.start_time = parse_time_input(options.start_time)
    else:
        options.start_time = parse_time_input('now')

    child_channels = []
    if options.child_channels:
        child_channels = [cnl.strip() for cnl in options.child_channels.split(',')]

    for system in sorted(systems):
        system_id = self.get_system_id(system)
        if not system_id:
            logging.warning(_N('Cannot find system ') + str(system) + _('. Skipping it.'))
            continue

        print(_('Scheduling Product migration for system ') + str(system))
        print(_('Migration target ') + str(migration_target))

        try:
            result = self.client.system.scheduleProductMigration(self.session,
                                                            system_id, migration_target, base_channel_label,
                                                            child_channels, options.dry_run, options.allow_vendor_change,
                                                            options.remove_products_without_successor, options.start_time)
            print(_('Scheduled action ID: ') + str(result))
        except xmlrpclib.Fault as detail:
            logging.error(_N('Failed to schedule %s') % detail)

    return

####################


def help_system_bootstrap(self):
    print(_("system_bootstrap: Bootstrap a system for management via either Salt or Salt SSH."))
    print(_('''usage: bootstrap [options]

    options:
        -H HOSTNAME
        -p SSH_PORT
        -u SSH_USERNAME
        -P SSH_PASSWORD
        -k SSH_PRIVATEKEY_PATH
        -S SSH_PRIVATEKEY_PASSWORD
        -a ACTIVATION_KEY
        -r REACTIVATION_KEY
        --proxyid PROXY_SYSID
        --saltssh
        '''))
    print('')


def do_system_bootstrap(self, args):

    arg_parser = get_argument_parser()
    arg_parser.add_argument('-H', '--hostname')
    arg_parser.add_argument('-p', '--port', default=22)
    arg_parser.add_argument('-u', '--ssh-user', default="root")
    arg_parser.add_argument('-P', '--ssh-password')
    arg_parser.add_argument('-k', '--ssh-privatekey-file')
    arg_parser.add_argument('-S', '--ssh-privatekey-password', default="")
    arg_parser.add_argument('-a', '--activation-key', default="")
    arg_parser.add_argument('-r', '--reactivation-key')
    arg_parser.add_argument('--proxyid')
    arg_parser.add_argument('--saltssh', action='store_true', default=False)

    (args, options) = parse_command_arguments(args, arg_parser)

    if not (options.hostname or options.ssh_password or options.ssh_privatekey_file):
        options.hostname = prompt_user(_('Hostname:'))
        options.port = prompt_user(_('Port [22]:'))
        if not options.port:
            options.port = 22
        options.ssh_user = prompt_user(_('SSH User [root]:'))
        if not options.ssh_user:
            options.ssh_user = "root"
        options.ssh_password = getpass(_('SSH Password:'))
        if not options.ssh_password:
            options.ssh_privatekey_file = prompt_user(_('SSH Private Key File:'))
            options.ssh_privatekey_password = getpass(_('SSH Private Key Password:'))
            if not options.ssh_privatekey_password:
                options.ssh_privatekey_password = ""
        options.activation_key = prompt_user(_('Activation Key:'))
        if not options.activation_key:
            options.activation_key = ""
        options.reactivation_key = prompt_user(_('Reactivation Key:'))
        options.proxyid = prompt_user(_('Proxy System ID:'))
        options.saltssh = False
        answer = prompt_user(_('Manage with Salt SSH') + ' [y/N]:')
        if answer in ['y', 'Y']:
            options.saltssh = True

    if isinstance(options.port, str) and options.port.isnumeric():
        options.port = int(options.port)

    if not options.hostname:
        logging.error(_N("Hostname must be provided"))
        return 1
    if not (options.ssh_password or options.ssh_privatekey_file):
        logging.error(_N("Either a SSH Password or a private key must be provided"))
        return 1

    args = []
    if self.check_api_version('25.0'):
        if options.reactivation_key:
            args.append(options.reactivation_key)
    elif options.reactivation_key:
        logging.error(_N("Server is not supporting reactivation keys at bootstrap time"))
        return 1

    if options.proxyid:
        args.append(int(options.proxyid))
    args.append(options.saltssh)

    print(_("Bootstrapping '{}'. This may take a while.".format(options.hostname)))
    if options.ssh_password:
        self.client.system.bootstrap(self.session, options.hostname, options.port,
                options.ssh_user, options.ssh_password, options.activation_key,
                *args)
    else:
        with open(options.ssh_privatekey_file, "r") as key:
            pkey = key.read()

            # use ssh key
            self.client.system.bootstrapWithPrivateSshKey(self.session,
                    options.hostname, options.port, options.ssh_user,
                    pkey, options.ssh_privatekey_password, options.activation_key,
                    *args)
    print(_("Initial phase successfully finished. Check event history for actions still running"))
    return 0

####################

