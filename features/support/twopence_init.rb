require "twopence"
require "lavanda"

# initialize ssh targets from environment variables.

raise "Server ip var empty" if ENV['TESTHOST'].nil?
raise "client ip var empty" if ENV['CLIENT'].nil?
raise "minion ip var empty" if ENV['MINION'].nil?
raise "ceos-minion ip var empty" if ENV['CENTOSMINION'].nil?

$server_ip = ENV['TESTHOST']
$client_ip = ENV['CLIENT']
$minion_ip = ENV['MINION']
$ceos_minion_ip = ENV['CENTOSMINION']

# define twopence object.
$client = Twopence.init("ssh:#{$client_ip}")
$server = Twopence.init("ssh:#{$server_ip}")
$minion = Twopence.init("ssh:#{$minion_ip}")
$ceos_minion = Twopence.init("ssh:#{$ceos_minion_ip}")

# lavanda library module extension.
# we have here for moment the command : $target.run call, $server.run("uptime")
$server.extend(LavandaBasic)
$client.extend(LavandaBasic)
$minion.extend(LavandaBasic)
$ceos_minion.extend(LavandaBasic)

# add here new vms ( fedora, redhat) etc.
nodes = [$server, $client, $minion, $ceos_minion]
node_hostnames = []
node_fqn = []
# get the hostnames of various vms
for node in nodes
  hostname, _local, _remote, code = node.test_and_store_results_together("hostname", "root", 500)
  raise "cannot get hostname for node" if code.nonzero?
  fqn, _local, _remote, code = node.test_and_store_results_together("hostname -f", "root", 500)
  raise "no full qualified hostname for node" if code.nonzero?
  # store normal hostname and full qualified hoststname
  node_fqn.push(fqn.strip)
  node_hostnames.push(hostname.strip)
end

# this glob variable are used in cucumber steps.
$server_hostname = node_hostnames[0]
$server_fullhostname = node_fqn[0]
$client_hostname = node_hostnames[1]
$client_fullhostname = node_fqn[1]
$minion_hostname = node_hostnames[2]
$minion_fullhostname = node_fqn[2]
$ceos_minion_hostname = node_hostnames[3]
$ceos_minion_fullhostname = node_fqn[3]

# helper functions for moment this are used in salt.steps but maybe move this to lavanda.rb
def file_exist(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("test -f #{file}", "root", 500)
  code
end

def file_delete(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("rm  #{file}", "root", 500)
  code
end
