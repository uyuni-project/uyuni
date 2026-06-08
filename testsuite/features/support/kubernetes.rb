# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'erb'

# Render a cert-manager Certificate YAML from the shared ERB template.
#
# @param opts [Hash] Template variables: name, secret_name, fqdn, namespace, issuer_name, issuer_kind, issuer_group, subject, is_ca (nil=omit, false=emit isCA:false with leaf key settings).
# @return [String] The rendered YAML string.
def render_certificate_yaml(opts)
  defaults = { namespace: nil, issuer_group: nil, subject: nil, is_ca: nil }
  opts = defaults.merge(opts)
  template_path = File.join(File.dirname(__FILE__), '..', 'upload_files', 'certificate.yaml.erb')
  ERB.new(File.read(template_path), trim_mode: '-').result_with_hash(opts)
end

# Create an SSL certificate using cert-manager and return the path on the server where the files have been copied
#
# @param name [String] The name of the certificate.
# @param fqdn [String] The fully qualified domain name (FQDN) for the certificate.
# @return [Array<String>] An array containing the paths to the generated certificate files: [crt_path, key_path, ca_path].
def generate_certificate(name, fqdn)
  certificate = render_certificate_yaml(
    name: "uyuni-#{name}",
    secret_name: "uyuni-#{name}-cert",
    fqdn: fqdn,
    issuer_name: 'uyuni-ca-issuer',
    issuer_kind: 'Issuer',
    subject: { country: 'DE', province: 'Bayern', locality: 'Nuernberg', org: 'SUSE', ou: 'SUSE', email: 'galaxy-noise@suse.de' }
  )
  _out, return_code = get_target('server').run_local("cat <<'CERT_EOF' | kubectl apply -f -\n#{certificate}\nCERT_EOF")
  raise SystemCallError, "Failed to define #{name} Certificate resource" unless return_code.zero?

  # cert-manager takes some time to generate the secret, wait for it before continuing
  repeat_until_timeout(timeout: 600, message: "Kubernetes uyuni-#{name}-cert secret has not been defined") do
    _result, code = get_target('server').run_local("kubectl get secret uyuni-#{name}-cert", check_errors: false)
    break if code.zero?

    sleep 1
  end

  crt_path = "/tmp/#{name}.crt"
  key_path = "/tmp/#{name}.key"
  ca_path = '/tmp/ca.crt'

  _out, return_code = get_target('server').run_local("kubectl get secret uyuni-#{name}-cert -o jsonpath='{.data.tls\\.crt}' | base64 -d >#{crt_path}")
  raise SystemCallError, "Failed to store #{name} certificate" unless return_code.zero?

  _out, return_code = get_target('server').run_local("kubectl get secret uyuni-#{name}-cert -o jsonpath='{.data.tls\\.key}' | base64 -d >#{key_path}")
  raise SystemCallError, "Failed to store #{name} key" unless return_code.zero?

  get_target('server').run_local("kubectl get secret uyuni-#{name}-cert -o jsonpath='{.data.ca\\.crt}' | base64 -d >#{ca_path}")
  [crt_path, key_path, ca_path]
end

# Returns whether the server is running in a k3s container or not
#
# @return [Boolean] Returns true if the k3s service is running, false otherwise.
def running_k3s?
  _out, code = get_target('server').run_local('systemctl is-active k3s', check_errors: false)
  code.zero?
end

# Returns whether the server is running in a rke2 cluster or not
#
# @return [Boolean] Returns true if the rke2 service is running, false otherwise.
def running_rke2?
  _out, code = get_target('server').run_local('systemctl is-active rke2-server', check_errors: false)
  code.zero?
end

# Performs a label-based pod lookup using JSONPath.
#
# @param target [String] The target host where the kubectl command will be run.
# @param component [String] The component label value to search for.
# @return [String, nil] The name of the first matching pod, or nil if not found.
def get_pod_name(target, component)
  label = "app.kubernetes.io/component=#{component}"
  cmd = "kubectl get pods -n uyuni -l #{label} -o jsonpath='{.items[0].metadata.name}'"
  out, code = get_target(target).run_local(cmd)
  out if code.zero?
