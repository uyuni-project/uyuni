#!/bin/bash

# SPDX-FileCopyrightText: 2019-2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

test -f /.kconfig && . /.kconfig
test -f /.profile && . /.profile

if [ -f /usr/bin/venv-salt-call ] ; then
    systemctl enable venv-salt-minion.service

    # notify SUSE Manager about newly deployed image
    systemctl enable image-deployed-bundle.service

    systemctl enable migrate-to-bundle.service

    # move the activation key injected by SUMA
    mv /etc/salt/minion.d/kiwi_activation_key.conf /etc/venv-salt-minion/minion.d
else
    systemctl enable salt-minion.service

    # notify SUSE Manager about newly deployed image
    systemctl enable image-deployed.service
fi

# install bootloader and generate boot menu
systemctl enable install-local-bootloader.service
