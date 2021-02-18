#! /usr/bin/ruby
require 'json'

# if true we exit with 0, so the test is sucessefull. otherwise the test fail.
def check_changelog?(pr_body, files)
  return true if pr_body.match(/\[x\]\s+No\s+changelog\s+needed/i)
  
  puts 'Looking for a .changes file...'
  files.any? { |f| f.include? '.changes' } 
end

github_event = JSON.parse(File.read(ENV['GITHUB_EVENT_PATH']))
files = ARGV[0].split(' ')
puts "-" * 20, "PR Description: #{github_event['pull_request']['body']}"
puts "-" * 20, "Files modified: #{files}"
check_changelog?(github_event['pull_request']['body'], files) ?  exit(0) : exit(1)
