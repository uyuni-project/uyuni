# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.

import argparse
from spacewalk.susemanager.mgr_sync.version import VERSION


def _create_parser():
    # create the top-level parser
    parser = argparse.ArgumentParser(
        prog='mgr-sync',
        description="Synchronize SUSE Manager repositories.")

    # Generic options
    parser.add_argument(
        '--version',
        action='version',
        version=VERSION,
        help='Print mgr-sync version')

    parser.add_argument('-v', '--verbose', default=False,
                        action='store_true', help='Be verbose')

    parser.add_argument("-s", "--store-credentials", action="store_true",
                        dest="store_credentials",
                        default=False,
                        help="Store credentials to the local dot file.")

    parser.add_argument("-d", "--debug", default=1,
                        action="store", dest="debug",
                        choices=["1", "2", "3"],
                        help="Log additional debug information depending on DEBUG")

    subparsers = parser.add_subparsers(title='Subcommands')

    _create_list_subparser(subparsers)
    _create_add_subparser(subparsers)
    _create_refresh_subparser(subparsers)
    _create_delete_subparser(subparsers)

    return parser


def _create_add_subparser(subparsers):
    """ Create the parser for the "add" command. """

    add_parser = subparsers.add_parser('add',
                                       help='add channels, SCC organization credentials or products')
    add_parser.add_argument(
        'add_target',
        choices=['channel', 'channels', 'credentials', 'product', 'products'])
    add_parser.add_argument(
        'target',
        nargs='*',
        help='element to add, could be either a channel, SCC organization credentials or a product')
    add_parser.add_argument(
        '--from-mirror', action='store', dest='mirror', default="",
        help='URL of a local mirror like SMT. Only to download the RPMs.')
    add_parser.add_argument(
        '--primary',
        action='store_true',
        dest='primary',
        help='Designate SCC organization credentials as primary')
    add_parser.add_argument(
        '--no-optional',
        action='store_true',
        dest='no_optionals',
        default=False,
        help='do not list optional channels in interactive mode')
    add_parser.add_argument(
        '--no-recommends',
        action='store_true',
        dest='no_recommends',
        default=False,
        help='do not enable recommended products automatically')


def _create_list_subparser(subparsers):
    """ Create the parser for the "list" command. """

    list_parser = subparsers.add_parser('list',
                                        help='List channels, SCC organization credentials or products')
    list_parser.add_argument(
        'list_target',
        choices=['channel', 'channels', 'credentials', 'product', 'products'])
    list_parser.add_argument(
        '-e', '--expand',
        action='store_true',
        default=False,
        dest="expand",
        help='show also children, if the parent is not installed yet')
    list_parser.add_argument(
        '-f', '--filter',
        action='store',
        dest="filter",
        help="show only labels, which contains the filter word "
             "(case-insensitive)")
    list_parser.add_argument(
        '--no-optional',
        action='store_true',
        dest='no_optionals',
        default=False,
        help='do not list optional channels')
    list_parser.add_argument(
        '-c', '--compact',
        action='store_true',
        default=False,
        dest="compact",
        help='Compact output')


def _create_delete_subparser(subparsers):
    """ Create the parser for the "delete" command. """

    delete_parser = subparsers.add_parser(
        'delete',
        help='Delete SCC organization credentials')

    delete_parser.add_argument(
        'delete_target',
        choices=['credentials'])
    delete_parser.add_argument(
        'target',
        nargs='*',
        help='SCC organization credentials to delete')


def _create_refresh_subparser(subparsers):
    """ Create the parser for the "refresh" command. """

    refresh_parser = subparsers.add_parser(
        'refresh',
        help='Refresh product, channel and subscription')
    refresh_parser.set_defaults(refresh=True)

    refresh_parser.add_argument(
        '--refresh-channels',
        action='store_true',
        dest='refresh_channels',
        default=False,
        help='Schedule a refresh of all the installed channels.')
    refresh_parser.add_argument(
        '--from-mirror', action='store', dest='mirror', default="",
        help='URL of a local mirror like SMT. Only to download the RPMs.')
    refresh_parser.add_argument(
        '--schedule', action='store_true', dest='schedule',
        default=False, help='Schedule a refresh asynchronously (always enabled in case of ISS).')


def get_options(args=None):
    """ Parsers the command line options and returns them. """

    return _create_parser().parse_args(args)
