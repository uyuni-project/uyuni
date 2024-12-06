# API

## General

We use 2 different APIs for testing

- SUSE Manager: XML-RPC API (HTTPS + XML)
- Uyuni: REST API (HTTPS + JSON), which we internally call HTTP API

## Testing the API with a standalone script

You can use this template:

```bash
#! /usr/bin/ruby

require_relative 'api_test'
require 'json'

server = 'uyuni-master-srv.mgr.suse.de'
user = 'admin'
password = 'admin'
DEFAULT_TIMEOUT = 300

# $api_test = ApiTestHttp.new(server)
$api_test = ApiTestXmlrpc.new(server)
puts $api_test.system.search.hostname('min')
```

The same script can work with both APIs (XML-RPC and HTTP),
just comment out the API you don't use in this script.

Place the script on the controller, in `spacewalk/testsuite/features/support`,
make it executable with `chmod +x myscript.rb`, and run it with `./myscript.rb`.

## Choose the API protocol

You can choose the API protocol by setting the `API_PROTOCOL` environment variable. 
You can set it to `http` to use the HTTP API or `xmlrpc` to use the XML-RPC API.
