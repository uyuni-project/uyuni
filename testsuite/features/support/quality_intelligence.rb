# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.
require_relative 'database_handler'
require_relative 'prometheus_handler'

# Quality Intelligence handler to produce, parse and report Quality Intelligence from the test suite
class QualityIntelligence
  QI = 'quality_intelligence'.freeze
  private_constant :QI

  # Initialize the QualityIntelligence handler
  def initialize
    @db_handler = DatabaseHandler.new(ENV.fetch('REDIS_HOST', nil), ENV.fetch('REDIS_PORT', nil), ENV.fetch('REDIS_USERNAME', nil), ENV.fetch('REDIS_PASSWORD', nil))
    @prometheus_handler = PrometheusHandler.new(ENV.fetch('PROMETHEUS_URL', 'prometheus.mgr.suse.de:9090')) # TODO: Remove default value
    @environment = ENV.fetch('SERVER', nil)
  end

  # Report the time to complete a bootstrap of a system passed as parameter,
  # raising an error if the Prometheus request fails.
  #
  # @param system [String] the system to bootstrap
  # @param time [Integer] the time to complete the bootstrap in seconds
  # @return [void]
  def push_bootstrap_duration(system, time)
    @prometheus_handler.push_metric_to_prometheus(QI, 'system_bootstrap_duration_seconds', time, labels: { 'system' => system, 'environment' => @environment })
  end

  # Report the time to complete the onboarding of a system passed as parameter,
  # raising an error if the Prometheus request fails.
  #
  # @param system [String] the system to be onboarded
  # @param time [Integer] the time to complete the onboarding in seconds
  # @return [void]
  def push_onboarding_duration(system, time)
    @prometheus_handler.push_metric_to_prometheus(QI, 'system_onboarding_duration_seconds', time, labels: { 'system' => system, 'environment' => @environment })
  end

  # Report the time to complete a synchronization of a product passed as parameter,
  # raising an error if the Prometheus request fails.
  #
  # @param product [String] the product to synchronize
  # @param time [Integer] the time to complete the synchronization in seconds
  # @return [void]
  def push_synchronization_duration(product, time)
    @prometheus_handler.push_metric_to_prometheus(QI, 'product_synch_duration_seconds', time, labels: { 'product' => product, 'environment' => @environment })
  end
end
