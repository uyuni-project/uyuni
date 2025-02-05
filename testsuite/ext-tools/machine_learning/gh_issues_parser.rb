# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

# Collect all the issues from a GitHub project board column
# and tag the corresponding Cucumber feature files with a given tag

require 'base64'
require 'csv'
require 'find'
require 'json'
require 'net/http'
require 'netrc'
require 'optparse'

# GitHub Project Board module query and process data from the GH Board
module GithubProjectBoard
  # Method to fetch issues from a GitHub project board
  # @param [Integer] Organization Id
  # @param [Integer] Project number
  # @param [Integer] Pagination helper
  # @param [Map] HTTP Headers
  # @return [Array] Array of items
  def self.query_items(organization, project_number, after, headers)
    graphql_endpoint = URI('https://api.github.com/graphql')
    graphql_query = <<~GRAPHQL
      query($organization: String!, $number: Int!, $after: String) {
        organization(login: $organization) {
          projectV2(number: $number) {
            items(first: 100, after: $after) {
              pageInfo {
                endCursor
                hasNextPage
              }
              nodes {
                fieldValueByName(name: "Status") {
                  ... on ProjectV2ItemFieldSingleSelectValue {
                    name
                    item {
                      content {
                        ... on Issue {
                          url
                          title
                          bodyText
                          comments(first: 100) {
                            nodes {
                              body
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    GRAPHQL

    results = []
    loop do
      request_body = {
        query: graphql_query,
        variables: { organization: organization, number: project_number, after: after }
      }.to_json
      http = Net::HTTP.new(graphql_endpoint.host, graphql_endpoint.port)
      http.use_ssl = true
      request = Net::HTTP::Post.new(graphql_endpoint.request_uri)
      request.body = request_body
      headers.each { |key, value| request[key] = value }
      response = http.request(request)

      if response.code == '200'
        parsed_response = JSON.parse(response.body)
        if parsed_response['data']
          items = parsed_response['data']['organization']['projectV2']['items']
          nodes = items['nodes']
          results.concat(nodes)
          page_info = items['pageInfo']
          break unless page_info && page_info['hasNextPage']

          after = page_info['endCursor']
        else
          puts 'No data found in the GraphQL response.'
          break
        end
      else
        puts "HTTP Request failed with status code: #{response.code}"
        puts response.body
        break
      end
    end
    results
  end

  # Method to fetch issues from a GitHub project board by column
  # @param [Integer] Organization Id
  # @param [Integer] Project number
  # @param [Integer] Pagination helper
  # @param [Map] HTTP Headers
  # @return [Array] Array of items
  def self.fetch_issues(organization, project_number, column, headers)
    nodes = query_items(organization, project_number, nil, headers)
    gh_card_titles = []
    nodes.each do |item|
      status_field = item['fieldValueByName']
      next if status_field['name'] != column

      if status_field && status_field['item'] && status_field['item']['content']
        issue_title = status_field['item']['content']['title']
        gh_card_titles.push(issue_title)
        puts "\e[36mCard found\e[0m => #{issue_title}"
      else
        puts "No data found in the GraphQL response for this item: #{item}"
      end
    end
    gh_card_titles
  end

  #  Method to fetch issues from a GitHub project board and covert it to a dataset
  # @param [Integer] Organization Id
  # @param [Integer] Project number
  # @param [Map] HTTP Headers
  # @return [Array] Dataset of issues
  def self.generate_dataset(organization, project_number, headers)
    nodes = query_items(organization, project_number, nil, headers)
    label_mapping = {
      'New' => 0,
      'Debugging' => 1,
      'Test Framework issues' => 2,
      'Flaky Tests' => 3,
      'Bugs' => 4,
      'Fixed' => 5,
      'Fixed with bug workaround' => 6
    }
    dataset = []
    nodes.each do |item|
      status_field = item['fieldValueByName']
      label = status_field['name']
      # TODO: Implement a query to obtain the timeline of that issue.
      #       As of today, 20-11-2024, there is no support to retrieve it through graphql in new project boards
      #       Neither REST API or trying to browse it (due to the login constraint with a 2FA).
      #       Without being able to know the previous state of a Fixed issue, a Machine Learning model will not be accurate.
      # if label == 'Fixed'
      #   get_timeline(item['fieldValueByName']['item']['content']['url'])
      # end
      if status_field && status_field['item'] && status_field['item']['content']
        title = clean_text(status_field['item']['content']['title'])
        description = Base64.encode64(clean_text(status_field['item']['content']['bodyText']))
        comments = Base64.encode64(status_field['item']['content']['comments']['nodes'].map { |node| clean_text(node['body']) }.to_s)
        matches = title.match(/Feature:(.*)\s*\|\s*Scenario:(.*)/)
        gh_issue_content = {
          feature: matches.nil? ? title : matches[1].strip,
          scenario: matches.nil? ? title : matches[2].strip,
          description: description,
          comments: comments
        }
        dataset.push({ label: label_mapping[label], description: gh_issue_content })
        puts "\e[36mCard found\e[0m => #{title}"
      else
        puts "No data found in the GraphQL response for this item: #{item}"
      end
    end
    dataset
  end
end

# Clean text for Machine Learning
def clean_text(text)
  text.gsub(/[\n\t\r]/, ' ').gsub(/\s+/, ' ').strip
end

# Function to tag Cucumber feature files
def tag_cucumber_feature_files(directory_path, gh_card_titles, tag)
  features_tagged = 0
  scenarios_tagged = 0

  Find.find(directory_path) do |cucumber_file_path|
    next unless File.extname(cucumber_file_path) == '.feature'

    gh_card_titles.each do |title|
      feature_matched = false
      matches = title.match(/Feature:(.*)\s*\|\s*Scenario:(.*)/)
      next if matches.nil?

      match_feature = "Feature: #{matches[1].strip}"
      match_scenario = "Scenario: #{matches[2].strip}"

      temp_file_path = 'temp_file'
      previous_line = ''
      File.open(temp_file_path, 'w') do |temp_file|
        File.foreach(cucumber_file_path).with_index do |line, _index|
          feature_match = line.include?(match_feature) && !previous_line.include?("@#{tag}")
          scenario_match = line.include?(match_scenario) && !previous_line.include?("@#{tag}")

          if feature_match
            puts "\n\e[34mMatched feature\e[0m  => #{match_feature} in #{cucumber_file_path}"
            temp_file.puts("@#{tag}")
            features_tagged += 1
            feature_matched = true
          elsif feature_matched && scenario_match
            puts "\e[33mMatched scenario\e[0m => #{match_scenario} in #{cucumber_file_path}"
            temp_file.puts("@#{tag}")
            scenarios_tagged += 1
            feature_matched = false
          else
            # Do nothing
          end
          temp_file.puts(line)
          previous_line = line
        end
      end
      File.rename(temp_file_path, cucumber_file_path)
    end
  end

  [features_tagged, scenarios_tagged]
end

# Main function
def main
  options = {}
  parser =
    OptionParser.new do |opts|
      opts.banner = 'Usage: ruby gh_issues_parser.rb [options]'

      opts.on('-g', '--generate_dataset', 'Generate a dataset from GitHub project board issues') do
        options[:generate_dataset] = true
      end

      opts.on('-c', '--collect_and_tag', 'Collect flaky tests and tag Cucumber features') do
        options[:collect_and_tag] = true
      end

      opts.on('-d', '--directory_path PATH', 'Directory path to search for Cucumber feature files') do |path|
        options[:directory_path] = path
      end

      opts.on('-o', '--output_path PATH', 'File path to store the dataset (JSON format)') do |path|
        options[:output_path] = path
      end

      opts.on('-h', '--help', 'Show this help message') do
        puts opts
        exit
      end
    end

  parser.parse!

  unless options[:generate_dataset] || options[:collect_and_tag]
    puts 'Please specify either --generate_dataset or --collect_and_tag'
    exit 1
  end

  if options[:generate_dataset] && !options[:output_path]
    puts 'Please specify the file path using --output_path'
    exit 1
  end

  if options[:collect_and_tag] && !options[:directory_path]
    puts 'Please specify the file path using --directory_path'
    exit 1
  end

  if ENV.key? 'GITHUB_TOKEN'
    token = ENV.fetch('GITHUB_TOKEN', nil)
  else
    netrc = Netrc.read
    github_credentials = netrc['github.com']
    token = github_credentials.password unless github_credentials.nil?
  end

  exit(1) if token.nil?

  organization = 'SUSE'
  project_number = 23
  headers = {
    'Content-Type' => 'application/json,text/html',
    'Accept' => 'application/vnd.github.starfox-preview+vnd.github.bane-preview+vnd.github+json,*/*',
    'Authorization' => "Bearer #{token}",
    'User-Agent' => 'Ruby script'
  }

  if options[:generate_dataset]
    dataset = GithubProjectBoard.generate_dataset(organization, project_number, headers)
    File.write(options[:output_path], dataset.to_json)
  elsif options[:collect_and_tag]
    columns = {
      'New' => 'new_issue',
      'Debugging' => 'under_debugging',
      'Bugs' => 'bug_reported',
      'Test Framework issues' => 'test_issue',
      'Flaky Tests' => 'flaky',
      'Fixed' => 'fixed',
      'Fixed with bug workaround' => 'workaround'
    }

    columns.each do |column, tag|
      gh_card_titles = GithubProjectBoard.fetch_issues(organization, project_number, column, headers)
      puts ">> Found #{gh_card_titles.length} issues in the '#{column}' column of the GitHub project board."
      unless gh_card_titles.empty?
        features_tagged, scenarios_tagged = tag_cucumber_feature_files(options[:directory_path], gh_card_titles, tag)
        puts ">> Tagged #{features_tagged} feature and #{scenarios_tagged} scenarios with the '#{tag}' tag."
      end
    end
  end
end

main
