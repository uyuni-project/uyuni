# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'minitest/autorun'

DEFAULT_TIMEOUT = 1 unless defined?(DEFAULT_TIMEOUT)

require_relative '../features/support/remote_node'

# Focused regression coverage for public address route fallback validation.
class RemoteNodeTest < Minitest::Test
  ROUTE_COMMAND = 'ip -4 route get 1.1.1.1'.freeze
  private_constant :ROUTE_COMMAND

  def build_node(route:, route_code: 0, fixed_responses: {})
    node = RemoteNode.allocate
    node.host = 'external_controller'
    node.os_family = 'Linux'
    commands = []
    route_command = ROUTE_COMMAND

    node.define_singleton_method(:run_local) do |command, **|
      commands << command
      if fixed_responses.key?(command)
        fixed_responses[command]
      elsif command == route_command
        [route, route_code]
      else
        ['', 1]
      end
    end

    [node, commands]
  end

  def resolve_public_ip(node)
    node.__send__(:client_public_ip)
  end

  def test_known_interface_probe_takes_precedence
    probe = "ip address show dev ens4 | grep 'inet '"
    node, commands = build_node(
      route: '1.1.1.1 dev route0 src 192.0.2.10',
      fixed_responses: { probe => ["    inet 198.51.100.20/24 scope global ens4\n", 0] }
    )

    assert_equal '198.51.100.20', resolve_public_ip(node)
    assert_equal 'ens4', node.public_interface
    refute_includes commands, ROUTE_COMMAND
  end

  def test_route_fallback_accepts_canonical_ipv4_and_safe_interface
    node, commands = build_node(route: '1.1.1.1 via 192.0.2.1 dev enp6s0.100 src 192.0.2.10 uid 0')

    assert_equal '192.0.2.10', resolve_public_ip(node)
    assert_equal 'enp6s0.100', node.public_interface
    assert_equal ROUTE_COMMAND, commands.last
  end

  def test_route_fallback_accepts_15_character_interface
    node, = build_node(route: '1.1.1.1 dev abcdefghijklmno src 192.0.2.10')

    assert_equal '192.0.2.10', resolve_public_ip(node)
    assert_equal 'abcdefghijklmno', node.public_interface
  end

  def test_route_fallback_rejects_noncanonical_or_non_ipv4_sources
    ['192.000.2.10', '192.0.2.10/32', '2001:db8::10', 'not-an-address'].each do |source|
      node, = build_node(route: "1.1.1.1 dev route0 src #{source}")

      error = assert_raises(ArgumentError) { resolve_public_ip(node) }
      assert_equal 'Cannot resolve public ip of external_controller', error.message
      assert_nil node.public_interface
    end
  end

  def test_route_fallback_rejects_unsafe_or_oversized_interfaces
    ['eth0;id', 'eth0$(id)', '-eth0', 'eth0/peer', 'abcdefghijklmnop'].each do |device|
      node, = build_node(route: "1.1.1.1 dev #{device} src 192.0.2.10")

      error = assert_raises(ArgumentError) { resolve_public_ip(node) }
      assert_equal 'Cannot resolve public ip of external_controller', error.message
      assert_nil node.public_interface
    end
  end

  def test_route_fallback_preserves_failure_for_missing_fields_or_command_error
    [
      ['1.1.1.1 dev route0', 0],
      ['1.1.1.1 src 192.0.2.10', 0],
      ['1.1.1.1 dev route0 src 192.0.2.10', 1]
    ].each do |route, route_code|
      node, = build_node(route: route, route_code: route_code)

      error = assert_raises(ArgumentError) { resolve_public_ip(node) }
      assert_equal 'Cannot resolve public ip of external_controller', error.message
      assert_nil node.public_interface
    end
  end
end
