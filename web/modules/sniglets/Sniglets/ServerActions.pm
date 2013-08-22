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

package Sniglets::ServerActions;

use Carp;

use RHN::Action;
use RHN::Set;
use RHN::Scheduler;
use PXT::HTML;
use RHN::ConfigChannel;

use PXT::Utils;
use Date::Parse;

use RHN::Exception;

use POSIX;

use RHN::Date ();

sub register_tags {
  my $class = shift;
  my $pxt = shift;

  $pxt->register_tag('rhn-raw-script-output' => \&raw_script_output);
  $pxt->register_tag('rhn-schedule-action-interface' => \&schedule_action_interface, 2);

  $pxt->register_tag('rhn-reschedule-form-if-failed-action' => \&reschedule_form_if_failed_action);

  $pxt->register_tag('rhn-package-event-result' => \&package_event_result);
}

sub register_callbacks {
  my $class = shift;
  my $pxt = shift;

  $pxt->register_callback('rhn:server_set_errata_set_actions_cb' => \&server_set_errata_set_actions_cb);

  # currently used for mass deletes, mass pkg/hw prof updates, etc...
  $pxt->register_callback('rhn:reschedule_action_cb' => \&reschedule_action_cb);

}

sub raw_script_output {
  my $pxt = shift;
  my $aid = $pxt->param('hid');
  my $sid = $pxt->param('sid');

  my $action = RHN::Action->lookup(-id => $aid);
  my $output = $action->script_server_results($sid);
  $output = $output->{OUTPUT};

  $pxt->manual_content(1);

  $pxt->content_type('text/plain; charset=UTF-8');
  $pxt->header_out('Content-Length' => length $output);
  $pxt->send_http_header;

  $pxt->print($output);
}

sub schedule_action_interface {
  my $pxt = shift;
  my %params = @_;

  my $block = $params{__block__} || $pxt->include('/network/components/schedule_action-interface.pxi');

  my %subst;

  $subst{date_selection} = date_pickbox($pxt);

  $subst{button} = PXT::HTML->submit(-name => $params{action},
				     -value => $params{label});
  $subst{button} .= PXT::HTML->hidden(-name => 'pxt:trap', -value => $params{callback});
  my $passthrough = $params{passthrough};

  if ($passthrough) {
    $subst{button} .= PXT::HTML->hidden(-name => $passthrough, -value => $pxt->passthrough_param($passthrough));
  }

  $block = PXT::Utils->perform_substitutions($block, \%subst);

  return $block;
}

sub reschedule_form_if_failed_action {
  my $pxt = shift;
  my %params = @_;

  my $action_id = $pxt->param('hid');
  my $sid = $pxt->param('sid');

  $pxt->user->verify_action_access($action_id)
      or $pxt->redirect('/network/permission.pxt');

  my $action = RHN::Action->lookup(-id => $action_id);
  return '' unless $action;

  my $reschedule_text = '';

  if ($action->get_server_status($sid) eq 'Failed') {
    $reschedule_text = $pxt->include('/network/components/systems/reschedule_action_form.pxi');

    my $prereq = $action->prerequisite();

    if ($prereq) {
      my $prereq_action = RHN::Action->lookup(-id => $prereq);

      unless ($prereq_action->get_server_status($sid) eq 'Completed') {
	my $prior_action = PXT::HTML->link2(text => 'prior action',
					    url => "/network/systems/details/history/event.pxt?sid=$sid&amp;hid=$prereq",
					   );

	$reschedule_text = "This action requires the successful completion of a $prior_action before it can be rescheduled."
      }
    }

    return PXT::Utils->perform_substitutions($params{__block__}, { reschedule_info => $reschedule_text });
  }

  return '';
}

my @months = qw/January February March April May June July August September October November December/;

