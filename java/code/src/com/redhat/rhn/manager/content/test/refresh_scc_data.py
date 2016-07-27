#!/usr/bin/env python

import httplib2
import re
import json
import sys

def find_next_url(resp):
  if "link" in resp:
    regex = re.compile(r'<([^>]+)>; rel="next"')
    match = regex.search(resp["link"])
    if match is not None:
        return match.group(1)

def get_paginated(http, url):
  result = []
  current_url = url
  while current_url is not None:
    (resp, content) = http.request(current_url, "GET")

    if resp.status != 200:
        print("Unexpected HTTP status received on %s: %d" % (current_url, resp.status))
        sys.exit(1)

    result.extend(json.loads(content))
    current_url = find_next_url(resp)
  return result

def save_json(content, path):
  with open(path, 'w') as out_file:
    json.dump(content, out_file, sort_keys=True, indent=2)

http = httplib2.Http(".cache", disable_ssl_certificate_validation=True)
http.add_credentials("UCUSER", "UCPASSWORD")

products = get_paginated(http, "https://scc.suse.com/connect/organizations/products/unscoped")
save_json(products, "products.json")
print("products.json refreshed")

repositories = get_paginated(http, "https://scc.suse.com/connect/organizations/repositories")
save_json(repositories, "repositories.json")
print("repositories.json refreshed")

subscriptions = get_paginated(http, "https://scc.suse.com/connect/organizations/subscriptions")
save_json(subscriptions, "organizations_subscriptions.json")
print("subscriptions.json refreshed")

orders = get_paginated(http, "https://scc.suse.com/connect/organizations/orders")
save_json(orders, "organizations_orders.json")
print("organizations_orders.json refreshed")
