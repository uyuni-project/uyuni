# Copyright (c) 2019-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'cucumber/formatter/pretty'

# Pretty Formatter extension
module PrettyFormatterExtended
  PrettyFormatter ||= CustomFormatter::PrependsFeatureName.formatter(Cucumber::Formatter::Pretty)
end
