require "xmlrpc/client"
require "socket"

rpc = XMLRPCActionChain.new(ENV["TESTHOST"])
sysrpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
scdrpc = XMLRPCScheduleTest.new(ENV['TESTHOST'])

users = nil
roles = nil
password = "die gurke"
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
    fail if not servers
    hostname = Socket.gethostbyname(Socket.gethostname).first # Needs proper DNS!
    for server in servers do
      if server["name"] == hostname
        $clientId = server["id"]
        break
      end
    end
    fail ("Client %s is not yet registered?" % [hostname]) if not $clientId
  end

  # Flush all chains
  for chain in rpc.listChains() do
    rpc.removeActionChain(chain["name"])
  end
end

# Listing chains
When(/^I call XML\-RPC\/createActionChain with chainLabel "(.*?)"$/) do |label|
  actionId = rpc.createActionChain(label)
  fail if actionId < 1
  $chainLabel = label
end

When(/^I call actionchain\.listChains\(\) if label "(.*?)" is there$/) do |label|
  fail if not rpc.listChains().include?(label)
end

# Deleting chain
Then(/^I delete the action chain$/) do
  begin
    rpc.removeActionChain($chainLabel)
  rescue XMLRPC::FaultException => e
    fail "removeActionChain: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

Then(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  begin
    rpc.removeActionChain(label)
  rescue XMLRPC::FaultException => e
    fail "removeActionChain: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

Then(/^there should be no action chain with the label "(.*?)"\.$/) do |label|
  fail if rpc.listChains().include?(label)
end

# Renaming chain
Then(/^I call actionchain\.renameChain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |oldLabel, newLabel|
  rpc.renameChain(oldLabel, newLabel)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  fail if not rpc.listChains().include?(label)
end

Then(/^there should be an action chain with the label "(.*?)"$/) do |label|
  fail if not rpc.listChains().include?(label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  fail if rpc.listChains().include?(label)
end

Then(/^no action chain with the label "(.*?)"\.$/) do |label|
  fail if rpc.listChains().include?(label)
end

#
# Schedule scenario
#
When(/^I call actionchain\.addScriptRun\(\) with the script like "(.*?)"$/) do |script|
  fail if rpc.addScriptRun($clientId, script, $chainLabel) < 1
end

Then(/^I should be able to see all these actions in the action chain$/) do
  begin
    actions = rpc.listChainActions($chainLabel)
    fail if not actions
    puts "Running actions:"
    for action in actions do
      puts "\t- " + action["label"]
    end
  rescue XMLRPC::FaultException => e
    fail "Error listChainActions: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

# Reboot
When(/^I call actionchain\.addSystemReboot\(\)$/) do
  fail if rpc.addSystemReboot($clientId, $chainLabel) < 1
end

# Packages operations
When(/^I call actionchain\.addPackageInstall\(\)$/) do
  pkgs = sysrpc.listAllInstallablePackages($clientId)
  fail "No installable packages found!" if not pkgs
  fail if rpc.addPackageInstall($clientId, [pkgs[0]["id"]], $chainLabel) < 1
end

When(/^I call actionchain\.addPackageRemoval\(\)$/) do
  pkgs = sysrpc.listAllInstallablePackages($clientId)
  fail if rpc.addPackageRemoval($clientId, [pkgs[0]["id"]], $chainLabel) < 1
end

When(/^I call actionchain\.addPackageUpgrade\(\)$/) do
  pkgs = sysrpc.listLatestUpgradablePackages($clientId)
  fail "No upgradable packages found!" if not pkgs
  fail if rpc.addPackageUpgrade($clientId, [pkgs[0]["to_package_id"]], $chainLabel) < 1
end

When(/^I call actionchain\.addPackageVerify\(\)$/) do
  pkgs = sysrpc.listAllInstallablePackages($clientId)
  fail "No verifiable packages found!" if not pkgs
  fail if rpc.addPackageVerify($clientId, [pkgs[0]["id"]], $chainLabel) < 1
end

# Manage actions within the action chain
When(/^I call actionchain\.removeAction on each action within the chain$/) do
  begin
    actions = rpc.listChainActions($chainLabel)
    fail if not actions
    for action in actions do
      fail if rpc.removeAction($chainLabel, action["id"]) < 0
      puts "\t- Removed \"" + action["label"] + "\" action"
    end
  rescue XMLRPC::FaultException => e
    fail "Error removeAction: XML-RPC failure, code %s: %s" % [e.faultCode, e.faultString]
  end
end

Then(/^I should be able to see that the current action chain is empty$/) do
  fail if rpc.listChainActions($chainLabel).length > 0
end


# Scheduling the action chain
When(/^I schedule the action chain$/) do
  fail if rpc.schedule($chainLabel, DateTime.now) < 0
end

Then(/^there should be no more my action chain$/) do
  fail if rpc.listChains().include?($chainLabel)
end

Then(/^I should see scheduled action, called "(.*?)"$/) do |label|
  found = false
  for action in scdrpc.listInProgressActions() do
    if action["name"] == label
      found = true
      break
    end
  end
  fail if not found
end

Then(/^I cancel all scheduled actions$/) do
  for action in scdrpc.listInProgressActions() do
    # One-by-one, this is against single call in the API on purpose.
    scdrpc.cancelActions([action["id"]])
    puts "\t- Removed \"" + action["name"] + "\" action"
  end
end

Then(/^there should be no more any scheduled actions$/) do
  fail if scdrpc.listInProgressActions().length > 0
end
