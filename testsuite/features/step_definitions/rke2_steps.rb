# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

Given('The first-time setup job is successful') do
  # Check the job status via JSON
  cmd = "kubectl get jobs -n uyuni -l app.kubernetes.io/component=server-setup -o jsonpath='{.items[0].status.succeeded}'"
  status, code = get_target('server').run_local(cmd)
  raise 'Failed to get server setup job status' unless code.zero?
  raise 'Server setup job did not succeed' unless status.to_i >= 1
end

Then('the setup marker file should exist on "server"') do
  server_pod = get_pod_name('server', 'server')
  cmd = "kubectl exec -n uyuni #{server_pod} -- test -f /root/.MANAGER_SETUP_COMPLETE && echo 'EXISTS'"
  status, code = get_target('server').run_local(cmd)
  raise 'Failed to check server setup marker file' unless code.zero?
  raise 'Server setup marker file does not exist' unless status.include? 'EXISTS'
end

Given(/^The Kubernetes cluster is ready on "(.*)"$/) do |target|
  _out, code = get_target(target).run_local('kubectl get nodes && kubectl get namespace uyuni')
  raise "Kubernetes cluster is not ready or uyuni namespace is missing on #{target}" unless code.zero?
end

And(/^(?:the|I wait until the) "(.*)" deployment on "(.*)" (?:becomes|should become) ready within (.*) minutes$/) do |name, target, mins|
  wait_for_deployment(target, name, mins.to_i)
end

### External CA setup and teardown steps

Given('I back up the CA certificates on the server and proxy') do
  backup_dir = '/root/ca-backup'
  ca_dir = '/root/test-external-ca'
  ca_cn = 'External Test CA'

  add_context(:backup_dir, backup_dir)
  add_context(:external_ca_dir, ca_dir)
  add_context(:external_ca_cn, ca_cn)

  server = get_target('server')
  server.run_local("mkdir -p #{backup_dir}")

  _out, code = server.run_local(
    "kubectl get secret uyuni-ca -n cert-manager -o yaml --show-managed-fields=false > #{backup_dir}/uyuni-ca-secret.yaml"
  )
  raise SystemCallError, 'Failed to backup uyuni-ca secret' unless code.zero?

  _out, code = server.run_local(
    "kubectl get certificate uyuni-ca -n cert-manager -o yaml --show-managed-fields=false > #{backup_dir}/uyuni-ca-cert.yaml"
  )
  raise SystemCallError, 'Failed to backup uyuni-ca Certificate CR' unless code.zero?

  begin
    proxy = get_target('proxy')
    proxy.run_local("mkdir -p #{backup_dir}")
    proxy.run_local(
      "kubectl get configmap uyuni-ca -n uyuni -o yaml > #{backup_dir}/uyuni-ca-configmap.yaml",
      check_errors: false
    )
  rescue StandardError
    $stdout.puts 'Proxy configmap backup skipped (proxy not available yet)'
  end
end

