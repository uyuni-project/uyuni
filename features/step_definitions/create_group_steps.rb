
Given /^I am on the groups page$/ do
    Given "I am on the Systems page"
    Given "I follow \"System Groups\" in \"sidenav\""
end

When /^I check this client$/ do
  hostname = `hostname`
  hostname.chomp!
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'#{hostname}')]]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end
