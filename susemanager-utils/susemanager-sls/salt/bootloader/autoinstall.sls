{% if pillar['uyuni-reinstall-kernel'] and pillar['uyuni-reinstall-initrd'] %}
mgr_copy_kernel:
  file.managed:
    - name: /boot/uyuni-reinstall-kernel
    - source: salt://bootloader/{{ pillar.get('uyuni-reinstall-kernel') }}

mgr_copy_initrd:
  file.managed:
    - name: /boot/uyuni-reinstall-initrd
    - source: salt://bootloader/{{ pillar.get('uyuni-reinstall-initrd') }}

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

mgr_autoinstall_start:
  cmd.run:
    - name: shutdown -r +1
    - onchanges:
      - cmd: mgr_generate_grubconf

{% endif %}
