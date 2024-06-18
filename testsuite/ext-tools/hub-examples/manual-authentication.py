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

print("Manual login...")
hubSessionKey = client.hub.login(args.user, args.password)
print(f"hubSessionKey: {hubSessionKey}\n")

print("Get the server IDs...")
serverIds = client.hub.listServerIds(hubSessionKey)
print(f"serverIds: {serverIds}\n")

# For simplicity, this example assumes you are using the same username and password here, as on the hub server.
# However, in most cases, every server has its own individual credentials.
usernames = [args.user for s in serverIds]
passwords = [args.password for s in serverIds]

# Each server uses the credentials set above, client.hub.attachToServers needs
# them passed as lists with as many elements as there are servers.
client.hub.attachToServers(hubSessionKey, serverIds, usernames, passwords)

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
