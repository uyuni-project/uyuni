
When /^I refresh the metadata$/ do
   `rhn_check`
   fail if ! $?.success?
   client_refresh_metadata
end

Then /^I should have "([^"]*)" in the metadata$/ do |text|
   arch=`uname -m`
   arch.chomp!
   if arch != "x86_64"
     arch = "i586"
   end
   `zgrep #{text} #{client_raw_repodata_dir("sles11-sp1-updates-#{arch}-channel")}/primary.xml.gz`
   fail if ! $?.success?
end

