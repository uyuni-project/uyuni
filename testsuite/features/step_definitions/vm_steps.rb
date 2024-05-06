# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains all steps concerning virtual machine management

When(/^I create a (leap|sles|rhlike|deblike) virtual machine named "([^"]*)" (with|without) cloudinit on "([^"]*)"$/) do |os_type, vm_name, cloudinit, host|
  node = get_target(host)
  disk_path = "/tmp/#{vm_name}_disk.qcow2"

  # The cloud init images are generated via sumaform and can be modified there
  # The names of the qcow2 images are also defined there
  cloudinit_path = "/tmp/cloudinit-disk-#{os_type}.iso"
  cloudinit_image = "/var/testsuite-data/cloudinit-disk-#{os_type}.iso"
  memory = '512'
  mac = 'RANDOM'

  case os_type
  when 'leap'
    name = 'leap-disk-image-template.qcow2'
    net = 'salt-leap'
    os = 'opensuse15.5'
    memory = '1024'
  when 'sles'
    name = 'sles-disk-image-template.qcow2'
    net = 'salt-sles'
    os = 'sle15sp4'
    mac = 'RANDOM'
  when 'rhlike'
    name = 'rhlike-disk-image-template.qcow2'
    net = 'salt-rhlike'
    os = 'rocky8'
  when 'deblike'
    name = 'deblike-disk-image-template.qcow2'
    net = 'salt-deblike'
    os = 'ubuntu2204'
  else
    name = 'empty'
  end
  net = 'test-net0' if cloudinit == 'without'
  raise ScriptError, "/var/testsuite-data/#{name} not found" unless file_exists?(node, "/var/testsuite-data/#{name}")

  node.run("cp /var/testsuite-data/#{name} #{disk_path}")

  # Actually define the VM, but don't start it
  raise ScriptError, 'not found: virt-install' unless file_exists?(node, '/usr/bin/virt-install')

  command = "virt-install --name #{vm_name} --memory #{memory} --vcpus 1 --disk path=#{disk_path},bus=virtio \
            --network network=#{net},mac=#{mac} --graphics vnc,listen=0.0.0.0 --serial file,path=/tmp/#{vm_name}.console.log \
            --import --hvm --noautoconsole --noreboot --osinfo #{os} "
  # with cloud init image
  if cloudinit == 'with'
    raise ScriptError, "#{cloudinit_image} not found" unless file_exists?(node, cloudinit_image)

    command_cloudinit = "--disk path=#{cloudinit_image},device=cdrom,bus=sata"
    node.run("cp #{cloudinit_image} #{cloudinit_path}")
    node.run(command + command_cloudinit, verbose: true)
  else
    node.run(command, verbose: true)
  end
end

When(/^I stop the virtual machine named "([^"]*)" on "([^"]*)"$/) do |vm_name, host|
  node = get_target(host)
  _running, code = node.run("virsh list --name | grep #{vm_name}", check_errors: false)
  raise ScriptError, "Virtual machine #{vm_name} is not running" unless code.zero?
  raise ScriptError, 'not found: virsh' unless file_exists?(node, '/usr/bin/virsh')

  node.run("virsh destroy #{vm_name}")
end

When(/^I delete the virtual machine named "([^"]*)" on "([^"]*)"$/) do |vm_name, host|
  node = get_target(host)
  raise ScriptError, 'not found: virsh' unless file_exists?(node, '/usr/bin/virsh')

  step %(I stop the virtual machine named "#{vm_name}" on "#{host}")
  node.run("virsh undefine --nvram #{vm_name}")
end

When(/^I create ([^ ]*) virtual network on "([^"]*)"$/) do |net_name, host|
  node = get_target(host)

  networks = {
    'test-net0' => { 'bridge' => 'virbr0', 'subnet' => 124 },
    'test-net1' => { 'bridge' => 'virbr1', 'subnet' => 126 },
    'salt-sles' => { 'bridge' => 'br0' },
    'salt-leap' => { 'bridge' => 'br0' },
    'salt-rhlike' => { 'bridge' => 'br0' },
    'salt-deblike' => { 'bridge' => 'br0' }
  }

  net = networks[net_name]
  case net_name
  when 'test-net0', 'test-net1'
    netdef = '<network>' \
             "  <name>#{net_name}</name>" \
             '  <forward mode=\'nat\'/>' \
             "  <bridge name='#{net['bridge']}' stp='on' delay='0'/>" \
             "  <ip address='192.168.#{net['subnet']}.1' netmask='255.255.255.0'>" \
             '    <dhcp>' \
             "      <range start='192.168.#{net['subnet']}.2' end='192.168.#{net['subnet']}.254'/>" \
             '    </dhcp>' \
             '  </ip>' \
             '</network>'
  when 'salt-sles', 'salt-leap', 'salt-rhlike', 'salt-deblike'
    netdef = "<network>  <name>#{net_name}</name>  <forward mode='bridge'/>  <bridge name='#{net['bridge']}'/></network>"
  else
    raise ScriptError, "#{net_name} case is not implemented."
  end

  # Some networks like the default one may already be defined.
  _output, code = node.run("virsh net-dumpxml #{net_name}", check_errors: false)
  node.run("echo -e \"#{netdef}\" >/tmp/#{net_name}.xml && virsh net-define /tmp/#{net_name}.xml") unless code.zero?

  # Ensure the network is started
  node.run("virsh net-start #{net_name}", check_errors: false)