sub date_pickbox {
  my $pxt = shift;
  my %params = @_;

  my $prefix = $params{prefix} || '';
  my $blank = $params{start_blank} || undef;

  # 1 == +1, which means this year + 1
  # 0 means this year
  # -1 means last year
  my @year_modifiers = $params{years} ? split /[\s,;]/, $params{years} : (0, 1);

  my $ret;
  my $date;
  if ($params{'preserve'}) {
    my $epoch = Sniglets::ServerActions->parse_date_pickbox($pxt,
							    prefix    => $prefix,
							    long_date => 0,
							   );
    $epoch ||= time;
    $date = new RHN::Date(epoch => $epoch, user => $pxt->user);
  }
  else {
    $date = new RHN::Date(now => 1, user => $pxt->user);
  }

  my @month_list = map { [ $months[$_], $_ + 1 ] } 0..11;

  if ($blank) {
    foreach (@month_list) {
      $_->[2] = 0;
    }
    unshift @month_list, ['Month','', 1];
  }
  else {
    foreach (@month_list) {
      $_->[2] = ($_->[1] == $date->month);
    }
  }

  $ret .= PXT::HTML->select(-name => "${prefix}month",
			    -size => 1,
			    -options => [ @month_list ]);

  my @days = map { [ $_, $_, 0] } 1..31;

  if ($blank) {
    foreach (@days) {
       $_->[2] = 0;
     }
    unshift @days, ['Day', '', 1];
  }
  else {
     foreach (@days) {
       $_->[2] = ($_->[1] == $date->day ? 1 : 0);
     }
  }

  $ret .= PXT::HTML->select(-name => "${prefix}day",
			    -size => 1,
			    -options => [@days]);

  my $cur_yr = $date->year;

  my @years;
  foreach my $modifier (@year_modifiers) {
    my $year = ($cur_yr + $modifier);
    push @years, [$year, $year, 0];
  }

  unshift @years, ['Year', '', 1] if $blank;

  $years[0]->[2] = 1;

  $ret .= PXT::HTML->select(-name => "${prefix}year",
			    -size => 1,
			    -options => [ @years ],
			   );

  $ret .= '&#160;';

  my @hours;

  foreach my $hour (1 .. 12) {
    my $date_hour = $date->hour % 12 || "12";
    push @hours, [ $hour, $hour, $hour == $date_hour ? 1 : 0 ];
  }

  unshift @hours, ['Hour', '', 1] if $blank;

  $ret .= PXT::HTML->select(-name => "${prefix}hour",
			    -size => 1,
			    -options => \@hours,
			   );

  $ret .= ':';

  my @minutes;

  foreach my $minute ("00" .. "59") {
    push @minutes, [ $minute, $minute, $minute == $date->minute ? 1 : 0 ];
  }

  unshift @minutes, ['Minute', '', 1] if $blank;

  $ret .= PXT::HTML->select(-name => "${prefix}minute",
			    -size => 1,
			    -options => \@minutes,
			   );

  my $pm = $date->hour > 11 ? 1 : 0;

  $ret .= PXT::HTML->select(-name => "${prefix}am_pm",
			    -size => 1,
			    -options => [ [ 'AM', 'AM', $pm ? 0 : 1 ],
					  [ 'PM', 'PM', $pm ? 1 : 0 ] ]);

  $ret .= " " . $pxt->user->get_tz_str;

  return $ret;
}

