# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Schedule namespace
class NamespaceSchedule
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # It lists all actions.
  def list_all_actions
    @test.call('schedule.listAllActions', sessionKey: @test.token)
  end

  ##
  # Returns a list of actions that are currently in progress.
  def list_in_progress_actions
    @test.call('schedule.listInProgressActions', sessionKey: @test.token)
  end

  ##
  # Returns a list of systems that are currently in progress for the given action.
  #
  # Args:
  #   action_id: The ID of the action you want to list systems for.
  def list_in_progress_systems(action_id)
    @test.call('schedule.listInProgressSystems', sessionKey: @test.token, actionId: action_id)
  end

  ##
  # Returns a list of completed actions for the current user.
  def list_completed_actions
    @test.call('schedule.listCompletedActions', sessionKey: @test.token)
  end

  ##
  # Returns a list of failed actions.
  def list_failed_actions
    @test.call('schedule.listFailedActions', sessionKey: @test.token)
  end

  ##
  # Returns a list of systems that failed to execute the action.
  #
  # Args:
  #   action_id: The ID of the action to list failed systems for.
  def list_failed_systems(action_id)
    @test.call('schedule.listFailedSystems', sessionKey: @test.token, actionId: action_id)
  end

  ##
  # Cancels actions in the schedule.
  #
  # Args:
  #   actions: An array of action IDs to cancel.
  def cancel_actions(actions)
    @test.call('schedule.cancelActions', sessionKey: @test.token, actionIds: actions)
  end

  ##
  # Fails a system action.
  #
  # Args:
  #   system_id: The ID of the system you want to schedule an action for.
  #   action_id: The action ID of the action you want to fail.
  def fail_system_action(system_id, action_id)
    @test.call('schedule.failSystemAction', sessionKey: @test.token, sid: system_id, actionId: action_id)
  end
end
