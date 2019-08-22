# Copyright (c) 2013-2019 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'cucumber/formatter/pretty'

module CustomFormatter
  PrettyFormatter = PrependsFeatureName.formatter(Cucumber::Formatter::Pretty)
end
