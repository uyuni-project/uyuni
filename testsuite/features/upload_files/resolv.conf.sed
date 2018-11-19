# Add a comment on first line
1i\# Retail: use the branch DNS server on the terminals\n

# Remove old comments
/^#/d

# Add the branch domain at beginning of search list
s/^search /search example.org /

# Add branch server as unique name server
$i\nameserver 192.168.5.254

# Remove all name servers
/^nameserver /d