end

When(/^I delete ([^ ]*) virtual network on "([^"]*)"((?: without error control)?)$/) do |net_name, host, error_control|
  node = get_target(host)
  _output, code = node.run("virsh net-dumpxml #{net_name}", check_errors: false)
  if code.zero?
    steps %(
      When I run "virsh net-destroy #{net_name}" on "#{host}"#{error_control}
      And I run "virsh net-undefine #{net_name}" on "#{host}"#{error_control}
    )
  end
end

When(/^I create ([^ ]*) virtual storage pool on "([^"]*)"$/) do |pool_name, host|
  node = get_target(host)

  pool_def = %(<pool type='dir'>
      <name>#{pool_name}</name>
      <capacity unit='bytes'>0</capacity>
      <allocation unit='bytes'>0</allocation>
      <available unit='bytes'>0</available>
      <source>
      </source>
      <target>
        <path>/var/lib/libvirt/images/#{pool_name}</path>
      </target>
    </pool>
  )

  # Some pools like the default one may already be defined.
  _output, code = node.run("virsh pool-dumpxml #{pool_name}", check_errors: false)
  node.run("echo -e \"#{pool_def}\" >/tmp/#{pool_name}.xml && virsh pool-define /tmp/#{pool_name}.xml") unless code.zero?
  node.run("mkdir -p /var/lib/libvirt/images/#{pool_name}")

  # Ensure the pool is started
  node.run("virsh pool-start #{pool_name}", check_errors: false)
end

