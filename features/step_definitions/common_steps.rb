#
# Sleep for X seconds
#
When /^I wait for "(\d+)" seconds$/ do |arg1|
  sleep(arg1.to_i)
end
