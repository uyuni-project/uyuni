#!/usr/bin/perl

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only


use Frontier::Client;
use Data::Dumper;
use CGI;
use Date::Parse;

#EDITABLE options here:
my $HOST = 'dhcp59-112.rdu.redhat.com';
my $user = 'admin';
my $pass = 'spacewalk';
#END editable options


my $client = new Frontier::Client(url => "http://$HOST/rpc/api");
my $session = $client->call('auth.login',$user, $pass);

my $system = 1000010052;

my $channels = $client->call('system.getEventHistory', $session, $system);

foreach my $channel (@$channels) {
        $date = $channel->{'completed'};
        print $channel->{'summary'};
        print " - ".$date->value;
        print " - ".$channel->{'details'};
        print "\n";
}










