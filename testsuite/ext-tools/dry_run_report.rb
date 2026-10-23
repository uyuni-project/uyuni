# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# Reads the JSON of `cucumber --dry-run` and reports every step that cannot be matched to exactly one step
# definition, as GitHub annotations on the feature file, with a hint on how to fix it.
# Usage: ruby dry_run_report.rb <dry_run.json>
# Exit status: 0 when every step matches, 1 otherwise.

require 'json'

AMBIGUOUS_HINT = 'Make the regular expressions mutually exclusive (anchor them with ^ and $) or delete the duplicate step definition.'.freeze
UNDEFINED_HINT = 'Reuse an existing step (grep features/step_definitions for similar wording) or add the missing step definition. ' \
                 'Also check typos, quotes around parameters and the Given/When/Then keyword.'.freeze

# Returns [title, hint] when the step is a problem, nil when it is fine.
def classify(step)
  result = step['result'] || {}
  case result['status']
  when 'undefined' then ['Undefined step', UNDEFINED_HINT]
  when 'failed'
    ambiguous = result['error_message'].to_s.start_with?('Ambiguous match')
    [ambiguous ? 'Ambiguous step' : 'Step failed in dry run', ambiguous ? AMBIGUOUS_HINT : 'See the cucumber log.']
  end
end

# GitHub workflow commands need %, CR and LF escaped
def escape(text)
  text.to_s.gsub('%', '%25').gsub("\r", '%0D').gsub("\n", '%0A')
end

# All problem steps of all features
def problems(features)
  found =
    features.flat_map do |feature|
      steps = feature.fetch('elements', []).flat_map { |element| element.fetch('steps', []) }
      steps.filter_map do |step|
        title, hint = classify(step)
        next unless title

        {
          file: feature['uri'],
          line: step['line'],
          title: title,
          text: "#{step['keyword']}#{step['name']}",
          hint: hint,
          detail: step.dig('result', 'error_message')
        }
      end
    end
  # scenario outlines repeat the same step
  found.uniq { |problem| problem.values_at(:file, :line, :title) }
end

# Prints one GitHub annotation per problem, or a plain message outside GitHub Actions
def report(found)
  found.each do |problem|
    message = ["#{problem[:title]}: #{problem[:text]}", problem[:detail], "How to fix: #{problem[:hint]}"].compact.join("\n")
    if ENV['GITHUB_ACTIONS']
      # GitHub prints the annotation in the log too, so a second plain line would duplicate it
      puts "::error file=testsuite/#{problem[:file]},line=#{problem[:line]},title=#{escape(problem[:title])}::#{escape(message)}"
    else
      puts "#{problem[:file]}:#{problem[:line]}: #{message}\n\n"
    end
  end
  puts(found.empty? ? 'All steps match exactly one step definition.' : "#{found.size} step problem(s) found.")
end

if $PROGRAM_NAME == __FILE__
  json_file = ARGV.first
  unless File.exist?(json_file)
    puts "::error title=Cucumber dry run::No #{json_file}: cucumber could not load. Check the log above for the Ruby error (env.rb, support files or a feature syntax error)."
    exit 1
  end
  found = problems(JSON.parse(File.read(json_file)))
  report(found)
  exit(found.empty? ? 0 : 1)
end
