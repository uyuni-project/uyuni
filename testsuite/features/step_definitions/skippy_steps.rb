# Copyright (c) 2010-2018 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Texts and links
#

Given(/^the "([^"]*)" is a SLE-"(.*?)" client$/) do |target, version|
  node = get_target(target)
  os_version = get_os_version(node)
  skip_this_scenario unless os_version.include? version
end
