When /^I check the ram value$/ do
   ram_value = `grep MemTotal /proc/meminfo |awk '{print $2}'`
   ram_MB = ram_value.to_i / 1024
   step "I should see a \"#{ram_MB}\" text"
end
