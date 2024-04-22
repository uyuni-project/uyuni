# Copyright (c) 2019-2023 SUSE LLC
# Licensed under the terms of the MIT license.

# CustomFormatter module provides a custom formatter for Cucumber tests.
# It extends the Cucumber Pretty Formatter and prepends the feature name on each step.
module CustomFormatter
  # PrependsFeatureName module is included in the custom formatter class.
  # It overrides some methods to modify the behavior of the formatter.
  module PrependsFeatureName
    ##
    # A class method that takes a formatter class as an argument and returns a new class
    # that includes the PrependsFeatureName module.
    #
    # @param formatter_class [Class] The formatter class to be extended.
    # @return [Class] The new class that includes the PrependsFeatureName module.
    def self.formatter(formatter_class)
      Class.new(formatter_class) { include PrependsFeatureName }
    end

    ##
    # This method is called before each feature is executed.
    # It takes the feature object as an argument and stores the feature file name in the @current_feature_name instance variable.
    #
    # @param feature [Cucumber::Core::Ast::Feature] The feature object.
    def before_feature(feature)
      @current_feature_name = feature.location.file
      super(feature)
    end

    ##
    # This method is called for each step in the feature.
    # It overrides the step name value by prepending the feature name.
    #
    # @param keyword [String] The keyword of the step (Given, When, Then, etc).
    # @param step_match [Cucumber::Core::Test::StepMatch] Represents the match found between a Test Step and its activation.
    # @param status [Symbol] The status of the step, which can be: passed, failed, undefined, skipped, pending, exception.
    # @param source_indent [String] The source code of the step definition indented.
    # @param background [Cucumber::Core::Ast::Background] The background object in case this step is part of a background.
    # @param file_colon_line [String] The line number where the step is defined.
    def step_name(keyword, step_match, status, source_indent, background, file_colon_line)
      keyword_changed = prepend_feature_name_to(keyword)
      super(keyword_changed, step_match, status, source_indent, background, file_colon_line)
    end

    ##
    # This method prepends the feature name to the given text.
    #
    # @param text [String] The text to be prepended with the feature name.
    # @return [String] The modified text with the feature name prepended.
    def prepend_feature_name_to(text)
      "[#{@current_feature_name}] #{text}"
    end
  end
end
