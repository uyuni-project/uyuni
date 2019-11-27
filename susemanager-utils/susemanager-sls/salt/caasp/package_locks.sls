skuba_update_plugin:
    file.managed:
    - name: /usr/share/susemanager/caasp/mgr-skuba-update-plugin.sh
    - makedirs: True
    - source: salt://caasp/mgr-skuba-update-plugin.sh
    - user: root
    - group: root
    - mode: 744    

skuba_update_config:
    cmd.run:
    - name: sed -ri 's#SKUBA_UPDATE_OPTIONS="(.*)"#SKUBA_UPDATE_OPTIONS="\1 --plugin /usr/share/susemanager/caasp/mgr-skuba-update-plugin.sh"#' /etc/sysconfig/skuba-update
    - unless: grep -q 'SKUBA_UPDATE_OPTIONS=".*--plugin.*"' /etc/sysconfig/skuba-update

lock_caasp_packages:
    cmd.run:
    - name: /usr/share/susemanager/caasp/mgr-skuba-update-plugin.sh after-update
    - require:
      - file: skuba_update_plugin