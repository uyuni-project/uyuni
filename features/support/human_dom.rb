# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

def element_for(desc)
  case desc
  when /left menu/ then "sidenav"
  when /tab bar|tabs/ then "mainNavWrap"
  else raise "Unknown element with description '#{desc}'"
  end
end
