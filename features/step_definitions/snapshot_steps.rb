# snapshots

When(/^I take a snapshot "([^"]*)"$/) do |name|
  $sshout = ""
  $sshout = `echo | ssh -o StrictHostKeyChecking=no root@$VHOST qemu-img snapshot -c #{name} $IMGDIR/$VMDISK.qcow2 `
  puts "Creating snapsnot failed..." unless $?.success?
end
