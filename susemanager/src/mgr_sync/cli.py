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
    parser = argparse.ArgumentParser(prog='mgr-sync',
                                     description="Synchronize SUSE Manager repositories.")

    # Generic options
    parser.add_argument('--version',
                        action='version',
                        version=VERSION,
                        help='Print mgr-sync version')

    parser.add_argument('-v', '--verbose', default=False,
                        action='store_true', help='Be verbose')

    parser.add_argument("-s", "--save-config", action="store_true",
                        dest="saveconfig",
                        default=False,
                        help="Save the configuration to the local dot file.")

    subparsers = parser.add_subparsers(title='Subcommands')

    _create_list_subparser(subparsers)
    _create_add_subparser(subparsers)
    _create_refresh_subparser(subparsers)

    return parser


def _create_add_subparser(subparsers):
    """ Create the parser for the "add" command. """

    add_parser = subparsers.add_parser('add',
                                       help='add channels or products')
    add_parser.add_argument('add_target', choices=['channel', 'product'])
    add_parser.add_argument('target',
                            nargs='*',
                            help='element to add, could be either a channel or a product')
    add_parser.add_argument('--from-mirror', action='store', dest='mirror',
                            help='URL of a local mirror like SMT. Only to download the RPMs.')


def _create_list_subparser(subparsers):
    """ Create the parser for the "list" command. """

    list_parser = subparsers.add_parser('list',
                                        help='List channels or products')
    list_parser.add_argument('list_target', choices=['channel', 'product'])
    list_parser.add_argument('-e', '--expand',
                             action='store_true',
                             default=False,
                             dest="expand",
                             help='show also children, if the parent is not synced yet')
    list_parser.add_argument('-f', '--filter',
                             action='store',
                             dest="filter",
                             help='show only labels, which contains the filter word (case-insensitive)')
    list_parser.add_argument('--no-optional',
                             action='store_true',
                             dest='no_optionals',
                             default=False,
                             help='do not list optional channels')


def _create_refresh_subparser(subparsers):
    """ Create the parser for the "refresh" command. """

    refresh_parser = subparsers.add_parser('refresh',
                                           help='Refresh product, channel and subscription')
    refresh_parser.set_defaults(refresh=True)

def get_options():
    """ Parsers the command line options and returns them. """
    return _create_parser().parse_args()
