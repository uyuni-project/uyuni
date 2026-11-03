# Copyright (c) 2024 SUSE LLC.
# SPDX-License-Identifier: MIT
require_relative 'metrics_collector_handler'

# Quality Intelligence handler to produce, parse and report Quality Intelligence from the test suite
class QualityIntelligence
  QI = 'quality_intelligence'.freeze
  private_constant :QI

  # Initialize the QualityIntelligence handler
  def initialize
    @metrics_collector_handler = MetricsCollectorHandler.new(ENV.fetch('PROMETHEUS_PUSH_GATEWAY_URL', 'http://nsa.mgr.suse.de:9091'))
    @environment = ENV.fetch('SERVER', nil)
  end

  # Report the time to complete a bootstrap of a system passed as parameter,
  # raising an error if the Metrics Collector request fails.
  #
  # @param system [String] the system to bootstrap
  # @param time [Integer] the time to complete the bootstrap in seconds
  # @return [void]
  def push_bootstrap_duration(system, time)
    @metrics_collector_handler.push_metrics(QI, 'system_bootstrap_duration_seconds', time, { :system => system, :environment => @environment })
  end

  # Report the time to complete the onboarding of a system passed as parameter,
  # raising an error if the Metrics Collector request fails.
  #
  # @param system [String] the system to be onboarded
  # @param time [Integer] the time to complete the onboarding in seconds
  # @return [void]
  def push_onboarding_duration(system, time)
    @metrics_collector_handler.push_metrics(QI, 'system_onboarding_duration_seconds', time, { :system => system, :environment => @environment })
  end

  # Report the time to complete a synchronization of a product passed as parameter,
  # raising an error if the Metrics Collector request fails.
  #
  # @param product [String] the product to synchronize
  # @param time [Integer] the time to complete the synchronization in seconds
  # @return [void]
  def push_synchronization_duration(product, time)
    @metrics_collector_handler.push_metrics(QI, 'product_synch_duration_seconds', time, { :system => product, :environment => @environment })
  end
end
