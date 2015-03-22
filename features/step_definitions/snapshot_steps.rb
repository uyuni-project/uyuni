#snapshots

When /^I take a snapshot "([^"]*)"$/ do |name|
  $sshout = ""
  $sshout = `echo | ssh -o StrictHostKeyChecking=no root@$VHOST qemu-img create -f qcow -b $IMGDIR/$VMDISK.qcow2 $IMGDIR/#{name}.qcow2`
  raise "Creating snapsnot failed..." unless $?.success?
end
