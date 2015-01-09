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
package RHN::DB::ConfigRevision;

use Date::Parse;
use Params::Validate qw/:all/;
Params::Validate::validation_options(strip_leading => "-");

use RHN::DB;
use RHN::SimpleStruct;
use RHN::ConfigChannel ();
use RHN::ConfigRevision ();

our @ISA = qw/RHN::SimpleStruct/;

our @core_fields = qw/id revision config_file_id config_content_id
                      config_info_id delim_start delim_end created
                      modified username groupname filemode latest_id latest path
                      md5sum file_size org_id config_channel_id filetype selinux_ctx symlink_target_filename_id/;

our @transient_fields = qw/__contents__ is_binary/;
our @simple_struct_fields = (@core_fields, @transient_fields);

sub lookup {
  my $class = shift;
  my %params = validate(@_, {id => 1});

  my $dbh = RHN::DB->connect();
  my $query = <<EOS;
SELECT
       CR.*,
       CI.username,
       CI.groupname,
       CI.filemode,
       CI.selinux_ctx,
       CI.symlink_target_filename_id,
       CFN.path,
       CC.org_id,
       CC.id config_channel_id,
       Csum.checksum md5sum,
       coalesce(CCon.file_size, 0),
       (SELECT CFt.latest_config_revision_id FROM rhnConfigFile CFt WHERE CFT.id = CR.config_file_id) LATEST_ID,
       CCon.is_binary,
       CFT.name as filetype,
       CCon.delim_start,
       CCon.delim_end
  FROM rhnConfigRevision CR
  JOIN rhnConfigInfo CI
    ON CI.id = CR.config_info_id
  JOIN rhnConfigFile CF
    ON CF.id = CR.config_file_id
  JOIN rhnConfigFileName CFN
    ON CFN.id = CF.config_file_name_id
  JOIN rhnConfigChannel CC
    ON CC.id = CF.config_channel_id
  JOIN rhnConfigFileType CFT
    ON CFT.id = CR.config_file_type_id
  LEFT JOIN rhnConfigContent CCon
    ON CR.config_content_id = CCon.id
  LEFT JOIN rhnChecksum Csum
    ON CCon.checksum_id = Csum.id
 WHERE CR.id = :id
EOS
  my $sth = $dbh->prepare($query);
  $sth->execute_h(id => $params{id});

  my $row = $sth->fetchrow_hashref;

  return undef unless $row;

  die "Multiple returns from id-based select?" if $sth->fetchrow;

  my $self = $class->new;
  for my $field (@simple_struct_fields) {
    $self->$field($row->{+uc $field});
  }
  $self->latest($self->latest_id == $self->id ? 'Y' : 'N');

  $self->is_binary($row->{IS_BINARY} eq 'Y' ? 1 : 0);

  return $self;
}

sub create_config_contents {
  my $class = shift;
  my $contents = shift;
  my $delim_start = shift;
  my $delim_end = shift;
  use Digest::MD5 qw/md5_hex/;
  my $md5sum = md5_hex($contents);

  my $dbh = RHN::DB->connect;
  my $sth;
  $sth = $dbh->prepare(<<EOS);
INSERT INTO rhnConfigContent
  (id, checksum_id, file_size, contents, delim_start, delim_end)
VALUES
  (sequence_nextval('rhn_confcontent_id_seq'), lookup_checksum('md5', :md5sum), :file_size, :contents, :delim_start, :delim_end)
RETURNING id INTO :ccid
EOS

  my $ccid;
  $sth->execute_h(ccid => \$ccid, md5sum => $md5sum, file_size => length($contents),
                  contents => $dbh->encode_blob($contents, "contents"), delim_start => $delim_start, delim_end => $delim_end);

  $dbh->commit;
  return $ccid;
}

## Lookup the id for a given filetype label
sub getFileTypeId {
    my $filetype = shift || '';

    #make sure we're in lowercase
    $filetype =~ tr/A-Z/a-z/;

    if ($filetype ne "file" and $filetype ne "directory" and $filetype ne "symlink") {
        #default to file for now...
        $filetype = "file";
    }

    my $query = <<EOS;
SELECT FT.id
  FROM rhnConfigFileType FT
 WHERE FT.label = :filetype
EOS

    my $dbh = RHN::DB->connect();
    my $sth = $dbh->prepare($query);
    $sth->execute_h(filetype => $filetype);

    my $row = $sth->fetchrow_hashref;

    return undef unless $row;

    die "Multiple returns from id-based select?" if $sth->fetchrow;

    return $row->{ID};
}

