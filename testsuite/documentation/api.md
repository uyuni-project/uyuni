## Testing the API with a standalone script

You can use this template:
```
#! /usr/bin/ruby

require_relative 'api_test'
require 'json'

server = 'uyuni-master-srv.mgr.suse.de'
user = 'admin'
password = 'admin'
DEFAULT_TIMEOUT = 300

# $api_test = ApiTestHttp.new(server)
$api_test = ApiTestXmlrpc.new(server)

$api_test.auth.login(user, password)
puts $api_test.system.search.hostname('min')
$api_test.auth.logout
```
The same script can work with both APIs (XML-RPC and HTTP),
just comment out the API you don't use in this script.

Place the script on the controller, in `spacewalk/testsuite/features/support`,
make it executable with `chmod +x myscript.rb`, and run it with `./myscript.rb`.
