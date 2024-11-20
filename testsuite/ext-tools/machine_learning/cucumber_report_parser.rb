# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'json'
require 'nokogiri'
require 'optparse'

# Extract dataset from JSON test report
def extract_dataset_from_json(json_report_path)
  file = File.read(json_report_path)
  data = JSON.parse(file)

  gh_issue_label_mapping = {
    'new_issue' => 0,
    'under_debugging' => 1,
    'test_issue' => 2,
    'flaky' => 3,
    'bug_reported' => 4,
    'fixed' => 5,
    'workaround' => 6
  }

  label_mapping = {
    'passed' => 0,
    'skipped' => 1,
    'failed' => 2
  }

  dataset = []
  data.each do |feature|
    feature['elements'].each do |scenario|
      logs = []
      screenshots = []
      if scenario['after'] && scenario['after'].size > 1 && scenario['after'][1].key?('embeddings')
        logs += scenario['after'][1]['embeddings'].select { |e| e['mime_type'] == 'text/plain' }.map { |e| e['data'] }
        screenshots += scenario['after'][1]['embeddings'].select { |e| e['mime_type'] == 'image/png' }.map { |e| e['data'] }
      end

      feature_data = {
        test_name: scenario['name'],
        label: label_mapping[scenario['steps'].last['result']['status']],
        execution_time: scenario['steps'].sum { |step| step['result']['duration'] || 0 } / 1_000_000.0
      }

      feature_data[:error_message] = scenario['steps'].last['result']['error_message'] if scenario['steps'].last['result'].key?('error_message')
      feature_data[:tags] = scenario['tags'].map { |tag| tag['name'][1..-1] } if scenario.key?('tags')
      feature_data[:logs] = logs unless logs.empty?
      feature_data[:screenshots] = screenshots unless screenshots.empty?
      if scenario['before'] && scenario['before'].size > 3 && scenario['before'][3].key?('output')
        timestamp_string = scenario['before'][3]['output'][0].strip
        timestamp_match = timestamp_string.match(/(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})/)
        feature_data[:timestamp] = timestamp_match[1] if timestamp_match
      end
      dataset_entry = {
        description: feature_data,
        gh_issue_label: 'unknown'
      }
      found_gh_issue_label = feature_data[:tags].select { |tag| %w[new_issue under_debugging bug_reported test_issue flaky].include?(tag) }.first unless feature_data[:tags].nil?
      dataset_entry[:gh_issue_label] = gh_issue_label_mapping[found_gh_issue_label] unless found_gh_issue_label.nil?
      dataset << dataset_entry
    end
  end

  dataset.to_json
end

# Parse command-line arguments
options = {}
parser =
  OptionParser.new do |opts|
    opts.banner = 'Usage: ruby cucumber_report_parser.rb [options]'

    opts.on('-r', '--report_path PATH', 'Path to the Cucumber report file') do |f|
      options[:report_path] = f
    end

    opts.on('-o', '--output_path PATH', 'Path to the processed report file') do |f|
      options[:output_path] = f
    end

    opts.on('-h', '--help', 'Prints this help') do
      puts opts
      exit
    end
  end

parser.parse!

# Validate required arguments
if options[:report_path].nil? || options[:output_path].nil?
  puts 'Missing required arguments. Use -h for help.'
  exit
end

dataset = extract_dataset_from_json(options[:report_path])
File.write(options[:output_path], dataset)
