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

def help_package_details(self):
    self.help_package_search('package_details',
                             _('Show the details of a software package'))


def complete_package_details(self, text, line, beg, end):
    return tab_completer(self.get_package_names(True), text)


def do_package_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args.replace('\\', '\\\\'), arg_parser)

    if not args:
        self.help_package_details()
        return None

    packages = self.do_package_search(' '.join(args), True)

    if not packages:
        logging.warning(_N('No packages found'))
        return 1

    add_separator = False

    for package in packages:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        package_ids = self.get_package_id(package)

        if not package_ids:
            logging.warning(_N('%s is not a valid package') % package)
            continue

        for package_id in package_ids:
            details = self.client.packages.getDetails(self.session, package_id)

            channels = \
                self.client.packages.listProvidingChannels(self.session, package_id)

            installed_systems = \
                self.client.system.listSystemsWithPackage(self.session, package_id)

            print(_('Name:      %s' % details.get('name')))
            print(_('Version:   %s' % details.get('version')))
            print(_('Release:   %s' % details.get('release')))
            print(_('Epoch:     %s' % details.get('epoch')))
            print(_('Arch:      %s' % details.get('arch_label')))
            print('')
            print(_('File:      %s' % details.get('file')))
            print(_('Path:      %s' % details.get('path')))
            print(_('Size:      %s' % details.get('size')))
            print(_('Retracted: %s' % (_('Yes') if details.get('part_of_retracted_patch')
                                       else _('No'))))
            print('%s%s' % ((details.get('checksum_type').upper() + ":").ljust(11),
                            details.get('checksum')))
            print('')
            print(_('Installed Systems: %i') % len(installed_systems))
            print('')
            print(_('Description'))
            print('-----------')
            print('\n'.join(wrap(details.get('description'))))
            print('')
            print(_('Available From Channels'))
            print('-----------------------')
            print('\n'.join(sorted([c.get('label') for c in channels])))
            print('')

    return 0

####################


def help_package_search(self,
                        command='package_search',
                        description='Find packages that meet the given criteria'):
    print('%s: %s' % (command, description))
    print(_('usage: %s NAME|QUERY' % command))
    print('')
    print(_('Example: %s kernel' % command))
    print('')
    print(_('Advanced Search:'))
    print(_('Available Fields: name, epoch, version, release, arch, description, summary'))
    print(_('Example: name:kernel AND version:2.6.18 AND -description:devel'))


def do_package_search(self, args, doreturn=False):
    if not args:
        self.help_package_search()
        return None

    _args, _options = parse_command_arguments(args, get_argument_parser())

    fields = ('name:', 'epoch:', 'version:', 'release:',
              'arch:', 'description:', 'summary:')
    # Check fields
    for arg in _args:
        if ":" in arg:
            field = arg.split(":")[0] + ":"
            if field not in fields:
                self.help_package_search()
                return None

    packages = []
    advanced = False

    for f in fields:
        if args.find(f) != -1:
            logging.debug('Using advanced search')
            advanced = True
            break

    if advanced:
        packages = self.client.packages.search.advanced(self.session, args)
        packages = build_package_names(packages)
    else:
        # for non-advanced searches, use local regex instead of
        # the APIs for searching; this is done because the fuzzy
        # search on the server gives a lot of garbage back
        packages = filter_results(self.get_package_names(True),
                                  _args, search=True)

    if doreturn:
        return packages
    if packages:
        print('\n'.join(sorted(packages)))

    return None

####################


def help_package_remove(self):
    print(_('package_remove: Remove a package from Spacewalk'))
    print(_('usage: package_remove PACKAGE ...'))


def complete_package_remove(self, text, line, beg, end):
    return tab_completer(self.get_package_names(True), text)


def do_package_remove(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args.replace('\\', '\\\\'), arg_parser)

    if not args:
        self.help_package_remove()
        return 1

    to_remove = filter_results(self.get_package_names(True), args)

    if not to_remove:
        logging.debug("Failed to find packages for criteria %s", str(args))
        print(_("No packages found to remove"))
        return 1

    if not self.user_confirm(_('Remove these packages [y/N]:')):
        print(_("No packages has been removed"))
        return 1

    print(_('Packages'))
    print('--------')
    print('\n'.join(sorted(to_remove)))

    for package in to_remove:
        for package_id in self.get_package_id(package):
            try:
                self.client.packages.removePackage(self.session, package_id)
            except xmlrpclib.Fault:
                logging.error(_N('Failed to remove package ID %i') % package_id)

    # regenerate the package cache after removing these packages
    self.generate_package_cache(True)

    return 0

