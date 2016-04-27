require "xmlrpc/client"
require "socket"

rpc = XMLRPCActionChain.new(ENV["TESTHOST"])
sysrpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
scdrpc = XMLRPCScheduleTest.new(ENV['TESTHOST'])

users = nil
roles = nil
clientId = nil
chainLabel = nil

# Auth
Given(/^I am logged in via XML\-RPC\/actionchain as user "(.*?)" and password "(.*?)"$/) do |luser, password|
  # Find target server once.
  if not $clientId
    rpc.login(luser, password)
    sysrpc.login(luser, password)
    scdrpc.login(luser, password)

    servers = sysrpc.listSystems()
    refute_nil(servers)
    hostname = Socket.gethostbyname(Socket.gethostname).first # Needs proper DNS!
    $clientId = servers
                .select {|s| s['name'] == hostname }
                .map {|s| s['id'] }.first
    refute_nil($clientId, "Client #{hostname} is not yet registered?")
  end

  # Flush all chains
  rpc.listChains.each do |label|
    rpc.deleteChain(label)
  end
end

# Listing chains
When(/^I call XML\-RPC\/createChain with chainLabel "(.*?)"$/) do |label|
  actionId = rpc.createChain(label)
  refute(actionId < 1)
  $chainLabel = label
end

When(/^I call actionchain\.listChains\(\) if label "(.*?)" is there$/) do |label|
  assert_includes(rpc.listChains(), label)
end

# Deleting chain
Then(/^I delete the action chain$/) do
  begin
    rpc.deleteChain($chainLabel)
  rescue XMLRPC::FaultException => e
    fail "deleteChain: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

Then(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  begin
    rpc.deleteChain(label)
  rescue XMLRPC::FaultException => e
    fail "deleteChain: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

# Renaming chain
Then(/^I call actionchain\.renameChain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |oldLabel, newLabel|
  rpc.renameChain(oldLabel, newLabel)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  assert_includes(rpc.listChains(), label)
end

Then(/^there should be an action chain with the label "(.*?)"$/) do |label|
  assert_includes(rpc.listChains(), label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  refute_includes(rpc.listChains(), label)
end

Then(/^no action chain with the label "(.*?)"\.$/) do |label|
  refute_includes(rpc.listChains(), label)
end

#
# Schedule scenario
#
When(/^I call actionchain\.addScriptRun\(\) with the script like "(.*?)"$/) do |script|
  refute(rpc.addScriptRun($clientId, script, $chainLabel) < 1)
end

Then(/^I should be able to see all these actions in the action chain$/) do
  begin
    actions = rpc.listChainActions($chainLabel)
    refute_nil(actions)
    puts "Running actions:"
    actions.each do |action|
      puts "\t- " + action["label"]
    end
  rescue XMLRPC::FaultException => e
    fail "Error listChainActions: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

# Reboot
When(/^I call actionchain\.addSystemReboot\(\)$/) do
  refute(rpc.addSystemReboot($clientId, $chainLabel) < 1)
end

# Packages operations
When(/^I call actionchain\.addPackageInstall\(\)$/) do
  pkgs = sysrpc.listAllInstallablePackages($clientId)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.addPackageInstall($clientId, [pkgs[0]["id"]], $chainLabel) < 1)
end

When(/^I call actionchain\.addPackageRemoval\(\)$/) do
  pkgs = sysrpc.listAllInstallablePackages($clientId)
  refute(rpc.addPackageRemoval($clientId, [pkgs[0]["id"]], $chainLabel) < 1)
end

When(/^I call actionchain\.addPackageUpgrade\(\)$/) do
  pkgs = sysrpc.listLatestUpgradablePackages($clientId)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.addPackageUpgrade($clientId, [pkgs[0]["to_package_id"]], $chainLabel) < 1)
end

When(/^I call actionchain\.addPackageVerify\(\)$/) do
  pkgs = sysrpc.listAllInstallablePackages($clientId)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.addPackageVerify($clientId, [pkgs[0]["id"]], $chainLabel) < 1)
end

# Manage actions within the action chain
When(/^I call actionchain\.removeAction on each action within the chain$/) do
  begin
    actions = rpc.listChainActions($chainLabel)
    refute_nil(actions)
    for action in actions do
      refute(rpc.removeAction($chainLabel, action["id"]) < 0)
      puts "\t- Removed \"" + action["label"] + "\" action"
    end
  rescue XMLRPC::FaultException => e
    fail "Error removeAction: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

Then(/^I should be able to see that the current action chain is empty$/) do
  assert_empty(rpc.listChainActions($chainLabel))
end

# Scheduling the action chain
When(/^I schedule the action chain$/) do
  refute(rpc.scheduleChain($chainLabel, DateTime.now) < 0)
end

Then(/^there should be no more my action chain$/) do
  refute_includes(rpc.listChains(), $chainLabel)
end

Then(/^I should see scheduled action, called "(.*?)"$/) do |label|
  assert_includes(
    scdrpc.listInProgressActions().map {|a| a['name']},
    label)
end

Then(/^I cancel all scheduled actions$/) do
  for action in scdrpc.listInProgressActions() do
    # One-by-one, this is against single call in the API on purpose.
    scdrpc.cancelActions([action["id"]])
    puts "\t- Removed \"" + action["name"] + "\" action"
  end
end

Then(/^there should be no more any scheduled actions$/) do
  assert_empty(scdrpc.listInProgressActions())
end
