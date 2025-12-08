{% if pillar['kernel'] and pillar['initrd'] %}
mgr_copy_kernel:
  file.managed:
    - name: /boot/uyuni-reinstall-kernel
    - source: salt://bootloader/{{ pillar.get('kernel') }}

mgr_copy_initrd:
  file.managed:
    - name: /boot/uyuni-reinstall-initrd
    - source: salt://bootloader/{{ pillar.get('initrd') }}

{% set loader_type = salt['cmd.run']('if [ -f /etc/sysconfig/bootloader ]; then source /etc/sysconfig/bootloader 2> /dev/null; fi;
if [ -z "${LOADER_TYPE}" ]; then
if [ $(which grubonce 2> /dev/null) ] && [ !$(which grub2-mkconfig 2> /dev/null) ]; then LOADER_TYPE="grub";
elif [ $(which elilo 2> /dev/null) ] && [ !$(which grub2-mkconfig 2> /dev/null) ]; then LOADER_TYPE="elilo";
fi;
fi; echo "${LOADER_TYPE}"', python_shell=True) %}
{% if loader_type == 'grub' %}
mgr_create_grub_entry:
  file.append:
    - name: /boot/grub/menu.lst
    - template: jinja
    - source: salt://bootloader/grub1_uyuni_reinstall.templ
    - require:
      - file: mgr_copy_kernel
      - file: mgr_copy_initrd

mgr_grub_boot_once:
  cmd.run:
    - name: grubonce "{{ pillar.get('uyuni-reinstall-name') }}"
    - onchanges:
      - file: mgr_create_grub_entry
{% elif loader_type == 'elilo' %}
mgr_create_elilo_entry:
  file.append:
    - name: /etc/elilo.conf
    - template: jinja
    - source: salt://bootloader/elilo_uyuni_reinstall.templ
    - require:
      - file: mgr_copy_kernel
      - file: mgr_copy_initrd

mgr_set_default_boot:
  file.replace:
    - name: /etc/elilo.conf
    - pattern: default = .*
    - repl: default = {{ pillar.get('uyuni-reinstall-name') }}
    - require:
      - file: mgr_create_elilo_entry

mgr_elilo_copy_config:
  cmd.run:
    - name: elilo
    - onchanges:
      - file: mgr_create_elilo_entry
      - file: mgr_set_default_boot
{% else %}
mgr_create_grub2_entry:
  file.managed:
    - name: /etc/grub.d/42_uyuni_reinstall
    - source: salt://bootloader/42_uyuni_reinstall.templ
    - template: jinja
    - mode: 0755

mgr_set_default_boot:
  file.replace:
    - name: /etc/default/grub
    - pattern: GRUB_DEFAULT=.*
    - repl: GRUB_DEFAULT={{ pillar.get('uyuni-reinstall-name') }}
    - require:
      - file: mgr_create_grub2_entry

mgr_generate_grubconf:
  cmd.run:
    - name: grub2-mkconfig -o /boot/grub2/grub.cfg
    - onchanges:
      - file: mgr_copy_kernel
      - file: mgr_copy_initrd
      - file: mgr_create_grub2_entry
      - file: mgr_set_default_boot
{% endif %}

mgr_autoinstall_start:
  cmd.run:
    - name: shutdown -r +1
    - require:
{% if loader_type == 'grub' %}
      - cmd: mgr_grub_boot_once
{% elif loader_type == 'elilo' %}
      - cmd: mgr_elilo_copy_config
{% else %}
      - cmd: mgr_generate_grubconf
{% endif %}

{% endif %}
