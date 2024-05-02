# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Create an SSL certificate using cert-manager and return the path on the server where the files have been copied
#
# @param name [String] The name of the certificate.
# @param fqdn [String] The fully qualified domain name (FQDN) for the certificate.
# @return [Array<String>] An array containing the paths to the generated certificate files: [crt_path, key_path, ca_path].
def generate_certificate(name, fqdn)
  certificate = 'apiVersion: cert-manager.io/v1\\n'\
                'kind: Certificate\\n'\
                'metadata:\\n'\
                "  name: uyuni-#{name}\\n"\
                'spec:\\n'\
                "  secretName: uyuni-#{name}-cert\\n"\
                '  subject:\\n'\
                "    countries: ['DE']\\n"\
                "    provinces: ['Bayern']\\n"\
                "    localities: ['Nuernberg']\\n"\
                "    organizations: ['SUSE']\\n"\
                "    organizationalUnits: ['SUSE']\\n"\
                '  emailAddresses:\\n'\
                '    - galaxy-noise@suse.de\\n'\
                "  commonName: #{fqdn}\\n"\
                '  dnsNames:\\n'\
                "    - #{fqdn}\\n"\
                '  issuerRef:\\n'\
                '    name: uyuni-ca-issuer\\n'\
                '    kind: Issuer'
  _out, return_code = get_target('server').run_local("echo -e \"#{certificate}\" | kubectl apply -f -")
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
