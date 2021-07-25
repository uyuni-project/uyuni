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
# Copyright 2011 Aron Parsons <aronparsons@gmail.com>
#

# NOTE: the 'self' variable is an instance of SpacewalkShell

# wildcard import
# pylint: disable=W0401,W0614

# unused argument
# pylint: disable=W0613

# invalid function name
# pylint: disable=C0103

import gettext
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

def help_repo_list(self):
    print(_('repo_list: List all available user repos'))
    print(_('usage: repo_list'))


def do_repo_list(self, args, doreturn=False):
    repos = self.client.channel.software.listUserRepos(self.session)
    repos = [c.get('label') for c in repos]

    if doreturn:
        return repos
    if repos:
        print('\n'.join(sorted(repos)))

    return None

####################


def help_repo_details(self):
    print(_('repo_details: Show the details of a user repo'))
    print(_('usage: repo_details <repo ...>'))


def complete_repo_details(self, text, line, beg, end):
    return tab_completer(self.do_repo_list('', True), text)


def do_repo_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_repo_details()
        return 1

    # allow globbing of repo names
    repos = filter_results(self.do_repo_list('', True), args)
    if repos:
        add_separator = False
        for repo in repos:
            details = self.client.channel.software.getRepoDetails(
                self.session, repo)

            if add_separator:
                print(self.SEPARATOR)
            add_separator = True

            print(_('Repository Label:                  %s') % details.get('label'))
            print(_('Repository URL:                    %s') % details.get('sourceUrl'))
            print(_('Repository Type:                   %s') % details.get('type'))
            print(_('Repository SSL Ca Certificate:     %s') % (details.get('sslCaDesc') or "None"))
            print(_('Repository SSL Client Certificate: %s') % (details.get('sslCertDesc') or "None"))
            print(_('Repository SSL Client Key:         %s') % (details.get('sslKeyDesc') or "None"))
    else:
        print(_("No repositories found for '{}' query").format(' '.join(args)))
        return 1

    return 0

####################


def help_repo_listfilters(self):
    print(_('repo_listfilters: Show the filters for a user repo'))
    print(_('usage: repo_listfilters repo'))


def complete_repo_listfilters(self, text, line, beg, end):
    return tab_completer(self.do_repo_list('', True), text)


