Then(/^I should see a this client as a minion in the Pending section$/) do
  fail if not find('#pending-list').find("b", :text => @this_client_hostname).visible?
end

When(/^when I accept minion$/) do
  link = "/rhn/manager/minions/accept/#{@this_client_hostname}"
  fail if not page.has_xpath?("//a[@href='#{link}']")
  find('#pending-list').find(:xpath, "//a[@href='#{link}']").click
  sleep(5)
  visit "/rhn/YourRhn.do"
  visit "/rhn/manager/minions"
end

Then(/^I should see a this client as a minion in the Accepted section$/) do
  fail if not page.find('#accepted-list').find("b", :text => @this_client_hostname).visible?
end

When(/^when I see the contents of the minion$/) do
  visit "/rhn/manager/minions/#{@this_client_hostname}"
end
