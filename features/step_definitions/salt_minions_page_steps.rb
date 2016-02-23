# Verify content
Then(/^I should see this client in the Pending section$/) do
  fail if not find('#pending-list').find("b", :text => $myhostname).visible?
end

Then(/^I should see this client in the Rejected section$/) do
  fail if not find('#rejected-list').find("b", :text => $myhostname).visible?
end

Then(/^I should not see this client as a Minion anywhere$/) do
  step %[I should not see a "#{$myhostname}" text]
end

# Perform actions
When(/^I reject this client from the Pending section$/) do
  find("button[formaction='/rhn/manager/minions/#{$myhostname}/reject']").click
end

When(/^I delete this client from the Rejected section$/) do
  find("button[formaction='/rhn/manager/minions/#{$myhostname}/delete']").click
end

When(/^I accept this client's minion key$/) do
  find("button[formaction='/rhn/manager/minions/#{$myhostname}/accept']").click
end

When(/^I go to the minion onboarding page$/) do
  steps %{
    And I follow "Systems"
    And I follow "Salt Master"
    And I follow "Onboarding"
  }
end
