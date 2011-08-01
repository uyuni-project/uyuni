File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'


class XMLRPCSystemTest < XMLRPCBaseTest
  def listSystems()
    return (@connection.call("system.listSystems", @sid) or [])
  end


  # Go wild...
  # No need to write monstrous scenario for a little checks.
  # We just do it all at once instead.
  def getSysInfo(server)
    serverId = server['id']
    begin
      connPath = @connection.call("system.getConnectionPath", @sid, serverId)
      puts connPath
    rescue Exception => ex
      puts "Ouch: " + ex
      return false
    end

    return true
  end
end