end

# Returns the NodePort assigned to the TFTP service on the given target cluster.
# Needed because the LoadBalancer EXTERNAL-IP is pending on bare-metal RKE2;
# the NodePort is the only externally reachable entry point.
#
# @param target [String] The target host where kubectl commands will be executed.
# @return [Integer] The NodePort number for the TFTP UDP service.
def get_tftp_node_port(target)
  cmd = "kubectl get svc tftp -n uyuni -o jsonpath='{.spec.ports[0].nodePort}'"
  out, code = get_target(target).run_local(cmd)
  raise 'Failed to query TFTP NodePort' unless code.zero?

  port = out.strip.to_i
  raise 'TFTP service has no NodePort assigned' if port.zero?

  port
end

# Polls the Kubernetes Deployment status until all replicas are ready or a timeout is reached.
#
# @param target [String] The target host where the kubectl command will be run.
# @param deploy_name [String] The name of the deployment to check.
# @param namespace [String] The name of the namespace where to check the pods.
# @param timeout_mins [Integer] The maximum time to wait in minutes. Defaults to 15.
# @return [Boolean] Returns true if the deployment becomes ready.
# @raise [RuntimeError] If the timeout is reached before the deployment is ready.
def wait_for_deployment(target, deploy_name, namespace, timeout_mins = 15)
  start_time = Time.now
  loop do
    raise "Timeout: #{deploy_name} on #{target} not ready" if (Time.now - start_time) > (timeout_mins * 60)

    out, _code = get_target(target).run_local("kubectl get deployment #{deploy_name} -n #{namespace} -o json")
    data = JSON.parse(out)

    return true if data.dig('status', 'readyReplicas') == data.dig('spec', 'replicas')

    puts "Waiting for #{deploy_name} on #{target}... (#{(Time.now - start_time).to_i}s)"
    sleep 10
  end
end

# Polls the Kubernetes Pods status until all pods are ready or a timeout is reached.
#
# @param target [String] The target host where the kubectl command will be run.
# @param pod_name [String] The pod name or name prefix to check.
# @param namespace [String] The name of the namespace where to check the pods.
# @param timeout_mins [Integer] The maximum time to wait in minutes. Defaults to 15.
# @return [Boolean] Returns true if the pod becomes ready.
# @raise [RuntimeError] If the timeout is reached before the pod is ready.
def wait_for_pods(target, pod_name, namespace, timeout_mins = 15)
  start_time = Time.now
  loop do
    raise "Timeout: #{pod_name} on #{target} not ready" if (Time.now - start_time) > (timeout_mins * 60)

    out, code = get_target(target).run_local("kubectl get pods -n #{namespace} -o json", check_errors: false)
    unless code.zero?
      puts "Waiting for #{pod_name} on #{target}... (pod not found yet)"
      sleep 10
      next
    end

    data = JSON.parse(out)
    pods =
      (data['items'] || []).select do |pod|
        name = pod.dig('metadata', 'name').to_s
        name == pod_name || name.start_with?("#{pod_name}-")
      end

    if pods.empty?
      puts "Waiting for #{pod_name} on #{target}... (matching pod not found yet)"
      sleep 10
      next
    end

    # A pod is considered up when it is Running and has Ready=True.
    all_ready = pods.any? && pods.all? do |pod|
      phase_running = pod.dig('status', 'phase') == 'Running'
      ready_condition =
        pod.dig('status', 'conditions')&.any? do |cond|
          cond['type'] == 'Ready' && cond['status'] == 'True'
        end
      phase_running && ready_condition
    end

    return true if all_ready

    puts "Waiting for #{pod_name} on #{target}... (#{(Time.now - start_time).to_i}s)"
    sleep 10
  end
end
