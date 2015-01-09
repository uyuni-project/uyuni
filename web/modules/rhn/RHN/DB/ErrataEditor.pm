#
# Copyright (c) 2008--2012 Red Hat, Inc.
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

use strict;

package RHN::DB::ErrataEditor;

use RHN::DB;
use RHN::Errata;
use RHN::ErrataTmp;
use RHN::DataSource::Errata;
use RHN::Exception qw/throw/;

use Params::Validate qw/:all/;
Params::Validate::validation_options(strip_leading => "-");

sub publish_errata {
  my $class = shift;
  my $temp_errata = shift;
  my $transaction = shift;

  throw "RHN::ErrataEditor::publish_errata - no temp_errata param"
    unless ($temp_errata);

  my $dbh = $transaction || RHN::DB->connect;
  $dbh->nest_transactions;

  my $new_errata = RHN::Errata->create_errata;

  foreach my $meth ($temp_errata->method_names) {
    $new_errata->$meth($temp_errata->$meth());
  }

  $new_errata->commit(0, $dbh);

  my $temp_eid = $temp_errata->id;
  my $new_eid = $new_errata->id;

  my ($query, $sth);

  $query =<<EOQ;
INSERT
  INTO rhnErrataBugList
       (errata_id, bug_id, summary, href)
       (SELECT :new_eid, BL.bug_id, BL.summary, BL.href
          FROM rhnErrataBuglistTmp BL
         WHERE BL.errata_id = :temp_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, temp_eid => $temp_eid);

  $query =<<EOQ;
DELETE FROM rhnErrataBugListTmp
 WHERE errata_id = :temp_eid
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(temp_eid => $temp_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataPackage
       (errata_id, package_id)
       (SELECT :new_eid, EP.package_id
          FROM rhnErrataPackageTmp EP
         WHERE EP.errata_id = :temp_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, temp_eid => $temp_eid);

  $query =<<EOQ;
DELETE FROM rhnErrataPackageTmp
 WHERE errata_id = :temp_eid
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(temp_eid => $temp_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataKeyword
       (errata_id, keyword)
       (SELECT :new_eid, EK.keyword
          FROM rhnErrataKeywordTmp EK
         WHERE EK.errata_id = :temp_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, temp_eid => $temp_eid);

  $query =<<EOQ;
DELETE FROM rhnErrataKeywordTmp
 WHERE errata_id = :temp_eid
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(temp_eid => $temp_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataCloned
       (original_id, id)
       (SELECT ECT.original_id, :new_eid
          FROM rhnErrataClonedTmp ECT
         WHERE ECT.id = :temp_eid)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, temp_eid => $temp_eid);

  $query =<<EOQ;
DELETE
  FROM rhnErrataClonedTmp ECT
 WHERE ECT.id = :temp_eid
    OR ECT.original_id = :temp_eid
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(temp_eid => $temp_eid);

  $query =<<EOQ;
DELETE FROM rhnErrataTmp
 WHERE id = :temp_eid
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(temp_eid => $temp_eid);

  $query =<<EOQ;
INSERT INTO rhnErrataCve
        (errata_id, cve_id)
        (SELECT ECL.id, EC.cve_id
           FROM rhnErrataCVE EC, rhnErrataCloned ECL
          WHERE ECL.original_id = EC.errata_id
           AND ECL.id = :new_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid);

  my $errata = RHN::ErrataTmp->lookup_managed_errata(-id => $new_eid);
  $errata->refresh_erratafiles();

  $dbh->nested_commit;

  return ($new_eid);
}

# This duplicates the algorithm in PublishErrataHelper.java
sub find_next_advisory {
  my $adv = shift || '';
  my $adv_name = shift || '';
  my $eid = shift;

  # Set adv equal to adv_name if unset
  unless ($adv) {
    $adv = $adv_name;
  }

  if (not erratum_is_clone($eid)) {
    # For RH errata, replace 'RH' with 'CL-', else prepend
    if ('RH' eq substr($adv_name, 0, 2)) {
      $adv = 'CL-' . substr($adv, 2);
      $adv_name = 'CL-' . substr($adv_name, 2);
    } else {
      $adv = 'CL-' . $adv;
      $adv_name = 'CL-' . $adv_name;
    }
  } else {
    # Erratum is a clone, prepend 'CL-', if there is no prefix yet
    if ('-' ne substr($adv, 2, 1) || '-' ne substr($adv_name, 2, 1)) {
      $adv = 'CL-' . $adv;
      $adv_name = 'CL-' . $adv_name;
    }
  }

  # Find the next valid advisory name that doesn't exist yet
  while (advisory_exists($adv) || advisory_name_exists($adv_name)) {
    my $c1 = substr($adv, 1, 1);
    if ('Z' eq $c1) {
      # Get the next c0
      my $c0next = ++substr($adv, 0, 1);
      $adv = $c0next . 'A' . substr($adv, 2);
      $adv_name = $c0next . 'A' . substr($adv_name, 2);
    } else {
      # Get the next c1
      my $c1next = ++$c1;
      $adv = substr($adv, 0, 1) . $c1next . substr($adv, 2);
      $adv_name = substr($adv_name, 0, 1) . $c1next . substr($adv_name, 2);
    }
  }

  return ($adv, $adv_name);
}

sub clone_into_org {
  my $class = shift;
  my $old_eid = shift;
  my $org_id = shift;

  throw "No eid" unless $old_eid;

  my $errata = RHN::Errata->lookup(-id => $old_eid);
  my $new = RHN::ErrataTmp->create_errata;

  my $update_date = $errata->update_date;

  foreach my $meth ($errata->method_names) {
    $new->$meth($errata->$meth());
  }

  my $new_update = $new->update_date;

  $new->org_id($org_id);

  my $adv = $new->advisory;
  my $adv_name = $new->advisory_name;

  ($adv, $adv_name) = find_next_advisory($adv, $adv_name, $old_eid);

  $new->advisory($adv);
  $new->advisory_name($adv_name);

  $new->commit(0);
  my $new_eid = $new->id;

  my $dbh = RHN::DB->connect;

  my ($query, $sth);

  $query =<<EOQ;
INSERT
  INTO rhnErrataBugListTmp
       (errata_id, bug_id, summary, href)
       (SELECT :new_eid, BL.bug_id, BL.summary, BL.href
          FROM rhnErrataBuglist BL
         WHERE BL.errata_id = :old_eid)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataPackageTmp
       (errata_id, package_id)
       (SELECT :new_eid, EP.package_id
          FROM rhnErrataPackage EP
         WHERE EP.errata_id = :old_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataFileTmp
       (id, errata_id, type, checksum_id, filename)
       (SELECT sequence_nextval('rhn_erratafile_id_seq'), :new_eid, EF.type, EF.checksum_id, EF.filename
          FROM rhnErrataFile EF
         WHERE EF.errata_id = :old_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataKeywordTmp
       (errata_id, keyword)
       (SELECT :new_eid, EK.keyword
          FROM rhnErrataKeyword EK
         WHERE EK.errata_id = :old_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataClonedTmp
       (original_id, id)
       VALUES
       (:old_eid, :new_eid)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);


  return ($new_eid);
}

sub erratum_is_clone {
  my $eid = shift;
  my $dbh = RHN::DB->connect;

  my $query =<<EOQ;
SELECT 1
  FROM dual
 WHERE EXISTS (SELECT 1 FROM rhnErrataCloned E WHERE E.id = :eid)
    OR EXISTS (SELECT 1 FROM rhnErrataClonedTmp ET WHERE ET.id = :eid)
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(eid => $eid);
  my ($exists) = $sth->fetchrow;
  $sth->finish;

  return ($exists ? 1 : 0);
}

sub advisory_exists {
  my $adv = shift;

  my $dbh = RHN::DB->connect;

  my $query =<<EOQ;
SELECT 1
  FROM dual
 WHERE EXISTS (SELECT 1 FROM rhnErrata E WHERE E.advisory = :advisory)
    OR EXISTS (SELECT 1 FROM rhnErrataTmp ET WHERE ET.advisory = :advisory)
EOQ

  my $sth = $dbh->prepare($query);

  $sth->execute_h(advisory => $adv);

  my ($exists) = $sth->fetchrow;
  $sth->finish;

  return ($exists ? 1 : 0);
}

sub advisory_name_exists {
  my $adv_name = shift;

  my $dbh = RHN::DB->connect;

  my $query =<<EOQ;
SELECT 1
  FROM dual
 WHERE EXISTS (SELECT 1 FROM rhnErrata E WHERE E.advisory_name = :advisory_name)
    OR EXISTS (SELECT 1 FROM rhnErrataTmp ET WHERE ET.advisory_name = :advisory_name)
EOQ

  my $sth = $dbh->prepare($query);

  $sth->execute_h(advisory_name => $adv_name);

  my ($exists) = $sth->fetchrow;
  $sth->finish;

  return ($exists ? 1 : 0);
}

sub find_clones_of_errata {
  my $class = shift;
  my %attr = validate(@_, {eid => 1, org_id => 1});

  my $ds1 = new RHN::DataSource::Errata (-mode => 'published_clones_of_errata');
  my $data = $ds1->execute_query(-eid => $attr{eid}, -org_id => $attr{org_id});

  my $ds2 = new RHN::DataSource::Errata (-mode => 'unpublished_clones_of_errata');
  my $data2 = $ds2->execute_query(-eid => $attr{eid}, -org_id => $attr{org_id});

  push @{$data}, @{$data2};

  return $data;
}

sub clone_errata_fast {
  my $class = shift;
  my $old_eid = shift;
  my $org_id = shift;

  throw "No eid" unless $old_eid;

  my $errata = RHN::Errata->lookup(-id => $old_eid);
  my $new = RHN::Errata->create_errata;

  my $update_date = $errata->update_date;

  foreach my $meth ($errata->method_names) {
    $new->$meth($errata->$meth());
  }

  my $new_update = $new->update_date;

  $new->org_id($org_id);

  my $adv = $new->advisory;
  my $adv_name = $new->advisory_name;

  ($adv, $adv_name) = find_next_advisory($adv, $adv_name, $old_eid);

  $new->advisory($adv);
  $new->advisory_name($adv_name);

  $new->commit(0);
  my $new_eid = $new->id;

  my $dbh = RHN::DB->connect;

  my ($query, $sth);

  $query =<<EOQ;
INSERT
  INTO rhnErrataBugList
       (errata_id, bug_id, summary, href)
       (SELECT :new_eid, BL.bug_id, BL.summary, BL.href
          FROM rhnErrataBuglist BL
         WHERE BL.errata_id = :old_eid)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataPackage
       (errata_id, package_id)
       (SELECT :new_eid, EP.package_id
          FROM rhnErrataPackage EP
         WHERE EP.errata_id = :old_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataFile
       (id, errata_id, type, checksum_id, filename)
       (SELECT sequence_nextval('rhn_erratafile_id_seq'), :new_eid, EF.type, EF.checksum_id, EF.filename
          FROM rhnErrataFile EF
         WHERE EF.errata_id = :old_eid)
EOQ
  #$sth = $dbh->prepare($query);
  #$sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataKeyword
       (errata_id, keyword)
       (SELECT :new_eid, EK.keyword
          FROM rhnErrataKeyword EK
         WHERE EK.errata_id = :old_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT
  INTO rhnErrataCloned
       (original_id, id)
       VALUES
       (:old_eid, :new_eid)
EOQ

  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid, old_eid => $old_eid);

  $query =<<EOQ;
INSERT INTO rhnErrataCve
        (errata_id, cve_id)
        (SELECT ECL.id, EC.cve_id
           FROM rhnErrataCVE EC, rhnErrataCloned ECL
          WHERE ECL.original_id = EC.errata_id
           AND ECL.id = :new_eid)
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute_h(new_eid => $new_eid);

  return ($new_eid);
}

1;

