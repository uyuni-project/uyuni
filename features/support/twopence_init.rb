require "twopence"

# initialize ssh targets from environment variables.
$server_ip = ENV['TESTHOST']
$client_ip = ENV['CLIENT']

$client = Twopence.init("ssh:#{$client_ip}")
$server = Twopence.init("ssh:#{$server_ip}")

# get hostname client
client_hostname, _local, _remote, code = $client.test_and_store_results_together("hostname -f", "root", 500)
if code != 0
  print $client_hostname
  raise "no full qualified hostname for client"
end
$client_hostname = client_hostname.strip

server_hostname, _local, _remote, code = $server.test_and_store_results_together("hostname -f", "root", 500)
if code != 0
  print $server_hostname
  raise "no full qualified hostname for client"
end
$server_hostname = server_hostname.strip

def file_exist(node, file)
  _out, _local, _remote, _code = node.test_and_store_results_together("test -f #{file}", "root", 500)
end

def run_cmd(node, cmd, timeout)
  _out, _local, _remote, _code = node.test_and_store_results_together(cmd, "root", timeout)
end
