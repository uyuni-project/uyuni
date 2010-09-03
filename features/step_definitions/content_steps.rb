Then /^I should see something$/ do
  assert page.has_content?('Sign In')
  assert page.has_content?('About')
end