sub commit {

  my $self = shift;

  if (defined $self->id) {
    die "Unable to commit a ConfigRevision with an existing ID; make a new revision";
  }
  my $ftype = $self->filetype || 'file';
  my $dbh = RHN::DB->connect;
  my $ciid;
  if ($ftype eq 'symlink') {
      $ciid = $dbh->call_function('lookup_config_info', $self->username, $self->groupname, $self->filemode,
         ((defined $self->selinux_ctx && $self->selinux_ctx ne '')?$self->selinux_ctx:undef),
         $self->symlink_target_filename_id);
  }
  else {
    $ciid = $dbh->call_function('lookup_config_info', $self->username, $self->groupname, $self->filemode,
       ((defined $self->selinux_ctx && $self->selinux_ctx ne '')?$self->selinux_ctx:undef),
       undef);
  }

  my $ccid = $self->config_content_id;
  my $cftid = getFileTypeId($self->filetype);
;

  if ($ftype eq 'file' and defined $self->__contents__)  {
    $ccid = RHN::ConfigRevision->create_config_contents($self->__contents__, $self->delim_start, $self->delim_end);
    $self->config_content_id($ccid);
  }

  my $sth = $dbh->prepare(<<EOS);
DECLARE
BEGIN
  :crid := rhn_config.insert_revision(:revision, :cfid, :ccid, :ciid, :filetype);
END;
EOS
  my $crid;

  $self->revision($self->next_revision);
  $sth->execute_h(cfid => $self->config_file_id, ccid => $ccid, ciid => $ciid, crid => \$crid,
                  revision => $self->revision, filetype => $cftid);

  $dbh->do_h("UPDATE rhnConfigFile SET latest_config_revision_id = :crid WHERE id = :cfid",
             crid => $crid, cfid => $self->config_file_id);

  $self->commit_binary_flag($dbh);

  $dbh->commit;
  $self->id($crid);
}

sub commit_binary_flag {
  my $self = shift;
  my $tx = shift;

  my $dbh = $tx || RHN::DB->connect;

  $dbh->do_h("UPDATE rhnConfigContent SET is_binary = :flag WHERE id = :ccid",
             ccid => $self->config_content_id, flag => $self->is_binary ? 'Y' : 'N');

  $dbh->commit unless $tx;
}

sub copy_revision {
  my $self = shift;

  my $copy = (ref $self)->new;

  $copy->$_($self->$_()) for @simple_struct_fields;
  $copy->id(undef);

  return $copy;
}

sub set_contents {
  my $self = shift;

  $self->__contents__(shift);
}

sub contents {
  my $self = shift;

  if (@_) {
    return $self->set_contents(@_);
  }

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare("SELECT CCon.contents FROM rhnConfigContent CCon WHERE id = :ccid");
  $sth->execute_h(ccid => $self->config_content_id);
  my ($ret) = $sth->fetchrow;
  $sth->finish;

  return $ret;
}

sub config_channel {
  my $self = shift;

  return RHN::ConfigChannel->lookup(-id => $self->config_channel_id);
}

# Find other files with the same path in a namespace
sub find_alternate_in_namespace {
  my $self = shift;
  my $ccid = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT target_CFR.id AS id, target_CFR.revision
  FROM rhnConfigFile current_CF,
       rhnConfigFile target_CF,
       rhnConfigRevision current_CFR,
       rhnConfigRevision target_CFR
 WHERE current_CFR.config_file_id = (SELECT config_file_id FROM rhnConfigRevision WHERE id = :crid)
   AND target_CFR.config_file_id = target_CF.id
   AND current_CFR.config_file_id = current_CF.id
   AND target_CF.config_channel_id = :ccid
   AND target_CF.config_file_name_id = current_CF.config_file_name_id
   AND target_CF.latest_config_revision_id = target_CFR.id
EOQ

  $sth->execute_h(crid => $self->id, ccid => $ccid);

  my $ret = $sth->fetchrow_hashref;
  $sth->finish;

  return $ret;
}

sub next_revision {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT MAX(CR.revision)
  FROM rhnConfigRevision CR,
       rhnConfigFile CF
 WHERE CF.id = :cfid
   AND CR.config_file_id = CF.id
EOS
  $sth->execute_h(cfid => $self->config_file_id);
  my ($ret) = $sth->fetchrow;
  $sth->finish;

  return ($ret || 0) + 1;
}


1;
