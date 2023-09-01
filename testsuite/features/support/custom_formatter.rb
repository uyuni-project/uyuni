# Copyright (c) 2019-2023 SUSE LLC
# Licensed under the terms of the MIT license.

# Extend the Cucumber Pretty Formatter and prepends the feature name on each step
module CustomFormatter
  # Module that prepends the feature name into the step name
  module PrependsFeatureName
    # A class method that takes a formatter class as an argument.
    def self.formatter(formatter_class)
      Class.new(formatter_class) { include PrependsFeatureName }
    end

    ##
    # It takes the feature file name and stores it in a variable called @current_feature_name.
    #
    # Args:
    #   feature: The feature object.
    def before_feature(feature)
      @current_feature_name = feature.location.file
      super(feature)
    end

    ##
    # This method override the step name value by prepending the feature name. This helps to identify each step in the console output when we run features in parallel.
    #
    # Args:
    #   keyword: The keyword of the step (Given, When, Then, etc).
    #   step_match: Represents the match found between a Test Step and its activation
    #   status: The status, which can be: passed, failed, undefined, skipped, pending, exception.
    #   source_indent: The source code of the step definition indented
    #   background: The background object in case this step is part of a background
    #   file_colon_line: The line number where the step is defined
    def step_name(keyword, step_match, status, source_indent, background, file_colon_line)
      keyword_changed = prepend_feature_name_to(keyword)
      super(keyword_changed, step_match, status, source_indent, background, file_colon_line)
    end

    ##
    # Adds a given text after the feature name.
    #
    # Args:
    #   text: The text to be prepended with the feature name.
    def prepend_feature_name_to(text)
      "[#{@current_feature_name}] #{text}"
    end
  end
end
