File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'


class XMLRPCScheduleTest < XMLRPCBaseTest
  def listAllActions()
    return (@connection.call("schedule.listAllActions", @sid) || [])
  end


  def listInProgressActions()
    return (@connection.call("schedule.listInProgressActions", @sid) || [])
  end


  def cancelActions(actions)
    return @connection.call("schedule.cancelActions", @sid, actions)
  end
end
