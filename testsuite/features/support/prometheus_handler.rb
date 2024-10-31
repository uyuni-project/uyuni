# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'net/http'
require 'prometheus/client'
require 'uri'

# Prometheus handler to push metrics to Prometheus
class PrometheusHandler
  def initialize(prometheus_url)
    @prometheus_url = prometheus_url
    @registry = Prometheus::Client::Registry.new
  end

  # Push a metric to Prometheus, raising an error if the Prometheus request fails.
  #
  # @param job_name [String] the job name to push the metric to
  # @param metric_name [String] the metric name to push
  # @param metric_value [Integer] the metric value to push
  # @param labels [Hash] the labels to add to the metric
  # @return [void]
  # @raise [SystemCallError] if the Prometheus request fails
  def push_metric_to_prometheus(job_name, metric_name, metric_value, labels = {})
    # Define the metric if it doesn't exist
    gauge = @registry.get(metric_name) || Prometheus::Client::Gauge.new(metric_name, docstring: job_name)
    @registry.register(gauge) unless @registry.exist?(gauge)

    # Set the value of the metric with optional labels
    gauge.set(metric_value, labels: labels)

    # HTTP request
    uri = URI("#{@prometheus_url}/metrics/job/#{job_name}")
    http = Net::HTTP.new(uri.host, uri.port)
    request = Net::HTTP::Post.new(uri.path)
    request['Content-Type'] = 'text/plain; version=0.0.4'
    request.body = @registry.text
    response = http.request(request)

    return if response.code.to_i == 200

    raise SystemCallError, "Failed to push metric: #{response.body}"
  end
end
