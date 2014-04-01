# Copyright 2011-2014 Novell

When /^I make the credentials primary$/ do
   fail if not find('i.fa-star-o').click
end


When /^I delete the primary credentials$/ do
   fail if not find('i.fa-trash-o', :match => :first).click
   step 'I click on "Delete"'
end

When /^I view the primary subscription list$/ do
   fail if not find('i.fa-th-list', :match => :first).click
end

When /^I click on "([^"]*)" link in the setup wizard$/ do |arg1|
  tag = case arg1
  when /Edit/ then "i.fa-pencil"
  when /List/ then "i.fa-th-list"
  when /Verify/ then "i.fa-check-square"
  else raise "Unknown element"
  end
  within(".text-left") do
     fail if not find(tag).click
  end
end


