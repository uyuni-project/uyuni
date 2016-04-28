# Copyright (c) 2013 SUSE
# Licensed under the terms of the MIT license.

Then(/^I should see an alphabar link to this system$/) do
  within(:xpath, "//div[@class='spacewalk-list-alphabar']") do
    fail if not find_link("#{$myhostname[0,1].upcase}").visible?
  end
end

