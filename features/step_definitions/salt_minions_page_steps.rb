# Verify content
Then(/^I should see this client in the Pending section$/) do
  fail if !find('#pending-list').find("b", :text => $myhostname).visible?
end

Then(/^I should see this client in the Rejected section$/) do
  fail if !find('#rejected-list').find("b", :text => $myhostname).visible?
end

Then(/^I should not see this client as a Minion anywhere$/) do
  step %(I should not see a "#{$myhostname}" text)
end

# Perform actions
When(/^I reject this client from the Pending section$/) do
  find("button[title='reject']").click
end

When(/^I delete this client from the Rejected section$/) do
  find("button[title='delete']").click
end

When(/^I see my fingerprint$/) do
  output = `salt-call --local key.finger`
  fing = output.split("\n")[1].strip!

  fail if !page.has_content?(fing)
end

When(/^I accept this client's minion key$/) do
  find("button[title='accept']").click
end

When(/^I go to the minion onboarding page$/) do
  steps %(
    And I follow "Salt"
    )
end

When(/^I should see this hostname as text$/) do
  within('#spacewalk-content') do
    fail if !page.has_content?($myhostname)
  end
end

When(/^I should see a "(.*)" text in the content area$/) do |txt|
  within('#spacewalk-content') do
    fail if !page.has_content?(txt)
  end
end
