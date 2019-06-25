# check if the minion is a SLE-15
def minion_is_sle15
  node = get_target('sle-minion')
  os_version, os_family = get_os_version(node)
  return false if os_version.nil? || os_family.nil?
  (os_version =~ /^15/) && (os_family =~ /^sles/)
end
