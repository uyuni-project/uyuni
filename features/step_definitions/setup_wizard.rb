# Copyright 2011-2014 Novell

When /^I make the credentials primary$/ do
   fail if not find('i.fa-star-o').click
end


When /^I delete the primary credentials$/ do
   fail if not find('i.fa-trash-o', :match => :first).click
   step 'I click on "Delete"'
end

When /^I view the primary subscription list$/ do
   fail if not find('i.fa-th-list', :match => :first).click
end

When /^I click on "([^"]*)" link in the setup wizard$/ do |arg1|
  tag = case arg1
  when /Edit/ then "i.fa-pencil"
  when /List/ then "i.fa-th-list"
  when /Verify/ then "i.fa-check-square"
  else raise "Unknown element"
  end
  within(".text-left") do
     fail if not find(tag).click
  end
end

When /^I select "([^\"]*)" as a product for the "([^\"]*)" architecture$/ do |product, architecture|
   within(:xpath, "//span[contains(text(), '#{product}')]/ancestor::tr[td[contains(text(), '#{architecture}')]]") do
      fail if not find("input[type='checkbox']").click
   end
end

When /^I click the Add Product button$/ do
   fail if not find("button#synchronize").click
end

When /^I wait until it has finished$/ do
   find("button#synchronize .fa-plus")
end

When /^I verify the products were added$/ do 
   $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-ncc-sync -l` 
   fail if not $sshout.include? '[P] sles11-sp3-vmware-pool-x86_64'
   fail if not $sshout.include? '[P] sle11-sp2-webyast-1.3-pool-x86_64-vmware-sp3'
   fail if not $sshout.include? '[P] sle11-sp2-webyast-1.3-updates-x86_64-vmware-sp3'
end

When(/^I click the channel list of product "(.*?)" for the "(.*?)" architecture$/) do |product, architecture|
   within(:xpath, "//span[contains(text(), '#{product}')]/ancestor::tr[td[contains(text(), '#{architecture}')]]") do
      fail if not find('.product-channels-btn').click
   end
end
