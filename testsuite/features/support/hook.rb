# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

Before('@slow') do
  @slow_feature = true
end

AfterStep do
  if @slow_feature
    sleep 2 # Sleep for 2 seconds after each step
  end
end