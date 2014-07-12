#
# Copyright (c) 2008--2014 Red Hat, Inc.
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

package Sniglets::ListView::ActionList;

use Sniglets::ListView::List;
use RHN::Action;
use RHN::DataSource::Action;

use Data::Dumper;

our @ISA = qw/Sniglets::ListView::List/;

my %mode_data;
sub mode_data { return \%mode_data }

_register_modes();

sub trap {
  return "rhn:action_list_cb";
}

sub list_of { return "actions" }

sub _register_modes {

  Sniglets::ListView::List->add_mode(-mode => "system_events_history",
			   -datasource => RHN::DataSource::Action->new,
			   -provider => \&system_history_provider);

  Sniglets::ListView::List->add_mode(-mode => "events_in_set",
			   -datasource => RHN::DataSource::Action->new,
			   -provider => \&system_history_provider,
			   -action_callback => \&events_in_set_cb);

  Sniglets::ListView::List->add_mode(-mode => "system_events_pending",
			   -datasource => RHN::DataSource::Action->new,
			   -provider => \&system_history_provider);

  Sniglets::ListView::List->add_mode(-mode => "supported_system_history",
			   -datasource => RHN::DataSource::Action->new,
			   -provider => \&system_history_provider);

  Sniglets::ListView::List->add_mode(-mode => "pending_action_list",
			   -datasource => RHN::DataSource::Action->new,
			   -action_callback => \&default_callback);

  Sniglets::ListView::List->add_mode(-mode => "completed_action_list",
			   -datasource => RHN::DataSource::Action->new,
			   -action_callback => \&default_callback);

  Sniglets::ListView::List->add_mode(-mode => "failed_action_list",
			   -datasource => RHN::DataSource::Action->new,
			   -action_callback => \&default_callback);

}


my %history_type_icons = (
        'packages.refresh_list' => 'event-type-package',
        'packages.delta'        => 'event-type-package',
        'packages.update'       => 'event-type-package',
        'packages.remove'       => 'event-type-package',
        'packages.runTransaction' => 'event-type-package',
        'up2date_config.get'    => 'event-type-preferences',
        'up2date_config.update' => 'event-type-preferences',
        'rollback.config'       => 'event-type-preferences',
        'rollback.listTransactions' => 'event-type-package',    # bad icon for this
        'errata.update'         => 'event-type-errata',
        'hardware.refresh_list' => 'event-type-system',         # bad icon for this
        'reboot.reboot'         => 'event-type-system',         # bad icon for this
        'configfiles.upload'    => 'event-type-system',
        'configfiles.deploy'    => 'event-type-system',
        'configfiles.verify'    => 'event-type-system',
        'configfiles.diff'      => 'event-type-system',
 );

my %history_status_icons = ('Completed' => 'action-ok',
                            'Failed'    => 'action-failed',
                            'Picked Up' => 'action-running',
                           );

sub system_history_provider {
  my $self = shift;
  my $pxt = shift;

  my %ret = $self->default_provider($pxt);

  foreach my $event (@{$ret{data}}) {


    # pick the most recent relevent time/date for the entry's
    # "TIME" in the server history...
     if (defined $event->{COMPLETED}) {
       $event->{TIME} = $event->{COMPLETED};
     }
     elsif (defined $event->{PICKED_UP}) {
       $event->{TIME} = $event->{PICKED_UP};
     }
     elsif (defined $event->{CREATED}) {
       $event->{TIME} = $event->{CREATED};
     }

    if (defined $event->{HISTORY_TYPE}) {

      if ($history_type_icons{$event->{HISTORY_TYPE}}) {
	$event->{HISTORY_TYPE} = PXT::HTML->icon(
                                -type => $history_type_icons{$event->{HISTORY_TYPE}},
                                -title => $event->{HISTORY_TYPE_NAME});
      }
      else {
	PXT::Debug->log(2,"no icon for scheduled action type?!  type:  " . $event->{HISTORY_TYPE});
	$event->{HISTORY_TYPE} = PXT::HTML->icon(-type => "event-type-system",
                                                 -title => "System Event");
      }
    }
    else {
      $event->{HISTORY_TYPE} = PXT::HTML->icon(-type => "event-type-system",
                                               -title => "System Event");
    }

    if (defined $event->{HISTORY_STATUS}) {
      $event->{HISTORY_STATUS} = PXT::HTML->icon(
                             -type => $history_status_icons{$event->{HISTORY_STATUS}},
                             -title => $event->{HISTORY_STATUS});
    }
    else {
      $event->{HISTORY_STATUS} = '<span class="no-details">(n/a)</span>';
    }

  }

  return (%ret);
}

