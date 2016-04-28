When(/^I check radio button "(.*?)"$/) do |arg1|
   fail unless choose(arg1)
end

When(/^I open the action chain box$/) do
   fail unless find('#s2id_action-chain span.select2-arrow').click
end

When(/^I enter "(.*?)" in action-chain$/) do |arg1|
   find('#select2-drop input.select2-input').set(arg1)
end

When(/^I enter as remote command this script in$/) do |multiline|
 #  lines = arg1.strip.split('\n')
   within(:xpath, "//section") do
      x = find('textarea#fSptInput')
      x.set(multiline) # find("#{arg1}") #.set(lines)
   end
end
