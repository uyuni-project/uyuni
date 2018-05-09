# check if the minion is a SLE-15
def minion_is_sle15
  node = get_target('sle-minion')
  os_version = get_os_version(node)
  os_version.include? '15'
end
