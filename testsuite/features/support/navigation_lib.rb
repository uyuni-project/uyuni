# Copyright (c) 2013-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

# Navigation Lib module includes a bunch of useful methods of our navigation step definitions
module NavigationLib
  # get registration URL
  # the URL depends on whether we use a proxy or not
  def registration_url
    if $proxy.nil?
      "https://#{$server.ip}/XMLRPC"
    else
      "https://#{$proxy.ip}/XMLRPC"
    end
  end

  # Wraps a click button and wait using Capybara
  def click_button_and_wait(locator = nil, **options)
    click_button(locator, options)
    begin
      raise(Timeout::Error, 'Timeout: Waiting AJAX transition (click link)') unless has_no_css?('.senna-loading', wait: 5)
    rescue StandardError, Capybara::ExpectationNotMet => e
      # Skip errors related to .senna-loading element
      puts(e.message)
    end
  end

  # Wraps a click link and wait using Capybara
  def click_link_and_wait(locator = nil, **options)
    click_link(locator, options)
    begin
      raise(Timeout::Error, 'Timeout: Waiting AJAX transition (click link)') unless has_no_css?('.senna-loading', wait: 5)
    rescue StandardError, Capybara::ExpectationNotMet => e
      # Skip errors related to .senna-loading element
      puts(e.message)
    end
  end

  # Wraps a click link or button and wait using Capybara
  def click_link_or_button_and_wait(locator = nil, **options)
    click_link_or_button(locator, options)
    begin
      raise(Timeout::Error, 'Timeout: Waiting AJAX transition (click link)') unless has_no_css?('.senna-loading', wait: 5)
    rescue StandardError, Capybara::ExpectationNotMet => e
      # Skip errors related to .senna-loading element
      puts(e.message)
    end
  end

  # Wraps a find and click in Capybara
  def find_and_wait_click(*args, **options, &optional_filter_block)
    element = find(*args, options, &optional_filter_block)
    element.extend(CapybaraNodeElementExtension)
  end
end
