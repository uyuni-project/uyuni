
Then /^I should see something$/ do
  fail if not page.has_content?('Sign In')
  fail if not page.has_content?('About')
end

