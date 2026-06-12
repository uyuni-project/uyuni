# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

### Step definitions for optional real-world storage benchmark workloads.

require 'json'
require 'shellwords'
require 'tmpdir'

UYUNI_BENCH_LOCAL_DIR = File.expand_path('../../ext-tools/uyuni-bench', __dir__).freeze
UYUNI_BENCH_HOST_DIR = '/tmp/uyuni-bench'.freeze
UYUNI_BENCH_POD_DIR = '/tmp/uyuni-bench'.freeze
UYUNI_BENCH_DEFAULT_DATASET = 'manifests/datasets/uyuni-test-packages-smoke.json'.freeze
UYUNI_BENCH_DEFAULT_REPO_DIR = '/var/spacewalk/uyuni-bench/repos/repodata'.freeze
UYUNI_BENCH_DEFAULT_RESULTS_PARENT = '/var/spacewalk/uyuni-bench/results/repodata'.freeze

# Return the Kubernetes server pod running Uyuni.
def uyuni_bench_server_pod
  @uyuni_bench_server_pod ||= begin
    pod = get_pod_name('server', 'server')
    raise ScriptError, 'Unable to determine the Uyuni server pod name' if pod.nil? || pod.empty?

    pod
  end
end

# Run a shell command on the RKE2 host.
def uyuni_bench_run_on_host(command, timeout: DEFAULT_TIMEOUT, verbose: true)
  get_target('server').run_local(command, timeout: timeout, verbose: verbose)
end

# Run a shell command inside the Uyuni server pod.
def uyuni_bench_run_in_server_pod(command, timeout: DEFAULT_TIMEOUT, verbose: true, check_errors: true)
  pod = Shellwords.escape(uyuni_bench_server_pod)
  escaped_command = Shellwords.escape(command)
  uyuni_bench_run_on_host(
    "kubectl -n uyuni exec #{pod} -- bash -lc #{escaped_command}",
    timeout: timeout,
    verbose: verbose,
  )
rescue ScriptError
  raise if check_errors

  ['', 1]
end

# Return a dataset path as seen inside the server pod.
def uyuni_bench_dataset_path
  dataset = ENV.fetch('UYUNI_BENCH_DATASET', UYUNI_BENCH_DEFAULT_DATASET)
  dataset.start_with?('/') ? dataset : File.join(UYUNI_BENCH_POD_DIR, dataset)
end

# Return the repository directory as seen inside the server pod.
def uyuni_bench_repo_dir
  ENV.fetch('UYUNI_BENCH_REPO_DIR', UYUNI_BENCH_DEFAULT_REPO_DIR)
end

# Return the storage backend label for result metadata.
def uyuni_bench_storage_backend
  ENV.fetch('UYUNI_BENCH_STORAGE_BACKEND', ENV.fetch('STORAGE_BACKEND', 'unknown'))
end

# Return the benchmark results directory as seen inside the server pod.
def uyuni_bench_results_dir
  return ENV.fetch('UYUNI_BENCH_RESULTS_DIR') if ENV['UYUNI_BENCH_RESULTS_DIR']

  timestamp = Time.now.utc.strftime('%Y%m%d%H%M%S')
  File.join(UYUNI_BENCH_DEFAULT_RESULTS_PARENT, "#{uyuni_bench_storage_backend}-#{timestamp}")
end

# Create a tarball of the benchmark helper and upload it to the RKE2 host.
def uyuni_bench_upload_to_host
  raise ScriptError, "Uyuni benchmark tool not found at #{UYUNI_BENCH_LOCAL_DIR}" unless Dir.exist?(UYUNI_BENCH_LOCAL_DIR)

  Dir.mktmpdir('uyuni-bench') do |dir|
    archive = File.join(dir, 'uyuni-bench.tar.gz')
    tar_command = [
      'tar',
      '--exclude', 'results/*',
      '--exclude', '__pycache__',
      '-czf', archive,
      '-C', UYUNI_BENCH_LOCAL_DIR,
      '.'
    ]
    raise ScriptError, 'Failed to package uyuni-bench helper' unless system(*tar_command)

    remote_archive = '/tmp/uyuni-bench.tar.gz'
    get_target('server').scp_upload(archive, remote_archive)
    uyuni_bench_run_on_host(
      "rm -rf #{Shellwords.escape(UYUNI_BENCH_HOST_DIR)} && " \
      "mkdir -p #{Shellwords.escape(UYUNI_BENCH_HOST_DIR)} && " \
      "tar -xzf #{Shellwords.escape(remote_archive)} -C #{Shellwords.escape(UYUNI_BENCH_HOST_DIR)}",
      timeout: 300,
    )
  end
end

