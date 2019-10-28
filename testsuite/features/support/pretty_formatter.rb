# Copyright (c) 2013-2019 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'cucumber/formatter/pretty'

module CustomFormatter
  PrettyFormatter = PrependsFeatureName.formatter(Cucumber::Formatter::Pretty)
end
