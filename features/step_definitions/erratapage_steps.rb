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

