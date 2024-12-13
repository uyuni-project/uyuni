#!/usr/bin/python3
import xmlrpc.client
import argparse

# Parsing arguments
parser = argparse.ArgumentParser()
parser.add_argument('hub_fqdn', type=str, help='The Hub FQDN')
parser.add_argument('user', type=str, help='The username for the Hub')
parser.add_argument('password', type=str, help='The password for the Hub')
args = parser.parse_args()

api = f"http://{args.hub_fqdn}:2830/hub/rpc/api"
client = xmlrpc.client.ServerProxy(api)

print("Login with authrelay mode...")
hubSessionKey = client.hub.loginWithAuthRelayMode(args.user, args.password)
print(f"hubSessionKey: {hubSessionKey}\n")

print("Get the server IDs...")
serverIds = client.hub.listServerIds(hubSessionKey)
print(f"serverIds: {serverIds}\n")

# Authenticate those servers(same credentials will be used as of hub to authenticate)
client.hub.attachToServers(hubSessionKey, serverIds)

print("Get the list of systems by server IDs...")
systemsPerServer = client.multicast.system.list_systems(hubSessionKey, serverIds)
successfulResponses = systemsPerServer["Successful"]
failedResponses = systemsPerServer["Failed"]

if successfulResponses["Responses"]:
  print(f"Systems responding successfully through a multicast api call:\n{successfulResponses}\n")

if failedResponses["Responses"]:
  print(f"Systems failing to reply through a multicast api call:\n{failedResponses}\n")

# Log out
client.hub.logout(hubSessionKey)
