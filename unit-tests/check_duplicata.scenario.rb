#! /usr/bin/ruby
# this run on travis
duplicata = `cat features/*.feature | grep Scenario | sort | uniq -cd`.strip
if duplicata.to_s.empty?
  puts "no duplicata great job"
  exit 0
else
  puts "+++++++++++++++++++++++++++++++"
  puts "found duplicatas Scenario names!"
  puts "please remove them"
  puts duplicata
  exit 1
end
