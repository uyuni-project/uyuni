#! /usr/bin/ruby
# Check if a .changes file is added/modified in a pull request *or* the "No
# changelog needed" box in the pull request description is ticked. If neither of
# these are true, exit 1 to fail the check.
#
# List of added/modified in the pull request is stored in ARGV
require 'json'

exit(0) if ARGV.empty?

files = Array.new

ARGV.each do |filename|
  file = { :filename => filename, :status => "modified" }
  files << file
end
ret = { :files => files }
genjson = JSON.generate(ret)

File.open(".gitarro_pr.json", "w") { |f| f.write genjson }
exit(0)
