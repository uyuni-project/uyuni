# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

### Step definitions for PostgreSQL database storage benchmark workloads.

require 'base64'
require 'json'
require 'shellwords'
require 'time'

UYUNI_BENCH_PG_DEFAULT_RESULTS_PARENT = '/tmp/uyuni-bench/results/postgres'.freeze

# Storage backend label stored in the benchmark summary.
def postgres_benchmark_storage_backend
  ENV.fetch('UYUNI_BENCH_STORAGE_BACKEND', 'unknown')
end

# Kubernetes namespace where Uyuni is deployed.
def postgres_benchmark_namespace
  ENV.fetch('UYUNI_BENCH_NAMESPACE', 'uyuni')
end

# Database name used for pgbench workload execution.
# Strictly rejects any production or critical database names.
def postgres_benchmark_db_name
  name = ENV.fetch('UYUNI_BENCH_PG_DB', 'uyuni_bench_pgbench').strip
  forbidden = %w[susemanager reportdb postgres template0 template1]
  if forbidden.include?(name.downcase) || !name.match?(/\A[a-zA-Z0-9_]+\z/)
    raise ScriptError, "Invalid or prohibited benchmark database name: #{name.inspect}"
  end

  name
end

# Parse an integer environment variable and raise a clear error for invalid values.
def postgres_benchmark_integer_env(name, default, minimum:)
  value = ENV.fetch(name, default).to_s
  valid_value = value.match?(/\A\d+\z/) && value.to_i >= minimum
  raise ScriptError, "#{name} must be an integer >= #{minimum}, got #{value.inspect}" unless valid_value

  value.to_i
end

# Scaling factor for pgbench schema initialization (default: 10).
def postgres_benchmark_scale
  postgres_benchmark_integer_env('UYUNI_BENCH_PG_SCALE', '10', minimum: 1)
end

# Number of concurrent database clients (default: 10).
def postgres_benchmark_clients
  postgres_benchmark_integer_env('UYUNI_BENCH_PG_CLIENTS', '10', minimum: 1)
end

# Number of worker threads (default: 2).
def postgres_benchmark_threads
  postgres_benchmark_integer_env('UYUNI_BENCH_PG_THREADS', '2', minimum: 1)
end

# Duration in seconds for each benchmark phase (default: 60).
def postgres_benchmark_duration
  postgres_benchmark_integer_env('UYUNI_BENCH_PG_DURATION', '60', minimum: 5)
end

# Overall execution timeout for benchmark commands.
def postgres_benchmark_timeout
  postgres_benchmark_integer_env('UYUNI_BENCH_PG_TIMEOUT', '1800', minimum: 60)
end

# Directory for this benchmark run as seen by the database container.
def postgres_benchmark_results_dir
  return @postgres_benchmark_results_dir if @postgres_benchmark_results_dir

  @postgres_benchmark_results_dir =
    if ENV['UYUNI_BENCH_RESULTS_DIR']
      ENV['UYUNI_BENCH_RESULTS_DIR'].delete_suffix('/')
    else
      timestamp = Time.now.utc.strftime('%Y%m%d%H%M%S')
      File.join(UYUNI_BENCH_PG_DEFAULT_RESULTS_PARENT, "#{postgres_benchmark_storage_backend}-#{timestamp}")
    end
end

