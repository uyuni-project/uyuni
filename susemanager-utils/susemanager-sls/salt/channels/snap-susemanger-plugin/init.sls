{# == Auto-discover snaps from /var/spacewalk/packages via salt://packages == #}
{# Pillar knobs (optional) #}
{% set cfg       = pillar.get('snaps', {}) %}
{% set repo_root = cfg.get('repo_root', 'salt://packages') %}
{% set dst_dir   = cfg.get('install_dir', '/opt/snaps') %}

{# List ALL files from fileserver, then filter to packages/...*.snap #}
{% set all_files = salt['cp.list_master']('') or [] %}

snapd-pkg:
  pkg.installed:
    - pkgs: [snapd]

snapd-svc:
  service.running:
    - name: snapd
    - enable: True
    - require:
      - pkg: snapd-pkg

snap-stage-dir:
  file.directory:
    - name: {{ dst_dir }}
    - makedirs: True
    - mode: '0755'

{# Iterate over all .snap files under packages/ #}
{% set idx = 0 %}
{% for p in all_files %}
  {% if p.startswith('packages/') and p.endswith('.snap') %}
    {% set idx = idx + 1 %}
    {% set filename = p.split('/')[-1] %}
    {% set dst_file = dst_dir ~ '/' ~ filename %}

    {# Parse name/version from filename:
       <name>-<version>.<arch>-snap.snap  e.g. wxdatecalc-0.2.4-9.amd64-snap.snap #}
    {% set base_no_ext = filename[:-5] %}           {# drop .snap #}
    {% set core = base_no_ext.rsplit('.', 2)[0] %}  {# drop .<arch>-snap #}
    {% if '-' in core %}
      {% set snap_name = core.rsplit('-', 1)[0] %}
      {% set ver_full  = core.rsplit('-', 1)[1] %}
      {% set ver_main  = ver_full.rsplit('-', 1)[0] %}
      {% set maybe_rev = ver_full.rsplit('-', 1)[1] if '-' in ver_full and ver_full.rsplit('-',1)[1].isdigit() else None %}

copy-{{ idx }}-{{ snap_name }}:
  file.managed:
    - name: {{ dst_file }}
    - source: salt://{{ p }}
    - makedirs: True
    - mode: '0644'
    - require:
      - file: snap-stage-dir

install-{{ idx }}-{{ snap_name }}:
  cmd.run:
    - name: snap install --dangerous {{ dst_file }}
    - require:
      - file: copy-{{ idx }}-{{ snap_name }}
      - service: snapd-svc
    - unless: >
        sh -euc '
          out="$(snap list {{ snap_name }} 2>/dev/null || true)";
          [ -z "$out" ] && exit 1;
          cur_ver="$(echo "$out" | awk "NR==2{print \$2}")";
          cur_rev="$(echo "$out" | awk "NR==2{print \$3}")";
          [ "$cur_ver" = "{{ ver_main }}" ]{% if maybe_rev %} && [ "$cur_rev" = "{{ maybe_rev }}" ]{% endif %}
        '
    {% endif %}
  {% endif %}
{% endfor %}