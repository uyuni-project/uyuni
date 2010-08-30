Then /^I should see something$/ do
  page.should have_content('Sign In')
  page.should have_content('About')
end



