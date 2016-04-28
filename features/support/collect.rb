# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
#
# collector helpers
#

def collect_all_hrefs
  hrefs = []
  testhost_url = "https://#{ENV['TESTHOST']}"
  all(:xpath, "//a[@href]").each do |a|
    href = a[:href].to_s
    #$stderr.puts "Find #{href}"
    if href.start_with?(testhost_url) # URL start with http but is local
      href.sub!(/^#{testhost_url}/, '')
    end
    next if href[0, 4] == "http" # skip absolute/outside links
    next if href.nil?
    next if href =~ %r{/rhn/Logout.do} # this will close our session
    next if href =~ %r{/rhn/help/dispatcher} # this will redirect to redhat.com
    next if href =~ %r{javascript:} # oops, javascript
    next if href =~ %r{mailto:} # oops, javascript
    next if href =~ %r{/download} # no downloads
    next if href =~ %r{/rhn/CSVDownloadAction.do} # this pops up a file dialog
    href = href.split("#")[0] if href.include?(".jsp#")
    #$stderr.puts "add #{href}"
    hrefs << href
  end
  hrefs.uniq.sort
end
