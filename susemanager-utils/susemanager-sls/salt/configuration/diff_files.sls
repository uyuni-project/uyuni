{% if pillar.get('param_files', []) %}
{%- for file in pillar.get('param_files') %}

file_deploy_{{ loop.index }}:
{% if file.type == 'file' %}
    file.managed:
{% elif file.type == 'directory' %}
    file.directory:
{% elif file.type == 'symlink' %}
    file.symlink:
{% endif %}
    -   name: {{ file.name }}
    -   makedirs: True
{% if file.type == 'file' %}
    -   source: {{ file.source }}
    -   user: {{ file.user }}
    -   group: {{ file.group }}
    -   mode: {{ file.mode }}
{% elif file.type == 'directory' %}
    -   user: {{ file.user }}
    -   group: {{ file.group }}
    -   mode: {{ file.mode }}
{% elif file.type == 'symlink' %}
    -   target: {{ file.target }}
{% endif %}
{%- endfor %}
{% endif %}

