#!/usr/bin/perl

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only


use Frontier::Client;
use Data::Dumper;
use CGI;


#EDITABLE options here:
my $HOST = 'dhcp59-112.rdu.redhat.com';
my $user = 'admin';
my $pass = 'spacewalk';
#END editable options


my $client = new Frontier::Client(url => "http://$HOST/rpc/api");
my $session = $client->call('auth.login',$user, $pass);

my $system = 1000010052;

my $channels = $client->call('system.listAdministrators', $session, $system);

foreach my $channel (@$channels) {
        print "USER:\n";
        print $channel->{'id'};
        print "\n";
        print $channel->{'login'}."\n";
        print $channel->{'login_uc'}."\n\n";
}










