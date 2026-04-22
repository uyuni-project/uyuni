# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

require 'fileutils'
require 'open3'
require 'shellwords'
require 'timeout'
require 'yaml'
require_relative 'base_backend'

module AITestReviewer
  # Backend implementation that prepares FailTale input files and executes the reviewer command.
  class FailTaleBackend < BaseBackend
    ROLE_BY_HOST_ENV = {
      'SERVER' => 'server',
      'PROXY' => 'proxy',
      'MINION' => 'minion',
      'SSH_MINION' => 'minion',
      'RHLIKE_MINION' => 'minion',
      'DEBLIKE_MINION' => 'minion',
      'BUILD_HOST' => 'build_host'
    }.freeze
    private_constant :ROLE_BY_HOST_ENV

    # Executes a FailTale review run for the provided scenario context.
    #
    # @param context [Hash] review context
    # @option context [Object] :scenario failed Cucumber scenario
    # @option context [String, nil] :screenshot_path optional path to failure screenshot
    # @return [void]
    def review(context)
      scenario = context.fetch(:scenario)
      screenshot_path = context[:screenshot_path]
      out_dir = create_output_directory(scenario)

      report_path = File.join(out_dir, 'test_report.txt')
      failure_path = File.join(out_dir, 'test_failure.txt')
      config_path = File.join(out_dir, 'config.yaml')

      File.write(report_path, scenario_report(scenario))
      File.write(failure_path, scenario_failure(scenario))
      write_config(config_path)

      env = command_environment(report_path, failure_path, config_path, screenshot_path)
      stdout, stderr, status = run_command(env)

      File.write(File.join(out_dir, 'reviewer.stdout.log'), stdout)
      File.write(File.join(out_dir, 'reviewer.stderr.log'), stderr) unless stderr.to_s.empty?

      $stdout.puts "[AI Test Reviewer] Output directory: #{out_dir}"
      $stdout.puts "[AI Test Reviewer] Result:\n#{stdout}" unless stdout.to_s.empty?
      warn "[AI Test Reviewer] Errors:\n#{stderr}" unless status.success?
      stdout
    end

    private

    # Creates a timestamped output directory for one reviewer run.
    #
    # @param scenario [Object] scenario used to derive a safe directory suffix
    # @return [String] absolute path to the created output directory
    def create_output_directory(scenario)
      ts = Time.now.utc.strftime('%Y%m%d_%H%M%S')
      safe_name = scenario.name.to_s.gsub(/[^a-zA-Z0-9_-]/, '_')
      out_dir = File.expand_path(File.join(output_root, "#{ts}_#{safe_name}"), Dir.pwd)
      FileUtils.mkdir_p(out_dir)
      out_dir
    end

    # Builds the report text consumed by FailTale.
    #
    # @param scenario [Object] failed scenario
    # @return [String] structured report data
    def scenario_report(scenario)
      gherkin_document = File.read(scenario.location.file)
      parsed_document = ::Gherkin::Parser.new.parse(gherkin_document)
      feature_name = extract_feature_name(parsed_document, scenario)
      steps_text = extract_scenario_steps(parsed_document, scenario)
      <<~REPORT
        Feature: #{feature_name}
        Scenario: #{scenario.name}
        Location: #{scenario.location}
        Status: failed
        Tags: #{scenario.source_tag_names.join(', ')}
        Steps:
        #{steps_text}
      REPORT
    end

    # Extracts the feature name from parser output across old/new message shapes.
    #
    # @param parsed_document [Object] Gherkin parser result
    # @param scenario [Object] failed scenario used for filename fallback
    # @return [String] feature name or a safe fallback
    def extract_feature_name(parsed_document, scenario)
      if parsed_document.respond_to?(:feature)
        name = parsed_document.feature&.name.to_s.strip
        return name unless name.empty?
      end

      if parsed_document.respond_to?(:[])
        name = parsed_document.dig(:feature, :name).to_s.strip
        return name unless name.empty?
      end

      File.basename(scenario.location.file.to_s)
    end

    # Extracts the steps of the matching scenario from the parsed Gherkin document.
    #
    # @param parsed_document [Object] Gherkin parser result
    # @param scenario [Object] failed scenario used to locate the matching definition
    # @return [String] formatted steps text, or an empty string when steps cannot be resolved
    def extract_scenario_steps(parsed_document, scenario)
      children = resolve_scenario_children(parsed_document)
      return '' if children.nil?

      target_line = scenario.location.respond_to?(:line) ? scenario.location.line : nil
      matched = children.find { |child| scenario_node(child)&.then { |s| s.respond_to?(:name) ? s.name == scenario.name : s[:name] == scenario.name } }
      matched ||= children.find { |child| target_line && scenario_node(child)&.then { |s| (s.respond_to?(:location) ? s.location.line : s.dig(:location, :line)) == target_line } } if target_line

      return '' if matched.nil?

      steps = scenario_steps(scenario_node(matched))
      return '' if steps.nil? || steps.empty?

      steps.map { |step| format_step(step) }.join("\n")
    end

    # Resolves scenario children from the parsed document, handling both object and hash shapes.
    def resolve_scenario_children(parsed_document)
      if parsed_document.respond_to?(:feature)
        parsed_document.feature&.children
      elsif parsed_document.respond_to?(:[])
        parsed_document.dig(:feature, :children)
      end
    end

    # Extracts the scenario node from a feature child (may be wrapped in :scenario key).
    def scenario_node(child)
      if child.respond_to?(:scenario)
        child.scenario
      elsif child.respond_to?(:[]) && child[:scenario]
        child[:scenario]
      else
        child
      end
    end

    # Extracts steps from a scenario node, handling both object and hash shapes.
    def scenario_steps(node)
      return if node.nil?

      if node.respond_to?(:steps)
        node.steps
      elsif node.respond_to?(:[])
        node[:steps]
      end
    end

    # Formats a single Gherkin step as "Keyword Text".
    def format_step(step)
      keyword = (step.respond_to?(:keyword) ? step.keyword : step[:keyword]).to_s.strip
      text = (step.respond_to?(:text) ? step.text : step[:text]).to_s.strip
      "  #{keyword} #{text}"
    end

    # Extracts a human-readable failure message from the scenario exception.
    #
    # @param scenario [Object] failed scenario
    # @return [String] failure details, or an empty string when no exception exists
    def scenario_failure(scenario)
      return '' if scenario.exception.nil?

      return scenario.exception.full_message.to_s if scenario.exception.respond_to?(:full_message)

      scenario.exception.message.to_s
    end

    # Writes the generated FailTale YAML configuration file.
    #
    # @param config_path [String] destination path for the configuration file
    # @return [void]
    def write_config(config_path)
      config = {
        'ssh_defaults' => {
          'username' => ENV.fetch('AI_TEST_REVIEWER_SSH_USER', ENV.fetch('FAILTALE_SSH_USER', 'root')),
          'private_key_path' => ENV.fetch('AI_TEST_REVIEWER_SSH_KEY', ENV.fetch('FAILTALE_SSH_KEY', "#{Dir.home}/.ssh/id_ed25519")),
          'port' => Integer(ENV.fetch('AI_TEST_REVIEWER_SSH_PORT', '22')),
          'connection_timeout' => Integer(ENV.fetch('AI_TEST_REVIEWER_CONNECTION_TIMEOUT', '10')),
          'command_timeout' => Integer(ENV.fetch('AI_TEST_REVIEWER_COMMAND_TIMEOUT', '30'))
        },
        'components' => components_config,
        'hosts' => hosts_inventory,
        'uyuni_mcp' => {
          'port' => Integer(ENV.fetch('UYUNI_PORT', '443')),
          'uyuni_user' => ENV.fetch('UYUNI_USER', 'admin'),
          'uyuni_pass' => ENV.fetch('UYUNI_PASS', 'admin'),
          'image_version' => ENV.fetch('UYUNI_MCP_IMAGE_VERSION', 'latest'),
          'ssl_verify' => ENV.fetch('UYUNI_MCP_SSL_VERIFY', 'false')
        }
      }

      File.write(config_path, config.to_yaml)
    end

    # Builds host inventory from testsuite environment variables.
    #
    # @return [Array<Hash{String => String}>] list of host entries with hostname and role
    # @raise [ArgumentError] when the server host is missing
    def hosts_inventory
      hosts = []
      ROLE_BY_HOST_ENV.each do |host_env, role|
        hostname = ENV[host_env].to_s.strip
        next if hostname.empty?

        hosts << { 'hostname' => hostname, 'role' => role }
      end

      raise ArgumentError, 'Unable to generate FailTale config: SERVER host is missing from environment' unless hosts.any? { |host| host['role'] == 'server' }

      hosts
    end

    # Loads the component command catalog from the FailTale template.
    #
    # @return [Hash] component configuration for data collection
    def components_config
      template = File.join(failtale_home, 'examples', 'uyuni', 'config.yaml')
      return fallback_components unless File.exist?(template)

      parsed = YAML.load_file(template)
      parsed.fetch('components', fallback_components)
    rescue StandardError => e
      warn "[AI Test Reviewer] Unable to load components from template: #{e.message}"
      fallback_components
    end

    # Provides default component commands when no template is available.
    #
    # @return [Hash] fallback component configuration
    def fallback_components
      {
        'server' => {
          'useful_data' => [
            { 'description' => 'Web UI log tail', 'command' => 'tail -n 200 /var/log/rhn/rhn_web_ui.log' },
            { 'description' => 'Web API log tail', 'command' => 'tail -n 200 /var/log/rhn/rhn_web_api.log' },
            { 'description' => 'Taskomatic log tail', 'command' => 'tail -n 200 /var/log/rhn/rhn_taskomatic_daemon.log' }
          ]
        },
        'minion' => {
          'useful_data' => [
            { 'description' => 'Salt minion log tail', 'command' => 'tail -n 200 /var/log/salt/minion' }
          ]
        },
        'proxy' => {
          'useful_data' => [
            { 'description' => 'Squid access log tail', 'command' => 'tail -n 200 /var/log/squid/access.log' }
          ]
        }
      }
    end

    # Builds environment variables passed to the reviewer command.
    #
    # @param report_path [String] path to the scenario report file
    # @param failure_path [String] path to the failure details file
    # @param config_path [String] path to the generated YAML config
    # @param screenshot_path [String, nil] optional screenshot path
    # @return [Hash{String => String}] environment variables for command execution
    def command_environment(report_path, failure_path, config_path, screenshot_path)
      env = {
        'CONFIG_PATH' => config_path,
        'TEST_REPORT_PATH' => report_path,
        'TEST_FAILURE_PATH' => failure_path
      }
      screenshot = screenshot_path.to_s
      env['SCREENSHOT_PATH'] = File.expand_path(screenshot, Dir.pwd) unless screenshot.empty?

      %w[GOOGLE_API_KEY OPENAI_API_KEY GEMINI_API_KEY].each do |key|
        env[key] = ENV[key] if ENV[key]
      end

      env
    end

    # Executes the configured reviewer command inside the FailTale home directory.
    #
    # @param env [Hash{String => String}] environment variables for the command
    # @return [Array<(String, String, Process::Status)>] stdout, stderr and process status
    # @raise [ArgumentError] when required home path is missing or invalid
    # @raise [Timeout::Error] when command execution exceeds configured timeout
    def run_command(env)
      command = Shellwords.split(ENV.fetch('AI_TEST_REVIEWER_CMD', 'crewai run'))
      home = failtale_home
      timeout_seconds = Integer(ENV.fetch('AI_TEST_REVIEWER_TIMEOUT', ENV.fetch('FAILTALE_TIMEOUT', '600')))

      raise ArgumentError, 'AI_TEST_REVIEWER_HOME (or FAILTALE_HOME) is not set' if home.empty?
      raise ArgumentError, "AI reviewer home does not exist: #{home}" unless Dir.exist?(home)

      $stdout.puts "[AI Test Reviewer] Running #{command.join(' ')} in #{home} (timeout #{timeout_seconds}s)"

      Timeout.timeout(timeout_seconds) do
        Open3.capture3(env, *command, chdir: home)
      end
    rescue Timeout::Error
      raise Timeout::Error, "AI reviewer command timed out after #{timeout_seconds}s"
    end

    # Returns the root directory where reviewer outputs are stored.
    #
    # @return [String] relative or absolute output root path
    def output_root
      ENV.fetch('AI_TEST_REVIEWER_OUT', File.join('tmp', 'ai_test_reviewer'))
    end

    # Resolves and validates the FailTale home directory from environment variables.
    #
    # @return [String] absolute FailTale home path, or an empty string when unset
    def failtale_home
      raw_home = ENV.fetch('AI_TEST_REVIEWER_HOME', ENV.fetch('FAILTALE_HOME', '')).to_s.strip
      return '' if raw_home.empty?

      File.expand_path(raw_home, Dir.pwd)
    end
  end
end
