# COPYRIGHT 2015 SUSE LLC

rpctest = XMLRPCUserTest.new(ENV["TESTHOST"])
users = nil
roles = nil
password = "die gurke"

Given /^I am logged in via XML\-RPC\/user as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  rpctest.login(luser, password)
end

When /^I call user\.listUsers\(\)$/ do
  users = rpctest.getUserIds()
end

Then /^I should get at least user "([^"]*)"$/ do |luser|
  seen = false
  for user in users
    if luser == user['login']
      seen = true
      break
    end
  end

  fail if not seen
end

When /^I call user\.getDetails\(\) on user "([^"]*)"$/ do |luser|
  roles = rpctest.getUserRoles(luser)
end

Then /^I should see at least one role that matches "([^"]*)" suffix$/ do |sfx|
  fail if roles.find_all{|el| el =~ /#{sfx}/}.length < 1
end

When /^I call user\.create\(sid, login, pwd, name, lastname, email\) with login "([^"]*)"$/ do |luser|
  fail if rpctest.createUser(luser, password) != 1
end

Then /^when I call user\.listUsers\(\), I should see a user "([^"]*)"$/ do |luser|
  users = rpctest.getUserIds()
  seen = false
  for user in users
    if luser == user['login']
      seen = true
      break
    end
  end

  fail if not seen
end

When /^I call user\.addRole\(\) on "([^"]*)" with the role "([^"]*)"$/ do |luser, role|
  fail if rpctest.addRole(luser, role) != 1
end

Then /^I should see "([^"]*)" when I call user\.listRoles\(\) with "([^"]*)"$/ do |rolename, luser|
  passed = false
  roles = rpctest.getUserRoles(luser)
  for role in roles
    if role == rolename
      passed = true
    end
  end
  fail if not passed
end

Then /^I logout from XML\-RPC\/user namespace\.$/ do
  fail if not rpctest.logout()
end

When /^I delete user "([^"]*)"$/ do |luser|
  rpctest.deleteUser(luser)
end

Given /^I make sure "([^"]*)" is not present$/ do |luser|
  users = rpctest.getUserIds()
  for user in users
    if luser == user['login']
      rpctest.deleteUser(luser)
      break
    end
  end
end

When /^I call user\.removeRole\(\) against uid "([^"]*)" with the role "([^"]*)"$/ do |luser, rolename|
  fail if rpctest.delRole(luser, rolename) != 1
end

Then /^I shall not see "([^"]*)" when I call user\.listRoles\(\) with "([^"]*)" uid$/ do |rolename, luser|
  roles = rpctest.getUserRoles(luser)
  fail if roles != nil ? roles.length != 0 : false
end
