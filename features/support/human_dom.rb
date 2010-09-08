
def element_for(desc)
  case desc
  when /left menu/ then "sidenav"
  when /tab bar|tabs/ then "mainNavWrap"
  else raise "Unknown element with description '#{desc}'"
  end
end
