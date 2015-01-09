#
# Copyright (c) 2008--2012 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#

use strict;

use lib '/var/www/lib';

package RHN::DB::Org;

use RHN::DB;
use RHN::DB::TableClass;
use RHN::Exception;

use RHN::DataSource::Channel;

use PXT::Config ();
use PXT::Debug ();
use RHN::DB::Server ();

use Params::Validate qw/:all/;
Params::Validate::validation_options(strip_leading => "-");

my @org_fields = qw/ID NAME CREATED MODIFIED/;

my @skip_cert_channels = qw/%%-beta%% %%-staging%% rhn-satellite%%/;

my $o = new RHN::DB::TableClass("web_customer", "O", "", @org_fields);

sub lookup {
  my $class = shift;
  my %params = validate(@_, { id => 0 });

  my $query;
  my $value;

 if ($params{id}) {
    $query = $o->select_query("O.ID = ?");
    $value = $params{id};
  }
  else {
    Carp::croak "must use -id in lookup";
  }

  my $dbh = RHN::DB->connect;
  my $sth;

  $sth = $dbh->prepare($query);
  $sth->execute($value);

  my @columns = $sth->fetchrow;
  $sth->finish;

  my $ret;
  if ($columns[0]) {
    $ret = $class->blank_org;

    $ret->{__id__} = $columns[0];
    $ret->$_(shift @columns) foreach $o->method_names;
    delete $ret->{":modified:"};
  }
#  else {
#    local $" = ", ";
#    die "Error loading org $value; no ID? (@columns)";
#  }
  else {
    return;
  }

  $query = <<EOQ;
  SELECT   label
    FROM   rhnOrgEntitlementType OET, rhnOrgEntitlements OE
   WHERE   OE.org_id = ?
     AND   OET.id = OE.entitlement_id
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute($ret->id);

  my $has_enterprise;
  my @ents;
  while (my ($ent) = $sth->fetchrow) {
    push @ents, $ent;
    $has_enterprise = 1 if $ent eq 'sw_mgr_enterprise';
  }
  push @ents, 'sw_mgr_personal' unless $has_enterprise;
  $ret->entitlements(@ents);

  return $ret;
}


my @valid_org_entitlements = qw/sw_mgr_personal sw_mgr_enterprise rhn_provisioning rhn_nonlinux rhn_monitor rhn_solaris/;

sub has_entitlement {
  my $self = shift;
  my $entitlement = shift;

  throw "no entitlement given" unless $entitlement;
  throw "Invalid org entitlement type '$entitlement'." unless grep { $entitlement eq $_ } @valid_org_entitlements;

  return (grep { $_ eq $entitlement } $self->entitlements) ? 1 : 0;
}

sub entitlements {
  my $self = shift;

  if (@_) {
    $self->{__entitlements__} = [ @_ ];
  }

  return @{$self->{__entitlements__} || []};
}

sub blank_org {
  my $class = shift;

  my $self = bless { }, $class;

  return $self;
}

# build some accessors
foreach my $field ($o->method_names) {
  my $sub = q {
    sub [[field]] {
      my $self = shift;
      if (@_ and "[[field]]" ne "id") {
        $self->{":modified:"}->{[[field]]} = 1;
        $self->{__[[field]]__} = shift;
      }
      return $self->{__[[field]]__};
    }
  };

  $sub =~ s/\[\[field\]\]/$field/g;
  eval $sub;

  if ($@) {
    die $@;
  }
}

sub commit {
  my $self = shift;
  my $mode = 'update';

  if ($self->id == -1) {
    die "dead code - how did you get here?";
  }

  die "$self->commit called on org without valid id" unless $self->id and $self->id > 0;

  my @modified = keys %{$self->{":modified:"}};
  my %modified = map { $_ => 1 } @modified;

  return unless @modified;

  my $dbh = RHN::DB->connect;

  my $query;
  if ($mode eq 'update') {
    $query = $o->update_query($o->methods_to_columns(@modified));
    $query .= "O.ID = ?";
  }
  else {
    $query = $o->insert_query($o->methods_to_columns(@modified));
  }

  #warn "ins/upd query: $query";

  my $sth = $dbh->prepare($query);
  $sth->execute((map { $self->$_() } grep { $modified{$_} } $o->method_names), ($mode eq 'update') ? ($self->id) : ());

  $dbh->commit;
  $self->oai_customer_sync();

  delete $self->{":modified:"};
}

