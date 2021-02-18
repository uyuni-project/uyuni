#! /usr/bin/ruby
require 'json'

# if true we exit with 0, so the PR needs QA Squad as a reviewer
def needs_qa_squad_review?(files)
  files.reject! { |f| f.include? '.feature' }
  files.any? { |f| f.include? 'testsuite' } 
end

files = ARGV[0].split(' ')
puts needs_qa_squad_review?(files).to_s.downcase
