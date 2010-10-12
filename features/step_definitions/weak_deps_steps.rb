
When /^I refresh the metadata$/ do
   `rhn_check`
   fail if ! $?.success?
   `yum clean all`
   fail if ! $?.success?
   `yum makecache`
   fail if ! $?.success?
end

Then /^I should have "([^"]*)" in the metadata$/ do |text|
   `zgrep #{text} /var/cache/yum/test_base_channel/primary.xml.gz`
   fail if ! $?.success?
end

