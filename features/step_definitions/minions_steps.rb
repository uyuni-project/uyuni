def click_n_refresh(link)
  fail if not page.has_xpath?("//a[@href='#{link}']")
  find('#pending-list').find(:xpath, "//a[@href='#{link}']").click
  sleep(5)
  visit "/rhn/YourRhn.do"
  visit "/rhn/manager/minions"
end

Then(/^I should see a this client as a minion in the Pending section$/) do
  fail if not find('#pending-list').find("b", :text => @this_client_hostname).visible?
end

When(/^I accept minion$/) do
  click_n_refresh("/rhn/manager/minions/accept/#{@this_client_hostname}")
end

Then(/^I should see a this client as a minion in the Accepted section$/) do
  fail if not page.find('#accepted-list').find("b", :text => @this_client_hostname).visible?
end

When(/^I see the contents of the minion$/) do
  visit "/rhn/manager/minions/#{@this_client_hostname}"
end

# Accepted removal
When(/^I delete this client as a minion from the Accepted section$/) do
  click_n_refresh("/rhn/manager/minions/delete/#{@this_client_hostname}")
end

Then(/^I should not see this client as a minion anywhere$/) do
  find('#accepted-list').all('b').each do |el|
    fail if el.text.include? @this_client_hostname
  end
end

# Reject pending minion
When(/^I restart a minion of this client$/) do
  system("rcsalt-minion restart")
end

When(/^I refresh "(.*?)"$/) do |arg1|
  visit "/rhn/YourRhn.do"
  visit "/rhn/manager/minions"
end

When(/^I reject this client as a minion from the Pending section$/) do
  click_n_refresh("/rhn/manager/minions/reject/#{@this_client_hostname}")
end

Then(/^I should see this client as a minion in the Rejected section$/) do
  fail if not find('#rejected-list').find("b", :text => @this_client_hostname).visible?
end

# Rejected removal
When(/^I delete this client as a minion from the Rejected section$/) do
  click_n_refresh("/rhn/manager/minions/delete/#{@this_client_hostname}")
end