# Copy the benchmark helper from the RKE2 host into the Uyuni server pod.
def uyuni_bench_copy_to_server_pod
  pod = Shellwords.escape(uyuni_bench_server_pod)
  host_dir = Shellwords.escape(UYUNI_BENCH_HOST_DIR)
  pod_dir = Shellwords.escape(UYUNI_BENCH_POD_DIR)

  uyuni_bench_run_on_host(
    "kubectl -n uyuni exec #{pod} -- sh -c #{Shellwords.escape("rm -rf #{UYUNI_BENCH_POD_DIR} && mkdir -p #{UYUNI_BENCH_POD_DIR}")}",
    timeout: 300,
  )
  uyuni_bench_run_on_host(
    "tar -C #{host_dir} -czf - . | kubectl -n uyuni exec -i #{pod} -- tar -xzf - -C #{pod_dir}",
    timeout: 600,
  )
  uyuni_bench_run_in_server_pod("chmod +x #{UYUNI_BENCH_POD_DIR}/uyuni-bench")
end

Given('the Uyuni benchmark tool is available in the server pod') do
  uyuni_bench_upload_to_host
  uyuni_bench_copy_to_server_pod
  uyuni_bench_run_in_server_pod("cd #{UYUNI_BENCH_POD_DIR} && ./uyuni-bench --version")
end

When('I prepare the repodata generation benchmark dataset') do
  dataset = Shellwords.escape(uyuni_bench_dataset_path)
  repo_dir = Shellwords.escape(uyuni_bench_repo_dir)
  workers = ENV.fetch('UYUNI_BENCH_PREPARE_WORKERS', '4').to_i
  timeout = ENV.fetch('UYUNI_BENCH_PREPARE_TIMEOUT', '7200').to_i

  command = "cd #{UYUNI_BENCH_POD_DIR} && ./uyuni-bench repodata prepare " \
            "--dataset #{dataset} --repo-dir #{repo_dir} --workers #{workers}"
  command += " --limit #{ENV.fetch('UYUNI_BENCH_LIMIT').to_i}" if ENV['UYUNI_BENCH_LIMIT']
  command += ' --force' if ENV.fetch('UYUNI_BENCH_FORCE_PREPARE', 'false') == 'true'
  command += ' --verify-checksum' if ENV.fetch('UYUNI_BENCH_VERIFY_CHECKSUM', 'false') == 'true'

  uyuni_bench_run_in_server_pod(command, timeout: timeout)
end

When('I run the repodata generation benchmark') do
  @uyuni_bench_results_dir = uyuni_bench_results_dir
  @uyuni_bench_summary_path = File.join(@uyuni_bench_results_dir, 'summary.json')

  repo_dir = Shellwords.escape(uyuni_bench_repo_dir)
  results_dir = Shellwords.escape(@uyuni_bench_results_dir)
  storage_backend = Shellwords.escape(uyuni_bench_storage_backend)
  mode = Shellwords.escape(ENV.fetch('UYUNI_BENCH_REPODATA_MODE', 'full'))
  iterations = ENV.fetch('UYUNI_BENCH_ITERATIONS', '1').to_i
  timeout = ENV.fetch('UYUNI_BENCH_RUN_TIMEOUT', '7200').to_i

  command = "cd #{UYUNI_BENCH_POD_DIR} && ./uyuni-bench repodata run " \
            "--repo-dir #{repo_dir} --results-dir #{results_dir} " \
            "--storage-backend #{storage_backend} --mode #{mode} --iterations #{iterations}"
  command += ' --drop-caches' if ENV.fetch('UYUNI_BENCH_DROP_CACHES', 'false') == 'true'

  uyuni_bench_run_in_server_pod(command, timeout: timeout)
end

Then('the repodata generation benchmark should finish successfully') do
  raise ScriptError, 'Repodata benchmark summary path is not defined' if @uyuni_bench_summary_path.nil?

  summary_path = Shellwords.escape(@uyuni_bench_summary_path)
  summary_json, code = uyuni_bench_run_in_server_pod("cat #{summary_path}", check_errors: false, verbose: false)
  raise ScriptError, "Repodata benchmark summary not found at #{@uyuni_bench_summary_path}" unless code.zero?

  summary = JSON.parse(summary_json)
  failed = summary.fetch('results', []).find { |result| result.dig('run', 'exit_code').to_i != 0 }
  raise ScriptError, "Repodata benchmark failed: #{failed}" unless failed.nil?

  durations = summary.fetch('results', []).map { |result| result.dig('run', 'duration_seconds') }
  log "Repodata benchmark summary: #{@uyuni_bench_summary_path}"
  log "Repodata benchmark durations: #{durations.join(', ')} seconds"
end
