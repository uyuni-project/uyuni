Given /^I am on the Errata page$/ do
  Given "I am authorized"
  within(:xpath, "//div[@id=\"mainNavWrap\"]") do
    find_link("Errata").click
  end
end

When /^I check test channel$/ do
  within(:xpath, "//form/table/tbody/tr[contains(.,'Test Base Channel')]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

When /^I check test erratum$/ do
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'Test Advisory')]]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

When /^I search for erratum$/ do
  within(:xpath, "//form/div/div/table/tbody/tr[contains(.,'Search For')]") do
    fill_in "search_string", :with => "Test"
    click_button "Search"
  end
end
