require_relative 'xmlrpctest'

# schedule class
class XMLRPCScheduleTest < XMLRPCBaseTest
  def listAllActions
    @connection.call('schedule.listAllActions', @sid)
  end

  def listInProgressActions
    @connection.call('schedule.listInProgressActions', @sid)
  end

  def cancelActions(actions)
    @connection.call('schedule.cancelActions', @sid, actions)
  end

  def listFailedActions
    @connection.call('schedule.listFailedActions', @sid)
  end
end
