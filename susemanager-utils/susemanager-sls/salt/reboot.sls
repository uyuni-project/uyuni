mgr_reboot:
  cmd.run:
{%- if grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - name: /sbin/shutdown -r +5
{%- else %}
    - name: /usr/sbin/shutdown -r +5
{% endif %}
