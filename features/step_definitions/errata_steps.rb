Given /^I am on the errata page$/ do
  Given "I am authorized"
  within(:xpath, "//div[@id=\"mainNavWrap\"]") do
    find_link("Errata").click
  end
end
                                                                                                                                                       
Given /^I am on the "([^"]*)" errata Details page$/ do |arg1| 
  Given "I am on the errata page"
    And "I follow \"All\" in the left menu" 
    And "I follow \"#{arg1}\""
end
                                                                                                                                                       
