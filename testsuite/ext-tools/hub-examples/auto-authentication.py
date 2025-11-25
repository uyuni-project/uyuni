#!/usr/bin/python3

# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only

import xmlrpc.client
import argparse

# Parsing arguments
parser = argparse.ArgumentParser()
parser.add_argument("hub_fqdn", type=str, help="The Hub FQDN")
parser.add_argument("user", type=str, help="The username for the Hub")
parser.add_argument("password", type=str, help="The password for the Hub")
args = parser.parse_args()

api = f"http://{args.hub_fqdn}:2830/hub/rpc/api"
client = xmlrpc.client.ServerProxy(api)

print("Login with autoconnect mode...")
loginResponse = client.hub.loginWithAutoconnectMode(args.user, args.password)
hubSessionKey = loginResponse["SessionKey"]
print(f"hubSessionKey: {hubSessionKey}\n")

print("Get the server IDs...")
serverIds = client.hub.listServerIds(hubSessionKey)
print(f"serverIds: {serverIds}\n")

print("Get the list of systems per server IDs...")
systemsPerServer = client.multicast.system.list_systems(hubSessionKey, serverIds)
successfulResponses = systemsPerServer["Successful"]
failedResponses = systemsPerServer["Failed"]

if successfulResponses["Responses"]:
    print(
        f"Systems responding successfully through a multicast api call:\n{successfulResponses}\n"
    )

if failedResponses["Responses"]:
    print(
        f"Systems failing to reply through a multicast api call:\n{failedResponses}\n"
    )

# Log out
client.hub.logout(hubSessionKey)
