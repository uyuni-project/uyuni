#!/usr/bin/ruby

if ARGV.length == 0
  puts "Usage: setup.rb featurefile OR setup.rb featurefile +"
  puts "Including the + includes featurefile in the list"
  exit
end

inc = false
stop_at = ARGV[0]

if ARGV.length == 2
  inc = true if ARGV[1].include? "+"
end

run_set = 'run_sets/testsuite.yml'
features = []

File.open(run_set, 'r') do |file|
  catch :done do
    while line = file.gets
      line.gsub!('- ', '').chomp!
      if ! line.include? '#'
        if line.include? stop_at
          features.push(line) if inc
          throw :done
        end
        features.push(line)
      end
    end
  end
end
# exec exits the ruby program and replaces it with a shell call
cmd = "cucumber " + features.join(' ')
puts "Now executing #{cmd}"
exec(cmd)
