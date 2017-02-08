# COPYRIGHT 2017 SUSE LLC
require "xmlrpc/client"

scdrpc = XMLRPCScheduleTest.new(ENV['TESTHOST'])
scdrpc.login('admin', 'admin')

Then(/^there should be no failed scheduled actions$/) do
  assert_empty(scdrpc.listFailedActions)
end
