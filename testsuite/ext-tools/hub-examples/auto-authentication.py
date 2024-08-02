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
  print(f"Systems responding successfully through a multicast api call:\n{successfulResponses}\n")

if failedResponses["Responses"]:
  print(f"Systems failing to reply through a multicast api call:\n{failedResponses}\n")

# Log out
client.hub.logout(hubSessionKey)