When('I restore the original CA certificates on the server and proxy') do
  backup_dir = get_context(:backup_dir)
  ca_dir = get_context(:external_ca_dir)
  server = get_target('server')

  server.run_local('kubectl delete certificate uyuni-ca -n cert-manager --ignore-not-found')
  server.run_local('kubectl delete secret uyuni-ca -n cert-manager --ignore-not-found')
  server.run_local("kubectl apply -f #{backup_dir}/uyuni-ca-secret.yaml")
  server.run_local("kubectl apply -f #{backup_dir}/uyuni-ca-cert.yaml")

  server.run_local('kubectl delete secret uyuni-cert db-cert proxy-cert -n uyuni --ignore-not-found')
  %w[uyuni-cert db-cert].each do |cert|
    server.run_local(
      "kubectl get certificate #{cert} -n uyuni -o yaml --show-managed-fields=false > /tmp/#{cert}-cr.yaml"
    )
    server.run_local("kubectl delete certificate #{cert} -n uyuni --ignore-not-found")
    server.run_local("kubectl apply -f /tmp/#{cert}-cr.yaml")
  end
  server.run_local('kubectl delete certificate proxy-cert -n uyuni --ignore-not-found')

  repeat_until_timeout(timeout: 300, message: 'uyuni-cert was not re-issued during restore') do
    _out, code = server.run_local('kubectl get secret uyuni-cert -n uyuni', check_errors: false)
    break if code.zero?

    sleep 5
  end

  local_ca = Tempfile.new('uyuni-restored-ca')
  remote_ca = "/tmp/uyuni-restored-ca-#{$PROCESS_ID}.crt"
  begin
    server.run_local("kubectl get secret uyuni-ca -n cert-manager -o jsonpath='{.data.tls\\.crt}' | base64 -d > #{remote_ca}")
    file_extract(server, remote_ca, local_ca.path)
    server.run_local("rm -f #{remote_ca}")
    FileUtils.cp(local_ca.path, "/etc/pki/trust/anchors/#{server.full_hostname}.cert")
    raise ScriptError, 'Failed to update CA certificates' unless system('update-ca-certificates')
  ensure
    local_ca.close
    local_ca.unlink
  end

  server.run_local("rm -rf #{backup_dir} #{ca_dir}")

  begin
    proxy = get_target('proxy')
    proxy.run_local(
      "test -f #{backup_dir}/uyuni-ca-configmap.yaml && kubectl apply -f #{backup_dir}/uyuni-ca-configmap.yaml --force",
      check_errors: false
    )
    proxy.run_local('kubectl delete secret proxy-cert -n uyuni --ignore-not-found', check_errors: false)
    proxy.run_local("rm -rf #{backup_dir}", check_errors: false)
  rescue StandardError
    $stdout.puts 'Proxy restore skipped (proxy not available)'
  end
end

### External CA replacement steps

When(/^I generate an external CA on "(.*)"$/) do |target|
  ca_dir = get_context(:external_ca_dir)
  ca_cn = get_context(:external_ca_cn)
  get_target(target).run_local("mkdir -p #{ca_dir}")
  _out, code = get_target(target).run_local(
    "openssl ecparam -genkey -name prime256v1 -noout -out #{ca_dir}/ca.key && " \
    "openssl req -new -x509 -key #{ca_dir}/ca.key -out #{ca_dir}/ca.crt " \
    "-days 3650 -subj '/C=DE/ST=Bayern/L=Nurnberg/O=#{ca_cn}/OU=Testing/CN=External CA'"
  )
  raise SystemCallError, 'Failed to generate external CA' unless code.zero?
end

When(/^I replace the uyuni-ca secret with the external CA on "(.*)"$/) do |target|
  ca_dir = get_context(:external_ca_dir)
  get_target(target).run_local('kubectl delete certificate uyuni-ca -n cert-manager --ignore-not-found')
  get_target(target).run_local('kubectl delete secret uyuni-ca -n cert-manager --ignore-not-found')
  cmd = 'kubectl create secret tls uyuni-ca -n cert-manager ' \
        "--cert=#{ca_dir}/ca.crt --key=#{ca_dir}/ca.key"
  _out, code = get_target(target).run_local(cmd)
  raise SystemCallError, 'Failed to replace uyuni-ca secret' unless code.zero?
end

When(/^I delete the leaf certificate secrets on "(.*)"$/) do |target|
  _out, code = get_target(target).run_local(
    'kubectl delete secret uyuni-cert db-cert -n uyuni --ignore-not-found'
  )
  raise SystemCallError, 'Failed to delete leaf certificate secrets' unless code.zero?
end

Then(/^the "(.*)" secret on "(.*)" should be re-issued within (\d+) minutes$/) do |secret, target, mins|
  repeat_until_timeout(timeout: mins.to_i * 60, message: "Secret #{secret} was not re-issued") do
    _out, code = get_target(target).run_local("kubectl get secret #{secret} -n uyuni", check_errors: false)
    break if code.zero?

    sleep 5
  end
