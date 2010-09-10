Given /^I am on the Users page$/ do
  Given 'I am authorized as "admin" with password "admin"'
  And 'I follow "Users"'
end

Given /^I am on the Details page$/ do
  Given "I am on the Users page"
  And 'I follow "user1"'
end
