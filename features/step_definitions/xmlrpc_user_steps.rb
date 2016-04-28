# COPYRIGHT 2015 SUSE LLC
CREATE_USER_PASSWORD = "die gurke"

Given(/^I am logged in via XML\-RPC\/user as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpc = XMLRPCUserTest.new(ENV["TESTHOST"])
  @rpc.login(luser, password)
end

When(/^I call user\.listUsers\(\)$/) do
  @users = @rpc.getUsers()
end

Then(/^I should get at least user "([^"]*)"$/) do |luser|
  assert_includes(@users.map { |u| u['login'] }, luser)
end

When(/^I call user\.getDetails\(\) on user "([^"]*)"$/) do |luser|
  @roles = @rpc.getUserRoles(luser)
end

Then(/^I should see at least one role that matches "([^"]*)" suffix$/) do |sfx|
  refute(@roles.find_all{ |el| el =~ /#{sfx}/ }.length < 1)
end

When(/^I call user\.create\(sid, login, pwd, name, lastname, email\) with login "([^"]*)"$/) do |luser|
  refute(@rpc.createUser(luser, CREATE_USER_PASSWORD) != 1)
end

Then(/^when I call user\.listUsers\(\), I should see a user "([^"]*)"$/) do |luser|
  steps %{
    When I call user.listUsers()
    Then I should get at least user "#{luser}"
  }
end

When(/^I call user\.addRole\(\) on "([^"]*)" with the role "([^"]*)"$/) do |luser, role|
  refute(@rpc.addRole(luser, role) != 1)
end

Then(/^I should see "([^"]*)" when I call user\.listRoles\(\) with "([^"]*)"$/) do |rolename, luser|
  assert_includes(@rpc.getUserRoles(luser), rolename)
end

Then(/^I logout from XML\-RPC\/user namespace\.$/) do
  assert(@rpc.logout())
end

When(/^I delete user "([^"]*)"$/) do |luser|
  @rpc.deleteUser(luser)
end

Given(/^I make sure "([^"]*)" is not present$/) do |luser|
  @rpc.getUsers()
    .map { |u| u['login'] }
    .select { |l| l == luser }
    .each { |l| @rpc.deleteUser(luser) }
end

When(/^I call user\.removeRole\(\) against uid "([^"]*)" with the role "([^"]*)"$/) do |luser, rolename|
  refute(@rpc.delRole(luser, rolename) != 1)
end

Then(/^I shall not see "([^"]*)" when I call user\.listRoles\(\) with "([^"]*)" uid$/) do |rolename, luser|
  refute_includes(@rpc.getUserRoles(luser), rolename)
end
