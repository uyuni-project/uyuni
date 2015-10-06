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
  click_link('', :href => "/rhn/manager/minions/reject/#{$myhostname}")
end

When(/^I delete this client from the Rejected section$/) do
  click_link('', :href => "/rhn/manager/minions/delete/#{$myhostname}")
end

When(/^I accept this client's minion key$/) do
  click_link('', :href => "/rhn/manager/minions/accept/#{$myhostname}")
end
