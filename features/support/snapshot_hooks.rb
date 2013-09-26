# snapshot books
Before do |scenario|
    #take a snapshot 
    step "I take a snapshot \"#{short_name(scenario.name)}\""
end

After do |scenario|
    #take a snapshot if the scenario fails
    name = short_name(scenario.name)
    if(scenario.failed?)
        step "I take a snapshot \"#{name}_failed\""
    else
        #nothing happened, delete snapshot as we dont need it   
        $sshout = ""
        $sshout = `echo | ssh root@$VHOST rm -f $IMGDIR/#{name}.qcow2`
        raise "Failed to remove snapshot" unless $?.success? 
    end
end

def short_name(name)
    name.downcase.tr(' ', '_').tr('"', '').slice(0..25)
end
