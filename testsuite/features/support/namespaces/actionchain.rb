# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "actionchain" namespace
class NamespaceActionchain
  def initialize(api_test)
    @test = api_test
  end

  def list_chains
    @test.call('actionchain.listChains', sessionKey: @test.token).map { |x| x['label'] }
  end

  def create_chain(label)
    @test.call('actionchain.createChain', sessionKey: @test.token, chainLabel: label)
  end

  def delete_chain(label)
    @test.call('actionchain.deleteChain', sessionKey: @test.token, chainLabel: label)
  end

  def remove_action(label, action_id)
    @test.call('actionchain.removeAction', sessionKey: @test.token, chainLabel: label, actionId: action_id)
  end

  def rename_chain(old_label, new_label)
    @test.call('actionchain.renameChain', sessionKey: @test.token, previousLabel: old_label, newLabel: new_label)
  end

  def add_script_run(system, label, uid, gid, timeout, script)
    @test.call('actionchain.addScriptRun', sessionKey: @test.token, serverId: system, chainLabel: label, uid: uid, gid: gid, timeout: timeout, scriptBody: Base64.strict_encode64(script))
  end

  def list_chain_actions(label)
    @test.call('actionchain.listChainActions', sessionKey: @test.token, chainLabel: label)
  end

  def add_system_reboot(system, label)
    @test.call('actionchain.addSystemReboot', sessionKey: @test.token, serverId: system, chainLabel: label)
  end

  def add_package_install(system, packages, label)
    @test.call('actionchain.addPackageInstall', sessionKey: @test.token, serverId: system, packages: packages, chainLabel: label)
  end

  def add_package_upgrade(system, packages, label)
    @test.call('actionchain.addPackageUpgrade', sessionKey: @test.token, serverId: system, packages: packages, chainLabel: label)
  end

  def add_package_verify(system, packages, label)
    @test.call('actionchain.addPackageVerify', sessionKey: @test.token, serverId: system, packages: packages, chainLabel: label)
  end

  def add_package_removal(system, packages, label)
    @test.call('actionchain.addPackageRemoval', sessionKey: @test.token, serverId: system, packages: packages, chainLabel: label)
  end

  def schedule_chain(label, earliest)
    @test.call('actionchain.scheduleChain', sessionKey: @test.token, chainLabel: label, date: earliest)
  end
end
