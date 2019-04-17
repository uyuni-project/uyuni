# Copyright 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Before do |_scenario|
  current_time = Time.new
  @scenario_start_time = current_time.to_i
  puts "This scenario ran at: #{current_time} - #{@scenario_start_time - STARTTIME} seconds since start"
end

After do |_scenario|
  current_epoch = Time.new.to_i
  puts "This scenario took: #{current_epoch - @scenario_start_time} seconds"
end
