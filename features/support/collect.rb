#
# collector helpers
#

  def collect_all_hrefs
    hrefs = []
    all(:xpath, "//a[@href]").each do |a|
      #      $stderr.puts "#{a[:href]}"
      href = a[:href].to_s
      next if href[0,4] == "http" # skip absolute/outside links
      hrefs << href
    end
    hrefs.uniq.sort
  end



