# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# The NamespaceSchedule class provides methods for interacting with the schedule API.
class NamespaceSchedule
  # Initializes a new instance of the NamespaceSchedule class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Lists all actions.
  #
  # @return [Array] A list of all actions.
  def list_all_actions
    @test.call('schedule.listAllActions', sessionKey: @test.token)
  end

  # Returns a list of actions that are currently in progress.
  #
  # @return [Array] A list of actions that are currently in progress.
  def list_in_progress_actions
    @test.call('schedule.listInProgressActions', sessionKey: @test.token)
  end

  # Returns a list of systems that are currently in progress for the given action.
  #
  # @param action_id [Integer] The ID of the action you want to list systems for.
  # @return [Array] A list of systems that are currently in progress for the given action.
  def list_in_progress_systems(action_id)
    @test.call('schedule.listInProgressSystems', sessionKey: @test.token, actionId: action_id)
  end

  # Returns a list of completed actions for the current user.
  #
  # @return [Array] A list of completed actions for the current user.
  def list_completed_actions
    @test.call('schedule.listCompletedActions', sessionKey: @test.token)
  end

  # Returns a list of failed actions.
  #
  # @return [Array] A list of failed actions.
  def list_failed_actions
    @test.call('schedule.listFailedActions', sessionKey: @test.token)
  end

  # Returns a list of systems that failed to execute the action.
  #
  # @param action_id [Integer] The ID of the action to list failed systems for.
  # @return [Array] A list of systems that failed to execute the action.
  def list_failed_systems(action_id)
    @test.call('schedule.listFailedSystems', sessionKey: @test.token, actionId: action_id)
  end

  # Cancels actions in the schedule.
  #
  # @param actions [Array] An array of action IDs to cancel.
  def cancel_actions(actions)
    @test.call('schedule.cancelActions', sessionKey: @test.token, actionIds: actions)
  end

  # Fails a system action.
  #
  # @param system_id [Integer] The ID of the system you want to schedule an action for.
  # @param action_id [Integer] The action ID of the action you want to fail.
  def fail_system_action(system_id, action_id)
    @test.call('schedule.failSystemAction', sessionKey: @test.token, sid: system_id, actionId: action_id)
  end
end
