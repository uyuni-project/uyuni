require_relative 'xmlrpctest'

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