####################


def help_package_listorphans(self):
    print(_('package_listorphans: List packages that are not in a channel'))
    print(_('usage: package_listorphans'))


def do_package_listorphans(self, args, doreturn=False):
    packages = self.client.channel.software.listPackagesWithoutChannel(
        self.session)

    packages = build_package_names(packages)

    if doreturn:
        return packages
    if packages:
        print('\n'.join(sorted(packages)))

    return None

####################


def help_package_removeorphans(self):
    print(_('package_removeorphans: Remove packages that are not in a channel'))
    print(_('usage: package_removeorphans'))


def do_package_removeorphans(self, args):
    packages = \
        self.client.channel.software.listPackagesWithoutChannel(self.session)

    if not packages:
        print(_("No orphaned packages has been found"))
        logging.warning(_N('No orphaned packages'))
        return 1

    if not self.user_confirm(_('Remove these packages [y/N]:')):
        print(_("No packages were removed"))
        return 1

    print(_('Packages'))
    print('--------')
    print('\n'.join(sorted(build_package_names(packages))))

    for package in packages:
        try:
            self.client.packages.removePackage(self.session, package.get('id'))
        except xmlrpclib.Fault:
            logging.error(_N('Failed to remove package ID %i') % package.get('id'))
            return 1

    return 0

####################


def help_package_listinstalledsystems(self):
    self.help_package_search('package_listinstalledsystems',
                             _('List the systems with a package installed'))


def complete_package_listinstalledsystems(self, text, line, beg, end):
    return tab_completer(self.get_package_names(True), text)


def do_package_listinstalledsystems(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args.replace('\\', '\\\\'), arg_parser)

    if not args:
        self.help_package_listinstalledsystems()
        return 1

    packages = self.do_package_search(' '.join(args), True)

    if not packages:
        logging.warning(_N('No packages found'))
        return 1

    add_separator = False

    for package in packages:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        systems = []
        for package_id in self.get_package_id(package):
            systems += self.client.system.listSystemsWithPackage(self.session,
                                                                 package_id)

        print(package)
        print('-' * len(package))

        if systems:
            print('\n'.join(sorted(['%s : %s' % (s.get('name'), s.get('id')) for s in systems])))

    return 0

####################


def help_package_listerrata(self):
    self.help_package_search('package_listerrata',
                             _('List the errata that provide this package'))


def complete_package_listerrata(self, text, line, beg, end):
    return tab_completer(self.get_package_names(True), text)


def do_package_listerrata(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args.replace('\\', '\\\\'), arg_parser)

    if not args:
        self.help_package_listerrata()
        return 1

    packages = self.do_package_search(' '.join(args), True)

    if not packages:
        logging.warning(_N('No packages found'))
        return 1

    add_separator = False

    for package in packages:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        for package_id in self.get_package_id(package):
            errata = self.client.packages.listProvidingErrata(self.session,
                                                              package_id)

            print(package)
            print('-' * len(package))

            if errata:
                print('\n'.join(sorted([e.get('advisory') for e in errata])))

    return 0

####################


def help_package_listdependencies(self):
    self.help_package_search('package_listdependencies',
                             _('List the dependencies for a package'))


def do_package_listdependencies(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args.replace('\\', '\\\\'), arg_parser)

    if not args:
        self.help_package_listdependencies()
        return 1

    packages = self.do_package_search(' '.join(args), True)

    if not packages:
        logging.warning(_N('No packages found'))
        return 1

    add_separator = False
    for package in packages:
        if add_separator:
            print(self.SEPARATOR)

        add_separator = False
        for package_id in self.get_package_id(package):
            if not package_id:
                logging.warning(_N('%s is not a valid package') % package)
                continue

            package_id = int(package_id)
            pkgdeps = self.client.packages.list_dependencies(self.session, package_id)
            print(_('Package Name: %s') % package)
            for dep in pkgdeps:
                print(_('Dependency: %s Type: %s Modifier: %s') % \
                      (dep['dependency'], dep['dependency_type'], dep['dependency_modifier']))
            print(self.SEPARATOR)
            add_separator = True

    return 0
