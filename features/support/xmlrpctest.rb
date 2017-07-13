require 'xmlrpc/client'

def retrieve_server_id(server)
  sysrpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  sysrpc.login('admin', 'admin')
  systems = sysrpc.listSystems
  refute_nil(systems)
  server_id = systems
              .select { |s| s['name'] == server }
              .map { |s| s['id'] }.first
  refute_nil(server_id, "client #{server} is not yet registered?")
  server_id
end

class XMLRPCBaseTest
  def initialize(host)
    @host = host
    @connection = XMLRPC::Client.new2("http://" + @host + "/rpc/api")
  end

  #
  # Authenticate against the $HOST
  #
  def login(luser, password)
    @sid = @connection.call("auth.login", luser, password)
  end

  def logout
    @connection.call("auth.logout", @sid)
  end
end
