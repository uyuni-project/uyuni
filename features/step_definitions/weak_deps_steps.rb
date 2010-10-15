
When /^I refresh the metadata$/ do
   `rhn_check`
   fail if ! $?.success?
   `yum clean all`
   fail if ! $?.success?
   `yum makecache`
   fail if ! $?.success?
end

Then /^I should have "([^"]*)" in the metadata$/ do |text|
   arch=`uname -m`
   arch.chomp!
   if arch != "x86_64"
     arch = "i586"
   end
   `zgrep #{text} /var/cache/yum/sles11-sp1-updates-#{arch}-channel/primary.xml.gz`
   fail if ! $?.success?
end

