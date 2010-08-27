Then /^I should see something$/ do
  page.should have_content('Sign In')
  page.should have_content('About')
end
      
Then /^I should see a "([^"]*)" text$/ do |arg1|                                                                                                                                                               
  page.should have_content(arg1)
end                                                                                                                                                                                                            
                                                                                                                                                                                                               
Then /^I should see a "([^"]*)" link$/ do |arg1|                                                                                                                                                               
  find_link(arg1).visible?
end                                                                                                                                                                                                            


