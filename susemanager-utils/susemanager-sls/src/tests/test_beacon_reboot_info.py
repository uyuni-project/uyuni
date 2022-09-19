from ..beacons import reboot_info

def _pending_transaction_false():
  return False

def _pending_transaction_true():
  return True

def test_should_fire_event_when_context_is_empty():
  """
    The __context__ is empty and reboot is not required
  """
  reboot_info.__context__ = {}
  reboot_info.__salt__ = {
    "transactional_update.pending_transaction": _pending_transaction_false
  }
  ret = reboot_info.beacon({})
  assert ret == [{ "reboot_needed": False }]

  """
    The __context__ is empty and reboot is required
  """
  reboot_info.__context__ = {}
  reboot_info.__salt__ = {
    "transactional_update.pending_transaction": _pending_transaction_true
  }
  ret = reboot_info.beacon({})
  assert ret == [{ "reboot_needed": True }]

def test_should_not_fire_event_when_already_fired():
  """
    The __context__ already register that reboot is required
  """
  reboot_info.__salt__ = {
    "transactional_update.pending_transaction": _pending_transaction_true
  }
  reboot_info.__context__ = { "reboot_needed": True }
  ret = reboot_info.beacon({})
  assert ret == []

  """
    The __context__ already register that reboot is not required
  """
  reboot_info.__salt__ = {
    "transactional_update.pending_transaction": _pending_transaction_false
  }
  reboot_info.__context__ = { "reboot_needed": False }
  ret = reboot_info.beacon({})
  assert ret == []

def test_should_fire_event_when_reboot_status_changes():
  """
    The __context__ register that reboot is required but there is no pending transaction
  """
  reboot_info.__salt__ = {
    "transactional_update.pending_transaction": _pending_transaction_false
  }
  reboot_info.__context__ = { "reboot_needed": True }
  ret = reboot_info.beacon({})
  assert ret == [{ "reboot_needed": False }]

  """
    The __context__ register that reboot is not required but there is a pending transaction
  """
  reboot_info.__salt__ = {
    "transactional_update.pending_transaction": _pending_transaction_true
  }
  reboot_info.__context__ = { "reboot_needed": False }
  ret = reboot_info.beacon({})
  assert ret == [{ "reboot_needed": True }]
