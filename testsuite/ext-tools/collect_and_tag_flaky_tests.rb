# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# Collect all the issues from a GitHub project board column
# and tag the corresponding Cucumber feature files with a given tag
#
# Usage: ruby collect_and_tag_flaky_tests.rb <directory_path>
# Example: ruby collect_and_tag_flaky_tests.rb <repository_path>/testsuite/features

require 'json'
require 'net/http'
require 'netrc'
require 'find'

# Function to fetch issues from a GitHub project board column
def fetch_github_issues(organization, project_number, column, headers)
  after = ''
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
                        title
                        url
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

  gh_cards = []

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
        nodes.each do |item|
          status_field = item['fieldValueByName']
          next if status_field.nil? || status_field['name'] != column

          if status_field && status_field['item'] && status_field['item']['content']
            issue_title = status_field['item']['content']['title']
            issue_url = status_field['item']['content']['url']
            next if issue_title.to_s.empty? || issue_url.to_s.empty?

            gh_cards.push({ title: issue_title, url: issue_url })
            puts "\e[36mCard found\e[0m => #{issue_title}"
          else
            puts "No data found in the GraphQL response for this item.: #{item}"
          end
        end
        page_info = items['pageInfo']
        break unless page_info && page_info['hasNextPage']

        after = page_info['endCursor']
      else
        puts 'No data found in the GraphQL response.'
      end
    else
      puts "HTTP Request failed with status code: #{response.code}"
      puts response.body
      break
    end
  end

  gh_cards
end

# Function to tag Cucumber feature files
def tag_cucumber_feature_files(directory_path, gh_cards, tag, regex_on_gh_card_title)
  features_tagged = 0
  scenarios_tagged = 0

  Find.find(directory_path) do |cucumber_file_path|
    next unless File.extname(cucumber_file_path) == '.feature'

    gh_cards.each do |card|
      title = card[:title]
      url = card[:url]
      feature_matched = false
      matches = regex_on_gh_card_title.match(title)
      next if matches.nil?

      match_feature = "Feature: #{matches[1].delete('|').strip}"
      match_scenario = "Scenario: #{matches[2].strip}"

      temp_file_path = 'temp_file'
      previous_line = ''
      lines = File.readlines(cucumber_file_path)
      skip_next_line = false
      File.open(temp_file_path, 'w') do |temp_file|
        lines.each_with_index do |line, index|
          if skip_next_line
            skip_next_line = false
            previous_line = line
            next
          end

          feature_line_match = line.include?(match_feature)
          feature_match = feature_line_match && !previous_line.include?("@#{tag}")
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
          if feature_line_match
            related_card_line = "  * Related GitHub Card: #{url}\n"
            next_line = lines[index + 1]
            if next_line&.match?(/^\s*\* Related GitHub Card:/)
              skip_next_line = true
              if next_line != related_card_line
                temp_file.puts(related_card_line)
              end
            else
              temp_file.puts(related_card_line)
            end
          end
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
  if ARGV.length == 1
    directory_path = ARGV[0]
  else
    puts '\e[31mUsage: ruby collect_and_tag_flaky_tests.rb <directory_path>\e[0m'
    exit(1)
  end

  unless File.directory?(directory_path)
    puts '\e[31mThe specified path is not a directory.\e[0m'
    exit(1)
  end

  if ENV.key? 'GITHUB_TOKEN'
    token = ENV.fetch('GITHUB_TOKEN', nil)
  else
    netrc = Netrc.read
    github_credentials = netrc['github.com']
    token = github_credentials.password unless github_credentials.nil?
  end

  if token.nil?
    puts 'GitHub token not found. Please set the GITHUB_TOKEN environment variable or add your credentials to the .netrc file.'
    exit(1)
  end

  organization = 'SUSE'
  project_number = 23
  regex_on_gh_card_title = /Feature:(.*)Scenario:(.*)/
  headers = {
    'Content-Type' => 'application/json',
    'Authorization' => "Bearer #{token}"
  }

  columns = {
    'New' => 'new_issue',
    'Debugging' => 'under_debugging',
    'Bugs' => 'bug_reported',
    'Test Framework issues' => 'test_issue',
    'Flaky Tests' => 'flaky'
  }

  columns.each do |column, tag|
    gh_cards = fetch_github_issues(organization, project_number, column, headers)
    puts ">> Found #{gh_cards.length} issues in the '#{column}' column of the GitHub project board."
    unless gh_cards.empty?
      features_tagged, scenarios_tagged = tag_cucumber_feature_files(directory_path, gh_cards, tag, regex_on_gh_card_title)
      puts ">> Tagged #{features_tagged} feature and #{scenarios_tagged} scenarios with the '#{tag}' tag."
    end
  end
end

main
