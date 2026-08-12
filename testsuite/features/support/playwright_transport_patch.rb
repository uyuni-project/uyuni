# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Monkey-patch for playwright-ruby-client 1.60.0.
#
# Fixes two bugs in Playwright::Transport that together cause the testsuite to
# hang indefinitely after a Cucumber/Ruby timeout fires mid-Playwright-call:
#
# 1. Torn protocol frames: send_message wrote the 4-byte length header and the
#    JSON payload in two separate IO#write calls. Timeout.timeout (used by
#    repeat_until_timeout) delivers Thread#raise asynchronously; if it landed
#    between those two writes, Node.js received a partial frame and crashed with:
#      SyntaxError: Unexpected token (non-ASCII diamond), e.g. in JSON at position 0
#    Fix: build the whole frame in one buffer and write it in a single call,
#    shielded by Thread.handle_interrupt so async exceptions are deferred until
#    the frame is fully on the wire.
#
# 2. Clean EOF not signalled: handle_stdout's while loop exited silently when
#    read(4) returned nil (normal EOF after the Node.js process was killed).
#    on_driver_closed was never called in the normal-exit path - only on IOError -
#    so all pending callbacks waited forever and the run hung for the full Jenkins
#    job timeout (~10 hours). Fix: call on_driver_closed after the loop exits.
#
# 3. Crash detection missed modern Node.js: handle_stderr only matched
#    'undefined:1' (Node <= 14). Modern Node prints '<anonymous_script>:1', so
#    on_driver_crashed never fired, again leaving pending callbacks unresolved.
#    Fix: match both formats and the JSON parse error message directly.
#
# TODO: remove once fixed upstream: https://github.com/YusukeIwaki/playwright-ruby-client

require 'playwright'

# rubocop:disable Style/Documentation
module Playwright
  # rubocop:disable Style/DocumentationMethod
  class Transport
    def send_message(message)
      debug_send_message(message) if @debug
      msg = JSON.dump(message)
      # Single buffer: 4-byte LE length header + payload. Written atomically so a
      # Thread#raise (e.g. Timeout::Error from repeat_until_timeout) cannot tear
      # the frame and corrupt the Node.js driver's JSON parse stream.
      frame = [msg.bytesize].pack('V') + msg.b
      Thread.handle_interrupt(Object => :never) do
        @mutex.synchronize { @stdin.write(frame) }
      end
    rescue Errno::EPIPE, IOError
      raise AlreadyDisconnectedError, 'send_message failed'
    end

    private

    def handle_stdout(packet_size: 32_768)
      while (chunk = @stdout.read(4))
        length = chunk.unpack1('V')
        buffer = StringIO.new
        (length / packet_size).to_i.times { buffer << @stdout.read(packet_size) }
        buffer << @stdout.read(length % packet_size)
        buffer.rewind
        obj = JSON.parse(buffer.read)
        debug_recv_message(obj) if @debug
        @on_message&.call(obj)
      end
      # Normal EOF: the Node.js driver process exited. Signal closure so all
      # pending callbacks are rejected immediately instead of waiting forever.
      @on_driver_closed&.call
    rescue IOError
      @on_driver_closed&.call
    end

    def handle_stderr
      while (err = @stderr.read)
        # Node <= 14 prints 'undefined:1'; modern Node prints '<anonymous_script>:1'
        # when the driver dies parsing a corrupted frame.
        if err.include?('undefined:1') || err.include?('<anonymous_script>:1') ||
           err.include?("is not valid JSON\n    at JSON.parse")

          $stderr.write(err)
          @on_driver_crashed&.call
          break
        end
        $stderr.write(err)
      end
    rescue IOError
      @on_driver_closed&.call
    end
  end
  # rubocop:enable Style/DocumentationMethod
end
# rubocop:enable Style/Documentation
