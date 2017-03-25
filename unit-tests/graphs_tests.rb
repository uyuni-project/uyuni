#! /usr/bin/ruby
# this run on travis
require 'rubygems'
require 'yaml'

def check_duplicate_features(a)
  if a.uniq.length == a.length
    puts "features does not contain duplicates"
  else
    puts "================================="
    puts "2 or more yaml file contains same feature!"
    raise "features does contain duplicates"
  end
end 

# load all yamls files
feat_set = YAML.load_file('run_sets/v2/testsuite.yml')

# load several files and add the final array

# load the graph files and create a sum array 
# then  check that all names from sum_array are contained grap == features
# otherwise raise an error. When an user create a new feature and doesn't addi it on the graph.

#  if there is a feature plus in graph, raise error, user has forgot to delete feature.

check_duplicate_features(feat_set)
feat_set.each do |feature|
  puts feature
end
