#! /usr/bin/ruby
# Check if a .changes file is added/modified in a pull request *or* the "No
# changelog needed" box in the pull request description is ticked. If neither of
# these are true, exit 1 to fail the check.
#
# List of added/modified in the pull request is stored in ARGV
require 'json'

def not_needed?(pr_body)
  pr_body.match(/\[x\]\s+No\s+changelog\s+needed/i)
end

github_event = JSON.parse(File.read(ENV['GITHUB_EVENT_PATH']))
pr_body = github_event['pull_request']['body']
puts '-' * 20, "PR Description: #{github_event['pull_request']['body']}"
puts '-' * 20, "Added or modified .changes files: #{ARGV}"

exit(0) unless ARGV.empty?
not_needed?(pr_body) ? exit(0) : exit(1)