sub events_in_set_cb {
  my $self = shift;
  my $pxt = shift;
  my %action = @_;

  PXT::Debug->log(7, "action:  " . Data::Dumper->Dump([(\%action)]));

  my $sid = $pxt->param('sid');

  if (exists $action{label} and $action{label} eq 'cancel-events') {

    RHN::Action->delete_system_from_action_set($sid, $pxt->user->id, 'schedule_action_list');

    my $set = new RHN::DB::Set('schedule_action_list', $pxt->user->id);
    $set->empty;
    $set->commit;

    $pxt->push_message(site_info => 'Events canceled.');
    $pxt->redirect("/rhn/systems/details/history/Pending.do?sid=$sid");
  }

  return 1;
}


sub default_callback {
  my $self = shift;
  my $pxt = shift;

  my %action = @_;

  my $label = '';
  my $action_type = '';
  my $set_label = '';

  if (exists $action{label}) {
    $label = $action{label};
    $label =~ m/archive_(.*)_actions/;
    $action_type = $1;
    $set_label = $action_type . '_action_list';

    my $set = new RHN::DB::Set $set_label, $pxt->user->id;
    my $count = scalar $set->contents;

    RHN::Action->archive_actions($pxt->user, $set);

    $set->empty;
    $set->commit;

    $pxt->push_message(site_info => sprintf('<strong>%d</strong> action%s archived.', $count, $count == 1 ? '' : 's'));

    $pxt->redirect($pxt->uri . "?lower=1");
  }

  return 1;
}


my %status_count_lookup = (Queued => 'IN_PROGRESS_COUNT',
			   'Picked Up' => 'IN_PROGRESS_COUNT',
			   Completed => 'SUCCESSFUL_COUNT',
			   Failed => 'FAILED_COUNT');
sub row_callback {
  my $self = shift;
  my $row = shift;
  my $pxt = shift;


#  SYNTAX:
#  if ( exists $row->{FOO} ) {
#   $row->{BAZ} = $row->{FOO} + $row->{BAR};
# }

  if ( exists $row->{EARLIEST} ) {
    $row->{EARLIEST} = $pxt->user->convert_time($row->{EARLIEST});
  }

  if ( exists $row->{__data__} ) {
    $row->{SUCCESSFUL_COUNT} = 0;
    $row->{FAILED_COUNT} = 0;
    $row->{IN_PROGRESS_COUNT} = 0;
    $row->{TOTAL_COUNT} = 0;

    foreach my $count (@{$row->{__data__}}) {
      $row->{TOTAL_COUNT} += $count->{TALLY};
      $row->{$status_count_lookup{$count->{ACTION_STATUS}}} += $count->{TALLY};
    }
  }

  return $row;
}

sub is_row_selectable {
  my $self = shift;
  my $pxt = shift;
  my $row = shift;

  my $mode = $self->datasource->mode();

  if (grep { $mode eq $_} qw/pending_action_list failed_action_list completed_action_list/) {
    unless ($pxt->user->is('org_admin') or $row->{SCHEDULER} == $pxt->user->id) {
      return 0;
    }
  }
  elsif ($mode eq 'system_events_pending') {
    return 0 if $row->{PREREQ_AID};
  }

  return 1;
}

sub clean_set {
  my $self = shift;
  my $set = shift;
  my $user = shift;
  my $formvars = shift;

  if($self->datasource->mode() eq 'system_events_pending') {
    $set->remove_prereq_actions($formvars->{sid});
  }

  return if $user->is('org_admin');

  $set->remove_unowned_actions($user);

  return;
}

1;
