#!/usr/bin/env ruby
# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'csv'
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

    opts.on('-o', '--output_path FILEPATH', 'Output file path (CSV format)') do |filepath|
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
    dataset =
      data['data']['result'].map do |result|
        metric = result['metric']
        {
          label: metric['status'].downcase,
          description: {
            jobname: metric['jobname'],
            scenario: metric['case'],
            feature: metric['suite'],
            failedsince: metric['failedsince'].to_i,
            age: result['value'][1].to_i
          }
        }
      end
    CSV.open(options[:output_path], 'w') do |csv|
      csv << dataset.first.keys
      dataset.each do |entry|
        csv << [entry[:label], entry[:description].to_json]
      end
    end
  else
    puts "Failed to fetch data from Prometheus: #{response.code} #{response.message}"
  end
rescue StandardError => e
  puts "An error occurred: #{e.message}"
end
