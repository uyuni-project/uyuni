When(/^I check the ram value$/) do
   ram_value = `grep MemTotal /proc/meminfo |awk '{print $2}'`
   ram_mb = ram_value.to_i / 1024
   step %(I should see a "#{ram_mb}" text)
end

When(/^I check the MAC address value$/) do
   mac_address = `ifconfig | grep eth0 | awk '{print $5}'`
   mac_address.downcase!
   step %(I should see a "#{mac_address}" text)
end

Then(/^I should see the CPU frequency of the client$/) do
   cpu_freq = `cat /proc/cpuinfo  | grep MHz | awk '{print $4}'`
   step %(I should see a "#{cpu_freq.to_i/1000} GHz" text)
end
