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

import base64
import gettext
from operator import itemgetter
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

def print_schedule_summary(self, action_type, args):
    args = args.split() or []

    if args:
        begin_date = parse_time_input(args[0])
        logging.debug('Begin Date: %s' % begin_date)
    else:
        begin_date = None

    if len(args) > 1:
        end_date = parse_time_input(args[1])
        logging.debug('End Date:   %s' % end_date)
    else:
        end_date = None

    if action_type == 'pending':
        actions = self.client.schedule.listInProgressActions(self.session)
    elif action_type == 'completed':
        actions = self.client.schedule.listCompletedActions(self.session)
    elif action_type == 'failed':
        actions = self.client.schedule.listFailedActions(self.session)
    elif action_type == 'archived':
        actions = self.client.schedule.listArchivedActions(self.session)
    elif action_type == 'all':
        # get actions in all states except archived
        in_progress = self.client.schedule.listInProgressActions(self.session)
        completed = self.client.schedule.listCompletedActions(self.session)
        failed = self.client.schedule.listFailedActions(self.session)

        actions = []
        added = []
        for action in in_progress + completed + failed:
            if action.get('id') not in added:
                actions.append(action)
                added.append(action.get('id'))
    else:
        return

    if not actions:
        return

    print(_('ID      Date                 C    F    P     Action'))
    print('--      ----                ---  ---  ---    ------')

    for action in sorted(actions, key=itemgetter('id'), reverse=True):
        if begin_date:
            if action.get('earliest') < begin_date:
                continue

        if end_date:
            if action.get('earliest') > end_date:
                continue

        if self.check_api_version('10.11'):
            print('%s  %s   %s  %s  %s    %s' %
                  (str(action.get('id')).ljust(6),
                   action.get('earliest'),
                   str(action.get('completedSystems')).rjust(3),
                   str(action.get('failedSystems')).rjust(3),
                   str(action.get('inProgressSystems')).rjust(3),
                   action.get('name')))
        else:
            # Satellite 5.3 compatibility
            in_progress = \
                self.client.schedule.listInProgressSystems(self.session,
                                                           action.get('id'))

            completed = \
                self.client.schedule.listCompletedSystems(self.session,
                                                          action.get('id'))

            failed = \
                self.client.schedule.listFailedSystems(self.session,
                                                       action.get('id'))

            print('%s  %s   %s  %s  %s    %s' %
                  (str(action.get('id')).ljust(6),
                   action.get('earliest'),
                   str(len(completed)).rjust(3),
                   str(len(failed)).rjust(3),
                   str(len(in_progress)).rjust(3),
                   action.get('name')))

####################


def help_schedule_cancel(self):
    print(_('schedule_cancel: Cancel scheduled actions'))
    print(_('usage: schedule_cancel ID|* ...'))


def complete_schedule_cancel(self, text, line, beg, end):
    try:
        actions = self.client.schedule.listInProgressActions(self.session)
        return tab_completer([str(a.get('id')) for a in actions], text)
    except xmlrpclib.Fault:
        return []


