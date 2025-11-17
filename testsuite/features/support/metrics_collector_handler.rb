# Copyright (c) 2024 SUSE LLC.
# SPDX-License-Identifier: MIT

require 'net/http'
require 'prometheus/client'
require 'prometheus/client/push'
require 'uri'

# Metrics Collector handler to push metrics to the Metrics Collector
class MetricsCollectorHandler
  def initialize(metrics_collector_url)
    @metrics_collector_url = metrics_collector_url
    @metrics_collector = Prometheus::Client::Registry.new
  end

  # Push a metric to the Metrics Collector, raising an error if the Metrics Collector request fails.
  #
  # @param job_name [String] the job name to push the metric to
  # @param metric_name [String] the metric name to push
  # @param metric_value [Integer] the metric value to push
  # @param labels [Hash] the labels to add to the metric
  # @return [void]
  # @raise [SystemCallError] if the Prometheus request fails
  def push_metrics(job_name, metric_name, metric_value, labels = {})
    begin
      gauge = @metrics_collector.get(metric_name.to_sym) || @metrics_collector.gauge(metric_name.to_sym, docstring: job_name, labels: labels.keys)
      gauge.set(metric_value, labels: labels)
      Prometheus::Client::Push.new(job: job_name, gateway: @metrics_collector_url).add(@metrics_collector)
      $stdout.puts("Pushed the metric #{metric_name} with value #{metric_value} to #{@metrics_collector_url}")
    rescue StandardError => e
      $stdout.puts("Error pushing the metric #{metric_name} with value #{metric_value}:\n#{e.full_message}")
      raise
    end
  end
end
