#
# Copyright (c) 2008--2011 Red Hat, Inc.
# Copyright (c) 2010 SUSE LINUX Products GmbH, Nuernberg, Germany.
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

# errata - DB layer
use strict;

package RHN::DB::Errata;

use Carp;
use Params::Validate qw/:all/;
Params::Validate::validation_options(strip_leading => "-");

use RHN::DB;
use RHN::Exception;

use RHN::DB::TableClass;

my @errata_fields = qw/ID ADVISORY ADVISORY_TYPE PRODUCT ERRATA_FROM DESCRIPTION SYNOPSIS TOPIC SOLUTION ISSUE_DATE:shortdate REFERS_TO CREATED:shortdate MODIFIED:longdate UPDATE_DATE:shortdate NOTES ORG_ID ADVISORY_NAME ADVISORY_REL LOCALLY_MODIFIED LAST_MODIFIED:longdate/;

my $e = new RHN::DB::TableClass("rhnErrata", "E", "", @errata_fields);


sub related_cves {
  my $self_or_id = shift;
  my $id;

  if (ref $self_or_id) {
    $id = $self_or_id->id;
  }
  else {
    $id = shift;
  }

  my $dbh = RHN::DB->connect;

  my $query;
  my $sth;

  $query = <<EOQ;
SELECT  CVE.name
  FROM  rhnCVE CVE, rhnErrataCVE ECVE
 WHERE  ECVE.errata_id = ?
   AND  ECVE.cve_id = CVE.id
ORDER BY UPPER(CVE.name)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute($id);

  my @ret;

  while (my ($cve) = $sth->fetchrow) {
    push @ret, $cve;
  }
  $sth->finish;

  return @ret;
}


sub lookup {
  my $class = shift;
  my %params = validate(@_, {id => 0, advisory_name => 0, transaction => 0});
  my $transaction = $params{transaction};


  my $dbh = $transaction || RHN::DB->connect;

  my $tc = $class->table_class;

  my $query;
  my $sth;

  if (defined $params{id}) {
    $query = $tc->select_query("E.ID = ?");
    $sth = $dbh->prepare($query);
    $sth->execute($params{id});
  }
  elsif (defined $params{advisory_name}) {
    $query = $tc->select_query("E.advisory_name = ?");
    $sth = $dbh->prepare($query);
    $sth->execute($params{advisory_name});
  }
  else {
    throw "no info to perform lookup on";
  }
  my @columns = $sth->fetchrow;
  $sth->finish;

  my $ret;
  my $tmp_col;
  $ret = $class->blank_errata;
  if ($columns[0]) {
    $ret->{__id__} = $columns[0];
    foreach ($tc->method_names) {
      $tmp_col = shift @columns;
      if ($_ eq "severity_id") {
        $tmp_col = -1 unless defined($tmp_col);
      }
      $ret->$_($tmp_col);
    }
    #$ret->$_(shift @columns) foreach $tc->method_names;
    delete $ret->{":modified:"};
  }
  else {
    local $" = ", ";
    throw "Error loading patch; no ID? (@columns)";
  }

  return $ret;
}

sub bugs_fixed {
  my $self = shift;
  my $dbh = RHN::DB->connect;

  my $bl_table = $self->table_map('rhnErrataBugList');
  my $query;
  my $sth;

  $query = <<EOQ;
SELECT  EBL.bug_id, EBL.summary, EBL.href
  FROM  $bl_table EBL
 WHERE  EBL.errata_id = ?
ORDER BY EBL.bug_id
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute($self->id);

  my @ret;

  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }
  $sth->finish;

  return @ret;
}

sub keywords {
  my $self = shift;
  my $dbh = RHN::DB->connect;

  my $kw_table = $self->table_map('rhnErrataKeyword');

  my $query;
  my $sth;

  $query = <<EOQ;
SELECT  EK.keyword
  FROM  $kw_table EK
 WHERE  EK.errata_id = ?
ORDER BY UPPER(EK.keyword)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute($self->id);

  my @ret;

  while (my @row = $sth->fetchrow) {
    push @ret, @row;
  }
  $sth->finish;

  return @ret;
}

#all channels affected by errata, pulled from rhnChannelErrata
sub channels {
  my $class = shift;
  my $eid = shift;

  my $dbh = RHN::DB->connect;

  my $query = <<EOQ;
SELECT CE.channel_id
  FROM rhnChannelErrata CE
 WHERE CE.errata_id = ?
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute($eid);

  my @ret;

  while (my ($cid) = $sth->fetchrow) {
    push @ret, $cid;
  }

  return @ret;
}