# Return the Kubernetes pod running the Uyuni PostgreSQL database.
def postgres_benchmark_db_pod
  return @postgres_benchmark_db_pod if @postgres_benchmark_db_pod

  command =
    Shellwords.join(
      [
        'kubectl',
        '--namespace',
        postgres_benchmark_namespace,
        'get',
        'pods',
        '--selector',
        'app.kubernetes.io/component=db',
        '--output=json'
      ]
    )
  stdout, stderr, code = get_target('localhost').run_local(
    command,
    separated_results: true,
    check_errors: false
  )
  raise ScriptError, "Unable to query the PostgreSQL database pod: #{stderr}" unless code.zero?

  pods = JSON.parse(stdout)['items']
  raise ScriptError, 'The PostgreSQL database pod response did not contain an items array' unless pods.is_a?(Array)

  ready_pods =
    pods.select do |pod|
      conditions = pod.dig('status', 'conditions')
      pod.dig('status', 'phase') == 'Running' &&
        conditions.is_a?(Array) &&
        conditions.any? { |condition| condition['type'] == 'Ready' && condition['status'] == 'True' }
    end
  raise ScriptError, "Expected exactly one ready PostgreSQL database pod, found #{ready_pods.length}" unless ready_pods.length == 1

  pod = ready_pods.first.dig('metadata', 'name')
  raise ScriptError, 'The ready PostgreSQL database pod has no metadata.name' unless pod.is_a?(String) && !pod.empty?

  @postgres_benchmark_db_pod = pod
rescue JSON::ParserError => e
  raise ScriptError, "Unable to parse the PostgreSQL database pod response: #{e.message}"
end

# Run a shell command inside the PostgreSQL database container from the testsuite controller.
def postgres_benchmark_run_in_db_pod(command, timeout: DEFAULT_TIMEOUT, verbose: true, check_errors: true)
  kubectl_command =
    Shellwords.join(
      [
        'kubectl',
        '--namespace',
        postgres_benchmark_namespace,
        'exec',
        '--container',
        'db',
        postgres_benchmark_db_pod,
        '--',
        'sh',
        '-lc',
        command
      ]
    )
  get_target('localhost').run_local(
    kubectl_command,
    timeout: timeout,
    verbose: verbose,
    check_errors: check_errors
  )
end

# Write JSON content into the database container without host path dependencies.
def postgres_benchmark_write_json_in_db_pod(path, payload)
  encoded = Base64.strict_encode64(JSON.pretty_generate(payload))
  postgres_benchmark_run_in_db_pod(
    "mkdir -p #{Shellwords.escape(File.dirname(path))} && " \
    "printf '%s' #{Shellwords.escape(encoded)} | base64 -d > #{Shellwords.escape(path)}"
  )
end

# Parse standard pgbench metrics from raw stdout.
def parse_pgbench_output(output)
  metrics = {}

  if (m = output.match(/tps = ([0-9.]+) \((?:without initial connection time|excluding connections establishing)\)/))
    metrics[:tps_excluding_conn] = m[1].to_f
  end
  if (m = output.match(/tps = ([0-9.]+) \((?:with initial connection time|including connections establishing)\)/))
    metrics[:tps_including_conn] = m[1].to_f
  end
  if !metrics[:tps_excluding_conn] && (m = output.match(/tps = ([0-9.]+)/))
    metrics[:tps_excluding_conn] = m[1].to_f
  end

  if (m = output.match(/latency average = ([0-9.]+) ms/))
    metrics[:latency_avg_ms] = m[1].to_f
  end
  if (m = output.match(/latency stddev = ([0-9.]+) ms/))
    metrics[:latency_stddev_ms] = m[1].to_f
  end
  if (m = output.match(/number of transactions actually processed: (\d+)/))
    metrics[:transactions_processed] = m[1].to_i
  end
  if (m = output.match(/number of failed transactions: (\d+)/))
    metrics[:failed_transactions] = m[1].to_i
  end

  metrics
end

Given('the database benchmark inputs are valid') do
  @postgres_benchmark_results = {}

  log "PostgreSQL storage benchmark settings:"
  log "  Database: #{postgres_benchmark_db_name}"
  log "  Scale factor: #{postgres_benchmark_scale}"
  log "  Clients: #{postgres_benchmark_clients}"
  log "  Threads: #{postgres_benchmark_threads}"
  log "  Duration: #{postgres_benchmark_duration} seconds"
  log "  Storage backend: #{postgres_benchmark_storage_backend}"
end

