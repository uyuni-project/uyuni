# Copyright (c) 2017 Suse Linux
# Licensed under the terms of the MIT license.

And(/^I select sle-minion hostname in Build Host$/) do
  select($minion_fullhostname, :from => 'host')
end
