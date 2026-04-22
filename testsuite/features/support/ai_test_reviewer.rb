# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

require_relative 'ai_test_reviewer/failtale_backend'

# Entry point for AI-powered post-failure scenario reviews.
#
# Orchestrates reviewer enablement checks, run limits, and backend selection.
module AITestReviewer
  module_function

  # Runs a review for a failed scenario when the reviewer is enabled.
  #
  # @param scenario [Object] Cucumber scenario object for the failed run
  # @param screenshot_path [String, nil] path to the screenshot captured on failure
  # @return [void]
  def review!(scenario, screenshot_path)
    return unless enabled?

    $ai_test_reviewer_runs ||= 0
    max_runs = Integer(ENV.fetch('AI_TEST_REVIEWER_MAX_RUNS', '5'))
    if $ai_test_reviewer_runs >= max_runs
      warn "[AI Test Reviewer] Skipping: max runs reached (#{max_runs})"
      return
    end

    $ai_test_reviewer_runs += 1
    result = backend.review(scenario: scenario, screenshot_path: screenshot_path)
    result.to_s
  rescue StandardError => e
    warn "[AI Test Reviewer] Review failed: #{e.class}: #{e.message}"
    nil
  end

  # Returns whether AI test review is enabled via environment variables.
  #
  # Accepted truthy values are: `1`, `true`, `yes`, `on`.
  #
  # @return [Boolean] true when reviewer execution is enabled
  def enabled?
    value = ENV.fetch('AI_TEST_REVIEWER_ENABLED', 'false').downcase
    %w[1 true yes on].include?(value)
  end

  # Builds the configured backend implementation for AI review.
  #
  # @return [BaseBackend] backend instance configured by `AI_TEST_REVIEWER_BACKEND`
  # @raise [ArgumentError] when the configured backend name is not supported
  def backend
    case ENV.fetch('AI_TEST_REVIEWER_BACKEND', 'failtale').downcase
    when 'failtale'
      FailTaleBackend.new
    else
      raise ArgumentError, "Unsupported AI reviewer backend: #{ENV.fetch('AI_TEST_REVIEWER_BACKEND', nil)}"
    end
  end
end
