{#
  Alias for the newer 'util.syncall' for backwards compatibility.
#}
include:
  - util.syncall

mgr_synccustomall_notify:
  test.show_notification:
    - text: |
        util.synccustomall is deprecated and is only available for backwards compatibility.
        Please switch to use 'util.syncall' instead.
