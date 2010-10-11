
When /^I refresh the metadata$/ do
   `yum clean all`
   fail if ! $?.success?
   `yum makecache`
   fail if ! $?.success?
end

Then /^I should have "([^"]*)" in the metadata$/ do |text|
   `zgrep #{text} /var/cache/yum/testbasechannel/primary.xml.gz`
   fail if ! $?.success?
end

