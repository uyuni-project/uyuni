# Copyright (c) 2013-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

# Capybara Node Element extension to override click method, clicking and then waiting for ajax transition
module CapybaraNodeElementExtension
  # Overload the click method
  def click
    super
    begin
      raise(ScriptError, 'Timeout: Waiting AJAX transition (click link)') unless has_no_css?('.senna-loading', wait: 5)
    rescue StandardError, Capybara::ExpectationNotMet => e
      # Skip errors related to .senna-loading element
      puts(e.message)
    end
  end
end