Given('the PostgreSQL database pod is ready in the uyuni namespace') do
  pod = postgres_benchmark_db_pod
  log "Target database pod: #{pod}"

  _output, code = postgres_benchmark_run_in_db_pod('command -v pgbench && command -v psql', check_errors: false, verbose: false)
  raise ScriptError, 'pgbench or psql tool is missing in the database container' unless code.zero?
end

When('I initialize the pgbench benchmark database schema') do
  db = postgres_benchmark_db_name
  scale = postgres_benchmark_scale
  results_dir = Shellwords.escape(postgres_benchmark_results_dir)

  init_command = [
    "mkdir -p #{results_dir}",
    "dropdb -U \"${POSTGRES_USER:-postgres}\" --if-exists #{Shellwords.escape(db)}",
    "createdb -U \"${POSTGRES_USER:-postgres}\" #{Shellwords.escape(db)}",
    "pgbench -U \"${POSTGRES_USER:-postgres}\" -i -s #{scale} #{Shellwords.escape(db)}"
  ].join(' && ')

  started_monotonic = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  _output, code = postgres_benchmark_run_in_db_pod(
    init_command,
    timeout: postgres_benchmark_timeout,
    check_errors: false
  )
  elapsed = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started_monotonic

  raise ScriptError, "Failed to initialize pgbench schema in #{db} (exit code: #{code})" unless code.zero?

  log "Initialized pgbench schema in #{db} with scale #{scale} in #{elapsed.round(2)}s"
end

When('I run the read-write transaction benchmark') do
  db = postgres_benchmark_db_name
  clients = postgres_benchmark_clients
  threads = postgres_benchmark_threads
  duration = postgres_benchmark_duration
  results_dir = Shellwords.escape(postgres_benchmark_results_dir)
  stdout_path = File.join(postgres_benchmark_results_dir, 'pgbench_read_write.stdout.log')
  stderr_path = File.join(postgres_benchmark_results_dir, 'pgbench_read_write.stderr.log')

  bench_command = "mkdir -p #{results_dir} && " \
                  "pgbench -U \"${POSTGRES_USER:-postgres}\" -c #{clients} -j #{threads} -T #{duration} -r #{Shellwords.escape(db)} " \
                  "> #{Shellwords.escape(stdout_path)} 2> #{Shellwords.escape(stderr_path)}"

  started_at = Time.now.utc
  started_monotonic = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  _output, code = postgres_benchmark_run_in_db_pod(
    bench_command,
    timeout: postgres_benchmark_timeout,
    check_errors: false
  )
  elapsed = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started_monotonic
  finished_at = Time.now.utc

  raise ScriptError, "Read-write pgbench benchmark failed with exit code #{code}" unless code.zero?

  stdout_content, = postgres_benchmark_run_in_db_pod("cat #{Shellwords.escape(stdout_path)}")
  metrics = parse_pgbench_output(stdout_content)

  @postgres_benchmark_results[:read_write] = {
    workload: 'read_write_tpc_b',
    started_at: started_at.iso8601,
    finished_at: finished_at.iso8601,
    duration_seconds: elapsed.round(3),
    clients: clients,
    threads: threads,
    scale: postgres_benchmark_scale,
    metrics: metrics,
    stdout_path: stdout_path,
    stderr_path: stderr_path
  }

  log "Read-Write Complete: TPS = #{metrics[:tps_excluding_conn]}, Avg Latency = #{metrics[:latency_avg_ms]} ms"
end

