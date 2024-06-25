# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Action Chain namespace
class NamespaceActionchain
  # Initializes a new instance of the NamespaceActionchain class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Returns a list of all the action chains in the account.
  #
  # @return [Array<String>] The list of action chain labels.
  def list_chains
    @test.call('actionchain.listChains', sessionKey: @test.token).map { |x| x['label'] }
  end

  # Creates a new action chain with the given label.
  #
  # @param label [String] The name of the chain you want to create.
  def create_chain(label)
    @test.call('actionchain.createChain', sessionKey: @test.token, chainLabel: label)
  end

  # Deletes an action chain with the specified label.
  #
  # @param label [String] The label of the chain you want to delete.
  def delete_chain(label)
    @test.call('actionchain.deleteChain', sessionKey: @test.token, chainLabel: label)
  end

  # Removes an action from an action chain.
  #
  # @param label [String] The label of the action chain you want to remove an action from.
  # @param action_id [String] The ID of the action to remove.
  def remove_action(label, action_id)
    @test.call('actionchain.removeAction', sessionKey: @test.token, chainLabel: label, actionId: action_id)
  end

  # Renames an action chain.
  #
  # @param old_label [String] The name of the action chain you want to rename.
  # @param new_label [String] The new name of the action chain.
  def rename_chain(old_label, new_label)
    @test.call('actionchain.renameChain', sessionKey: @test.token, previousLabel: old_label, newLabel: new_label)
  end

  # Adds a script run action to the action chain.
  #
  # @param system [String] The system ID of the system you want to run the action chain on.
  # @param label [String] The label of the action chain.
  # @param uid [String] The user ID to run the script as.
  # @param gid [String] The group ID of the user that will run the script.
  # @param timeout [Integer] The number of seconds to wait for the script to complete.
  # @param script [String] The script to run.
  def add_script_run(system, label, uid, gid, timeout, script)
    @test.call('actionchain.addScriptRun', sessionKey: @test.token, sid: system, chainLabel: label, uid: uid, gid: gid, timeout: timeout, scriptBody: Base64.strict_encode64(script))
  end

  # Lists all the actions in a given chain.
  #
  # @param label [String] The label of the chain you want to list the actions for.
  # @return [Array<Hash>] The list of actions in the chain.
  def list_chain_actions(label)
    @test.call('actionchain.listChainActions', sessionKey: @test.token, chainLabel: label)
  end

  # Adds a system reboot action to the action chain.
  #
  # @param system [String] The system ID of the system you want to reboot.
  # @param label [String] The label of the action chain to add the action to.
  def add_system_reboot(system, label)
    @test.call('actionchain.addSystemReboot', sessionKey: @test.token, sid: system, chainLabel: label)
  end

  # Adds a package install action to the action chain for the given system.
  #
  # @param system [String] The system ID of the system you want to add the package to.
  # @param packages [Array<String>] An array of package IDs.
  # @param label [String] The name of the action chain.
  def add_package_install(system, packages, label)
    @test.call('actionchain.addPackageInstall', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  # Adds a package upgrade action to the action chain.
  #
  # @param system [String] The system ID of the system you want to upgrade.
  # @param packages [Array<String>] An array of package IDs.
  # @param label [String] The name of the action chain.
  def add_package_upgrade(system, packages, label)
    @test.call('actionchain.addPackageUpgrade', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  # Adds a package verify action to the action chain.
  #
  # @param system [String] The system ID of the system you want to add the package to.
  # @param packages [String] The package ID of the package you want to add to the action chain.
  # @param label [String] The label of the action chain.
  def add_package_verify(system, packages, label)
    @test.call('actionchain.addPackageVerify', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  # Adds a package removal action to the action chain for the specified system.
  #
  # @param system [String] The ID of the system you want to add the package removal to.
  # @param packages [String] The package ID of the package you want to remove.
  # @param label [String] The label for the action chain.
  def add_package_removal(system, packages, label)
    @test.call('actionchain.addPackageRemoval', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  # Schedules a chain to run at a specific time.
  #
  # @param label [String] The label of the chain you want to schedule.
  # @param earliest [Time] The earliest time to run the chain.
  def schedule_chain(label, earliest)
    @test.call('actionchain.scheduleChain', sessionKey: @test.token, chainLabel: label, date: earliest)
  end
end
