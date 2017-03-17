# Copyright (c) 2017 Suse Linux
# Licensed under the terms of the MIT license.

And(/^I select sle-minion hostname in Build Host$/) do
  select($minion_fullhostname, :from => 'host')
end

And(/^I navigate to images webpage$/) do
  visit("https://#{$server_fullhostname}/rhn/manager/cm/images")
end

And(/^I navigate to images build webpage$/) do
  visit("https://#{$server_fullhostname}/rhn/manager/cm/build")
end
