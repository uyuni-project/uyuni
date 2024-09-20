#!/usr/bin/python3
import xmlrpc.client
import argparse
from datetime import datetime, timezone

# Parsing arguments
parser = argparse.ArgumentParser()
parser.add_argument('hub_fqdn', type=str, help='The Hub FQDN')
parser.add_argument('user', type=str, help='The username for the Hub')
parser.add_argument('password', type=str, help='The password for the Hub')
parser.add_argument('packageId', type=int, help='Package ID')
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

print("Multicast call to get the list of systems per server IDs...")
systemsPerServer = client.multicast.system.list_systems(hubSessionKey, serverIds)
successfulResponses = systemsPerServer["Successful"]
failedResponses = systemsPerServer["Failed"]

if successfulResponses["Responses"]:
    systemsPerServer = {server_id: [item['id'] for item in response] for server_id, response in zip(successfulResponses['ServerIds'], successfulResponses['Responses'])}
    for serverId, systemIds in systemsPerServer.items():
        print(f"Unicast call to {serverId} server, to schedule a install of package ID {args.packageId} into {systemIds} system")
        result = client.unicast.system.schedulePackageInstall(hubSessionKey, serverId, systemIds, [args.packageId], datetime.now())
        print(f"ActionIds: {result}")

if failedResponses["Responses"]:
    print(f"Systems failing to reply through a multicast api call:\n{failedResponses}\n")

print("Multicast call to get the list of systems per server IDs...")
result = client.multicast.system.list_systems(hubSessionKey, serverIds)
print(f"ActionIds: {result}")

# Log out
client.hub.logout(hubSessionKey)
