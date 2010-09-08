When /^I click on "([^"]+)" for "([^"]+)"$/ do |arg1, arg2|
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'#{arg2}')]]") do
    find_link(arg1).click
  end
end


