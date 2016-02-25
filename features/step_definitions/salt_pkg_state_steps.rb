When(/^I change the state for "(.*?)" to "(.*?)"$/) do |pkg, state|
  # Give a package name and state {"Unmanaged","Installed", "Removed"}
  within(:xpath, "//td[contains(text(), '#{pkg}')]/ancestor::tr") do   #[td[contains(text(), '#{architecture}')]])"  
  end
end

When(/^I list packages with "(.*?)"$/) do |str|
  find('input#package-search').set(str)
  find('button#search').click
end

When(/^I change the state of "([^"]*)" to "([^"]*)" and "([^"]*)"$/) do |pkg, state, instd_state|
  find("##{pkg}-pkg-state").select(state)
end

When(/^I click undo for "(.*?)"$/) do |pkg|
  find("button##{pkg}-undo").click
end

When(/^I click the$/) do
end
