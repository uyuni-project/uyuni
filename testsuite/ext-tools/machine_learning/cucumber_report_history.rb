#!/usr/bin/env ruby
# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'json'
require 'net/http'
require 'optparse'

# Parse command-line options
options = {}
parser =
  OptionParser.new do |opts|
    opts.banner = "Usage: #{$PROGRAM_NAME} [options]"

    opts.on('-s', '--server SERVER', 'Prometheus server URL') do |server|
      options[:server] = server
    end

    opts.on('-o', '--output_path FILEPATH', 'Output file path (JSON format)') do |filepath|
      options[:output_path] = filepath
    end

    opts.on('-h', '--help', 'Display help') do
      puts opts
      exit
    end
  end

parser.parse!

# Ensure all required options are provided
unless options[:server] && options[:output_path]
  puts 'Error: Missing required arguments.'
  puts 'Use --help for usage information.'
  exit 1
end

# Build the URL for Prometheus HTTP API
uri = URI("#{options[:server]}/api/v1/query")
uri.query = URI.encode_www_form({ query: 'jenkins_build_test_case_failure_age' })

begin
  response = Net::HTTP.get_response(uri)
  if response.is_a?(Net::HTTPSuccess)
    data = JSON.parse(response.body)
    label_mapping = {
      'PASSED' => 0,
      'SKIPPED' => 1,
      'FIXED' => 2,
      'REGRESSION' => 3,
      'FAILED' => 4
    }
    dataset =
      data['data']['result'].map do |result|
        metric = result['metric']
        {
          label: label_mapping[metric['status']],
          description: {
            scenario: metric['case'],
            feature: metric['suite'],
            # jobname: metric['jobname'],
            failedsince: metric['failedsince'].to_i,
            age: result['value'][1].to_i
          }
        }
      end
    File.write(options[:output_path], dataset.to_json)
  else
    puts "Failed to fetch data from Prometheus: #{response.code} #{response.message}"
  end
rescue StandardError => e
  puts "An error occurred: #{e.message}"
end
