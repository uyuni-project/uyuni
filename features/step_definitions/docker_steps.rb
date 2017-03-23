# Copyright (c) 2017 Suse Linux
# Licensed under the terms of the MIT license.

require "xmlrpc/client"
require 'time'
require 'date'

# container_operations
cont_op = XMLRPCImageTest.new(ENV['TESTHOST'])
# retrieve minion id, needed for scheduleImageBuild call
def retrieve_minion_id
  sysrpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  sysrpc.login('admin', 'admin')
  systems = sysrpc.listSystems
  refute_nil(systems)
  minion_id = systems
              .select { |s| s['name'] == $minion_fullhostname }
              .map { |s| s['id'] }.first
  refute_nil(minion_id, "Minion #{$minion_fullhostname} is not yet registered?")
  minion_id
end

And(/^I select sle-minion hostname in Build Host$/) do
  select($minion_fullhostname, :from => 'host')
end

And(/^I navigate to images webpage$/) do
  visit("https://#{$server_fullhostname}/rhn/manager/cm/images")
end

And(/^I navigate to images build webpage$/) do
  visit("https://#{$server_fullhostname}/rhn/manager/cm/build")
end

And(/^I verify that all container images were built correctly in the gui$/) do
  for c in 0..20
      raise "error detected while building images" if has_xpath?("//*[contains(@title, 'Failed')]")
      if has_xpath?("//*[contains(@title, 'Built')]", :count => 4)
          break
      end
      sleep 15
      step %(I navigate to images webpage)
  end
  raise "an image was not built correctly" unless has_xpath?("//*[contains(@title, 'Built')]", :count => 4)
end

And(/^I schedule the build of image "([^"]*)" via xmlrpc-call$/) do |image|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = ''
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.scheduleImageBuild(image, version_build, build_hostid, date_build)
end

And(/^I schedule the build of image "([^"]*)" with version "([^"]*)" via xmlrpc-call$/) do |image, version|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = version
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.scheduleImageBuild(image, version_build, build_hostid, date_build)
end
