#!/usr/bin/env ruby
# Copyright (c) 2010-2026 SUSE LLC
# Licensed under the terms of the MIT license.

# Fake Playwright driver for supervisor testing. Speaks the identical wire protocol
# ([4-byte LE length][JSON payload]) as the real driver, but implements just enough
# behavior to test the supervisor's timeout and kill logic.

require 'json'

def read_frame(io)
  length_prefix = io.read(4)
  return unless length_prefix && length_prefix.bytesize == 4

  length = length_prefix.unpack1('V')
  payload = io.read(length)
  return unless payload && payload.bytesize == length

  JSON.parse(payload)
end

def write_frame(io, message)
  payload = JSON.generate(message)
  io.write([payload.bytesize].pack('V') + payload)
  io.flush
end

$stdin.binmode
$stdout.binmode

# Write our PID to the file so tests can check if we're still alive
if ENV['FAKE_DRIVER_PID_FILE']
  File.write(ENV['FAKE_DRIVER_PID_FILE'], $$.to_s)
end

# Process incoming messages and respond (or hang, if requested)
loop do
  message = read_frame($stdin)
  break unless message

  params = message['params'] || {}
  id = message['id']

  # Drain requested stderr bytes (used to test backpressure handling)
  if params['stderrBytes']&.positive?
    $stderr.write('X' * params['stderrBytes'])
    $stderr.flush
  end

  # If hang is requested, just stop responding (supervisor should kill us)
  next if params['hang']

  # Otherwise echo back a response
  response = {
    'id' => id,
    'echoGuid' => message['guid']
  }

  # Simulate a delay if requested
  if params['delayMs']&.positive?
    sleep(params['delayMs'] / 1000.0)
  end

  write_frame($stdout, response)
end
