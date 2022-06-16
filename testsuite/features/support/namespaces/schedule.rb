# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "schedule" namespace
class NamespaceSchedule
  def initialize(api_test)
    @test = api_test
  end

  def list_all_actions
    @test.call('schedule.listAllActions', sessionKey: @test.token)
  end

  def list_in_progress_actions
    @test.call('schedule.listInProgressActions', sessionKey: @test.token)
  end

  def list_in_progress_systems(action_id)
    @test.call('schedule.listInProgressSystems', sessionKey: @test.token, actionId: action_id)
  end

  def list_completed_actions
    @test.call('schedule.listCompletedActions', sessionKey: @test.token)
  end

  def list_failed_actions
    @test.call('schedule.listFailedActions', sessionKey: @test.token)
  end

  def list_failed_systems(action_id)
    @test.call('schedule.listFailedSystems', sessionKey: @test.token, actionId: action_id)
  end

  def cancel_actions(actions)
    @test.call('schedule.cancelActions', sessionKey: @test.token, actionIds: actions)
  end

  def fail_system_action(system_id, action_id)
    @test.call('schedule.failSystemAction', sessionKey: @test.token, sid: system_id, actionId: action_id)
  end
end