sub entitlement_counts {
  my $self = shift;
  my $entitlement = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT current_members, max_members
  FROM rhnServerGroup SG
 WHERE SG.group_type = (SELECT id FROM rhnServerGroupType WHERE label = ?)
   AND SG.org_id = ?
EOQ

  $sth->execute($entitlement, $self->id);
  my @ret = $sth->fetchrow;
  $sth->finish;

  return @ret;
}

sub entitlement_data {
  my $self = shift;

  my $dbh = RHN::DB->connect;

  my $ent_data = { };

  foreach my $level (qw/sw_mgr_entitled enterprise_entitled provisioning_entitled
                        monitoring_entitled/) {

    my ($used, $max) = $self->entitlement_counts($level);

    $used ||= 0;
    $max ||= 0;

    $ent_data->{$level}->{used} = $used;
    $ent_data->{$level}->{max} = $max;
    $ent_data->{$level}->{available} = $max - $used;
  }

  $ent_data->{sw_mgr_entitled} ||= 1;

  return $ent_data;
}

sub owns_server_groups {
  my $self = shift;
  my @sg_ids = @_;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare("SELECT org_id FROM rhnServerGroup WHERE id = ?");

  foreach my $sgid (@sg_ids) {
    $sth->execute($sgid);
    my ($org_id) = $sth->fetchrow;
    $sth->finish;

    return 0 if $org_id != $self->id;
  }

  return 1;
}

sub has_channel_permission {
  my $self = shift;
  my $channel_id = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT  AC.channel_name
  FROM  rhnAvailableChannels AC
 WHERE  AC.org_id = ?
   AND  AC.channel_id = ?
EOQ

  $sth->execute($self->id, $channel_id);

  my @rows = $sth->fetchrow;
  $sth->finish;

  return 1 if (@rows);
  return 0;
}

sub has_channel_family_entitlement {
  my $self = shift;
  my $label = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT 1
  FROM rhnChannelFamilyPermissions CFP
 WHERE CFP.channel_family_id = (SELECT id FROM rhnChannelFamily WHERE label = ?)
   AND (CFP.org_id IS NULL OR CFP.org_id = ?)
EOS
  $sth->execute($label, $self->id);
  my ($ret) = $sth->fetchrow;

  $sth->finish;

  return $ret;
}

sub oai_customer_sync {
  my $self = shift;

  if (PXT::Config->get('enable_oai_sync')) {
    warn "OAI customer sync";
    my $dbh = RHN::DB->connect;

    $dbh->call_procedure("XXRH_OAI_WRAPPER.sync_customer", $self->id);
    $dbh->commit;
  }
  else {
    warn "No OAI customer sync";
  }
}

#get the org's channelfamil(y|ies)
sub get_channel_family {
  my $self = shift;
  my $org_id = shift || $self->id || return;

  my $dbh = RHN::DB->connect;
  my $query = <<EOQ;
  SELECT CF.id
    FROM rhnChannelFamily CF
   WHERE CF.org_id = ?
ORDER BY CF.id
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute($org_id);

  my @channel_family_ids;

  while (my ($data) = $sth->fetchrow) {
    push @channel_family_ids, $data;
  }

  return @channel_family_ids;

}

# all roles that are available to the org
sub available_roles {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT  DISTINCT UGT.label
  FROM  rhnUserGroupType UGT, rhnUserGroup UG
 WHERE  UGT.id = UG.group_type
   AND  UG.org_id = ?
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute($self->id);

  my @roles;

  while (my ($role) = $sth->fetchrow) {
    push @roles, $role;
  }

  return @roles;
}

sub has_role {
  my $self = shift;
  my $role = shift;

  return unless $role;

  my %roles = map { ($_, 1) } $self->available_roles;

  return (exists $roles{$role} ? 1 : 0);
}


# generalized slot name function; also caches for better performance
sub slot_name {
  my $self = shift;
  my $ent = shift;

  if ($ent eq 'sw_mgr_entitled') {
    return 'VERY BROKEN UPDATE ENTITLEMENT';
  }

  if (exists $self->{cached_slot_names}) {
    return $self->{cached_slot_names}->{$ent};
  }

  my %entitlement_name =
    (none => 'None',
     sw_mgr_entitled => 'Update',
     enterprise_entitled => 'Management',
     provisioning_entitled => 'Provisioning',
     virtualization_host => 'Virtualization',
     virtualization_host_platform => 'Virtualization Platform',
     monitoring_entitled => 'Monitoring',
     nonlinux_entitled => 'Non-Linux');

  $self->{cached_slot_names} = \%entitlement_name;

  return $self->{cached_slot_names}->{$ent};
}



