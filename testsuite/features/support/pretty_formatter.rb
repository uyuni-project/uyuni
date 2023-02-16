# Copyright (c) 2013-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'cucumber/formatter/pretty'

# CustomFormatter module
module CustomFormatter
  PrettyFormatter = PrependsFeatureName.formatter(Cucumber::Formatter::Pretty)
end