When('I run the read-only transaction benchmark') do
  db = postgres_benchmark_db_name
  clients = postgres_benchmark_clients
  threads = postgres_benchmark_threads
  duration = postgres_benchmark_duration
  results_dir = Shellwords.escape(postgres_benchmark_results_dir)
  stdout_path = File.join(postgres_benchmark_results_dir, 'pgbench_read_only.stdout.log')
  stderr_path = File.join(postgres_benchmark_results_dir, 'pgbench_read_only.stderr.log')

  bench_command = "mkdir -p #{results_dir} && " \
                  "pgbench -U \"${POSTGRES_USER:-postgres}\" -S -c #{clients} -j #{threads} -T #{duration} -r #{Shellwords.escape(db)} " \
                  "> #{Shellwords.escape(stdout_path)} 2> #{Shellwords.escape(stderr_path)}"

  started_at = Time.now.utc
  started_monotonic = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  _output, code = postgres_benchmark_run_in_db_pod(
    bench_command,
    timeout: postgres_benchmark_timeout,
    check_errors: false
  )
  elapsed = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started_monotonic
  finished_at = Time.now.utc

  raise ScriptError, "Read-only pgbench benchmark failed with exit code #{code}" unless code.zero?

  stdout_content, = postgres_benchmark_run_in_db_pod("cat #{Shellwords.escape(stdout_path)}")
  metrics = parse_pgbench_output(stdout_content)

  @postgres_benchmark_results[:read_only] = {
    workload: 'read_only_select',
    started_at: started_at.iso8601,
    finished_at: finished_at.iso8601,
    duration_seconds: elapsed.round(3),
    clients: clients,
    threads: threads,
    scale: postgres_benchmark_scale,
    metrics: metrics,
    stdout_path: stdout_path,
    stderr_path: stderr_path
  }

  log "Read-Only Complete: TPS = #{metrics[:tps_excluding_conn]}, Avg Latency = #{metrics[:latency_avg_ms]} ms"
end

Then('the database benchmark result report should exist and be valid') do
  raise ScriptError, 'No benchmark results captured' if @postgres_benchmark_results.nil? || @postgres_benchmark_results.empty?
  raise ScriptError, 'Read-write benchmark results missing' unless @postgres_benchmark_results[:read_write]
  raise ScriptError, 'Read-only benchmark results missing' unless @postgres_benchmark_results[:read_only]

  summary_path = File.join(postgres_benchmark_results_dir, 'summary.json')
  summary = {
    benchmark: 'postgresql_pgbench',
    storage_backend: postgres_benchmark_storage_backend,
    results_dir: postgres_benchmark_results_dir,
    database_name: postgres_benchmark_db_name,
    scale_factor: postgres_benchmark_scale,
    results: @postgres_benchmark_results
  }

  postgres_benchmark_write_json_in_db_pod(summary_path, summary)

  _output, code = postgres_benchmark_run_in_db_pod("test -s #{Shellwords.escape(summary_path)}", check_errors: false, verbose: false)
  raise ScriptError, "Benchmark summary #{summary_path} is missing or empty" unless code.zero?

  rw_tps = @postgres_benchmark_results.dig(:read_write, :metrics, :tps_excluding_conn)
  ro_tps = @postgres_benchmark_results.dig(:read_only, :metrics, :tps_excluding_conn)
  rw_lat = @postgres_benchmark_results.dig(:read_write, :metrics, :latency_avg_ms)
  ro_lat = @postgres_benchmark_results.dig(:read_only, :metrics, :latency_avg_ms)

  log '=========================================================='
  log 'Uyuni PostgreSQL Database Storage Benchmark Summary'
  log "Storage Backend: #{postgres_benchmark_storage_backend}"
  log "Read-Write Workload: #{rw_tps} TPS | #{rw_lat} ms latency"
  log "Read-Only Workload:  #{ro_tps} TPS | #{ro_lat} ms latency"
  log "Benchmark Report:    #{summary_path}"
  log '=========================================================='
end

Then('I clean up the pgbench benchmark database') do
  db = postgres_benchmark_db_name
  cleanup_command = "dropdb -U \"${POSTGRES_USER:-postgres}\" --if-exists #{Shellwords.escape(db)}"
  _output, code = postgres_benchmark_run_in_db_pod(cleanup_command, check_errors: false)
  raise ScriptError, "Failed to clean up benchmark database #{db}" unless code.zero?

  log "Successfully cleaned up benchmark database #{db}"
end
