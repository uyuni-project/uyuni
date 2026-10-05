# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

resume_actionchain_execution:
  local.mgractionchains.resume:
    - tgt: {{ data['id'] }}
    - metadata:
        suma-action-chain: True