# helper function for determining the scheduled date from rhn-date-pickbox
sub parse_date_pickbox {
  my $class = shift;
  my $pxt = shift;
  #my $prefix = shift || '';
  my %params = @_;

  my $prefix = defined $params{prefix} ? $params{prefix} : '';
  my $long_date = defined $params{long_date} ? $params{long_date} : 1;

  my @vars = (qw/month day year hour minute am_pm/);

  if (grep {not $pxt->dirty_param("${prefix}$_")} @vars) {
    return undef;
  }

  my $hour = $pxt->dirty_param("${prefix}hour");
  my $am_pm = $pxt->dirty_param("${prefix}am_pm");

  if ($am_pm eq 'AM') {
    $hour = 0 if $hour == 12;
  }
  elsif ($am_pm eq 'PM') {
    $hour += 12;
    $hour = 12 if $hour == 24;
  }
  else {
    throw "No ${prefix}am_pm parameter in call to parse_date_pickbox";
  }

  my $scheduled_time = RHN::Date->construct(year => $pxt->dirty_param("${prefix}year"),
					    month => $pxt->dirty_param("${prefix}month"),
					    day => $pxt->dirty_param("${prefix}day"),
					    hour => $hour,
					    minute => $pxt->dirty_param("${prefix}minute"),
					    second => 0,
					    time_zone => $pxt->user->get_timezone);

  if ($long_date) {
    return $scheduled_time->local_long_date;
  }
  else {
    return $scheduled_time->epoch;
  }
}

sub server_set_errata_set_actions_cb {
  my $pxt = shift;
  my $system_set = RHN::Set->lookup(-label => 'system_list', -uid => $pxt->user->id);
  my $errata_set = RHN::Set->lookup(-label => 'errata_list', -uid => $pxt->user->id);

  my $earliest_date = Sniglets::ServerActions->parse_date_pickbox($pxt);

  my $action_id;

  PXT::Debug->log(7, "in server_set_errata_set_actions_cb...");

  if ($pxt->dirty_param('schedule_errata_updates')) {
    RHN::Scheduler->schedule_errata_updates_for_systems(-org_id => $pxt->user->org_id,
							-user_id => $pxt->user->id,
							-earliest => $earliest_date,
							-server_set => $system_set,
							-errata_set => $errata_set);

  } else {
    croak "No valid actions selected!";
  }

  $errata_set->empty;
  $errata_set->commit;

  $pxt->push_message(site_info => "Patch updates scheduled.");

  $pxt->redirect('/network/systems/ssm/errata/index.pxt');
}


sub reschedule_action_cb {
  my $pxt = shift;

  my $action_id = $pxt->param('aid');

  die "no action id!" unless $action_id;

  my $action = RHN::Action->lookup(-id => $action_id);
  my $action_name = $action->name;
  $action_name = $action->action_type_name;
  
  #my $earliest_date = Sniglets::ServerActions->parse_date_pickbox($pxt);
  my $server_id = $pxt->param('sid');
  RHN::Scheduler->reschedule_action(-action_id => $action_id, -org_id => $pxt->user->org_id,
				    -user_id => $pxt->user->id, -server_id => $server_id, -server_set => undef);


  if (!$server_id) {
    $pxt->redirect("/network/schedule/reschedule_success.pxt?aid=$action_id");
  }
  else {
    $pxt->push_message(site_info => sprintf('<strong>%s</strong> successfully rescheduled.', $action_name));
    my $redir = $pxt->dirty_param('success_redirect');

    throw "Param 'success_redirect' needed but not provided." unless $redir;

    $pxt->redirect($redir . "?sid=$server_id&amp;aid=$action_id");
  }
}

sub package_event_result {
  my $pxt = shift;
  my %attr = @_;

  my $sid = $pxt->param('sid');
  my $hid = $pxt->param('hid');
  my $id_combo = $pxt->dirty_param('id_combo');

  my $result = RHN::Server->event_package_results(-sid => $sid, -aid => $hid, -id_combo => $id_combo);

  my %subst;

  $subst{event_summary} = "$result->{ACTION_TYPE} scheduled by $result->{LOGIN}";
  $subst{result_package_nvre} = $result->{NVRE};
  $subst{result_return_code} = $result->{RESULT_CODE};
  $subst{result_stdout} = $result->{STDOUT};
  $subst{result_stderr} = $result->{STDERR};

  my $html = $attr{__block__} || '';

  return PXT::Utils->perform_substitutions($html, \%subst);
}

1;
