# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# The NamespaceRecurring class provides methods for interacting with the recurring actions API.
class NamespaceRecurring
  # Initializes a new instance of the NamespaceRecurring class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Creates a recurring custom state action for a minion.
  #
  # @param minion_id [Integer] The ID of the minion.
  # @param name [String] The name of the recurring action.
  # @param cron_expr [String] The cron expression of the recurring action.
  # @param states [Array<String>] The state names to apply.
  # @return [Integer] The ID of the new recurring action.
  def create_custom_state(minion_id, name, cron_expr, states)
    action_props = { 'entity_type' => 'minion', 'entity_id' => minion_id, 'name' => name, 'cron_expr' => cron_expr, 'states' => states }
    @test.call('recurring.custom.create', sessionKey: @test.token, actionProps: action_props)
  end

  # Lists the recurring actions of a minion.
  #
  # @param minion_id [Integer] The ID of the minion.
  # @return [Array] The recurring actions of the minion.
  def list_by_minion(minion_id)
    @test.call('recurring.listByEntity', sessionKey: @test.token, type: 'minion', id: minion_id)
  end

  # Deletes a recurring action.
  #
  # @param action_id [Integer] The ID of the recurring action.
  def delete(action_id)
    @test.call('recurring.delete', sessionKey: @test.token, id: action_id)
  end
end