def do_schedule_cancel(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_schedule_cancel()
        return 1

    # cancel all actions
    if '.*' in args:
        if not self.user_confirm(_('Cancel all pending actions [y/N]:')):
            logging.info(_N("All pending actions left untouched"))
            return 1

        actions = self.client.schedule.listInProgressActions(self.session)
        strings = [a.get('id') for a in actions]
    else:
        strings = args

    # convert strings to integers
    actions = []
    failed_actions = []
    for a in strings:
        try:
            actions.append(int(a))
        except ValueError:
            logging.warning(_N('"%s" is not a valid ID') % str(a))
            failed_actions.append(a)

    if actions:
        self.client.schedule.cancelActions(self.session, actions)
        for a in actions:
            logging.info(_N('Canceled action: %i'), a)
    if failed_actions:
        for action in failed_actions:
            logging.info(_N("Failed action: %s"), action)

    print(_('Canceled %i action(s)') % len(actions))

    return 0

####################


def help_schedule_reschedule(self):
    print(_('schedule_reschedule: Reschedule failed actions'))
    print(_('usage: schedule_reschedule ID|* ...'))


def complete_schedule_reschedule(self, text, line, beg, end):
    try:
        actions = self.client.schedule.listFailedActions(self.session)
        return tab_completer([str(a.get('id')) for a in actions], text)
    except xmlrpclib.Fault:
        return []


def do_schedule_reschedule(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_schedule_reschedule()
        return 1

    failed_actions = self.client.schedule.listFailedActions(self.session)
    failed_actions = [a.get('id') for a in failed_actions]

    to_reschedule = []

    # reschedule all failed actions
    if '.*' in args:
        if not self.user_confirm(_('Reschedule all failed actions [y/N]:')):
            return 1
        to_reschedule = failed_actions
    else:
        # use the list of action IDs passed in
        for a in args:
            try:
                action_id = int(a)

                if action_id in failed_actions:
                    to_reschedule.append(action_id)
                else:
                    logging.warning(_N('"%i" is not a failed action') % action_id)
            except ValueError:
                logging.warning(_N('"%s" is not a valid ID') % str(a))
                continue

    if not to_reschedule:
        logging.warning(_N('No failed actions to reschedule'))
        return 1
    else:
        self.client.schedule.rescheduleActions(self.session, to_reschedule, True)
        print(_('Rescheduled %i action(s)') % len(to_reschedule))

    return 0

####################


def help_schedule_details(self):
    print(_('schedule_details: Show the details of a scheduled action'))
    print(_('usage: schedule_details ID'))


def do_schedule_details(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_schedule_details()
        return 1
    else:
        action_id = args[0]

    try:
        action_id = int(action_id)
    except ValueError:
        logging.warning(_N('The ID "%s" is invalid') % action_id)
        return 1

    completed = self.client.schedule.listCompletedSystems(self.session, action_id)
    failed = self.client.schedule.listFailedSystems(self.session, action_id)
    pending = self.client.schedule.listInProgressSystems(self.session, action_id)
    action = dict(map(lambda e: [e.get("id"), e], self.client.schedule.listAllActions(self.session))).get(action_id)

    if action is not None:
        print(_('ID:        %i') % action.get('id'))
        print(_('Action:    %s') % action.get('name'))
        print(_('User:      %s') % action.get('scheduler'))
        print(_('Date:      %s') % action.get('earliest'))
        print('')
        print(_('Completed: %s') % str(len(completed)).rjust(3))
        print(_('Failed:    %s') % str(len(failed)).rjust(3))
        print(_('Pending:   %s') % str(len(pending)).rjust(3))

        if completed:
            print('')
            print(_('Completed Systems'))
            print('-----------------')
            for s in completed:
                print(s.get('server_name'))

        if failed:
            print('')
            print(_('Failed Systems'))
            print('--------------')
            for s in failed:
                print(s.get('server_name'))

        if pending:
            print('')
            print(_('Pending Systems'))
            print('---------------')
            for s in pending:
                print(s.get('server_name'))
    else:
        logging.error(_N('No action found with the ID "%s"') % action_id)
        return 1

    return 0

####################


def help_schedule_getoutput(self):
    print(_('schedule_getoutput: Show the output from an action'))
    print(_('usage: schedule_getoutput ID'))


def do_schedule_getoutput(self, args):
    arg_parser = get_argument_parser()

    (args, _options) = parse_command_arguments(args, arg_parser)

    if not args:
        self.help_schedule_getoutput()
        return 1

    try:
        action_id = int(args[0])
    except ValueError:
        logging.error(_N('"%s" is not a valid action ID') % str(args[0]))
        return 1

    script_results = None
    try:
        script_results = self.client.system.getScriptResults(self.session, action_id)
    except xmlrpclib.Fault as exc:
        logging.debug("Exception occurrect while get script results: %s", str(exc))

    # scripts have a different data structure than other actions
    if script_results is not None:
        add_separator = False
        for r in script_results:
            if add_separator:
                print(self.SEPARATOR)
            add_separator = True

            if r.get('serverId'):
                system = self.get_system_name(r.get('serverId'))
            else:
                system = 'UNKNOWN'

            print(_('System:      %s') % system)
            print(_('Start Time:  %s') % r.get('startDate'))
            print(_('Stop Time:   %s') % r.get('stopDate'))
            print(_('Return Code: %i') % r.get('returnCode'))
            print('')
            print(_('Output'))
            print('------')
            if r.get('output_enc64'):
                print(base64.b64decode(r.get('output') or b'Ti9B\n').decode("utf-8"))
            else:
                print((r.get('output') or "N/A").encode('UTF8').decode("utf-8"))

    else:
        completed = self.client.schedule.listCompletedSystems(self.session, action_id)
        failed = self.client.schedule.listFailedSystems(self.session, action_id)

        add_separator = False

        if completed or failed:
            for action in completed + failed:
                if add_separator:
                    print(self.SEPARATOR)
                add_separator = True

                print(_('System:    %s') % action.get('server_name'))
                print(_('Completed: %s') % action.get('timestamp'))
                print('')
                print(_('Output'))
                print('------')
                print(action.get('message'))
        else:
            logging.error(_N("No systems found"))
            return 1

    return 0

####################


def help_schedule_listpending(self):
    print(_('schedule_listpending: List pending actions'))
    print(_('usage: schedule_listpending [BEGINDATE] [ENDDATE]'))
    print('')
    print(self.HELP_TIME_OPTS)


def do_schedule_listpending(self, args):
    return self.print_schedule_summary('pending', args)

####################


def help_schedule_listcompleted(self):
    print(_('schedule_listcompleted: List completed actions'))
    print(_('usage: schedule_listcompleted [BEGINDATE] [ENDDATE]'))
    print('')
    print(self.HELP_TIME_OPTS)


def do_schedule_listcompleted(self, args):
    return self.print_schedule_summary('completed', args)

####################


def help_schedule_listfailed(self):
    print(_('schedule_listfailed: List failed actions'))
    print(_('usage: schedule_listfailed [BEGINDATE] [ENDDATE]'))
    print('')
    print(self.HELP_TIME_OPTS)


def do_schedule_listfailed(self, args):
    return self.print_schedule_summary('failed', args)

####################


def help_schedule_listarchived(self):
    print(_('schedule_listarchived: List archived actions'))
    print(_('usage: schedule_listarchived [BEGINDATE] [ENDDATE]'))
    print('')
    print(self.HELP_TIME_OPTS)


def do_schedule_listarchived(self, args):
    return self.print_schedule_summary('archived', args)

####################


def help_schedule_list(self):
    print(_('schedule_list: List all actions'))
    print(_('usage: schedule_list [BEGINDATE] [ENDDATE]'))
    print('')
    print(self.HELP_TIME_OPTS)


def do_schedule_list(self, args):
    return self.print_schedule_summary('all', args)

####################

def help_schedule_deletearchived(self):
    print(_('schedule_deletearchived: Delete all archived actions older than given date.'))
    print(_('usage: schedule_deletearchived [yyyymmdd]'))
    print('')
    print(_('If no date is provided it will delete all archived actions'))

def do_schedule_deletearchived(self, args):
    """
    This method removes all of the archived actions older than provided date.
    If no date is provided it will delete all archived actions.
    """
    user_already_prompted = False
    args = args.split() or []

    if args:
        limit_date = parse_time_input(args[0])
        logging.debug('Limit Date: %s' % limit_date)
    else:
        limit_date = None

    while True:
        # Needs to happen in a loop, since we can only fetch in batches. 10k is the default.
        actions = self.client.schedule.listArchivedActions(self.session)

        # Filter out actions by date if limit is set
        if limit_date:
            actions = [action for action in actions if action.get('earliest') < limit_date]
            logging.debug("actions: {}".format(actions))

        if actions:
            if not user_already_prompted:
                if len(actions) >= 10000:
                    action_length = ">10000"
                else:
                    action_length = len(actions)
                user_answer = prompt_user(_("Do you want to delete all ({}) archived actions? [y/N]").format(action_length))
                if user_answer not in ("y", "Y", "yes", "Yes", "YES"):
                    break
                else:
                    user_already_prompted = True

            # Collect IDs of actions that should be deleted
            action_ids = [action.get('id') for action in actions]

            # Remove duplicates if any
            # set needs to be cast to list, since set cannot be marshalled
            action_ids = list(set(action_ids))

            if action_ids:
                # Process deletion in batches
                BATCH_SIZE = 50
                for i in range(0, len(action_ids), BATCH_SIZE):
                    # Pass list of actions that should be deleted
                    self.client.schedule.deleteActions(self.session, action_ids[i:i + BATCH_SIZE])
                    processed = i + BATCH_SIZE if i + BATCH_SIZE <= len(action_ids) else len(action_ids)
                    print("Deleted {} actions of {}".format(processed, len(action_ids)))
        else:
            if not user_already_prompted:
                print(_("No archived actions found."))
            break