When(/^I delete ([^ ]*) virtual storage pool on "([^"]*)"((?: without error control)?)$/) do |pool_name, host, error_control|
  node = get_target(host)
  _output, code = node.run("virsh pool-dumpxml #{pool_name}", check_errors: false)
  if code.zero?
    steps %(
      When I run "virsh pool-destroy #{pool_name}" on "#{host}"#{error_control}
      And I run "virsh pool-undefine #{pool_name}" on "#{host}"#{error_control}
    )
  end

  # only delete the folders we created
  step %(I run "rm -rf /var/lib/libvirt/images/#{pool_name}" on "#{host}"#{error_control}) if pool_name.start_with? 'test-'
end

Then(/^I should see "([^"]*)" virtual machine (shut off|running|paused) on "([^"]*)"$/) do |vm, state, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never reached state #{state}") do
    output, _code = node.run("virsh domstate #{vm}")
    break if output.strip == state

    sleep 3
  end
end

When(/^I wait until virtual machine "([^"]*)" on "([^"]*)" is started$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} OS failed did not come up yet") do
    _output, code = node.run("grep -i 'firstboot' /tmp/#{vm}.console.log", check_errors: false)
    break if code.zero?

    _output, code = node.run("grep -i 'login\:' /tmp/#{vm}.console.log", check_errors: false)
    break if code.zero?

    sleep 1
  end
end

Then(/^I should not see a "([^"]*)" virtual machine on "([^"]*)"$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} still exists") do
    _output, code = node.run("virsh dominfo #{vm}", check_errors: false)
    break if code == 1

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have ([0-9]*)MB memory and ([0-9]*) vcpus$/) do |vm, host, mem, vcpu|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got #{mem}MB memory and #{vcpu} vcpus") do
    output, _code = node.run("virsh dumpxml #{vm}")
    has_memory = output.include? "<memory unit='KiB'>#{Integer(mem) * 1024}</memory>"
    has_vcpus = output.include? ">#{vcpu}</vcpu>"
    break if has_memory && has_vcpus

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have ([a-z]*) graphics device$/) do |vm, host, type|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got #{type} graphics device") do
    output, _code = node.run("virsh dumpxml #{vm}")
    check_nographics = type == 'no' && (!output.include? '<graphics')
    break if (output.include? "<graphics type='#{type}'") || check_nographics

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have ([0-9]*) NIC using "([^"]*)" network$/) do |vm, host, count, net|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got #{count} network interface using #{net}") do
    output, _code = node.run("virsh dumpxml #{vm}")
    break if Nokogiri::XML(output).xpath("//interface/source[@network='#{net}']").size == count.to_i

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have a NIC with ([0-9a-zA-Z:]*) MAC address$/) do |vm, host, mac|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a network interface with #{mac} MAC address") do
    output, _code = node.run("virsh dumpxml #{vm}")
    break if output.include? "<mac address='#{mac}'/>"

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have a "([^"]*)" ([^ ]*) disk$/) do |vm, host, path, bus|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a #{path} #{bus} disk") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks =
      tree.xpath('//disk').select do |x|
        (x.xpath('source/@file')[0].to_s.include? path) && (x.xpath('target/@bus')[0].to_s == bus)
      end
    break unless disks.empty?

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have a "([^"]*)" ([^ ]+) disk from pool "([^"]*)"$/) do |vm, host, vol, bus, pool|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a #{vol} #{bus} disk from pool #{pool}") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks =
      tree.xpath('//disk').select do |x|
        (x.xpath('source/@pool')[0].to_s == pool) && (x.xpath('source/@volume')[0].to_s == vol) &&
          (x.xpath('target/@bus')[0].to_s == bus.downcase)
      end
    break unless disks.empty?

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have (no|a) ([^ ]*) ?cdrom$/) do |vm, host, presence, bus|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} #{presence == 'a' ? 'never got' : 'still has'} a #{bus} cdrom") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks = tree.xpath('//disk')
    disk_index = disks.find_index { |x| x.attribute('device').to_s == 'cdrom' }
    break if (disk_index.nil? && presence == 'no') ||
             (!disk_index.nil? && disks[disk_index].xpath('target/@bus')[0].to_s == bus && presence == 'a')

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have "([^"]*)" attached to a cdrom$/) do |vm, host, path|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a #{path} attached to cdrom") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks = tree.xpath('//disk')
    disk_index = disks.find_index { |x| x.attribute('device').to_s == 'cdrom' }
    source = (!disk_index.nil? && disks[disk_index].xpath('source/@file')[0].to_s) || ''
    break if source == path

    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should boot using autoyast$/) do |vm, host|
  node = get_target(host)
  output, _code = node.run("virsh dumpxml #{vm}")
  tree = Nokogiri::XML(output)
  has_kernel = tree.xpath('//os/kernel').size == 1
  has_initrd = tree.xpath('//os/initrd').size == 1
  has_autoyast = tree.xpath('//os/cmdline')[0].to_s.include? ' autoyast='
  unless has_kernel && has_initrd && has_autoyast
    raise ArgumentError,
          'Wrong kernel/initrd/cmdline configuration, ' \
          "kernel: #{has_kernel ? '' : 'not'} set, " \
          "initrd: #{has_initrd ? '' : 'not'} set, " \
          "autoyast kernel parameter: #{has_autoyast ? '' : 'not'} set"
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should boot on hard disk at next start$/) do |vm, host|
  node = get_target(host)
  output, _code = node.run("virsh dumpxml --inactive #{vm}")
  tree = Nokogiri::XML(output)
  has_kernel = tree.xpath('//os/kernel').size == 1
  has_initrd = tree.xpath('//os/initrd').size == 1
  has_cmdline = tree.xpath('//os/cmdline').size == 1
  unless !has_kernel && !has_initrd && !has_cmdline
    raise SystemCallError,
          'Virtual machine will not boot on hard disk at next start, ' \
          "kernel: #{has_kernel ? '' : 'not'} set, " \
          "initrd: #{has_initrd ? '' : 'not'} set, " \
          "cmdline: #{has_cmdline ? '' : 'not'} set"
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should (not stop|stop) on reboot((?: at next start)?)$/) do |vm, host, stop, next_start|
  node = get_target(host)
  inactive = next_start == ' at next start' ? '--inactive' : ''
  output, _code = node.run("virsh dumpxml #{inactive} #{vm}")
  tree = Nokogiri::XML(output)
  on_reboot = tree.xpath('//on_reboot/text()')[0].to_s
  unless (on_reboot == 'destroy' && stop == 'stop') || (on_reboot == 'restart' && stop == 'not stop')
    raise ScriptError, "Invalid reboot configuration #{next_start}: on_reboot: #{on_reboot}"
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should be UEFI enabled$/) do |vm, host|
  node = get_target(host)
  output, _code = node.run("virsh dumpxml #{vm}")
  tree = Nokogiri::XML(output)
  has_loader = tree.xpath('//os/loader').size == 1
  has_nvram = tree.xpath('//os/nvram').size == 1
  raise KeyError, 'No loader and nvram set: not UEFI enabled' unless has_loader && has_nvram
