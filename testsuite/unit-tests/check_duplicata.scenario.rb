#! /usr/bin/ruby
# this run on travis
duplicata = `cat features/*.feature | grep Scenario | sort | uniq -cd`.strip
if duplicata.to_s.empty?
  STDOUT.puts 'no duplicata great job'
  exit 0
else
  STDOUT.puts '+++++++++++++++++++++++++++++++'
  STDOUT.puts 'found duplicatas Scenario names!'
  STDOUT.puts 'please remove them'
  STDOUT.puts duplicata
  exit 1
end
