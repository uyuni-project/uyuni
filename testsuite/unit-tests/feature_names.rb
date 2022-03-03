#! /usr/bin/ruby
# this run on travis
require 'rubygems'
require 'yaml'

## this test, will avoid that you mispelled a name in yaml file
## and in features dir the features is called differently
# TODO:
## additionally, it will check if some feature are hidden, this shouldn't happen

def check_same_list(list1, list2)
  if list1.uniq.sort == list2.uniq.sort
    STDOUT.puts 'OK. good work'
  else
    STDOUT.puts 'ERROR:'
    STDOUT.puts 'The testsuite.yml has a feature that is non features dir!'
    STDOUT.puts '===================================='
    STDOUT.puts list1 - list2
    STDOUT.puts '===================================='
    raise "the testsuite.yml doesn't match the features"
  end
end

# *****************
# main
# ***************

# 0 load yml
feat_set = YAML.load_file('run_sets/testsuite.yml')
core_set = []
feat_set.each do |feature|
  # filter out core features
  core_set.push(feature.split('/').last) if feature.include?('core_')
end

# 1 check the dir features get feature in directory
core_dir = Dir.entries('features')
core_set2 = []
core_dir.each do |feature|
  # filter out core features in dir features
  core_set2.push(feature) if feature.include?('core_')
end

# We check atm only core, but secondary can be extended
check_same_list(core_set, core_set2)
