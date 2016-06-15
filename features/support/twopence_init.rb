require "twopence"

# initialize ssh targets from environment variables.
server = ENV['TESTHOST']
client = ENV['CLIENT']

$client = Twopence::init("ssh:#{client}")
$server = Twopence::init("ssh:#{server}")

# get hostname client 
client_hostname, local, remote, code = $client.test_and_store_results_together("hostname -f", "root", 500)
  if  code != 0
      print $client_hostname
      raise "no full qualified hostname for client"
  end

# remove white-space 
$client_hostname = client_hostname.strip
