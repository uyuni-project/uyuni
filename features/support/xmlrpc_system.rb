File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'


class XMLRPCSystemTest < XMLRPCBaseTest
  def listSystems()
    return (@connection.call("system.listSystems", @sid) or [])
  end
end