sub org_channel_setting {
  my $self = shift;
  my $cid = shift;
  my $label = shift;

  throw "No channel id" unless $cid;
  throw "No label id" unless $label;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT 1
  FROM rhnOrgChannelSettings OCS, rhnOrgChannelSettingsType OCST
 WHERE OCS.org_id = :org_id
   AND OCS.channel_id = :cid
   AND OCST.label = :label
   AND OCST.id = OCS.setting_id
EOQ

  my $sth = $dbh->prepare($query);

  $sth->execute_h(org_id => $self->id, cid => $cid, label => $label);
  my ($setting) = $sth->fetchrow;

  $sth->finish;

  return ($setting) ? 1 : 0;
}

sub remove_org_channel_setting {
  my $self = shift;
  my $cid = shift;
  my $label = shift;

  throw "No channel id" unless $cid;
  throw "No label" unless $label;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
DELETE
  FROM rhnOrgChannelSettings OCS
 WHERE OCS.org_id = :org_id
   AND OCS.channel_id = :cid
   AND OCS.setting_id = (SELECT id FROM rhnOrgChannelSettingsType WHERE label = :label)
EOQ

  my $sth = $dbh->prepare($query);

  $sth->execute_h(org_id => $self->id, cid => $cid, label => $label);

  $dbh->commit;
}

sub add_org_channel_setting {
  my $self = shift;
  my $cid = shift;
  my $label = shift;

#delete it first...

  $self->remove_org_channel_setting($cid, $label);

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
INSERT
  INTO rhnOrgChannelSettings
       (org_id, channel_id, setting_id)
VALUES (:org_id, :cid, (SELECT id FROM rhnOrgChannelSettingsType WHERE label = :label))
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(org_id => $self->id, cid => $cid, label => $label);

  $dbh->commit;

  return;
}

sub users_in_org_with_channel_role {
  my $self = shift;
  my %attr = validate(@_, { cid => 1, role => 1 });

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT DISTINCT CP.user_id
  FROM rhnChannelPermission CP, rhnChannelPermissionRole CPR, web_contact WC
 WHERE CP.channel_id = :cid
   AND CP.user_id = WC.id
   AND WC.org_id = :org_id
   AND CP.role_id = CPR.id
   AND CPR.label = :role_label
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(org_id => $self->id, cid => $attr{cid}, role_label => $attr{role});

  my @uids;

  while (my ($uid) = $sth->fetchrow) {
    push @uids, $uid;
  }

  return @uids;
}


sub remove_channel_permissions {
  my $self = shift;
  my %attr = validate(@_, { uids => 1, role => 1, cid => 1, transaction => 0});

  my $dbh = $attr{transaction} || RHN::DB->connect;

  my $query =<<EOQ;
DELETE
  FROM rhnChannelPermission CP
 WHERE CP.user_id = :user_id
   AND CP.channel_id = :cid
   AND CP.role_id = (SELECT id FROM rhnChannelPermissionRole WHERE label = :role_label)
EOQ

  my $sth = $dbh->prepare($query);

  foreach my $uid (@{$attr{uids}}) {
    $sth->execute_h(cid => $attr{cid}, user_id => $uid, role_label => $attr{role});
  }
  $sth->finish;

  unless ($attr{transaction}) {
    $dbh->commit;
  }

  return $dbh;
}


sub reset_channel_permissions {
  my $self = shift;
  my %attr = validate(@_, { uids => 1, role => 1, cid => 1 });

  die "uids param is not an arrayref" unless (ref $attr{uids} eq 'ARRAY');

  my $dbh = RHN::DB->connect;

  $attr{transaction} = $dbh;
  $dbh = $self->remove_channel_permissions(%attr);

  my $query =<<EOQ;
INSERT
  INTO rhnChannelPermission
       (channel_id, user_id, role_id)
VALUES (:cid, :user_id, (SELECT id FROM rhnChannelPermissionRole WHERE label = :role_label))
EOQ

  my $sth = $dbh->prepare($query);

  foreach my $uid (@{$attr{uids}}) {
    $sth->execute_h(cid => $attr{cid}, user_id => $uid, role_label => $attr{role});
  }

  $sth->finish;
  $dbh->commit;
}

sub channel_entitlements {
  my $self = shift;

  my $ds = new RHN::DataSource::Channel(-mode => 'channel_entitlements');
  my $channels = $ds->execute_query(-org_id => $self->id);

  foreach my $row (@{$channels}) {
    $row->{AVAILABLE_MEMBERS} = defined $row->{MAX_MEMBERS} ? ($row->{MAX_MEMBERS} - $row->{CURRENT_MEMBERS}) : undef;
  }

  return $channels;
}

1;
