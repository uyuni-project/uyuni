require 'rubygems'
require 'yaml'
require 'cucumber/rake/task'

$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)

outputfile = 'output.html'
json_result = '--format json -o .output.json'
junit_result = '--format junit -o junit_results'
html_results = "--format pretty --format html -o #{outputfile}"

Dir.glob(File.join(Dir.pwd, 'run_sets', '*.yml')).each do |entry|
  namespace :cucumber do
    Cucumber::Rake::Task.new(File.basename(entry, '.yml').to_sym) do |t|
      cucumber_opts = %W[#{json_result} #{junit_result} #{html_results} -f rerun --out failed.txt]
      features = YAML.safe_load(File.read(entry))
      t.cucumber_opts = cucumber_opts + features
    end
  end
end

task :cucumber do |t|
  Rake::Task['cucumber:testsuite'].invoke
end

task :docker do |t|
  Rake::Task['cucumber:docker'].invoke
end

task :default => [:cucumber]
