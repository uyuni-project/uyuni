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

def help_scap_listxccdfscans(self):
    print(_('scap_listxccdfscans: Return a list of finished OpenSCAP scans for given systems'))
    print(_('usage: scap_listxccdfscans <SYSTEMS>'))


def complete_system_scap_listxccdfscans(self, text, line, beg, end):
    return self.tab_complete_systems(text)


def do_scap_listxccdfscans(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_scap_listxccdfscans()
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

        scan_list = self.client.system.scap.listXccdfScans(self.session, system_id)

        for s in scan_list:
            print(_('XID: %d Profile: %s Path: (%s) Completed: %s')
                  % (s['xid'], s['profile'], s['path'], s['completed']))

    return 0

####################


def help_scap_getxccdfscanruleresults(self):
    print(_('scap_getxccdfscanruleresults: Return a full list of RuleResults for given OpenSCAP XCCDF scan'))
    print(_('usage: scap_getxccdfscanruleresults <XID>'))


def do_scap_getxccdfscanruleresults(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_scap_getxccdfscanruleresults()
        return 1

    add_separator = False

    for xid in args:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(args) > 1:
            print(_('XID: %s') % xid)
            print('')

        xid = int(xid)
        scan_results = self.client.system.scap.getXccdfScanRuleResults(self.session, xid)

        for s in scan_results:
            print(_('IDref: %s Result: %s Idents: (%s)') % (s['idref'], s['result'], s['idents']))

    return 0

####################


def help_scap_getxccdfscandetails(self):
    print(_('scap_getxccdfscandetails: Get details of given OpenSCAP XCCDF scan'))
    print(_('usage: scap_getxccdfscandetails <XID>'))


def do_scap_getxccdfscandetails(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_scap_getxccdfscandetails()
        return 1

    add_separator = False

    for xid in args:
        if add_separator:
            print(self.SEPARATOR)
        add_separator = True

        if len(args) > 1:
            print(_('XID: %s') % xid)
            print('')

        xid = int(xid)
        scan_details = self.client.system.scap.getXccdfScanDetails(self.session, xid)

        print(_("XID:"), scan_details['xid'], _("SID:"), scan_details['sid'], _("Action_ID:"),
              scan_details['action_id'], _("Path:"), scan_details['path'], \
              _("OSCAP_Parameters:"), scan_details['oscap_parameters'], \
              _("Test_Result:"), scan_details['test_result'], _("Benchmark:"), \
              scan_details['benchmark'], _("Benchmark_Version:"), \
              scan_details['benchmark_version'], _("Profile:"), scan_details['profile'], \
              _("Profile_Title:"), scan_details['profile_title'], _("Start_Time:"), \
              scan_details['start_time'], _("End_Time:"), scan_details['end_time'], \
              _("Errors:"), scan_details['errors'])

    return 0

####################


def help_scap_schedulexccdfscan(self):
    print(_('scap_schedulexccdfscan: Schedule Scap XCCDF scan'))
    print(_('usage: scap_schedulexccdfscan PATH_TO_XCCDF_FILE XCCDF_OPTIONS SYSTEMS'))
    print(_('       scap_schedulexccdfscan ssm PATH_TO_XCCDF_FILE XCCDF_OPTIONS'))
    print('')
    print(_('Example:'))
    print('> scap_schedulexccdfscan \'/usr/share/openscap/scap-security-xccdf.xml\'' +
          ' \'profile Web-Default\' system-scap.example.com')
    print(_('\nTo use systems in the ssm, pass the "ssm" keyword in front. Example:'))
    print("> scap_schedulexccdfscan ssm '/usr/share/openscap/scap-security-xccdf.xml'" +
          " 'profile Web-Default'")


def do_scap_schedulexccdfscan(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 3:
        self.help_scap_schedulexccdfscan()
        return 1

    path = args[0]
    param = "--"
    param += args[1]

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

        self.client.system.scap.scheduleXccdfScan(self.session, system_id, path, param)

    return 0
