require "twopence"
require "lavanda"

# initialize ssh targets from environment variables.
# this ip adress comes from slenkins-engine,
# but run.py can read the ip and pass the ip to env. variable, so we take the ip from there.
$server_ip = ENV['TESTHOST']
$client_ip = ENV['CLIENT']
$minion_ip = ENV['MINION']

# define twopence object.
$client = Twopence.init("ssh:#{$client_ip}")
$server = Twopence.init("ssh:#{$server_ip}")
$minion = Twopence.init("ssh:#{$minion_ip}")

# lavanda library module extension.
# we have here for moment the $target.run call
$server.extend(LavandaBasic)
$client.extend(LavandaBasic)
$minion.extend(LavandaBasic)

# add here new vms ( fedora, redhat) etc.
nodes = [$server, $client, $minion]
node_hostnames = []

# get the hostnames of various vms
for node in nodes
  hostname, _local, _remote, code = node.test_and_store_results_together("hostname -f", "root", 500)
  raise "no full qualified hostname for node" if code.nonzero?
  node_hostnames.push(hostname.strip)
end

# this glob variable are used in cucumber steps.
$server_hostname = node_hostnames[0]
$client_hostname = node_hostnames[1]
$minion_hostname = node_hostnames[2]

# helper functions for moment this are used in salt.steps but maybe move this to lavanda.rb
def file_exist(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("test -f #{file}", "root", 500)
  code
end

def file_delete(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("rm  #{file}", "root", 500)
  code
end
