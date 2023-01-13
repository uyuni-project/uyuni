# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.

module CustomFormatter
  # Extend the Cucumber Pretty Formatter and prepends the feature name on each step
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
    # It takes the keyword, prepends the feature name to it, and then calls the super function.
    #
    # Args:
    #   keyword: The keyword of the step (Given, When, Then, etc).
    #   step_match: The step match object.
    #   status: The status, which can be: passed, failed, undefined, skipped, pending, exception.
    #   source_indent: The indentation of the step definition.
    #   background: Whether the step is running in the background or not.
    #   file_colon_line: "features/step_definitions/my_steps.rb:7"
    def step_name(keyword, step_match, status, source_indent, background, file_colon_line)
      keyword_changed = prepend_feature_name_to(keyword)
      super(keyword_changed, step_match, status, source_indent, background, file_colon_line)
    end

    ##
    # It takes a string as an argument and returns a new string that is the original string prepended with the current
    # feature name
    #
    # Args:
    #   text: The text to be prepended with the feature name
    def prepend_feature_name_to(text)
      "[#{@current_feature_name}] #{text}"
    end
  end
end
