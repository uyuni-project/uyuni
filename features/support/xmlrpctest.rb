require 'xmlrpc/client'

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

