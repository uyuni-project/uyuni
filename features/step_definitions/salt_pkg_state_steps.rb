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
  # state is Installed + {Any, Latest}, Removed or Unmanaged  instd_state is not required
  # but must an emptry string must be given.
  within('#milkyway-dummy-row') do
    #select(state, :from => "#{pkg}-pkg-state")
    find(:xpath, ".//option[contains(text(), #{state})]").click
    if ! instd_state.to_s.empty?
      #select(instd_state, :from => "#{pkg}-version-constraint")
    end
    within('button#changes') do
    end
  end
end

When(/^I click undo for "(.*?)"$/) do |pkg|
  find("button#{pkg}-undo").click
end

When(/^I click the$/) do
end