# show only affected channels that have servers subscribing to them
sub affected_channels {
  my $self = shift;
  my $org_id = shift;

  my $dbh = RHN::DB->connect;

  my $query;
  my $sth;

#   $query = <<EOQ;
# SELECT  DISTINCT C.id, C.name
#   FROM  rhnChannel C, rhnChannelPackage CP, rhnErrataPackage EP
#  WHERE  EP.errata_id = ?
#    AND  EP.package_id = CP.package_id
#    AND  CP.channel_id = C.id
# ORDER BY UPPER(C.name)
# EOQ

  $query = <<EOQ;
SELECT * from (
  SELECT DISTINCT C.id, C.name
    FROM rhnAvailableChannels AC, rhnChannel C, rhnChannelErrata CE
   WHERE CE.errata_id = ?
     AND CE.channel_id = C.id
     AND AC.org_id = ?
     AND C.id = AC.channel_id
) X
ORDER BY UPPER(X.name)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute($self->id, $org_id);

  my @ret;

  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }
  $sth->finish;

  return @ret;

}

# show channels which own packages referred to by this errata
sub related_channels_owned_by_org {
  my $class = shift;
  my $eid = shift;
  my $org_id = shift;

  my $ep_table = $class->table_map('rhnErrataPackage');

  my $dbh = RHN::DB->connect;

  my $query;
  my $sth;

   $query = <<EOQ;
  SELECT DISTINCT C.id
    FROM rhnChannel C,
         rhnChannelPackage CP,
         rhnPackage P2,
         rhnPackage P1,
         $ep_table EP
   WHERE EP.errata_id = :eid
     AND EP.package_id = P1.id
     AND P1.name_id = P2.name_id
     AND P1.package_arch_id = P2.package_arch_id
     AND CP.package_id = P2.id
     AND C.id = CP.channel_id
     AND C.org_id = :org_id
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(eid => $eid, org_id => $org_id);

  my @ret;

  while (my ($id) = $sth->fetchrow) {
    push @ret, $id;
  }
  $sth->finish;

  return @ret;
}

sub packages_in_errata {
  my $class = shift;
  my $eid = shift;

  die "No patch id!" unless $eid;

  my $dbh = RHN::DB->connect;
  my $query;
  my $sth;

  $query = <<EOS;
  SELECT EP.package_id,
         P.name_id,
         PN.name,
         C.label,
         PA.name,
         P.path,
         PE.evr.version,
         PE.evr.release,
         C.name,
         Csum.checksum md5sum,
         P.path,
         PE.evr.epoch,
         PA.label,
         TO_CHAR(P.last_modified, 'YYYY-MM-DD HH24:MI:SS') AS PACKAGE_LAST_MODIFIED
    FROM rhnPackageArch PA,
         rhnPackageEVR PE,
         rhnPackageName PN,
         rhnPackage P,
         rhnChannel C,
         rhnChannelErrata CE,
         rhnChannelPackage CP,
         rhnErrataPackage EP,
         rhnChecksum Csum
 WHERE  EP.errata_id = ?
     AND CE.errata_id = ?
     AND EP.package_id = CP.package_id
     AND CE.channel_id = CP.channel_id
     AND CE.channel_id = C.id
   AND  EP.package_id = P.id
   AND  P.name_id = PN.id
   AND  P.evr_id = PE.id
     AND P.package_arch_id = PA.id
     AND P.checksum_id = Csum.id
EOS

  $sth = $dbh->prepare($query);

  $sth->execute($eid, $eid);

  my @result;

  while (my @row = $sth->fetchrow) {
    push @result, [ @row ];
  }

  return @result;
}

# shows the packages corresponding to the subscribed channels of your org.
sub rhn_files_overview {
  my $self = shift;
  my $org_id = shift;

  my $dbh = RHN::DB->connect;

  my $query;
  my $sth;

  $query = <<EOQ;
SELECT  DISTINCT EFP.package_id,
                 Csum.checksum md5sum,
                 EF.filename AS FILENAME,
                 C.name AS CHANNEL_NAME
            FROM rhnChecksum Csum
            JOIN rhnErrataFile EF ON EF.checksum_id = Csum.id
 LEFT OUTER JOIN rhnErrataFileChannel EFC ON EF.id = EFC.errata_file_id
 LEFT OUTER JOIN rhnErrataFilePackage EFP ON EF.id = EFP.errata_file_id
            JOIN rhnChannel C ON C.id = EFC.channel_id
           WHERE EF.errata_id = :errata_id
             AND EFC.channel_id IN (SELECT AC.channel_id FROM rhnAvailableChannels AC WHERE AC.org_id = :org_id)
        ORDER BY C.name, EF.filename DESC
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(errata_id => $self->id,
		  org_id => $org_id,
		 );

  my @ret;

  while (my $row = $sth->fetchrow_hashref) {
    push @ret, $row;
  }
  $sth->finish;

  return @ret;
}

