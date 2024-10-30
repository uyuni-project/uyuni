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

from datetime import datetime
import base64
import codecs
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

def help_configchannel_list(self):
    print(_('configchannel_list: List all configuration channels'))
    print(_('usage: configchannel_list'))


def do_configchannel_list(self, args, doreturn=False):
    channels = self.client.configchannel.listGlobals(self.session)
    channels = sorted([c.get('label') for c in channels])

    if doreturn:
        return channels
    else:
        if channels:
            print('\n'.join(channels))

    return None

####################


def help_configchannel_listsystems(self):
    print(_('configchannel_listsystems: List the systems subscribed to a'))
    print(_('                           configuration channel'))
    print(_('usage: configchannel_listsystems CHANNEL'))


def complete_configchannel_listsystems(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_listsystems(self, args):
    if not self.check_api_version('10.11'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()
    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_configchannel_listsystems()
        return 1

    channel = args[0]
    systems = self.client.configchannel.listSubscribedSystems(self.session, channel)
    systems = sorted([s.get('name') for s in systems])

    if systems:
        print('\n'.join(systems))
        return 0
    else:
        return 1

####################


def help_configchannel_listgroups(self):
    print(_('configchannel_listgroups: List the groups subscribed to a'))
    print(_('                           configuration channel'))
    print(_('usage: configchannel_listgroups CHANNEL'))


def complete_configchannel_listgroups(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_listgroups(self, args):
    if not self.check_api_version('25.0'):
        logging.warning(_N("This version of the API doesn't support this method"))
        return 1

    arg_parser = get_argument_parser()
    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_configchannel_listgroups()
        return 1

    channel = args[0]
    groups = self.client.configchannel.listAssignedSystemGroups(self.session, channel)
    groups = sorted([g.get('name') for g in groups])

    if groups:
        print('\n'.join(groups))
        return 0
    else:
        return 1

####################


def help_configchannel_listfiles(self):
    print(_('configchannel_listfiles: List the files in a config channel'))
    print(_('usage: configchannel_listfiles CHANNEL ...'))


def complete_configchannel_listfiles(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_listfiles(self, args, doreturn=False):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_configchannel_listfiles()
        return []

    for channel in args:
        files = sorted([f.get('path') for f in self.client.configchannel.listFiles(self.session, channel)])

        if doreturn:
            return files
        else:
            if files:
                print('\n'.join(files))

    return None

####################


def help_configchannel_forcedeploy(self):
    print(_('configchannel_forcedeploy: Forces a redeployment'))
    print(_('                           of files within this channel'))
    print(_('                           on all subscribed systems'))
    print(_('usage: configchannel_forcedeploy CHANNEL'))


def complete_configchannel_forcedeploy(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_forcedeploy(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args or len(args) > 1:
        self.help_configchannel_forcedeploy()
        return 1

    channel = args[0]

    files = self.client.configchannel.listFiles(self.session, channel)
    files = [f['path'] for f in files if f.get("path")]

    if not files:
        print(_('No files within selected configchannel.'))
        return 1
    else:
        systems = self.client.configchannel.listSubscribedSystems(self.session, channel)
        systems = sorted([s.get('name') for s in systems])
        if not systems:
            print(_('Channel has no subscribed Systems'))
            return 1
        else:
            print(_('Force deployment of the following configfiles:'))
            print('==============================================')
            print('\n'.join(files))
            print(_('\nOn these systems:'))
            print('=================')
            print('\n'.join(systems))
    if self.user_confirm(_('Really force deployment [y/N]:')):
        self.client.configchannel.deployAllSystems(self.session, channel)

    return 0

####################


def help_configchannel_filedetails(self):
    print(_('configchannel_filedetails: Show the details of a file'))
    print(_('in a configuration channel'))
    print(_('usage: configchannel_filedetails CHANNEL FILE [REVISION]'))


def complete_configchannel_filedetails(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_configchannel_list('', True),
                             text)
    if len(parts) > 2:
        return tab_completer(
            self.do_configchannel_listfiles(parts[1], True), text)

    return []


def do_configchannel_filedetails(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not 4 > len(args) > 1:
        self.help_configchannel_filedetails()
        return 1

    args.append(None)
    channel, filename, revision = args[:3]

    if revision:
        try:
            revision = int(args[2])
        except (ValueError, IndexError):
            logging.error(_N("Invalid revision: %s"), args[2])
            return 1

    # the server return a null exception if an invalid file is passed
    valid_files = self.do_configchannel_listfiles(channel, True)
    if filename not in valid_files:
        logging.warning(_N('%s is not in this configuration channel') % filename)
        return 1

    if revision:
        details = self.client.configchannel.lookupFileInfo(self.session,
                                                           channel,
                                                           filename,
                                                           revision)
    else:
        results = self.client.configchannel.lookupFileInfo(self.session,
                                                           channel,
                                                           [filename])

        # grab the first item since we only do one file
        details = results[0]

    details = DictToDefault(details, default="N/A")
    result = []
    result.append(_('Path:     %s') % details.get('path'))
    result.append(_('Type:     %s') % details.get('type'))
    result.append(_('Revision: %s') % details.get('revision'))
    result.append(_('Created:  %s') % details.get('creation'))
    result.append(_('Modified: %s') % details.get('modified'))

    if details.get('type') == 'symlink':
        result.append('')
        result.append(_('Target Path:     %s') % details.get('target_path'))
    else:
        result.append('')
        result.append(_('Owner:           %s') % details.get('owner'))
        result.append(_('Group:           %s') % details.get('group'))
        result.append(_('Mode:            %s') % details.get('permissions_mode'))

    result.append(_('SELinux Context: %s') % details.get('selinux_ctx'))

    if details.get('type') == 'file':
        result.append(_('SHA256:          %s') % details.get('sha256'))
        result.append(_('Binary:          %s') % details.get('binary'))

        if not details.get('binary'):
            result.append('')
            result.append(_('Contents'))
            result.append('--------')
            result.append(details.get('contents'))

    for line in result:
        print(line)

####################


def help_configchannel_backup(self):
    print(_('configchannel_backup: backup a config channel'))
    print(_('''usage: configchannel_backup CHANNEL [OUTDIR])

OUTDIR defaults to $HOME/spacecmd-backup/configchannel/YYYY-MM-DD/CHANNEL
'''))


def complete_configchannel_backup(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_configchannel_list('', True), text)

    return None

def do_configchannel_backup(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 1:
        self.help_configchannel_backup()
        return 1

    channel = args[0]

    # use an output base from the user if it was passed
    if len(args) == 2:
        outputpath_base = datetime.now().strftime(os.path.expanduser(args[1]))
    else:
        outputpath_base = os.path.expanduser('~/spacecmd-backup/configchannel')

        # make the final output path be <base>/date/channel
        outputpath_base = os.path.join(outputpath_base,
                                       datetime.now().strftime("%Y-%m-%d"),
                                       channel)

    try:
        if not os.path.isdir(outputpath_base):
            os.makedirs(outputpath_base)
    except OSError as exc:
        logging.error(_N('Could not create output directory: %s'), str(exc))
        return 1

    # the server return a null exception if an invalid file is passed
    valid_files = self.do_configchannel_listfiles(channel, True)
    results = self.client.configchannel.lookupFileInfo(self.session,
                                                       channel,
                                                       valid_files)

    fh_path = os.path.join(outputpath_base, ".metainfo")
    try:
        fh = open(fh_path, 'w')
    except IOError as exc:
        logging.error(_N('Could not create "%s" file: %s'), fh_path, str(exc))
        return 1

    for details in results:
        dumpfile = outputpath_base + details.get('path')
        dumpdir = dumpfile
        print(_('Output Path:   %s') % dumpfile)
        fh.write('[%s]\n' % details.get('path'))
        fh.write('type = %s\n' % details.get('type'))
        fh.write('revision = %s\n' % details.get('revision'))
        fh.write('creation = %s\n' % details.get('creation'))
        fh.write('modified = %s\n' % details.get('modified'))

        if details.get('type') == 'symlink':
            fh.write('target_path = %s\n' % details.get('target_path'))
        else:
            fh.write('owner = %s\n' % details.get('owner'))
            fh.write('group = %s\n' % details.get('group'))
            fh.write('permissions_mode = %s\n' % details.get('permissions_mode'))

        fh.write('selinux_ctx = %s\n' % details.get('selinux_ctx'))

        if details.get("type") in ("file", "sls"):
            dumpdir = os.path.dirname(dumpfile)

        if not os.path.isdir(dumpdir):
            os.makedirs(dumpdir)

        if details.get("type") in ("file", "sls"):
            fh.write('sha256 = %s\n' % details.get('sha256'))
            fh.write('binary = %s\n' % details.get('binary'))
            of = open(dumpfile, 'w')
            of.write(details.get('contents') or '')
            of.close()

        fh.write('\n')

    fh.close()

    return 0

####################


def help_configchannel_details(self):
    print(_('configchannel_details: Show the details of a config channel'))
    print(_('usage: configchannel_details CHANNEL ...'))


def complete_configchannel_details(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_configchannel_details()
        return None

    add_separator = False

    result = []
    for channel in args:
        details = self.client.configchannel.getDetails(self.session,
                                                       channel)

        files = self.client.configchannel.listFiles(self.session,
                                                    channel)

        if add_separator:
            result.append(self.SEPARATOR)
        add_separator = True

        result.append(_('Label:       %s') % details.get('label'))
        result.append(_('Name:        %s') % details.get('name'))
        result.append(_('Description: %s') % details.get('description'))
        result.append(_('Type:        %s') % details.get('configChannelType', {}).get('label'))

        result.append('')
        result.append(_('Files'))
        result.append('-----')
        for f in files:
            result.append(f.get('path'))
    return result

####################


def help_configchannel_create(self):
    print(_('configchannel_create: Create a configuration channel of specific type'))
    print(_('''usage: configchannel_create [options])

options:
  -n NAME
  -l LABEL
  -d DESCRIPTION
  -t TYPE('normal,'state')'''))


def do_configchannel_create(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-n', '--name')
    arg_parser.add_argument('-l', '--label')
    arg_parser.add_argument('-d', '--description')
    arg_parser.add_argument('-t', '--type')

    (args, options) = parse_command_arguments(args, arg_parser)

    if is_interactive(options):
        options.name = prompt_user(_('Name:'), noblank=True)
        options.label = prompt_user(_('Label:'))
        options.description = prompt_user(_('Description:'))
        options.type = prompt_user(_('Type [normal, state]:'))

        if options.label == '':
            options.label = options.name
        if options.description == '':
            options.description = options.name
        if options.type not in ('normal', 'state'):
            logging.error(_N('Only [normal/state] values are acceptable for type'))
            return 1
    else:
        if not options.name:
            logging.error(_N('A name is required'))
            return 1
        if not options.label:
            options.label = options.name
        if not options.description:
            options.description = options.name
        if not options.type:
            options.type = 'normal'
        if options.type not in ('normal', 'state'):
            logging.error(_N('Only [normal/state] values are acceptable for --type parameter'))
            return 1

    self.client.configchannel.create(self.session,
                                     options.label,
                                     options.name,
                                     options.description,
                                     options.type)

    return 0

####################


def help_configchannel_delete(self):
    print(_('configchannel_delete: Delete a configuration channel'))
    print(_('usage: configchannel_delete CHANNEL ...'))


def complete_configchannel_delete(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_delete(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_configchannel_delete()
        return 1

    # allow globbing of configchannel names
    channels = filter_results(self.do_configchannel_list('', True), args)
    logging.debug("configchannel_delete called with args %s, channels=%s" % (args, channels))

    if not channels:
        logging.error(_N("No channels matched argument(s): %s") % ", ".join(args))
        return 1

    # Print the channels prior to the confirmation
    print('\n'.join(sorted(channels)))

    if self.user_confirm(_('Delete these channels [y/N]:')):
        self.client.configchannel.deleteChannels(self.session, channels)

    return 0

####################


def configfile_getinfo(self, args, options, file_info=None, interactive=False):
    # Common code which is used in both configchannel_addfile and
    # system_addconfigfile.  Takes args/options from each call and
    # returns the file_info dict needed to create the file in either
    # the configchannel or sytem sandbox/local-override respectively
    #
    # file_info is the existing info from lookupFileInfo or None if
    # no file for this path exists already

    # initialize here instead of multiple times below
    contents = ''

    if interactive:
        # use existing values if available
        if file_info:
            for info in file_info:
                if info.get('path') == options.path:
                    logging.debug('Found existing file in channel')

                    options.owner = info.get('owner')
                    options.group = info.get('group')
                    options.mode = info.get('permissions_mode')
                    options.target_path = info.get('target_path')
                    options.selinux_ctx = info.get('selinux_ctx')
                    contents = info.get('contents')

                    if info.get('type') == 'symlink':
                        options.symlink = True

        if not options.owner:
            options.owner = 'root'
        if not options.group:
            options.group = 'root'

        # if this is a new file, ask if it's a symlink
        if not options.symlink:
            userinput = prompt_user(_('Symlink [y/N]:'))
            options.symlink = re.match('y', userinput, re.I)

        if options.symlink:
            target_input = prompt_user(_('Target Path:'), noblank=True)
            selinux_input = prompt_user(_('SELinux Context [none]:'))

            if target_input:
                options.target_path = target_input

            if selinux_input:
                options.selinux_ctx = selinux_input
        else:
            userinput = prompt_user(_('Directory [y/N]:'))
            options.directory = re.match('y', userinput, re.I)

            if not options.mode:
                if options.directory:
                    options.mode = '0755'
                else:
                    options.mode = '0644'

            owner_input = prompt_user(_('Owner [%s]:') % options.owner)
            group_input = prompt_user(_('Group [%s]:') % options.group)
            mode_input = prompt_user(_('Mode [%s]:') % options.mode)
            selinux_input = \
                prompt_user(_('SELinux Context [%s]:') % options.selinux_ctx)
            revision_input = prompt_user(_('Revision [next]:'))

            if owner_input:
                options.owner = owner_input

            if group_input:
                options.group = group_input

            if mode_input:
                options.mode = mode_input

            if selinux_input:
                options.selinux_ctx = selinux_input

            if revision_input:
                try:
                    options.revision = int(revision_input)
                except ValueError:
                    logging.warning(_N('The revision must be an integer'))

            if not options.directory:
                if self.user_confirm(_('Read an existing file [y/N]:'),
                                     nospacer=True, ignore_yes=True):
                    options.file = prompt_user(_('File:'))

                    contents = read_file(options.file)

                    if options.binary is None and self.file_is_binary(options.file):
                        options.binary = True
                        logging.debug("Binary detected")
                    elif options.binary:
                        logging.debug("Binary selected")
                else:
                    if contents:
                        template = contents
                    else:
                        template = ''

                    (contents, _ignore) = editor(template=template, delete=True)
    else:
        if not options.path:
            logging.error(_N('The path is required'))
            return None

        if not options.symlink and not options.directory:
            if options.file:
                contents = read_file(options.file)

                if options.binary is None:
                    options.binary = self.file_is_binary(options.file)
                    if options.binary:
                        logging.debug("Binary detected")
                elif options.binary:
                    logging.debug("Binary selected")
            else:
                logging.error(_N('You must provide the file contents'))
                return None

        if options.symlink and not options.target_path:
            logging.error(_N('You must provide the target path for a symlink'))
            return None

    # selinux_ctx can't be None
    if not options.selinux_ctx:
        options.selinux_ctx = ''

    # directory can't be None
    if not options.directory:
        options.directory = False

    if options.symlink:
        file_info = {'target_path': options.target_path,
                     'selinux_ctx': options.selinux_ctx}

        print(_('Path:            %s') % options.path)
        print(_('Target Path:     %s') % file_info['target_path'])
        print(_('SELinux Context: %s') % file_info['selinux_ctx'])
    else:
        if not options.owner:
            options.owner = 'root'
        if not options.group:
            options.group = 'root'
        if not options.mode:
            if options.directory:
                options.mode = '0755'
            else:
                options.mode = '0644'

        logging.debug("base64 encoding contents")
        contents = base64.b64encode(contents.encode('utf8')).decode()

        file_info = {'contents': ''.join(contents),
                     'owner': options.owner,
                     'group': options.group,
                     'selinux_ctx': options.selinux_ctx,
                     'permissions': options.mode,
                     'contents_enc64': True,
                     'binary': options.binary}

        # Binary set or detected
        if options.binary:
            file_info['binary'] = True

        print(_('Path:            %s') % options.path)
        print(_('Directory:       %s') % options.directory)
        print(_('Owner:           %s') % file_info['owner'])
        print(_('Group:           %s') % file_info['group'])
        print(_('Mode:            %s') % file_info['permissions'])
        print(_('Binary:          %s') % file_info['binary'])
        print(_('SELinux Context: %s') % file_info['selinux_ctx'])

        # only add the revision field if the user supplied it
        if options.revision:
            file_info['revision'] = int(options.revision)
            print(_('Revision:        %i') % file_info['revision'])

        if not options.directory:
            print('')
            if options.binary:
                print(_('Contents not displayed (base64 encoded)'))
            else:
                print(_('Contents'))
                print('--------')
                if file_info['contents_enc64']:
                    print(base64.b64decode(file_info['contents']))
                else:
                    print(file_info['contents'])

    return file_info


def help_configchannel_addfile(self):
    print(_('configchannel_addfile/configchannel_updatefile: Create a configuration file'))
    print(_('''usage: configchannel_addfile/configchannel_updatefile -c CHANNEL - p PATH -f LOCAL_FILE_PATH [OPTIONS])

options:
  -c CHANNEL
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
  -y automatically proceed with file contents

  Note re binary/base64: Some text files, notably those containing trailing
  newlines, those containing ASCII escape characters (or other charaters not
  allowed in XML) need to be sent as binary (-b).  Some effort is made to auto-
  detect files which require this, but you may need to explicitly specify.
'''))


def complete_configchannel_addfile(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_addfile(self, args, update_path=''):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-c', '--channel')
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
    arg_parser.add_argument('-y', '--yes', action='store_true')

    (args, options) = parse_command_arguments(args, arg_parser)

    file_info = None

    interactive = is_interactive(options)
    if interactive:
        # the channel name can be passed in
        if args:
            options.channel = args[0]
        else:
            failures = 0
            config_channels = sorted(self.do_configchannel_list('', True))
            while failures < 3:
                print(_('Configuration Channels'))
                print('----------------------')
                print('\n'.join(config_channels))
                print('')

                options.channel = prompt_user(_('Select:'), noblank=True)

                # ensure the user enters a valid configuration channel
                if options.channel in config_channels:
                    failures = 0
                    break
                print('')
                logging.warning(_N('%s is not a valid channel'),
                                options.channel)
                print('')
                failures += 1
            if failures > 0:
                logging.error(_N("Unable to obtain a valid channel. Aborting."))
                return 1

        if update_path:
            options.path = update_path
        else:
            options.path = prompt_user(_('Path:'), noblank=True)

        # check if this file already exists
        try:
            file_info = \
                self.client.configchannel.lookupFileInfo(self.session,
                                                         options.channel,
                                                         [options.path])
        except xmlrpclib.Fault:
            logging.debug("No existing file information found for %s" %
                          options.path)
            file_info = None

    file_info = self.configfile_getinfo(args, options, file_info, interactive)

    if not options.channel:
        logging.error(_N("No config channel specified!"))
        self.help_configchannel_addfile()
        return 1

    if not file_info:
        logging.error(_N("Error obtaining file info"))
        self.help_configchannel_addfile()
        return 1

    if options.yes or self.user_confirm():
        if options.symlink:
            self.client.configchannel.createOrUpdateSymlink(self.session,
                                                            options.channel,
                                                            options.path,
                                                            file_info)
        else:
            # TODO: This should be removed
            # compatibility for Satellite 5.3
            if not self.check_api_version('10.11'):
                if "selinux_ctx" in file_info:
                    del file_info['selinux_ctx']

                if 'revision' in file_info:
                    del file_info['revision']

            if options.directory:
                if 'contents' in file_info:
                    del file_info['contents']
                if 'contents_enc64' in file_info:
                    del file_info['contents_enc64']
                if 'binary' in file_info:
                    del file_info['binary']

            try:
                self.client.configchannel.createOrUpdatePath(self.session,
                                                             options.channel,
                                                             options.path,
                                                             options.directory,
                                                             file_info)
            except xmlrpclib.Fault as exc:
                logging.error(exc)
                return 1

    return 0

####################

def help_configchannel_updateinitsls(self):
    print(_('configchannel_updateinitsls: Update init.sls file'))
    print(_('''usage: configchannel_updateinitsls -c CHANNEL -f LOCAL_FILE_PATH [OPTIONS])

options:
  -c CHANNEL
  -r REVISION
  -f local path to file contents
  -y automatically proceed with file contents
'''))

def do_configchannel_updateinitsls(self, args, update_path=''):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-c', '--channel')
    arg_parser.add_argument('-f', '--file')
    arg_parser.add_argument('-r', '--revision')
    arg_parser.add_argument('-y', '--yes', action='store_true')
    (args, options) = parse_command_arguments(args, arg_parser)

    file_info = None
    path = "/init.sls"
    interactive = is_interactive(options)
    if interactive:
        # the channel name can be passed in
        if args:
            options.channel = args[0]
        else:
            while True:
                print(_('Configuration Channels'))
                print('----------------------')
                print('\n'.join(sorted(self.do_configchannel_list('', True))))
                print('')

                options.channel = prompt_user('Select:', noblank=True)

                # ensure the user enters a valid configuration channel
                if options.channel in self.do_configchannel_list('', True):
                    break
                print('')
                logging.warning(_N('%s is not a valid channel'),
                                options.channel)
                print('')
        # check if this file already exists
        try:
            file_info = \
               self.client.configchannel.lookupFileInfo(self.session,
                                                        options.channel,
                                                        [path])
        except xmlrpclib.Fault:
            logging.error(_N("No existing file information found for %s") %
                          options.path)
            return 1
        contents = file_info[0].get('contents')
        if self.user_confirm(_('Read an existing file [y/N]:'),
                             nospacer=True, ignore_yes=True):
            options.file = prompt_user('File:')
            contents = read_file(options.file)
            if self.file_is_binary(options.file):
                logging.debug("Binary selected")
        else:
            if contents:
                template = contents
            else:
                template = ''
            (contents, _ignore) = editor(template=template, delete=True)

        revision_input = prompt_user(_('Revision [next]:'))
        if revision_input:
            try:
                options.revision = int(revision_input)
            except ValueError:
                logging.warning(_('The revision must be an integer'))

    else:
        if options.file:
            contents = read_file(options.file)
        else:
            logging.error(_N('You must provide the file contents'))
            return 1

    contents = base64.b64encode(contents.encode('utf8')).decode()

    file_info = {'contents': ''.join(contents),
                 'contents_enc64': True
                }
    if options.revision:
        file_info['revision'] = options.revision


    if not options.channel:
        logging.error(_N("No config channel specified!"))
        self.help_configchannel_updateinitsls()
        return 1

    if not file_info:
        logging.error(_N("Error obtaining file info"))
        self.help_configchannel_updateinitsls()
        return 1
    if 'revision' in file_info:
        print(_('revision:                 %s') % file_info['revision'])
    print(_('contents_enc64:           %s') % file_info['contents_enc64'])
    print(_('Contents'))
    print('--------')
    print(base64.b64decode(file_info['contents']))
    if options.yes or self.user_confirm():
        self.client.configchannel.updateInitSls(self.session, options.channel, file_info)

    return 0

####################


def help_configchannel_updatefile(self):
    self.help_configchannel_addfile()


def complete_configchannel_updatefile(self, text, line, beg, end):
    return self.complete_configchannel_addfile(text, line, beg, end)


def do_configchannel_updatefile(self, args):
    return self.do_configchannel_addfile(args)

####################


def help_configchannel_removefiles(self):
    print(_('configchannel_removefiles: Remove configuration files'))
    print(_('usage: configchannel_removefiles CHANNEL <FILE ...>'))


def complete_configchannel_removefiles(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_configchannel_list('', True),
                             text)
    elif len(parts) > 2:
        channel = parts[1]
        return tab_completer(self.do_configchannel_listfiles(channel,
                                                             True),
                             text)

    return None


def do_configchannel_removefiles(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 2:
        self.help_configchannel_removefiles()
        return 1

    channel = args.pop(0)
    files = args

    if self.options.yes or self.user_confirm('Remove these files [y/N]:'):
        self.client.configchannel.deleteFiles(self.session, channel, files)

    return 0

####################


def help_configchannel_verifyfile(self):
    print(_('configchannel_verifyfile: Verify a configuration file'))
    print(_('usage: configchannel_verifyfile CHANNEL FILE <SYSTEMS>'))
    print('')
    print(self.HELP_SYSTEM_OPTS)


def complete_configchannel_verifyfile(self, text, line, beg, end):
    parts = line.split(' ')

    if len(parts) == 2:
        return tab_completer(self.do_configchannel_list('', True), text)
    elif len(parts) == 3:
        channel = parts[1]
        return tab_completer(self.do_configchannel_listfiles(channel, True),
                             text)
    elif len(parts) > 3:
        return self.tab_complete_systems(text)

    return None

def do_configchannel_verifyfile(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if len(args) < 3:
        self.help_configchannel_verifyfile()
        return 1

    channel = args[0]
    path = args[1]

    # use the systems listed in the SSM
    if re.match('ssm', args[2], re.I):
        systems = self.ssm.keys()
    else:
        systems = self.expand_systems(args[2:])

    system_ids = list(filter(None, (self.get_system_id(s) for s in systems)))

    if not system_ids:
        logging.error(_N('No valid system selected'))
        return 1
    action_id = \
        self.client.configchannel.scheduleFileComparisons(self.session,
                                                          channel,
                                                          path,
                                                          system_ids)

    logging.info(_N('Action ID: %i') % action_id)

    return 0

####################


def help_configchannel_export(self):
    print(_('configchannel_export: export config channel(s) to json format file'))
    print(_('''usage: configchannel_export <CHANNEL>... [options])
options:
    -f outfile.json : specify an output filename, defaults to <CHANNEL>.json
                      if exporting a single channel, ccs.json for multiple
                      channels, or cc_all.json if no CHANNEL specified
                      e.g (export ALL)

Note : CHANNEL list is optional, default is to export ALL'''))


def complete_configchannel_export(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def export_configchannel_getdetails(self, channel):
    # Get the cc details
    logging.info(_N("Getting config channel details for %s") % channel)
    details = self.client.configchannel.getDetails(self.session, channel)
    files = self.client.configchannel.listFiles(self.session, channel)
    details['files'] = []
    paths = [f['path'] for f in files]
    fileinfo = []
    # Some versions of the API blow up when lookupFileInfo is asked to
    # return details of files containing non-XML-valid characters.
    # later API versions simply return empty file contents, but to
    # ensure the least-bad operation with older (sat 5.3) API versions
    # we can iterate over each file, then we just error on individual files
    # instead of failing to export anything at all...
    for p in paths:
        logging.debug("Found file %s for %s" % (p, channel))
        try:
            pinfo = self.client.configchannel.lookupFileInfo(self.session,
                                                             channel, [p])
            if pinfo:
                fileinfo.append(pinfo[0])
        except xmlrpclib.Fault:
            logging.error(_N("Failed to get details for file %s from %s")
                          % (p, channel))
    # Now we strip the datetime fields from the Info structs, as they
    # are not JSON serializable with the default encoder, and we don't
    # need them on import anyway
    # We also strip some other fields which are not useful on import
    # This is a bit complicated because the createOrUpdateFoo functions
    # take two different struct formats, which are both different to
    # the format returned by lookupFileInfo, doh!
    # We get:                         We need:
    #                                 (file/dir)      (symlink)
    # string "type"                   Y               Y
    # string "path"                   Y               Y
    # string "target_path"            N               Y
    # string "channel"                N               N
    # string "contents"               Y               N
    # int "revision"                  N (auto)        N (auto)
    # dateTime.iso8601 "creation"     N               N
    # dateTime.iso8601 "modified"     N               N
    # string "owner"                  Y               N
    # string "group"                  Y               N
    # int "permissions"               Y (as string!)  N
    # string "permissions_mode"       N               N
    # string "selinux_ctx"            Y               Y
    # boolean "binary"                Y               N
    # string "sha256"                 N               N
    # string "macro-start-delimiter"  Y               N
    # string "macro-end-delimiter"    Y               N
    for f in fileinfo:

        if f['type'] == 'symlink':
            for k in ['contents', 'owner', 'group', 'permissions',
                      'macro-start-delimiter', 'macro-end-delimiter']:
                if k in f:
                    del f[k]
        else:
            if 'target_path' in f:
                del f['target_path']
            f['permissions'] = str(f['permissions'])

            # If we're using a recent API version files exported with no contents
            # i.e binary or non-xml encodable ascii files can be exported as
            # base64 encoded
            if not 'contents' in f:
                if f['type'] != 'directory':
                    if not self.check_api_version('11.1'):
                        logging.warning(_N("File %s could not be exported ") % f['path'] +
                                        _N("with this API version(needs base64 encoding)"))
                    else:
                        logging.info(_N("File %s could not be exported as") % f['path'] +
                                     _N(" text...getting base64 encoded version"))
                        b64f = self.client.configchannel.getEncodedFileRevision(
                            self.session, channel, f['path'], f['revision'])
                        f['contents'] = b64f['contents']
                        f['contents_enc64'] = b64f['contents_enc64']

        for k in ['channel', 'revision', 'creation', 'modified',
                  'permissions_mode', 'sha256']:
            if k in f:
                del f[k]

    details['files'] = fileinfo
    return details


def do_configchannel_export(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-f', '--file')

    args, options = parse_command_arguments(args, arg_parser)

    filename = ""
    if options.file is not None:
        logging.debug("Passed filename '%s' to do_configchannel_export command.", options.file)
        filename = options.file

    # Get the list of ccs to export and sort out the filename if required
    ccs = []
    if not args:
        if not filename:
            filename = "cc_all.json"
        logging.info(_N("Exporting ALL config channels to %s"), filename)
        ccs = self.do_configchannel_list('', True)
    else:
        # allow globbing of configchannel names
        ccs = filter_results(self.do_configchannel_list('', True), args)
        logging.debug("configchannel_export called with args %s, ccs=%s", args, ccs)
        if not ccs:
            logging.error(_N("Error, no valid config channel passed, "
                             "check name is  correct with spacecmd configchannel_list"))
            return 1
        if not filename:
            # No filename arg, so we try to do something sensible:
            # If we are exporting exactly one cc, we default to ccname.json
            # otherwise, generic ccs.json name
            if len(ccs) == 1:
                filename = "%s.json" % ccs[0]
            else:
                filename = "ccs.json"

    # Dump as a list of dict
    ccdetails_list = []
    for c in ccs:
        logging.info(_N("Exporting cc %s to %s"), c, filename)
        ccdetails_list.append(self.export_configchannel_getdetails(c))

    logging.debug("About to dump %d ccs to %s", len(ccdetails_list), filename)
    # Check if filepath exists, if it is an existing file
    # we prompt the user for confirmation
    if os.path.isfile(filename):
        if not self.options.yes and not self.user_confirm(_("File '{}' exists, confirm overwrite file? (y/n)"
                                                           ).format(filename)):
            return 1
    if not json_dump_to_file(ccdetails_list, filename):
        logging.error(_N("Error saving exported config channels to file: %s"), filename)
        return 1

    return 0

####################


def help_configchannel_import(self):
    print(_('configchannel_import: import config channel(s) from json file'))
    print(_('''usage: configchannel_import <JSONFILES...>'''))


def do_configchannel_import(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        logging.error(_N("Error, no filename passed"))
        self.help_configchannel_import()
        return 1

    for filename in args:
        logging.debug("Passed filename do_configchannel_import %s", filename)
        ccdetails_list = json_read_from_file(filename)
        if not ccdetails_list:
            logging.error(_N("Error, could not read json data from %s"), filename)
            return 1
        for ccdetails in ccdetails_list:
            if not self.import_configchannel_fromdetails(ccdetails):
                logging.error(_N("Error importing configchannel %s"), ccdetails['name'])

    return 0

# create a new cc based on the dict from export_configchannel_getdetails


def import_configchannel_fromdetails(self, ccdetails):

    # First we check that an existing channel with the same name does not exist
    existing_ccs = self.do_configchannel_list('', True)
    if ccdetails['name'] in existing_ccs:
        logging.warning(_N("Config channel %s already exists! Skipping!") %
                        ccdetails['name'])
        return False
    else:
        # create the cc, we need to drop the org prefix from the cc name
        logging.info(_N("Importing config channel  %s") % ccdetails['name'])

        channeltype = 'normal'
        if 'configChannelType' in ccdetails:
            channeltype = ccdetails['configChannelType']['label']

        # Create the channel
        self.client.configchannel.create(self.session,
                                         ccdetails['label'],
                                         ccdetails['name'],
                                         ccdetails['description'],
                                         channeltype)
        for filedetails in ccdetails['files']:
            path = filedetails['path']
            del filedetails['path']
            logging.info(_N("Found %s %s for cc %s") %
                         (filedetails['type'], path, ccdetails['name']))
            ret = None
            if filedetails['type'] == 'symlink':
                del filedetails['type']
                logging.debug("Adding symlink %s" % filedetails)
                ret = self.client.configchannel.createOrUpdateSymlink(
                    self.session, ccdetails['label'], path, filedetails)
            elif filedetails['type'] == 'sls':
                # Filter out everything except the file contents:
                init_sls_details = dict(filter(lambda e: e[0] in ['contents', 'contents_enc64'], filedetails.items()))
                ret = self.client.configchannel.updateInitSls(
                    self.session, ccdetails['label'], init_sls_details)
            else:
                if filedetails['type'] == 'directory':
                    isdir = True
                    if 'contents' in filedetails:
                        del filedetails['contents']
                else:
                    isdir = False
                    # If binary files (or those containing characters which are
                    # invalid in XML, e.g the ascii escape character) are
                    # exported, on older API versions, you end up with a file
                    # with no "contents" key (
                    # I guess the best thing to do here flag an error and
                    # import everything else
                    if not 'contents' in filedetails:
                        logging.error(
                            _("Failed trying to import file %s (empty content)")
                            % path)
                        logging.error(_N("Older APIs can't export encoded files"))
                        continue

                    if not filedetails['contents_enc64']:
                        logging.debug("base64 encoding file")
                        filedetails['contents'] = \
                            base64.b64encode(filedetails['contents'].encode('utf8'))

                        #change bytes to string before sending
                        filedetails['contents'] =  filedetails['contents'].decode('utf8')
                        filedetails['contents_enc64'] = True

                logging.debug("Creating %s %s" %
                              (filedetails['type'], filedetails))
                if 'type' in filedetails:
                    del filedetails['type']

                ret = self.client.configchannel.createOrUpdatePath(
                    self.session, ccdetails['label'], path, isdir, filedetails)
            if ret is not None:
                logging.debug("Added file %s to %s" %
                              (ret['path'], ccdetails['name']))
            else:
                logging.error(_N("Error adding file %s to %s") %
                              (filedetails['path'], ccdetails['label']))
                continue

    return True

####################


def help_configchannel_clone(self):
    print(_('configchannel_clone: Clone config channel(s)'))
    print(_('''usage examples:
                 configchannel_clone foo_label -c bar_label
                 configchannel_clone foo_label1 foo_label2 -c prefix
                 configchannel_clone foo_label -x "s/foo/bar"
                 configchannel_clone foo_label1 foo_label2 -x "s/foo/bar"

options:
  -c CLONE_LABEL : name/label of the resulting cc (note does not update
                   description, see -x option), treated as a prefix if
                   multiple keys are passed
  -x "s/foo/bar" : Optional regex replacement, replaces foo with bar in the
                   clone name, label and description
  Note : If no -c or -x option is specified, interactive is assumed'''))


def complete_configchannel_clone(self, text, line, beg, end):
    return tab_completer(self.do_configchannel_list('', True), text)


def do_configchannel_clone(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-c', '--clonelabel')
    arg_parser.add_argument('-x', '--regex')

    (args, options) = parse_command_arguments(args, arg_parser)
    allccs = sorted(filter(None, self.do_configchannel_list('', True)))

    if is_interactive(options):
        if not allccs:
            logging.error(_N("No config channels found"))
            return 1
        print('')
        print(_('Config Channels'))
        print('------------------')
        print('\n'.join(allccs))
        print('')

        if len(args) == 1:
            print(_("Channel to clone: %s") % args[0])
        else:
            # Clear out any args as interactive doesn't handle multiple ccs
            args = []
            args.append(prompt_user(_('Channel to clone:'), noblank=True))
        options.clonelabel = prompt_user(_('Clone label:'), noblank=True)
    else:
        if not options.clonelabel and not options.regex:
            logging.error(_N("Error - must specify either -c or -x options!"))
            self.help_configchannel_clone()
        else:
            logging.debug("%s : %s", options.clonelabel, options.regex)

    if not args:
        logging.error(_N("Error no channel label passed!"))
        self.help_configchannel_clone()
        return 1
    logging.debug("Got args=%s %d", args, len(args))
    # allow globbing of configchannel names
    ccs = filter_results(self.do_configchannel_list('', True), args)
    logging.debug("Filtered ccs %s", ccs)

    if not ccs:
        logging.error(_N("No suitable channels to clone has been found."))

    for cc in ccs:
        logging.debug("Cloning %s" % cc)
        ccdetails = self.export_configchannel_getdetails(cc)

        # If the -x/--regex option is passed, do a sed-style replacement over
        # the name, label and description.  This makes it easier to clone when
        # content is based on a known naming convention
        if options.regex:
            # Expect option to be formatted like a sed-replacement, s/foo/bar
            findstr = options.regex.split("/")[1]
            replacestr = options.regex.split("/")[2]
            logging.debug("--regex selected with %s, replacing %s with %s", options.regex, findstr, replacestr)
            newname = re.sub(findstr, replacestr, ccdetails['name'])
            ccdetails['name'] = newname
            newlabel = re.sub(findstr, replacestr, ccdetails['label'])
            ccdetails['label'] = newlabel
            newdesc = re.sub(findstr, replacestr, ccdetails['description'])
            ccdetails['description'] = newdesc
            logging.debug("regex mode : %s %s %s", ccdetails['name'], ccdetails['label'], ccdetails['description'])
        elif options.clonelabel:
            if len(ccs) > 1:
                newlabel = options.clonelabel + ccdetails['label']
                ccdetails['label'] = newlabel
                newname = options.clonelabel + ccdetails['name']
                ccdetails['name'] = newname
                logging.debug("clonelabel mode with >1 channel : %s", ccdetails['label'])
            else:
                newlabel = options.clonelabel
                ccdetails['label'] = newlabel
                newname = options.clonelabel
                ccdetails['name'] = newname
                logging.debug("clonelabel mode with 1 channel : %s", ccdetails['label'])

        # Finally : import the cc from the modified ccdetails
        if not self.import_configchannel_fromdetails(ccdetails):
            logging.error(_N("Failed to clone %s to %s"), cc, ccdetails['label'])

    return 0

####################
# configchannel helper


def is_configchannel(self, name):
    if not name:
        return None
    return name in self.do_configchannel_list(name, True)


def check_configchannel(self, name):
    if not name:
        logging.error(_N("no configchannel given"))
        return False
    if not self.is_configchannel(name):
        logging.error(_N("invalid configchannel label ") + name)
        return False
    return True


def dump_configchannel_filedetails(self, name, filename):
    content = self.do_configchannel_filedetails(name + " " + filename)
    return content


def dump_configchannel(self, name, replacedict=None, excludes=None):
    if not excludes:
        excludes = ["Revision:", "Created:", "Modified:"]
    content = self.do_configchannel_details(name)

    for filename in self.do_configchannel_listfiles(name, True):
        content.extend(self.dump_configchannel_filedetails(name, filename))

    content = get_normalized_text(content, replacedict=replacedict, excludes=excludes)

    return content

####################


def help_configchannel_diff(self):
    print(_('configchannel_diff: diff between config channels'))
    print('')
    print(_('usage: configchannel_diff SOURCE_CHANNEL TARGET_CHANNEL'))


def complete_configchannel_diff(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')
    args = len(parts)

    if args == 2:
        return tab_completer(self.do_configchannel_list('', True), text)
    if args == 3:
        return tab_completer(self.do_configchannel_list('', True), text)
    return []


def do_configchannel_diff(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not 1 <= len(args) < 3:
        self.help_configchannel_diff()
        return 1

    source_channel = args[0]
    if not self.check_configchannel(source_channel):
        return 1

    target_channel = None
    if len(args) == 2:
        target_channel = args[1]
    elif hasattr(self, "do_configchannel_getcorresponding"):
        # can a corresponding channel name be found automatically?
        target_channel = self.do_configchannel_getcorresponding(source_channel)
    if not self.check_configchannel(target_channel):
        return 1

    source_replacedict, target_replacedict = get_string_diff_dicts(source_channel, target_channel)

    source_data = self.dump_configchannel(source_channel, source_replacedict)
    target_data = self.dump_configchannel(target_channel, target_replacedict)

    for line in diff(source_data, target_data, source_channel, target_channel):
        print(line)

####################


def help_configchannel_sync(self):
    print(_('configchannel_sync:'))
    print(_('sync config files between two config channels'))
    print('')
    print(_('usage: configchannel_sync SOURCE_CHANNEL TARGET_CHANNEL'))


def complete_configchannel_sync(self, text, line, beg, end):
    parts = shlex.split(line)
    if line[-1] == ' ':
        parts.append('')
    args = len(parts)

    if args == 2:
        return tab_completer(self.do_configchannel_list('', True), text)
    if args == 3:
        return tab_completer(self.do_configchannel_list('', True), text)
    return []


def do_configchannel_sync(self, args, doreturn=False):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not 1 <= len(args) < 3:
        self.help_configchannel_sync()
        return 1

    source_channel = args[0]
    if not self.check_configchannel(source_channel):
        return 1

    target_channel = None
    if len(args) == 2:
        target_channel = args[1]
    elif hasattr(self, "do_configchannel_getcorresponding"):
        # can a corresponding channel name be found automatically?
        target_channel = self.do_configchannel_getcorresponding(source_channel)
    if not self.check_configchannel(target_channel):
        return 1

    logging.info(_N("syncing files from configchannel ") + source_channel + " to " + target_channel)

    source_files = set(self.do_configchannel_listfiles(source_channel, doreturn=True))
    target_files = set(self.do_configchannel_listfiles(target_channel, doreturn=True))

    both = source_files & target_files
    if both:
        print(_("files common in both channels:"))
        print("\n".join(both))
        print('')

    source_only = source_files.difference(target_files)
    if source_only:
        print(_("files only in source ") + source_channel)
        print("\n".join(source_only))
        print('')

    target_only = target_files.difference(source_files)
    if target_only:
        print(_("files only in target ") + target_channel)
        print("\n".join(target_only))
        print('')

    if both:
        print(_("files that are in both channels will be overwritten in the target channel"))
    if source_only:
        print(_("files only in the source channel will be added to the target channel"))
    if target_only:
        print(_("files only in the target channel will be deleted"))

    if not (both or source_only or target_only):
        logging.info(_N("nothing to do"))
        return 1

    if not self.options.yes:
        if not self.user_confirm(_('perform synchronisation [y/N]:')):
            return 1

    source_data_list = self.client.configchannel.lookupFileInfo(
        self.session, source_channel,
        list(both) + list(source_only))

    for source_data in source_data_list:
        if source_data.get('type') == 'file' or source_data.get('type') == 'directory':
            if source_data.get('contents') and not source_data.get('binary'):
                contents = codecs.encode(source_data.get('contents'), "base64")
            else:
                contents = source_data.get('contents')
            target_data = {
                'contents':                 contents,
                'contents_enc64':           True,
                'owner':                    source_data.get('owner'),
                'group':                    source_data.get('group'),
                # get permissions from permissions_mode instead of permissions
                'permissions':              source_data.get('permissions_mode'),
                'selinux_ctx':              source_data.get('selinux_ctx'),
                'macro-start-delimiter':    source_data.get('macro-start-delimiter'),
                'macro-end-delimiter':      source_data.get('macro-end-delimiter'),
            }
            _target_data = {}
            for k, v in target_data.items():
                if v:
                    _target_data[k] = v
            target_data = _target_data
            del _target_data

            if source_data.get('type') == 'directory':
                del target_data['contents_enc64']
            logging.debug(source_data.get('path') + ": " + str(target_data))
            self.client.configchannel.createOrUpdatePath(self.session,
                                                         target_channel,
                                                         source_data.get('path'),
                                                         source_data.get('type') == 'directory',
                                                         target_data)

        elif source_data.get('type') == 'symlink':
            target_data = {
                'target_path':  source_data.get('target_path'),
                'selinux_ctx':  source_data.get('selinux_ctx'),
            }
            logging.debug(source_data.get('path') + ": " + str(target_data))
            self.client.configchannel.createOrUpdateSymlink(self.session,
                                                            target_channel,
                                                            source_data.get('path'),
                                                            target_data)

        else:
            logging.warning(_N("unknown file type ") + source_data.type)

    # removing all files from target channel that did not exist on source channel
    if target_only:
        #self.do_configchannel_removefiles( target_channel + " " + "/.metainfo" + " ".join(target_only) )
        self.do_configchannel_removefiles(target_channel + " " + " ".join(target_only))

    return 0
