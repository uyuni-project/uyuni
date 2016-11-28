# Copyright 2016 (c) SUSE LLC

When(/^I enter the salt state$/) do |multiline|
  within(:xpath, "//section") do
    x = find('textarea[name="content"]')
    x.set(multiline) # find("#{arg1}") #.set(lines)
  end
end

When(/^I click on the css "(.*)"$/) do |css|
  find(css).click
end

When(/^I enter "(.*)" in the css "(.*)"$/) do |input, css|
  find(css).set(input)
end

When(/^I select the state "(.*)"$/) do |state|
  find("input##{state}-cbox").click
end

When(/^I wait for the file "(.*)"$/) do |file|
  # Wait 60 seconds for file to appear
  60.times do
    break if file_exist($minion, file)
    sleep 1
  end
  fail unless file_exist($minion, file)
end