sub blank_errata {
  my $class = shift;

  my $self = bless { }, $class;
  return $self;
}

sub create_errata {
  my $class = shift;

  my $err = $class->blank_errata;
  $err->{__id__} = -1;

  return $err;
}


# build some accessors
foreach my $field ($e->method_names) {
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

  my $id = shift || 0;
  my $transaction = shift;
  my $dbh = $transaction || RHN::DB->connect;

  my $tc = $self->table_class;

  if ($self->id == -1) {

    unless ($id) {
      my $sth = $dbh->prepare("SELECT sequence_nextval('rhn_errata_id_seq') FROM DUAL");
      $sth->execute;
      ($id) = $sth->fetchrow;
      die "No new patch id from seq rhn_errata_id_seq (possible error: " . $sth->errstr . ")" unless $id;
      $sth->finish;
    }

    $self->{":modified:"}->{id} = 1;
    $self->{__id__} = $id;

    $mode = 'insert';
  }

  die "$self->commit called on patch without valid id" unless $self->id and $self->id > 0;

  my @modified = keys %{$self->{":modified:"}};
  my %modified = map { $_ => 1 } @modified;

  return unless @modified;

  my $query;

  if ($mode eq 'update') {
      $query = $tc->update_query($tc->methods_to_columns(@modified));
      $query .= "E.ID = ?";
    }
  elsif ($mode eq 'insert') {
    $query = $tc->insert_query($tc->methods_to_columns(@modified));
    }
  else {
    die "Invalid mode - '$mode'";
  }

  my $sth = $dbh->prepare($query);
  my @list = map { $self->$_() } (grep { $modified{$_} } $tc->method_names), ($mode eq 'update') ? ('id') : ();

  $sth->execute(@list);

  unless ($transaction) {
    $dbh->commit;
  }
  delete $self->{":modified:"};

  return $transaction;
}

sub find_by_advisory {
  my $class = shift;
  my %params = @_;

  my ($type, $version, $release) =
    map { $params{"-" . $_} } qw/type version release/;

  my $release_str = '';

  if ($release) {
    $release_str = 'AND advisory_rel = ?'
  }

  my $query = <<EOQ;
SELECT id, advisory
  FROM rhnErrata
-- WHERE advisory LIKE ?
 WHERE advisory_name = ? $release_str
ORDER BY UPDATE_DATE DESC
EOQ

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare($query);

  $sth->execute($type . "-" . $version, ($release ? $release : ()));

  my @ret;
  while (my ($id, $adv) = $sth->fetchrow) {
      push @ret, [ $id, $adv ];
  }

  return @ret;
}

sub method_names {
  return $e->method_names;
}

sub errata_fields {
  return @errata_fields;
}

sub table_class {
  return $e;
}

sub table_map {
  my $class = shift;

  return $_[0];
}

sub packages {
  my $self = shift;
  my $eid = $self->id;

  my $ep_table = $self->table_map('rhnErrataPackage');

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT package_id FROM $ep_table WHERE errata_id = :eid
EOQ

  $sth->execute_h(eid => $eid);
  my @ret;

  while (my ($pid) = $sth->fetchrow) {
    push @ret, $pid;
  }

  return @ret;
}

# used by errata search to find the package names in an errata that
# match a given string
sub matching_packages_in_errata {
  my $class = shift;
  my $eid = shift;
  my $string = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT DISTINCT PN.name
  FROM rhnPackageName PN,
       rhnPackageEVR PE,
       rhnPackageArch PA,
       rhnPackage P,
       rhnErrataPackage EP
 WHERE EP.errata_id = :eid
   AND P.id = EP.package_id
   AND PN.id = P.name_id
   AND PE.id = P.evr_id
   AND PA.id = P.package_arch_id
   AND UPPER(PN.name) LIKE UPPER('%' || :search_string || '%')
EOQ

  $sth->execute_h(eid => $eid, search_string => $string);
  my @ret;

  while (my ($pid) = $sth->fetchrow) {
    push @ret, $pid;
  }

  return @ret;
}

sub remove_packages_in_set {
  my $self = shift;
  my %attr = validate(@_, { set_label => 1, user_id => 1 });

  my $ep_table = $self->table_map('rhnErrataPackage');

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
DELETE
  FROM $ep_table EP
 WHERE EP.errata_id = :eid
   AND EP.package_id IN (SELECT S.element FROM rhnSet S WHERE S.user_id = :user_id AND S.label = :set_label)
EOQ

  $sth->execute_h(%attr, eid => $self->id);
  $dbh->commit;

  return;
}

