Then(/^I should see this client as a registered system$/) do
  step %[I should see a "#{@this_client_hostname}" text]
end

Then(/^I follow this client as a registered system$/) do
  step %[I follow "#{@this_client_hostname}"]
end

Then(/^I press "(.*?)"$/) do |btn_title|
  click_button(btn_title)
end
