#!/usr/bin/env python

import argparse
import base64
import httplib
import json
import re
import sys

def parse_arguments():
  description = 'Generate JSON files with data from SCC'
  parser = argparse.ArgumentParser(description=description)
  parser.add_argument("-u", "--username", dest="username", help="SCC username",
                      required=True)
  parser.add_argument("-p", "--password", dest="password", help="SCC password",
                      required=True)
  parser.add_argument("-r", "--real-tokens", dest="real_tokens",
                      action='store_true',
                      help="When present, store URLs with real tokens at " +
                           "repositories.json (WARNING: This could leak " +
                           "private data!")
  results = parser.parse_args()
  return(results)

def find_next_path(resp):
  link = resp.getheader("Link")
  if link is not None:
    regex = re.compile(r'<https://scc.suse.com([^>]+)>; rel="next"')
    match = regex.search(link)
    if match is not None:
        return match.group(1)

def get_paginated(connection, headers, path):
  result = []
  current_path = path
  while current_path is not None:
    connection.request("GET", current_path, headers=headers)
    resp = connection.getresponse()
    content = resp.read()

    if resp.status != 200:
        print("Unexpected HTTP status received on %s: %d" % (current_path, resp.status))
        sys.exit(1)

    result.extend(json.loads(content))
    current_path = find_next_path(resp)
  return result

def save_json(content, path):
  with open(path, 'w') as out_file:
    json.dump(content, out_file, sort_keys=True, indent=2, separators=(',', ': '))

args = parse_arguments()

connection = httplib.HTTPSConnection("scc.suse.com")
token = base64.b64encode(b"%s:%s" %(args.username, args.password)).decode("ascii")
headers = { 'Authorization' : 'Basic %s' %  token }

products = get_paginated(connection, headers, "/connect/organizations/products/unscoped")
save_json(products, "products.json")
print("products.json refreshed")

repositories = get_paginated(connection, headers, "/connect/organizations/repositories")
if args.real_tokens:
  print("WARNING: Storing real repository tokens at repositories.json!")
  print("         This could leak private data!")
else:
  repo_url_regex = re.compile(r'^(https:\/\/.+\/)\?(.+)$')
  for repo_num in range(0,len(repositories)):
    repositories[repo_num]['url'] = repo_url_regex.sub(r'\1?my-fake-token',
                                                       repositories[repo_num]['url'])

save_json(repositories, "repositories.json")
print("repositories.json refreshed")

subscriptions = get_paginated(connection, headers, "/connect/organizations/subscriptions")
save_json(subscriptions, "organizations_subscriptions.json")
print("subscriptions.json refreshed")

orders = get_paginated(connection, headers, "/connect/organizations/orders")
save_json(orders, "organizations_orders.json")
print("organizations_orders.json refreshed")