sub add_packages_in_set {
  my $self = shift;
  my %attr = validate(@_, { set_label => 1, user_id => 1 });

  my $ep_table = $self->table_map('rhnErrataPackage');

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
INSERT
  INTO $ep_table
       (errata_id, package_id)
       SELECT :eid, S.element
         FROM rhnSet S
        WHERE S.user_id = :user_id
          AND S.label = :set_label
          AND NOT EXISTS (SELECT 1 FROM $ep_table EP2 WHERE EP2.errata_id = :eid AND EP2.package_id = S.element)
EOQ

  $sth->execute_h(%attr, eid => $self->id);
  $dbh->commit;

  return;
}

sub cloned_from {
  my $self = shift;

  my $er_table = $self->table_map('rhnErrataCloned');

  die "No eid" unless $self->id;

  my $dbh = RHN::DB->connect;

  my $query = <<EOQ;
SELECT EC.original_id as from_errata_id
  FROM $er_table EC
 WHERE EC.id = :eid
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(eid => $self->id);

  my ($progenitor) = $sth->fetchrow;
  $sth->finish;

  return $progenitor;
}

sub refresh_erratafiles {
  my $self = shift;

  my $dbh = RHN::DB->connect;

  my $ef_table = $self->table_map('rhnErrataFile');
  my $query = <<EOQ;
DELETE
  FROM $ef_table EF
 WHERE EF.errata_id = :eid
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(eid => $self->id);

  my $ep_table = $self->table_map('rhnErrataPackage');
  $query = <<EOQ;
SELECT sequence_nextval('rhn_erratafile_id_seq') AS ID, EFT.id AS TYPE_ID, Csum.checksum md5sum, P.path, P.id AS PACKAGE_ID,
       PN.name || '-' || evr_t_as_vre_simple(PE.evr) || '.' || PA.label AS NVREA
  FROM rhnErrataFileType EFT, rhnPackage P, $ep_table EP, rhnPackageName PN, rhnPackageEVR PE, rhnPackageArch PA, rhnChecksum Csum
 WHERE EFT.label = 'RPM'
   AND P.id = EP.package_id
   AND EP.errata_id = :eid
   AND P.evr_id = PE.id
   AND P.name_id = PN.id
   AND P.package_arch_id = PA.id
   AND P.checksum_id = Csum.id
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(eid => $self->id);

  my $ef_insert_query = <<EOQ;
INSERT
  INTO $ef_table
       (id, errata_id, type, checksum_id, filename)
VALUES (:id, :eid, :type, lookup_checksum('md5', :md5sum), :filename)
EOQ

  my $ef_insert_sth = $dbh->prepare($ef_insert_query);

  my $efp_table = $self->table_map('rhnErrataFilePackage');
  my $efp_insert_query = <<EOQ;
INSERT
  INTO $efp_table
       (errata_file_id, package_id)
VALUES (:ef_id, :pid)
EOQ

  my $efp_insert_sth = $dbh->prepare($efp_insert_query);

  my $efc_channel = $self->table_map('rhnErrataFileChannel');
  my $efc_insert_query = <<EOQ;
INSERT
  INTO $efc_channel
       (errata_file_id, channel_id)
VALUES (:ef_id, :cid)
EOQ

  my $efc_insert_sth = $dbh->prepare($efc_insert_query);

  my $channel_select_query = <<EOQ;
SELECT CP.channel_id AS ID
  FROM rhnChannelPackage CP, $ep_table EP
 WHERE CP.package_id = :pid
   AND EP.errata_id = :eid
   AND EP.package_id = CP.package_id
EOQ
  my $channel_select_sth = $dbh->prepare($channel_select_query);
  while (my $row = $sth->fetchrow_hashref) {
    $row->{PATH} ||= '/tmp/' . $row->{NVREA};
    $row->{PATH} =~ s|^redhat/linux/||;

    $ef_insert_sth->execute_h(id => $row->{ID},
			      eid => $self->id,
			      type => $row->{TYPE_ID},
			      md5sum => $row->{MD5SUM},
			      filename => $row->{PATH});

    $efp_insert_sth->execute_h(ef_id => $row->{ID},
			       pid => $row->{PACKAGE_ID});

    $channel_select_sth->execute_h(pid => $row->{PACKAGE_ID},
				   eid => $self->id);

    while (my $channel = $channel_select_sth->fetchrow_hashref) {
      $efc_insert_sth->execute_h(ef_id => $row->{ID},
				 cid => $channel->{ID});
    }
  }

  $dbh->commit;

  return;
}

1;
