mgr_absent_ca_package:
  pkg.removed:
    - name: rhn-org-trusted-ssl-cert

{% include 'certs/{0}.sls'.format(grains['os_family']|lower) %}

mgr_proxy_ca_cert_symlink:
  file.symlink:
    - name: /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT
    - target: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
    - onlyif: grep -Eq "^proxy.rhn_parent *= *[a-zA-Z0-9]+" /etc/rhn/rhn.conf && -e /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
