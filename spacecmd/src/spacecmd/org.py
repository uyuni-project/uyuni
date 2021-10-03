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
import logging
from getpass import getpass
from operator import itemgetter
from spacecmd.i18n import _N
from spacecmd.utils import *

_PREFIXES = ['Dr.', 'Mr.', 'Miss', 'Mrs.', 'Ms.']

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

def _org_create_handler(self, args, first):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-n', '--org-name')
    arg_parser.add_argument('-u', '--username')
    if not first:
        arg_parser.add_argument('-P', '--prefix')
    arg_parser.add_argument('-f', '--first-name')
    arg_parser.add_argument('-l', '--last-name')
    arg_parser.add_argument('-e', '--email')
    arg_parser.add_argument('-p', '--password')
    if not first:
        arg_parser.add_argument('--pam', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    if is_interactive(options):
        options.org_name = prompt_user(_('Organization Name:'), noblank=True)
        options.username = prompt_user(_('Username:'), noblank=True)
        if not first:
            options.prefix = prompt_user(_('Prefix (%s):') % ', '.join(_PREFIXES),
                                        noblank=True)
        options.first_name = prompt_user(_('First Name:'), noblank=True)
        options.last_name = prompt_user(_('Last Name:'), noblank=True)
        options.email = prompt_user(_('Email:'), noblank=True)
        if not first:
            options.pam = self.user_confirm(_('PAM Authentication [y/N]:'),
                                        nospacer=True,
                                        integer=False,
                                        ignore_yes=True)

        options.password = ''
        while options.password == '':
            password1 = getpass(_('Password: '))
            password2 = getpass(_('Repeat Password: '))

            if password1 == password2:
                options.password = password1
            elif len(password1) < 5:
                logging.warning(_N('Password must be at least 5 characters'))
            else:
                logging.warning(_N("Passwords don't match"))
    else:
        if not options.org_name:
            logging.error(_N('An organization name is required'))
            return 1

        if not options.username:
            logging.error(_N('A username is required'))
            return 1

        if not options.first_name:
            logging.error(_N('A first name is required'))
            return 1

        if not options.last_name:
            logging.error(_N('A last name is required'))
            return 1

        if not options.email:
            logging.error(_N('An email address is required'))
            return 1

        if not options.password:
            logging.error(_N('A password is required'))
            return 1

        if not options.pam:
            options.pam = False

        if not options.prefix:
            options.prefix = _('Dr.')

    if not first:
        if options.prefix[-1] != '.' and options.prefix != _('Miss'):
            options.prefix = options.prefix + '.'

    return options


def help_org_createfirst(self):
    print(_('org_createfirst: Create first organization and user after server setup'))
    print(_('''usage: org_createfirst [options])

options:
  -n ORG_NAME
  -u USERNAME
  -f FIRST_NAME
  -l LAST_NAME
  -e EMAIL
  -p PASSWORD'''))


def do_org_createfirst(self, args):
    options = _org_create_handler(self, args, True)

    self.client.org.createFirst(options.org_name,
                           options.username,
                           options.password,
                           options.first_name,
                           options.last_name,
                           options.email)

    return 0


def help_org_create(self):
    print(_('org_create: Create an organization'))
    print(_('''usage: org_create [options])

options:
  -n ORG_NAME
  -u USERNAME
  -P PREFIX (%s)
  -f FIRST_NAME
  -l LAST_NAME
  -e EMAIL
  -p PASSWORD
  --pam enable PAM authentication''' % ', '.join(_PREFIXES)))


def do_org_create(self, args): # pylint: disable=too-many-return-statements
    options = _org_create_handler(self, args, False)

    self.client.org.create(self.session,
                           options.org_name,
                           options.username,
                           options.password,
                           options.prefix.capitalize(),
                           options.first_name,
                           options.last_name,
                           options.email,
                           options.pam)

    return 0

####################


def help_org_delete(self):
    print(_('org_delete: Delete an organization'))
    print(_('usage: org_delete NAME'))


def complete_org_delete(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 1:
        self.help_org_delete()
        return 1

    name = args[0]
    org_id = self.get_org_id(name)
    if not org_id:
        logging.warning(_N("No organisation found for the name %s"), name)
        print(_("Organisation '{}' was not found").format(name))
        return 1
    elif self.user_confirm(_('Delete this organization [y/N]:')):
        self.client.org.delete(self.session, org_id)
        return 0

    return None

####################


def help_org_rename(self):
    print(_('org_rename: Rename an organization'))
    print(_('usage: org_rename OLDNAME NEWNAME'))


def complete_org_rename(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_rename(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_org_rename()
        return 1

    name, new_name = args
    org_id = self.get_org_id(name)
    if not org_id:
        logging.warning(_N("No organisation found for the name %s"), name)
        print(_("Organisation '{}' was not found").format(name))
        return 1
    else:
        new_name = args[1]
        self.client.org.updateName(self.session, org_id, new_name)
        return 0

####################


def help_org_addtrust(self):
    print(_('org_addtrust: Add a trust between two organizations'))
    print(_('usage: org_addtrust YOUR_ORG ORG_TO_TRUST'))


def complete_org_addtrust(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_addtrust(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_org_addtrust()
        return 1

    your_org, trust_org = args
    your_org_id = self.get_org_id(your_org)
    org_to_trust_id = self.get_org_id(trust_org)

    if your_org_id is None:
        logging.warning(_N("No organisation found for the name %s"), your_org)
        print(_("Organisation '{}' was not found").format(your_org))
        return 1
    elif org_to_trust_id is None:
        logging.warning(_N("No trust organisation found for the name %s"), trust_org)
        print(_("Organisation '{}' to trust for, was not found").format(trust_org))
        return 1
    else:
        self.client.org.trusts.addTrust(self.session, your_org_id, org_to_trust_id)
        return 0

####################


def help_org_removetrust(self):
    print(_('org_removetrust: Remove a trust between two organizations'))
    print(_('usage: org_removetrust YOUR_ORG TRUSTED_ORG'))


def complete_org_removetrust(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_removetrust(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_org_removetrust()
        return 1

    your_org, trust_org = args
    your_org_id = self.get_org_id(your_org)
    trusted_org_id = self.get_org_id(trust_org)
    if your_org_id is None:
        logging.warning(_N("No organisation found for the name %s"), your_org)
        print(_("Organisation '{}' was not found").format(your_org))
        return 1
    elif trusted_org_id is None:
        logging.warning(_N("No trust organisation found for the name %s"), trust_org)
        print(_("Organisation '{}' to trust for, was not found").format(trust_org))
        return 1
    else:
        systems = self.client.org.trusts.listSystemsAffected(self.session, your_org_id, trusted_org_id)
        print(_('Affected Systems'))
        print('----------------')

        if systems:
            print('\n'.join(sorted([s.get('systemName') for s in systems])))
        else:
            print(_('None'))

        if self.user_confirm(_('Remove this trust [y/N]:')):
            self.client.org.trusts.removeTrust(self.session, your_org_id, trusted_org_id)
        return 0


def help_org_trustdetails(self):
    print(_('org_trustdetails: Show the details of an organizational trust'))
    print(_('usage: org_trustdetails TRUSTED_ORG'))


def complete_org_trustdetails(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_trustdetails(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_org_trustdetails()
        return 1

    trusted_org = args[0]
    org_id = self.get_org_id(trusted_org)
    if org_id is None:
        logging.warning(_N("No trusted organisation found for the name %s"), trusted_org)
        print(_("Trusted organisation '{}' was not found").format(trusted_org))
        return 1
    else:
        details = self.client.org.trusts.getDetails(self.session, org_id)
        consumed = self.client.org.trusts.listChannelsConsumed(self.session, org_id)
        provided = self.client.org.trusts.listChannelsProvided(self.session, org_id)

        print(_('Trusted Organization:   %s') % trusted_org)
        print(_('Trusted Since:          %s') % details.get('trusted_since'))
        print(_('Systems Transferred From:  %i') % details.get('systems_transferred_from'))
        print(_('Systems Transferred To:    %i') % details.get('systems_transferred_to'))
        print('')
        print(_('Channels Consumed'))
        print('-----------------')
        if consumed:
            print('\n'.join(sorted([c.get('name') for c in consumed])))

        print('')

        print(_('Channels Provided'))
        print('-----------------')
        if provided:
            print('\n'.join(sorted([c.get('name') for c in provided])))
        return 0


def help_org_list(self):
    print(_('org_list: List all organizations'))
    print(_('usage: org_list'))


def do_org_list(self, args, doreturn=False):
    orgs = self.client.org.listOrgs(self.session)
    orgs = [o.get('name') for o in orgs]

    if doreturn:
        return orgs
    if orgs:
        print('\n'.join(sorted(orgs)))

    return None

####################


def help_org_listtrusts(self):
    print(_("org_listtrusts: List an organization's trusts"))
    print(_('usage: org_listtrusts NAME'))


def complete_org_listtrusts(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_listtrusts(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_org_listtrusts()
        return 1

    org_id = self.get_org_id(args[0])
    if org_id is None:
        logging.warning(_N("No organisation found for the name %s"), args[0])
        print(_("Organisation '{}' was not found").format(args[0]))
        return 1
    else:
        trusts = self.client.org.trusts.listTrusts(self.session, org_id)
        if not trusts:
            print(_("No trust organisation has been found"))
            logging.warning(_N("No trust organisation has been found"))
            return 1
        else:
            for trust in sorted(trusts, key=itemgetter('orgName')):
                if trust.get('trustEnabled'):
                    print(trust.get('orgName'))
            return 0


def help_org_listusers(self):
    print(_("org_listusers: List an organization's users"))
    print(_('usage: org_listusers NAME'))


def complete_org_listusers(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_listusers(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_org_listusers()
        return 1

    org_id = self.get_org_id(args[0])
    if org_id is None:
        logging.warning(_N("No organisation found for the name %s"), args[0])
        print(_("Organisation '{}' was not found").format(args[0]))
        return 1
    else:
        users = self.client.org.listUsers(self.session, org_id)
        print('\n'.join(sorted([u.get('login') for u in users])))
        return 0

####################


def help_org_details(self):
    print(_('org_details: Show the details of an organization'))
    print(_('usage: org_details NAME'))


def complete_org_details(self, text, line, beg, end):
    return tab_completer(self.do_org_list('', True), text)


def do_org_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_org_details()
        return 1

    name = args[0]

    details = self.client.org.getDetails(self.session, name)

    print(_('Name:                   %s') % details.get('name'))
    print(_('Active Users:           %i') % details.get('active_users'))
    print(_('Systems:                %i') % details.get('systems'))

    # trusts is optional, which is annoying...
    if 'trusts' in details:
        print(_('Trusts:                 %i') % details.get('trusts'))
    else:
        print(_('Trusts:                 %i') % 0)

    print(_('System Groups:          %i') % details.get('system_groups'))
    print(_('Activation Keys:        %i') % details.get('activation_keys'))
    print(_('Kickstart Profiles:     %i') % details.get('kickstart_profiles'))
    print(_('Configuration Channels: %i') % details.get('configuration_channels'))

    return 0
