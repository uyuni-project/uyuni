# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Action Chain namespace
class NamespaceActionchain
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Returns a list of all the action chains in the account.
  def list_chains
    @test.call('actionchain.listChains', sessionKey: @test.token).map { |x| x['label'] }
  end

  ##
  # Creates a new action chain with the given label.
  #
  # Args:
  #   label: The name of the chain you want to create.
  def create_chain(label)
    @test.call('actionchain.createChain', sessionKey: @test.token, chainLabel: label)
  end

  ##
  # Deletes an action chain with the specified label.
  #
  # Args:
  #   label: The label of the chain you want to delete.
  def delete_chain(label)
    @test.call('actionchain.deleteChain', sessionKey: @test.token, chainLabel: label)
  end

  ##
  # Removes an action from an action chain.
  #
  # Args:
  #   label: The label of the action chain you want to remove an action from.
  #   action_id: The ID of the action to remove.
  def remove_action(label, action_id)
    @test.call('actionchain.removeAction', sessionKey: @test.token, chainLabel: label, actionId: action_id)
  end

  ##
  # Renames an action chain.
  #
  # Args:
  #   old_label: The name of the action chain you want to rename.
  #   new_label: The new name of the action chain.
  def rename_chain(old_label, new_label)
    @test.call('actionchain.renameChain', sessionKey: @test.token, previousLabel: old_label, newLabel: new_label)
  end

  ##
  # Adds a script run action to the action chain.
  #
  # Args:
  #   system: The system ID of the system you want to run the action chain on.
  #   label: The label of the action chain.
  #   uid: The user ID to run the script as.
  #   gid: The group ID of the user that will run the script.
  #   timeout: The number of seconds to wait for the script to complete.
  #   script: The script to run.
  def add_script_run(system, label, uid, gid, timeout, script)
    @test.call('actionchain.addScriptRun', sessionKey: @test.token, sid: system, chainLabel: label, uid: uid, gid: gid, timeout: timeout, scriptBody: Base64.strict_encode64(script))
  end

  ##
  # Lists all the actions in a given chain.
  #
  # Args:
  #   label: The label of the chain you want to list the actions for.
  def list_chain_actions(label)
    @test.call('actionchain.listChainActions', sessionKey: @test.token, chainLabel: label)
  end

  ##
  # Adds a system reboot action to the action chain.
  #
  # Args:
  #   system: The system ID of the system you want to reboot.
  #   label: The label of the action chain to add the action to.
  def add_system_reboot(system, label)
    @test.call('actionchain.addSystemReboot', sessionKey: @test.token, sid: system, chainLabel: label)
  end

  ##
  # Adds a package install action to the action chain for the given system.
  #
  # Args:
  #   system: The system ID of the system you want to add the package to.
  #   packages: This is an array of package IDs. You can get these IDs by running the following command:
  #   label: This is the name of the action chain.
  def add_package_install(system, packages, label)
    @test.call('actionchain.addPackageInstall', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  ##
  # Adds a package upgrade action to the action chain.
  #
  # Args:
  #   system: The system ID of the system you want to upgrade.
  #   packages: This is an array of package IDs.
  #   label: The name of the action chain.
  def add_package_upgrade(system, packages, label)
    @test.call('actionchain.addPackageUpgrade', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  ##
  # Adds a package verify action to the action chain.
  #
  # Args:
  #   system: The system ID of the system you want to add the package to.
  #   packages: This is the package ID of the package you want to add to the action chain.
  #   label: The label of the action chain.
  def add_package_verify(system, packages, label)
    @test.call('actionchain.addPackageVerify', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  ##
  # Adds a package removal action to the action chain for the specified system.
  #
  # Args:
  #   system: The ID of the system you want to add the package removal to.
  #   packages: The package ID of the package you want to remove.
  #   label: The label for the action chain.
  def add_package_removal(system, packages, label)
    @test.call('actionchain.addPackageRemoval', sessionKey: @test.token, sid: system, packageIds: packages, chainLabel: label)
  end

  ##
  # Schedules a chain to run at a specific time.
  #
  # Args:
  #   label: The label of the chain you want to schedule.
  #   earliest: The earliest time to run the chain.
  def schedule_chain(label, earliest)
    @test.call('actionchain.scheduleChain', sessionKey: @test.token, chainLabel: label, date: earliest)
  end
end
