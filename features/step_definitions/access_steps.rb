# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am not authorized$/ do
  visit Capybara.app_host
  fail if not find_button('Sign In').visible?
end

When /^I go to the home page$/ do
  visit Capybara.app_host
end

Given /^I access the host the first time$/ do
  visit Capybara.app_host
  #fail if not page.has_content?("Create Spacewalk Administrator")
  fail if not page.has_content?("Create SUSE Manager Administrator")
end

Then /^no link should be broken$/ do
#  require File.expand_path(File.join(File.dirname(__FILE__), "..", "support", "collect"))
  Capybara.default_wait_time = 1
  visit Capybara.app_host

  hrefs = collect_all_hrefs

  visited = Hash.new

  loop do
    href = hrefs.shift
    next if href.nil?
    base = href.split("?")[0]
    $stderr.puts "Visiting '#{href}' '#{base}', #{hrefs.size} to go"
    visit href.to_s
    if page.has_content?('Page Not Found') || page.has_content?('Internal Server Error') || page.has_content?('/var/www/html')
      visited[base] = href
      $stderr.puts "-- ** failed"
    else
      collect_all_hrefs.each do |href|
        next if href[0,1] == "#" # relative link
        next if hrefs.include?(href)
        hbase = href.split("?")[0]
        next if visited[hbase]
        visited[hbase] = true
        unless href[0,1] == "/"
#	      $stderr.puts "From #{href} (#{base})"
	      hsplit = base.split("/")
	      hsplit.pop
	      hsplit << href
	      href = hsplit.join("/")
#	      $stderr.puts "\t to #{href}"
	    end
#	    $stderr.puts "Adding #{href}"
	    hrefs << href
      end
    end
    break if hrefs.empty?
  end
  $stderr.puts "\nFinished. Visited #{visited.size} pages. Failed pages:"
  failed_pages = ""
  visited.each_value do |f|
    next if f.is_a? TrueClass
    failed_pages << "\t#{f}\n"
    $stderr.puts "\t#{f}"
  end
  $stderr.puts "End of failed pages"
  if ! failed_pages.empty?
    raise "Failed pages:\n#{failed_pages}"
  end
end
