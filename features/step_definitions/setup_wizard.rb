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

When /^I select SLES SP3 VMWare as a product$/ do
   within(:xpath, "//tr[td[position()=2 and contains(text(), 'SUSE Linux Enterprise Server 11 SP3 VMWare')] and td[position()=3 and contains(text(), 'x86_64')]][1]") do
      fail if not find('i.start-sync-icon').click
   end
end

When /^I verify the product was added$/ do 
   $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-ncc-sync -l` 
   fail if not $sshout.include? '[P] sles11-sp3-vmware-pool-x86_64'
end  

When /^I select the product Webyast 1.3$/ do
   within(:xpath, "//tr[td[position()=2 and contains(text(), 'SUSE Linux Enterprise Server 11 SP3 VMWare')] and td[position()=3 and contains(text(), 'x86_64')]][1]/following-sibling::tr[1]") do
      fail if not find("input#select-single").click
   end
end

When /^I sync the repos$/ do
   fail if not find("button#synchronize").click
end

When /^the product should appear in the output of mgr-ncc-sync$/ do
   $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-ncc-sync -l`
   fail if not $sshout.include? '[P] sle11-sp2-webyast-1.3-pool-x86_64-vmware-sp3'
   fail if not $sshout.include? 'sle11-sp2-webyast-1.3-updates-x86_64-vmware-sp3'
end

