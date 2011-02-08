#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2009, 2010 Novell, Inc.
#   This library is free software; you can redistribute it and/or modify
# it only under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
#   This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
#   You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

import sys
from optparse import OptionParser
from spacewalk.susemanager import mgr_ncc_sync_lib

def main():
    parser = OptionParser(version="%prog 0.1",
                          description="Sync SUSE Manager repositories from NCC")

    parser.add_option("-l", "--list-channels", action="store_true", dest="list",
                      help="list all the channels which are available for you")
    parser.add_option("-c", "--channel", action="store",
                      help="add a new channel and trigger a reposync")
    parser.add_option("-p", "--products", action="store_true",
                      help="fetch all known products from NCC")
    parser.add_option("-f", "--update_cf", action="store_true",
                      help="update channel family by XML config")
    parser.add_option("-s", "--update_subscriptions", action="store_true",
                      help="update subscriptions by NCC data")
    parser.add_option('-q', '--quiet', action='store_true', dest='quiet', 
                      default=False, help="Print no output, still logs output")
    parser.add_option('-d', '--debug', dest='debug',
                      default=-1, help="debugging")
    parser.add_option("-t", "--test", action="store_true",
                      help="Testmode")


    (options, args) = parser.parse_args()

    syncer = mgr_ncc_sync_lib.NCCSync(quiet=options.quiet, debug=options.debug)
    if options.list:
        syncer.list_channels()
    elif options.channel:
        syncer.add_channel(options.channel)
    elif options.products:
        suse_products = syncer.get_suse_products_from_ncc()
        syncer.update_suse_products_table(suse_products)
    elif options.update_cf:
        syncer.update_channel_family_table_by_config()
    elif options.test:
        syncer.test_subscription_calculation()
    elif options.update_subscriptions:
        all_subs = syncer.get_subscriptions_from_ncc()
        cons_subs = syncer.consolidate_subscriptions(all_subs)
        syncer.reset_entitlements_in_table()
        for s in cons_subs.keys():
            if(syncer.is_entitlement(s)):
                syncer.edit_entitlement_in_table(s, cons_subs[s])
            else:
                syncer.edit_subscription_in_table(s, cons_subs[s])
    else:
        syncer.update_channel_family_table_by_config()
        suse_products = syncer.get_suse_products_from_ncc()
        syncer.update_suse_products_table(suse_products)
        all_subs = syncer.get_subscriptions_from_ncc()
        cons_subs = syncer.consolidate_subscriptions(all_subs)
        syncer.reset_entitlements_in_table()
        for s in cons_subs.keys():
            if( syncer.is_entitlement(s)):
                syncer.edit_entitlement_in_table(s, cons_subs[s])
            else:
                syncer.edit_subscription_in_table(s, cons_subs[s])
        syncer.sync_installed_channels()
        syncer.sync_suseproductchannel()

if __name__ == "__main__":
    try:
        main()
    except IOError, e:
        print "ERROR: %s" % e


