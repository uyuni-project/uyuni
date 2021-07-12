# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.

# Cucumber custom formatter
module CustomFormatter
  # Extend the Cucumber Pretty Formatter and prepends the feature name on each step
  module PrependsFeatureName
    # Initialize the formatter including the custom module
    def self.formatter(formatter_class)
      Class.new(formatter_class) { include PrependsFeatureName }
    end

    # Store the feature filename in a class variable
    def before_feature(feature)
      @current_feature_name = feature.location.file
      super(feature)
    end

    # Overload the step_name method prepending the filename of the feature
    def step_name(keyword, step_match, status, source_indent, background, file_colon_line)
      keyword_changed = prepend_feature_name_to(keyword)
      super(keyword_changed, step_match, status, source_indent, background, file_colon_line)
    end

    # Prepends the feature filename in a text
    def prepend_feature_name_to(text)
      "[#{@current_feature_name}] #{text}"
    end
  end
end
