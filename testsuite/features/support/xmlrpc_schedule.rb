require_relative 'xmlrpctest'

# schedule class
class XMLRPCScheduleTest < XMLRPCBaseTest
  def list_all_actions
    @connection.call('schedule.list_all_actions', @sid)
  end

  def list_in_progress_actions
    @connection.call('schedule.list_in_progress_actions', @sid)
  end

  def list_in_progress_systems(action_id)
    @connection.call('schedule.list_in_progress_systems', @sid, action_id)
  end

  def cancel_actions(actions)
    @connection.call('schedule.cancel_actions', @sid, actions)
  end

  def fail_system_action(system_id, action_id)
    @connection.call('schedule.fail_system_action', @sid, system_id, action_id)
  end

  def list_failed_actions
    @connection.call('schedule.list_failed_actions', @sid)
  end
end
