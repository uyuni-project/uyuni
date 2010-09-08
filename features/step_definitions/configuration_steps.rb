#
# Initial step for channel testing
#
Given /^I am testing configuration$/ do
  Given 'I am authorized as "admin" with password "admin"'
  Given "I follow \"Configuration\""
end
