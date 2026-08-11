#!/usr/bin/env ruby
# Copyright (c) 2010-2026 SUSE LLC
# Licensed under the terms of the MIT license.

# External protocol-aware supervisor for the Playwright driver process (BUG-031).
#
# Sits between capybara-playwright-driver's Transport and the real `playwright` Node driver,
# relaying the wire protocol ([4-byte LE length][UTF-8 JSON]) unmodified in both directions.
# For every outbound message that declares its own `metadata.timeout`, tracks a deadline; if
# that deadline elapses with no matching response, synthesizes a timeout-error frame for that
# call's id and SIGKILLs the driver's whole process group.
#
# This exists because a wedged blocking native pipe read starves env.rb's Layer-3 Ruby
# watchdog thread of the GVL, so that thread is scheduled to fire but never actually runs (see
# BUG-031: zero WATCHDOG log lines on a build that hung 10.4h). Running as a separate OS
# process is immune to that starvation.
#
# Messages with no metadata.timeout are forwarded with no deadline tracked - left entirely to
# the existing Layer-3 SCENARIO_HARD_LIMIT watchdog in env.rb.
#
# Disable/pass-through: set PLAYWRIGHT_SUPERVISOR_DISABLED to skip all of this and exec the
# real driver directly, byte-for-byte, with zero added behavior. Sole rollback mechanism.

require 'json'
require 'open3'

DRIVER_PATH = ENV.fetch('PLAYWRIGHT_CLI_EXECUTABLE_PATH', '/usr/local/bin/playwright')
POLL_INTERVAL = 0.05 # seconds between deadline sweeps

exec(DRIVER_PATH, *ARGV) if ENV['PLAYWRIGHT_SUPERVISOR_DISABLED']

# Reads one [4-byte LE length][JSON payload] frame; returns [raw_bytes, payload] or nil at EOF.
def read_frame(io)
  length_prefix = io.read(4)
  return unless length_prefix && length_prefix.bytesize == 4

  length = length_prefix.unpack1('V')
  payload = io.read(length)
  return unless payload && payload.bytesize == length

  [length_prefix + payload, payload]
end

# Encodes a message hash as one [4-byte LE length][JSON payload] wire frame.
def build_frame(message)
  payload = JSON.generate(message)
  [payload.bytesize].pack('V') + payload
end

# Builds the synthetic error frame sent upstream in place of a response that never arrived.
def timeout_error_frame(id, entry)
  build_frame(
    'id' => id,
    'error' => {
      'error' => {
        'name' => 'TimeoutError',
        'message' => "Supervisor: #{entry[:method]}(#{entry[:guid]}) exceeded its declared timeout of #{entry[:timeout_ms]}ms"
      }
    }
  )
end

# SIGKILLs pid's whole process group (pid == pgid, since the driver was spawned with pgroup: true).
def kill_process_group(pid)
  Process.kill('KILL', -pid)
rescue Errno::ESRCH, Errno::EPERM
  nil # already gone or permission denied (race condition)
end

$stdin.binmode
$stdout.binmode

# pgroup: true (matching capybara-playwright-driver's own spawn of the real driver) makes the
# child's pid double as its process group id, so `Process.kill('KILL', -pid)` below reaches the
# whole Chromium/Node subtree, not just the immediate child.
driver_stdin, driver_stdout, driver_stderr, wait_thr = Open3.popen3(DRIVER_PATH, *ARGV, pgroup: true)
driver_stdin.binmode
driver_stdout.binmode

# Drain and forward stderr unconditionally: an unread pipe fills once the driver logs enough
# (Node warnings, a crash trace) and blocks the driver's write, wedging it exactly like the bug
# this supervisor exists to catch. Forwarding (rather than just discarding) also keeps
# capybara-playwright-driver's own "undefined:1" crash-signature detection working, since
# Transport now watches our stderr instead of the real driver's.
Thread.new do
  IO.copy_stream(driver_stderr, $stderr)
rescue IOError
  nil
end

pending = {}
pending_mutex = Mutex.new
stdout_mutex = Mutex.new

# Ruby -> driver: parse each outbound message just enough to note its deadline (if it declares
# one), then forward the exact original bytes downstream unmodified.
stdin_pump =
  Thread.new do
    loop do
      frame = read_frame($stdin)
      break unless frame

      frame_bytes, payload = frame
      begin
        message = JSON.parse(payload)
        timeout_ms = message.dig('metadata', 'timeout')
        id = message['id']
        if timeout_ms && id
          pending_mutex.synchronize do
            pending[id] = {
              deadline: Process.clock_gettime(Process::CLOCK_MONOTONIC) + (timeout_ms / 1000.0),
              method: message['method'],
              guid: message['guid'],
              timeout_ms: timeout_ms
            }
          end
        end
      rescue JSON::ParserError
        nil # forward unmodified regardless; nothing to track
      end

      driver_stdin.write(frame_bytes)
      driver_stdin.flush
    end
  rescue IOError, Errno::EPIPE
    nil
  ensure
    driver_stdin.close unless driver_stdin.closed?
  end

# driver -> Ruby: clear the deadline for any id that got a real response, then forward the
# exact original bytes upstream unmodified.
stdout_pump =
  Thread.new do
    loop do
      frame = read_frame(driver_stdout)
      break unless frame

      frame_bytes, payload = frame
      begin
        id = JSON.parse(payload)['id']
        pending_mutex.synchronize { pending.delete(id) } if id
      rescue JSON::ParserError
        nil
      end

      stdout_mutex.synchronize do
        $stdout.write(frame_bytes)
        $stdout.flush
      end
    end
  rescue IOError, Errno::EPIPE
    nil
  end

# Deadline sweep: runs on the main thread so it can exit(1) directly once it intervenes.
loop do
  break unless stdin_pump.alive? && stdout_pump.alive?

  expired =
    pending_mutex.synchronize do
      now = Process.clock_gettime(Process::CLOCK_MONOTONIC)
      ids = pending.keys.select { |id| pending[id][:deadline] <= now }
      ids.map { |id| [id, pending.delete(id)] }
    end

  unless expired.empty?
    expired.each do |id, entry|
      warn "PLAYWRIGHT_SUPERVISOR: #{entry[:method]}(#{entry[:guid]}) exceeded its declared timeout of " \
           "#{entry[:timeout_ms]}ms - killing driver process group #{wait_thr.pid}"
      stdout_mutex.synchronize do
        $stdout.write(timeout_error_frame(id, entry))
        $stdout.flush
      end
    end
    kill_process_group(wait_thr.pid)
    exit(1)
  end

  sleep POLL_INTERVAL
end

stdin_pump.join
stdout_pump.join
exit(wait_thr.value.exitstatus || 1)
