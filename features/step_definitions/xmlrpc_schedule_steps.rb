# COPYRIGHT 2017 SUSE LLC
require "xmlrpc/client"
scdrpc = XMLRPCScheduleTest.new(ENV['TESTHOST'])

Then(/^there should be no failed scheduled actions$/) do
  scdrpc.login('admin', 'admin')
  assert_empty(scdrpc.listFailedActions)
end
