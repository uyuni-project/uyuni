# Copyright (c) 2010-2026 SUSE LLC
# Licensed under the terms of the MIT license.

# Standalone process-boundary test for the BUG-031 fix (see
# features/support/playwright_driver_supervisor.rb). No real Playwright/Node/Chromium needed:
# a small fixture "fake driver" (test/fixtures/fake_driver.rb) speaks the identical wire
# framing, and this test plays the role of the "Ruby side" that capybara-playwright-driver's
# Transport would normally be.
#
# Run: ruby test/playwright_driver_supervisor_test.rb

require 'io/wait'
require 'json'
require 'minitest/autorun'
require 'open3'
require 'tempfile'

SUPERVISOR = File.expand_path('../features/support/playwright_driver_supervisor.rb', __dir__)
FIXTURE_DRIVER = File.expand_path('fixtures/fake_driver.rb', __dir__)

class PlaywrightDriverSupervisorTest < Minitest::Test
  def setup
    @pid_file = Tempfile.new('fake_driver_pid')
    @stdin, @stdout, @wait_thr = spawn_supervisor
  end

  def teardown
    @stdin.close if @stdin && !@stdin.closed?
    Process.kill('KILL', -@wait_thr.pid) if @wait_thr
  rescue Errno::ESRCH, IOError
    nil
  ensure
    @wait_thr&.join
    @pid_file&.unlink
  end

  def test_call_completing_before_its_deadline_passes_through_unmodified
    write_frame(@stdin, id: 1, guid: 'page@1', method: 'click', params: { delayMs: 10 }, metadata: { timeout: 2000 })

    response = read_frame(@stdout)

    assert_equal 1, response['id']
    assert_equal 'page@1', response['echoGuid']
    refute response.key?('error')
    assert driver_alive?, 'a call that completes in time must not trigger a kill'
  end

  def test_call_exceeding_its_declared_timeout_gets_a_synthetic_error_and_kills_the_driver
    write_frame(@stdin, id: 2, guid: 'page@2', method: 'waitForSelector', params: { hang: true }, metadata: { timeout: 300 })

    response = read_frame(@stdout, timeout: 3)

    refute_nil response, 'expected a synthetic timeout frame'
    assert_equal 2, response['id']
    assert_equal 'TimeoutError', response.dig('error', 'error', 'name')

    sleep 0.2 # give the SIGKILL a moment to land
    refute driver_alive?, 'expected the fixture driver process group to be terminated'
  end

  def test_call_with_no_declared_timeout_is_left_to_layer_3
    write_frame(@stdin, id: 3, guid: 'page@3', method: 'waitForSelector', params: { hang: true })

    response = read_frame(@stdout, timeout: 1)

    assert_nil response, 'supervisor must not intervene on calls with no metadata.timeout'
    assert driver_alive?, 'supervisor must stay passive - only Layer 3 handles this case'
  end

  def test_concurrent_calls_only_the_timed_out_one_gets_a_synthetic_frame
    write_frame(@stdin, id: 4, guid: 'page@4', method: 'click', params: { delayMs: 50 }, metadata: { timeout: 2000 })
    write_frame(@stdin, id: 5, guid: 'page@5', method: 'waitForSelector', params: { hang: true }, metadata: { timeout: 300 })

    responses = [read_frame(@stdout, timeout: 3), read_frame(@stdout, timeout: 3)].compact

    normal = responses.find { |r| r['id'] == 4 }
    timed_out = responses.find { |r| r['id'] == 5 }
    refute_nil normal, 'the call that completed in time must still get its real response'
    refute normal.key?('error')
    refute_nil timed_out, 'the hung call must get its synthetic timeout frame'
    assert_equal 'TimeoutError', timed_out.dig('error', 'error', 'name')
  end

  def test_a_noisy_driver_does_not_block_on_undrained_stderr
    write_frame(@stdin, id: 8, guid: 'page@8', method: 'click', params: { stderrBytes: 500_000 }, metadata: { timeout: 2000 })

    response = read_frame(@stdout, timeout: 3)

    refute_nil response, 'the driver must not wedge writing to stderr just because nothing reads it'
    assert_equal 8, response['id']
    refute response.key?('error')
  end

  def test_disable_env_var_execs_the_real_driver_with_byte_for_byte_passthrough
    @stdin.close
    begin
      Process.kill('KILL', -@wait_thr.pid)
    rescue Errno::ESRCH
      nil
    end
    @wait_thr.join

    @stdin, @stdout, @wait_thr = spawn_supervisor('PLAYWRIGHT_SUPERVISOR_DISABLED' => '1')

    write_frame(@stdin, id: 6, guid: 'page@6', method: 'waitForSelector', params: { hang: true }, metadata: { timeout: 300 })
    sleep 0.5 # past what would have been the declared deadline under the enabled supervisor
    write_frame(@stdin, id: 7, guid: 'page@7', method: 'click', params: { delayMs: 10 })

    response = read_frame(@stdout, timeout: 3)

    assert_equal 7, response['id'], 'id 6 never responds (hang) - passthrough means no intervention, so id 7 arrives untouched'
  end

  private

  def spawn_supervisor(extra_env = {})
    env = { 'PLAYWRIGHT_CLI_EXECUTABLE_PATH' => FIXTURE_DRIVER, 'FAKE_DRIVER_PID_FILE' => @pid_file.path }.merge(extra_env)
    stdin, stdout, stderr, wait_thr = Open3.popen3(env, SUPERVISOR, 'run-driver', pgroup: true)
    stdin.binmode
    stdout.binmode
    # Drain the supervisor's own stderr, same as capybara-playwright-driver's Transport does in
    # production - otherwise an unread pipe here would recreate test/fixtures/fake_driver.rb's
    # stderrBytes scenario one level up, in the test harness itself.
    Thread.new { IO.copy_stream(stderr, File::NULL) }
    [stdin, stdout, wait_thr]
  end

  def write_frame(io, message)
    payload = JSON.generate(message)
    io.write([payload.bytesize].pack('V') + payload)
    io.flush
  end

  def read_frame(io, timeout: 5)
    return unless io.wait_readable(timeout)

    length_prefix = io.read(4)
    return unless length_prefix && length_prefix.bytesize == 4

    JSON.parse(io.read(length_prefix.unpack1('V')))
  end

  def driver_alive?
    deadline = Time.now + 2
    pid = nil
    loop do
      content = File.read(@pid_file.path)
      pid = content.to_i
      break if pid.positive? || Time.now > deadline
    end
    return false if pid.nil? || pid.zero?

    # A SIGKILLed process lingers as a zombie until its (already-exited) parent would reap it,
    # and kill(pid, 0) still succeeds against a zombie - so check /proc state, not just existence.
    state = File.read("/proc/#{pid}/stat").rpartition(')').last.strip.split.first
    state != 'Z'
  rescue Errno::ENOENT
    false
  end
end
