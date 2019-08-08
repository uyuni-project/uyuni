# Copyright (c) 2013-2019 Novell, Inc.
# Licensed under the terms of the MIT license.
module CustomFormatter
  # Extend the Cucumber Pretty Formatter and prepends the feature name on each step
  module PrependsFeatureName
    def self.formatter(formatter_class)
      Class.new(formatter_class) { include PrependsFeatureName }
    end

    def before_feature(feature)
      @current_feature_name = feature.location.filepath.filename
      super(feature)
    end

    def step_name(keyword, step_match, status, source_indent, background, file_colon_line)
      keyword_changed = prepend_feature_name_to(keyword)
      super(keyword_changed, step_match, status, source_indent, background, file_colon_line)
    end

    def prepend_feature_name_to(text)
      "[#{@current_feature_name}] #{text}"
    end
  end
end
