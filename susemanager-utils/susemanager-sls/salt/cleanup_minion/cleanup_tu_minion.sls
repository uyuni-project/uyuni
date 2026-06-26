# TU minion SLS execution is inside of a transaction, which means 
# schedule.present does not work. This is an asynchronous approach, where
# the common parts disable venv-salt-minion and this systemd script
# removes MLM-relevant configuration files.

/etc/systemd/system/cleanup-venv-salt-minion.service:
  file.managed:
    - contents: |
        [Unit]
        Description=Cleanup SUSE Multi-Linux Manager Salt configuration
        After=venv-salt-minion.service
        ConditionPathExists=!/etc/systemd/system/multi-user.target.wants/venv-salt-minion.service

        [Service]
        Type=oneshot
        ExecCondition=/bin/sh -c '! /usr/bin/systemctl is-active venv-salt-minion.service'
        ExecStart=/usr/bin/rm -f  /etc/sysconfig/rhn/systemid \
                                  /etc/venv-salt-minion/minion.d/_schedule.conf \
                                  /etc/venv-salt-minion/minion.d/susemanager.conf \
                                  /etc/venv-salt-minion/minion.d/master.conf \
                                  /etc/venv-salt-minion/pki/minion/minion.pem \
                                  /etc/venv-salt-minion/pki/minion/minion.pub \
                                  /etc/venv-salt-minion/pki/minion/minion_master.pub
        ExecStart=/usr/bin/rm -rf /var/cache/venv-salt-minion
        ExecStartPost=/usr/bin/rm -f /etc/systemd/system/cleanup-venv-salt-minion.service \
                                     /etc/systemd/system/multi-user.target.wants/cleanup-venv-salt-minion.service

        [Install]
        WantedBy=multi-user.target
    - mode: 644
    - user: root
    - group: root

/etc/systemd/system/multi-user.target.wants/cleanup-venv-salt-minion.service:
  file.symlink:
    - target: /etc/systemd/system/cleanup-venv-salt-minion.service
    - require:
      - file: /etc/systemd/system/cleanup-venv-salt-minion.service
