# snapshot books
Before do |scenario|
  # take a snapshot
  if ENV['VHOST']
    step "I take a snapshot \"#{short_name(scenario.name)}\""
  end
end

After do |scenario|
  # take a snapshot if the scenario fails
  if ENV['VHOST']
    name = short_name(scenario.name)
    if scenario.failed?
      step "I take a snapshot \"#{name}_failed\""
    else
      # nothing happened, delete snapshot as we dont need it
      $sshout = ""
      $sshout = `echo | ssh root@$VHOST rm -f $IMGDIR/#{name}.qcow2`
      puts "Failed to remove snapshot" unless $?.success?
    end
  end
end

def short_name(name)
  name.downcase.delete('^a-zA-z0-9 ').tr(' ', '_').slice(0..35)
end