end

When(/^I create empty "([^"]*)" qcow2 disk file on "([^"]*)"$/) do |path, host|
  node = get_target(host)
  node.run("qemu-img create -f qcow2 #{path} 1G")
end

When(/^I delete all "([^"]*)" volumes from "([^"]*)" pool on "([^"]*)" without error control$/) do |volumes, pool, host|
  node = get_target(host)
  output, _code = node.run("virsh vol-list #{pool} | sed -n -e 's/^[[:space:]]*\([^[:space:]]\+\).*$/\1/;/#{volumes}/p'", check_errors: false)
  output.each_line { |volume| node.run("virsh vol-delete #{volume} #{pool}", check_errors: false) }
end

When(/^I refresh the "([^"]*)" storage pool of this "([^"]*)"$/) do |pool, host|
  node = get_target(host)
  node.run("virsh pool-refresh #{pool}")
end

Then(/^I should not see a "([^"]*)" virtual network on "([^"]*)"$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual network on #{host} still exists") do
    _output, code = node.run("virsh net-info #{vm}", check_errors: false)
    break if code == 1

    sleep 3
  end
end

Then(/^I should see a "([^"]*)" virtual network on "([^"]*)"$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual network on #{host} still doesn't exist") do
    _output, code = node.run("virsh net-info #{vm}", check_errors: false)
    break if code.zero?

    sleep 3
  end
end

Then(/^"([^"]*)" virtual network on "([^"]*)" should have "([^"]*)" IPv4 address with ([0-9]+) prefix$/) do |net, host, ip, prefix|
  node = get_target(host)
  repeat_until_timeout(message: "#{net} virtual net on #{host} never got #{ip}/#{prefix} IPv4 address") do
    output, _code = node.run("virsh net-dumpxml #{net}")
    tree = Nokogiri::XML(output)
    ips = tree.xpath('//ip[@family="ipv4"]')
    break if !ips.empty? && ips[0]['address'] == ip && ips[0]['prefix'] == prefix

    sleep 3
  end
end