def do_repo_listfilters(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_repo_listfilters()
        return 1

    filters = self.client.channel.software.listRepoFilters(self.session, args[0])
    if filters:
        for flt in filters:
            print("%s%s" % (flt.get('flag'), flt.get('filter')))
    else:
        print(_("No filters found"))
        return 1

    return 0

####################


def help_repo_addfilters(self):
    print(_('repo_addfilters: Add filters for a user repo'))
    print(_('usage: repo_addfilters repo <filter ...>'))


def complete_repo_addfilters(self, text, line, beg, end):
    if len(line.split(' ')) <= 2:
        return tab_completer(self.do_repo_list('', True),
                             text)

    return None


def do_repo_addfilters(self, args):
    # arguments can start with -, so don't parse arguments in the normal way
    args = shlex.split(args)

    if len(args) < 2:
        self.help_repo_addfilters()
        return 1

    repo = args[0]

    for arg in args[1:]:
        flag = arg[0]
        repofilter = arg[1:]

        if not flag in ('+', '-'):
            logging.error(_N('Each filter must start with + or -'))
            return 1

        self.client.channel.software.addRepoFilter(self.session,
                                                   repo,
                                                   {'filter': repofilter,
                                                    'flag': flag})

    return 0

####################


def help_repo_removefilters(self):
    print(_('repo_removefilters: Remove filters from a user repo'))
    print(_('usage: repo_removefilters repo <filter ...>'))


def complete_repo_removefilters(self, text, line, beg, end):
    return tab_completer(self.do_repo_remove('', True), text)


def do_repo_removefilters(self, args):
    # arguments can start with -, so don't parse arguments in the normal way
    args = shlex.split(args)

    if len(args) < 2:
        self.help_repo_removefilters()
        return 1

    repo = args[0]

    for arg in args[1:]:
        flag = arg[0]
        repofilter = arg[1:]

        if not flag in('+', '-'):
            logging.error(_N('Each filter must start with + or -'))
            return 1

        self.client.channel.software.removeRepoFilter(self.session,
                                                      repo,
                                                      {'filter': repofilter,
                                                       'flag': flag})

    return 0

####################


def help_repo_setfilters(self):
    print(_('repo_setfilters: Set the filters for a user repo'))
    print(_('usage: repo_setfilters repo <filter ...>'))


def complete_repo_setfilters(self, text, line, beg, end):
    return tab_completer(self.do_repo_set('', True), text)


def do_repo_setfilters(self, args):
    # arguments can start with -, so don't parse arguments in the normal way
    args = shlex.split(args)

    if len(args) < 2:
        self.help_repo_setfilters()
        return 1

    repo = args[0]

    filters = []

    for arg in args[1:]:
        flag = arg[0]
        repofilter = arg[1:]

        if not flag in ('+', '-'):
            logging.error(_N('Each filter must start with + or -'))
            return 1

        filters.append({'filter': repofilter, 'flag': flag})

    self.client.channel.software.setRepoFilters(self.session, repo, filters)

    return 0

####################


def help_repo_clearfilters(self):
    print(_('repo_clearfilters: Clears the filters for a user repo'))
    print(_('''usage: repo_clearfilters repo <options>

options:
  -y, --yes   Confirm without prompt

'''))


def complete_repo_clearfilters(self, text, line, beg, end):
    return tab_completer(self.do_repo_clear('', True), text)


def do_repo_clearfilters(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-y', '--yes', default=False, action="store_true")

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_repo_clearfilters()
        return 1

    if _options.yes or self.user_confirm(_('Remove these filters [y/N]:')):
        self.client.channel.software.clearRepoFilters(self.session, args[0])

    return 0

####################


def help_repo_delete(self):
    print(_('repo_delete: Delete a user repo'))
    print(_('usage: repo_delete <repo ...>'))


def complete_repo_delete(self, text, line, beg, end):
    return tab_completer(self.do_repo_list('', True), text)


def do_repo_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_repo_delete()
        return 1

    # allow globbing of repo names
    repos = filter_results(self.do_repo_list('', True), args)

    print(_('Repos'))
    print('-----')
    print('\n'.join(sorted(repos)))

    if self.user_confirm(_('Delete these repos [y/N]:')):
        for repo in repos:
            try:
                self.client.channel.software.removeRepo(self.session, repo)
            except xmlrpclib.Fault:
                logging.error(_N('Failed to remove repo %s') % repo)

    return 0

####################


def help_repo_create(self):
    print(_('repo_create: Create a user repository'))
    print(_('''usage: repo_create <options>)

options:
  -n, --name   name of repository
  -u, --url    url of repository
  -t, --type   type of repository (defaults to yum)

  --ca         SSL CA certificate (not required)
  --cert       SSL Client certificate (not required)
  --key        SSL Client key (not required)'''))


def do_repo_create(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-n', '--name')
    arg_parser.add_argument('-u', '--url')
    arg_parser.add_argument('-t', '--type')
    arg_parser.add_argument('--ca', default='')
    arg_parser.add_argument('--cert', default='')
    arg_parser.add_argument('--key', default='')

    (args, options) = parse_command_arguments(args, arg_parser)

    if is_interactive(options):
        options.name = prompt_user(_('Name:'), noblank=True)
        options.url = prompt_user(_('URL:'), noblank=True)
        options.type = prompt_user(_('Type:'), noblank=True)
        options.ca = prompt_user(_('SSL CA cert:'))
        options.cert = prompt_user(_('SSL Client cert:'))
        options.key = prompt_user(_('SSL Client key:'))
    else:
        if not options.name:
            logging.error(_N('A name is required'))
            return 1

        if not options.url:
            logging.error(_N('A URL is required'))
            return 1

        if not options.type:
            options.type = 'yum'

    self.client.channel.software.createRepo(self.session,
                                            options.name,
                                            options.type,
                                            options.url,
                                            options.ca,
                                            options.cert,
                                            options.key)

    return 0

####################


def help_repo_rename(self):
    print(_('repo_rename: Rename a user repository'))
    print(_('usage: repo_rename OLDNAME NEWNAME'))


def complete_repo_rename(self, text, line, beg, end):
    if len(line.split(' ')) <= 2:
        return tab_completer(self.do_repo_list('', True),
                             text)

    return None


def do_repo_rename(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_repo_rename()
        return 1

    try:
        details = self.client.channel.software.getRepoDetails(self.session, args[0])
        oldname = details.get('id')
    except xmlrpclib.Fault:
        logging.error(_N('Could not find repo %s') % args[0])
        return 1

    newname = args[1]

    self.client.channel.software.updateRepoLabel(self.session, oldname, newname)

    return 0

####################


def help_repo_updateurl(self):
    print(_('repo_updateurl: Change the URL of a user repository'))
    print(_('usage: repo_updateurl <repo> <url>'))


def complete_repo_updateurl(self, text, line, beg, end):
    if len(line.split(' ')) == 2:
        return tab_completer(self.do_repo_list('', True),
                             text)

    return None


def do_repo_updateurl(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) != 2:
        self.help_repo_updateurl()
        return 1

    name, url = args
    self.client.channel.software.updateRepoUrl(self.session, name, url)

    return 0


def help_repo_updatessl(self):
    print(_('repo_updatessl: Change the SSL certificates of a user repository'))
    print(_('''usage: repo_updatessl <options>)
options:
  --ca         SSL CA certificate (not required)
  --cert       SSL Client certificate (not required)
  --key        SSL Client key (not required)'''))


def do_repo_updatessl(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-n', '--name')
    arg_parser.add_argument('--ca', default='')
    arg_parser.add_argument('--cert', default='')
    arg_parser.add_argument('--key', default='')

    (args, options) = parse_command_arguments(args, arg_parser)

    if is_interactive(options):
        options.name = prompt_user(_('Name:'), noblank=True)
        options.ca = prompt_user(_('SSL CA cert:'))
        options.cert = prompt_user(_('SSL Client cert:'))
        options.key = prompt_user(_('SSL Client key:'))
    else:
        if not options.name:
            logging.error(_N('A name is required'))
            return 1

    self.client.channel.software.updateRepoSsl(self.session,
                                               options.name,
                                               options.ca,
                                               options.cert,
                                               options.key)

    return 0
