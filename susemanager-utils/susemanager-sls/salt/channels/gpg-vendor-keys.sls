{# Red Hat and Clones do not trust their own keys by default #}

{% if grains['osfinger'] == 'CentOS Linux-7' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7' %}
{% elif grains['osfinger'] == 'CentOS Linux-8' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-centosofficial' %}
{% elif grains['osfinger'] == 'AlmaLinux-8' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-AlmaLinux' %}
{% elif grains['osfinger'] == 'Rocky Linux-8' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-rockyofficial' %}
{% elif grains['osfinger'] == 'Red Hat Enterprise Linux Server-7' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release' %}
{% elif grains['osfinger'] == 'Red Hat Enterprise Linux-8' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release' %}
{% elif grains['osfinger'] == 'Amazon Linux-2' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-amazon-linux-2' %}
{% elif grains['osfinger'] == 'Oracle Linux Server-7' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-oracle' %}
{% elif grains['osfinger'] == 'Oracle Linux Server-8' %}
  {% set gpg_key_path = '/etc/pki/rpm-gpg/RPM-GPG-KEY-oracle' %}
{% endif %}

{% if gpg_key_path is defined %}
mgr_trust_vendor_gpg_key:
  cmd.run:
    - name: rpm --import {{ gpg_key_path }}
    - runas: root
{% endif %}


