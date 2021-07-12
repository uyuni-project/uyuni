# Copyright (c) 2014-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Schedule API Namespace
class XMLRPCScheduleTest < XMLRPCBaseTest
  # List all scheduled actions
  def list_all_actions
    @connection.call('schedule.list_all_actions', @sid)
  end

  # List all in-progress actions
  def list_in_progress_actions
    @connection.call('schedule.list_in_progress_actions', @sid)
  end

  # List all in-progress actions
  def list_in_progress_systems(action_id)
    @connection.call('schedule.list_in_progress_systems', @sid, action_id)
  end

  # Cancel a list of actions
  def cancel_actions(actions)
    @connection.call('schedule.cancel_actions', @sid, actions)
  end

  # Fail an action by system ID and action ID
  def fail_system_action(system_id, action_id)
    @connection.call('schedule.fail_system_action', @sid, system_id, action_id)
  end

  # Get a list of failed actions
  def list_failed_actions
    @connection.call('schedule.list_failed_actions', @sid)
  end
end
