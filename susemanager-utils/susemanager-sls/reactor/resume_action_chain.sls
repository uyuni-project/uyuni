resume_actionchain_execution:
  local.mgractionchains.resume:
    - tgt: {{ data['id'] }}
    - metadata:
        suma-action-chain: True