end

Then(/^the "(.*)" certificate on "(.*)" should be signed by the external CA$/) do |secret, target|
  ca_cn = get_context(:external_ca_cn)
  issuer, code = get_target(target).run_local(
    "kubectl get secret #{secret} -n uyuni -o jsonpath='{.data.tls\\.crt}' | base64 -d | openssl x509 -noout -issuer"
  )
  raise SystemCallError, "Failed to read issuer from #{secret}" unless code.zero?
  raise ScriptError, "#{secret} not signed by external CA. Issuer: #{issuer}" unless issuer.include?(ca_cn)
end

When(/^I re-generate the proxy certificate on the server using the external CA$/) do
  proxy_fqdn = get_target('proxy').full_hostname

  get_target('server').run_local('kubectl delete certificate proxy-cert -n uyuni --ignore-not-found')
  get_target('server').run_local('kubectl delete secret proxy-cert -n uyuni --ignore-not-found')

  certificate = render_certificate_yaml(
    name: 'proxy-cert',
    secret_name: 'proxy-cert',
    fqdn: proxy_fqdn,
    namespace: 'uyuni',
    issuer_name: 'uyuni-issuer',
    issuer_kind: 'ClusterIssuer',
    issuer_group: 'cert-manager.io',
    is_ca: false
  )
  _out, code = get_target('server').run_local("cat <<'CERT_EOF' | kubectl apply -f -\n#{certificate}\nCERT_EOF")
  raise SystemCallError, 'Failed to create proxy-cert Certificate resource' unless code.zero?

  repeat_until_timeout(timeout: 600, message: 'proxy-cert secret was not created by cert-manager') do
    _out, code = get_target('server').run_local('kubectl get secret proxy-cert -n uyuni', check_errors: false)
    break if code.zero?

    sleep 5
  end
end

When(/^I transfer the proxy certificate from the server to "(.*)"$/) do |target|
  out, code = get_target('server').run_local(
    'kubectl get secret proxy-cert -n uyuni -o yaml --show-managed-fields=false'
  )
  raise SystemCallError, 'Failed to extract proxy-cert secret from server' unless code.zero?

  secret = YAML.safe_load(out)
  secret['metadata'].delete_if { |k, _| %w[uid resourceVersion creationTimestamp annotations].include?(k) }
  clean_yaml = YAML.dump(secret)

  secret_file = '/tmp/proxy-cert-secret.yaml'
  file = generate_temp_file('proxy-cert-secret', clean_yaml)
  success = file_inject(get_target(target), file.path, secret_file)
  file.close
  file.unlink
  raise ScriptError, 'Failed to inject proxy-cert secret into proxy' unless success

  _out, code = get_target(target).run_local(
    "kubectl apply -f #{secret_file}"
  )
  raise SystemCallError, 'Failed to apply proxy-cert secret on proxy cluster' unless code.zero?
end

When(/^I update the uyuni-ca configmap on "(.*)" with the external CA$/) do |target|
  ca_dir = get_context(:external_ca_dir)

  # Copy the external CA cert to the proxy node
  success = file_extract(get_target('server'), "#{ca_dir}/ca.crt", '/tmp/external-ca.crt')
  raise ScriptError, 'Failed to extract external CA cert from server' unless success

  success = file_inject(get_target(target), '/tmp/external-ca.crt', '/tmp/external-ca.crt')
  raise ScriptError, 'Failed to inject external CA cert into proxy' unless success

  # Replace the uyuni-ca configmap on the proxy cluster
  _out, code = get_target(target).run_local(
    'kubectl delete configmap uyuni-ca -n uyuni --ignore-not-found && ' \
    'kubectl create configmap uyuni-ca -n uyuni --from-file=ca.crt=/tmp/external-ca.crt'
  )
  raise SystemCallError, 'Failed to update uyuni-ca configmap on proxy' unless code.zero?
end
