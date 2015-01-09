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

package PXT::ApacheHandler;

use strict;
use Apache2::Request ();
use Apache2::Cookie ();
use Apache2::Const qw/:common REDIRECT M_GET/;
use constant HTTP_REQUEST_ENTITY_TOO_LARGE => 413;

use Apache2::URI ();
use Apache2::ServerUtil ();
use Apache2::Log ();
use Apache2::RequestIO ();

use BSD::Resource;
use Carp;
use Compress::Zlib;
use Sys::Hostname;
use Scalar::Util;
use File::Spec;

use PXT::Parser;
use PXT::Handlers;
use PXT::Utils;
use PXT::Config;
use PXT::Request;
use RHN::Session;
use RHN::Exception;
use RHN::I18N;
use RHN::Mail;

use PXT::Debug ();
use RHN::DB ();

use SUSEAuditlogClient;

our $make_vile;
use YAML::Syck;

use constant false => 0;
use constant true  => 1;


sub is_auditkeeper_running {
    open AKCONF, "<", "/etc/auditlog-keeper.conf" or return 0;
    while (my $line = <AKCONF>) {
	chomp($line);
	if ($line =~ /server\.pid\.filename/) {
	    $line =~ s/.*?\s+=\s+//g;
	    if (-s $line) {
		my $pid = do {
		    local $/ = undef;
		    open my $pidhandler, "<", $line or die $!;
		    <$pidhandler>;
		};
		chomp($pid);
		return (-d "/proc/$pid") ? 1 : 0;
	    }
	}
    }

    return 0;
}


sub get_log_event {
    my ($path, $request) = @_;
    my %event = ();

    if (!defined($request->pnotes('pxt_request'))) {
	return \%event;
    }

    if (!((-e $path) && (-r $path))) {
	return \%event;
    }
    
    my $yaml = YAML::Syck::LoadFile($path);
    my $urlopts = ${$yaml}{$request->pnotes('pxt_request')->uri()};
    my $reqparams = Apache2::Request->new($request, ())->param();
    my $found_required = defined(${$urlopts}{required}) ? false : true;

    if (!$found_required) {
	for my $el (@{${$urlopts}{required}}) {
	    for my $rparam (keys %$reqparams) {
		if ($rparam eq $el) {
		    $found_required = true;
		    last;
		}
	    }
	}
    }

    if (defined($urlopts) && $found_required && ${$urlopts}{type}) {
	$event{"EVT.TYPE"} = ${$urlopts}{type};
	for my $k (keys %$reqparams) {
	    my $v = $reqparams->{$k} . " ";
	    chomp($v);
	    $event{"REQ." . $k} = $v;
	}
    }

    return \%event;
}


sub handler {
  my $r = shift;

  # Log the request
  if (is_auditkeeper_running()) {
      my $extmap = get_log_event("/usr/share/spacewalk/audit/auditlog-config.yaml", $r);
      if (keys(%{$extmap}) > 0) {
	  SUSEAuditlogClient->new("localhost", 6888)->log($r->user(), $r->uri(), $r->connection->remote_ip, $extmap);
      }
  }

  local $make_vile = 0;
  $ENV{PATH} = "/bin:/usr/sbin";

  return DECLINED unless
    $r->content_type() eq 'text/pxt' or
      $r->content_type() eq 'text/html';

  return DECLINED unless
    grep { $r->method() eq $_ } qw/GET POST HEAD/;

  return OK if $r->main;

  my $status = PXT::ApacheHandler->initialize_pxt($r);
  return $status if $status;

  my $apr     = $r->pnotes('pxt_apr'); # I normally don't line equal signs up, but it looks sooo good this way
  my $session = $r->pnotes('pxt_session');
  my $cookies = $r->pnotes('pxt_cookies');
  my $request = $r->pnotes('pxt_request');

  my $filename = $r->filename;
  my ($file_contents, $file_classes);

  $request->content_type("text/html; charset=UTF-8");

  my $E;


  # see if the entire page was cached...
  $file_contents = $request->cached_copy() if PXT::Config->get('enable_caching');


  # don't allow caching in logged-in state...
  if ($file_contents and not $request->user) {

    PXT::Debug->log(7, "using cached copy of page contents...");

    $r->headers_out->add($request->cached_contents_header());
  }
  else {
    # not cached, parse the sucker
    ($file_contents, $file_classes) = load_pxt_file($r, $filename, $request->document_root);
    if (not defined $file_contents) {
      $r->log_reason("File not found");
      $request->trace_request(-result => NOT_FOUND);

      return NOT_FOUND;
    }

    Apache2::ServerUtil->server->push_handlers(PerlCleanupHandler => \&RHN::DB::connection_cleanup);

    my @then = (Time::HiRes::time, getrusage);
    eval { PXT::ApacheHandler->pxt_parse_data($request, \$file_contents); };
    $E = $@;
    my @now = (Time::HiRes::time, getrusage);
    $request->pnotes('page_render_time' => $now[0] - $then[0]);

    my $threshold = PXT::Config->get('page_timing_threshold');
    if ($threshold && ($now[0] - $then[0]) > $threshold) {
      warn sprintf "[timing] slow rendering of page '%s': %.4f seconds (%.4f user/%.4f sys)\n",
        $r->uri, ($now[0] - $then[0]), ($now[1] - $then[1]), ($now[2] - $then[2]);
    }
  }

  if ($E) {
    if (ref $E and ref $E eq 'PXT::Redirect') {
      return PXT::ApacheHandler->handle_redirect($r, $request, $E->{dest});
    }
    else {
      return handle_traceback($request, $filename, $r, $E);
    }
  }

  if ($request->use_sessions()) {
    $request->touch_session;

    if ($request->session_touched) {
      eval {
        $request->session->serialize;

        $r->headers_out->add('Set-Cookie' => $_->as_string)
          foreach $request->cookie_jar;
      };
      if ($@) {
        if (catchable($@)) {
          warn $@->as_string;
        }
        else {
          die $@;
        }
        return SERVER_ERROR;
      }
    }
  }

  if (not $request->manual_content) {
    $r->no_cache(1)
      if $request->pxt_no_cache;

    if ($request->cache_document_contents() and $request->is_cachable()) {
      $request->write_document_contents_to_cache($file_contents) if PXT::Config->get('enable_caching');
    }

    if (not $r->header_only) {
      if (PXT::Config->get("enable_i18n")) {
        $file_contents = RHN::I18N->translate($file_contents);
      }

      # Turn on byte pragma to get correct content length, and then turn it back off.
      use bytes;
      $r->headers_out->{'Content-Length'} = length $file_contents;
      no bytes;

      $r->print($file_contents);
    }

    $request->trace_request(-result => OK, -contents => \$file_contents);
  }
  else {
    $request->trace_request(-result => OK);
  }

  return OK;
}

my %file_cache;

sub load_pxt_file {
  my $r = shift;
  my $filename = shift;
  my $docroot = shift;

  my $use_cache = 0;
  my $file_age = 0;

  # disable cache for now
  if (0 and exists $file_cache{$filename}) {
    my $f = $file_cache{$filename};
    $file_age = (stat $filename)[8];
    if ($file_age <= $f->[1]) {
      return @{$file_cache{$filename}}[0, 2];
    }
    else {
      delete $file_cache{$filename};
    }
  }

  local * FH;
  unless ($use_cache) {
    open FH, '<', $filename or return;
  }

  local $/ = undef;
  my $data = <FH>;

  # All file IO is tainted.  We trust our docroot, though, so untaint.
  if (Scalar::Util::tainted($data)) {
    PXT::Utils->untaint(\$data);
  }

  $file_cache{$filename} = [ "", $file_age, [] ];
  close FH;

  $file_cache{$filename}->[0] = $data;

  return @{$file_cache{$filename}}[0, 2];
}

sub handle_redirect {
  my $class = shift;
  my $r = shift;
  my $request = shift;
  my $dest = shift;

  # RFC says all redirects MUST BE ABSOLUTE!  Most browsers, though,
  # don't care.  well, we do, dammit, so let's do the heavy lifting to
  # make w3c (and wget) happy

  my $url = $request->derelative_url($dest);

  if ($request->use_sessions()) {
    $request->touch_session;

    if ($request->session_touched) {
      $request->session->serialize;

      $r->err_headers_out->add('Set-Cookie' => $_->as_string)
        foreach $request->cookie_jar;
    }
  }

  # the odd concat below is in case $url->canonical is an
  # object... see perldoc URI for why it may not return a string.
  $request->trace_request(-result => REDIRECT, -extra => "" . $url->canonical);

  $r->content_type('text/html');
  $r->err_headers_out->{Location} = $url->canonical;
  $r->method("GET");
  $r->method_number(M_GET);
  $r->headers_in->unset('content-length');
  $r->status(REDIRECT);

  return REDIRECT;
}

sub initialize_pxt {
  my $class = shift;
  my $r = shift;

  return if $r->pnotes('pxt_initialized') or $r->main;
  clear_profile_timer();

  $SIG{__WARN__} = sub { my $str = shift; chomp $str; Apache2::ServerUtil->server->log_error($str) };

  my @hostname_personality;

  PXT::Config->load_configs(grep { -e $_ } map { "/etc/rhn/rhn-$_.conf" } @hostname_personality);

  $r->pnotes('pxt_initialized' => 1);

  my $uri = $r->uri;
  my $now = scalar localtime time;

  if ($r->method() eq 'POST' and $r->args()) {
    warn "[$now] POST $uri request to a url with args";
  }

  if ($r->method() eq 'POST' and not $r->headers_in->{'Content-type'}) {
    warn "[$now] POST $uri request with no Content-type header";
  }

  if ($r->method() eq 'POST' and not $r->headers_in->{'Content-Length'}) {
    warn "[$now] POST $uri request with no Content-Length header";
    $r->pnotes('pxt_suppress_traceback' => "no Content-Length header");
  }

  PXT::Config->reset_default('base_port');

  if ($r->headers_in->{'X-Server-Hostname'}) {
    PXT::Config->set(base_domain => $r->headers_in->{'X-Server-Hostname'});
  }
  elsif ($r->headers_in->{'Host'}) {
    PXT::Config->set(base_domain => $r->headers_in->{'Host'});
  }
  elsif (not PXT::Config->get('base_domain')) {
    PXT::Config->set(base_domain => $r->server->server_hostname);
  }

  # if, for some reason, base_domain looks like it has :port in it,
  # let's split into base_port.  this will ensure redirects really
  # truly do work (like, say, when tunneled over ssh)

  my $base_domain = PXT::Config->get('base_domain');
  if ($base_domain =~ /:(\d+)$/) {
    my $base_port = $1;
    $base_domain =~ s/:(\d+)$//;
    PXT::Config->set(base_domain => $base_domain);
    PXT::Config->set(base_port => $base_port);
  }

  if (PXT::Config->get('profile_queries')) {
    my $dbh = RHN::DB->connect;
    $dbh->enable_profile;
  }

  my $cookies = Apache2::Cookie->fetch;
  my $request = new PXT::Request $r, undef, $cookies;

  my $apr;
  my $xml_req;
  my $status;  # status from call to $apr->parse

  $apr = Apache2::Request->new($r, POST_MAX => PXT::Config->get("post_max"));

  $status = OK;
  eval {
      $apr->parse;
  };
  if ($@ and ref $@ eq 'APR::Request::Error') {
      APR::Request::Error::strerror($@);
      $status = SERVER_ERROR;
  }

  $r->pnotes('pxt_apr', $apr);
  $request->apr($apr);

  my $session_id;

  $r->pnotes('pxt_request', $request);
  $r->pnotes('pxt_cookies', $cookies);

  if (PXT::Config->get('use_sessions') and not $r->dir_config('no_sessions')) {
    $request->use_sessions(1);
  }
  else {
    $request->use_sessions(0);
  }

  return $status unless $request->use_sessions();

#  testing static sessions.  this crashes oracle (!!)
#  my $session = RHN::Session->load("517x9e1187d47b5b51678615a03a48c32c0a");

  my $session;
  eval {
    if ($session_id) {
      $session = RHN::Session->load($session_id);
    }
    elsif (my $pxt_session_cookie = $cookies->{$request->session_cookie_name}) {
      $session = RHN::Session->load($cookies->{$request->session_cookie_name}->value);
    }
    elsif ($request->dirty_param('pxt_session_id')) {
      $session = RHN::Session->load($request->dirty_param('pxt_session_id'));
    }
    else {
      # create session, use some data as chaff to "seed" the randomness
      if ($r->can('useragent_ip')) {
         $session = new RHN::Session $r->hostname, $r->useragent_ip;
      } else {
         $session = new RHN::Session $r->hostname, $r->connection->remote_ip;
      }
    }
  };
  if ($@ and catchable($@)) {
    warn $@->as_string;
  }
  elsif ($@) {
    warn "$@";
  }

  $request->session($session);

  $r->pnotes('pxt_session', $session);

  my @formvars = $apr->param;


  eval { $request->cleanse_params(); };
  my $E = $@;

  if ($E) {
      mail_traceback($request, $r, $E);
      throw $E;
  }

  return $status;
}

=head1 read_file

This subroutine is an internal function by <pxt-include> to open files

=cut

sub _read_file {
  my $class = shift;
  my $file = shift;
  my $root = shift;

  #my $root = $self->document_root();

  local * MYFILE;
  if (not open MYFILE, '<', "$root/$file") {
    Carp::cluck "Couldn't open file: $root/$file ($!)";
    return '';
  }

  my $f = join('', <MYFILE>);

  close MYFILE;

  # All file IO is tainted.  We trust our docroot, though, so untaint.
  if (Scalar::Util::tainted($f)) {
    PXT::Utils->untaint(\$f);
  }

  return $f;
}

=head1 clean

This subroutine will remove all <pxt-use /> tags from the html after the modules have been loaded..

=cut

sub clean {
  my $class = shift;
  my $data = shift;
  $data =~ s!<pxt-use +(.*?)>!!g;
  return $data;
}


sub pxt_parse_data {
  my $class = shift;
  my $pxt = shift;
  my $dataref = shift;

  my $p = new PXT::Parser;
  my @classes;

  # remove XML processing instructions for now
  $$dataref =~ s(<\?.*?\?>)()g;

  PXT::Handlers->register_primary_tags($p, $pxt, \@classes);
  $p->expand_tags($dataref, $pxt);

  my $p2 = new PXT::Parser;
  # register this one in p2 instead of p so it takes place in the same order as normal sniglets

  PXT::Handlers->register_secondary_tags($p2);
  foreach my $class (@classes) {
    my $module = "$class.pm";
    $module =~ s/::/\//g;
    require $module;

    $class->register_tags($p2) if $class->can("register_tags");
    $class->register_callbacks($p2) if $class->can("register_callbacks");
  }

  my %callbacks = $p2->callbacks;

  my $cb = $pxt->dirty_param("pxt:trap") || $pxt->dirty_param("pxt_trap");

  if ($cb and $callbacks{$cb}) {
    my $callback = $callbacks{$cb};

    if (ref $callback eq 'ARRAY') {
      $callback->[0]->($pxt, @{$callback}[1..$#$callback]);
    }
    else {
      $callbacks{$cb}->($pxt);
    }

    $pxt->param('pxt:trap', undef);
    $pxt->param('pxt_trap', undef);
  }

  $p2->expand_tags($dataref, $pxt);
}

sub handle_traceback {
  my ($pxt, $filename, $apache, $E) = @_;

  warn sprintf("Execution of %s failed at %s: $E", $filename, scalar localtime time);

  mail_traceback($pxt, $apache, $E);

  $pxt->trace_request(-result => SERVER_ERROR, -extra => $E);

  return SERVER_ERROR;
}

sub mail_traceback {
  my ($pxt, $apache, $E) = @_;

  if ($pxt->pnotes('pxt_suppress_traceback')) {
    warn "Traceback mail suppressed: " . $pxt->pnotes('pxt_suppress_traceback');
  }
  elsif (PXT::Config->get('traceback_mail')) {
    my $to = PXT::Config->get('traceback_mail');
    my $hostname = Sys::Hostname::hostname;
    my $subject = "WEB TRACEBACK from $hostname (" . scalar(localtime time) . ")";
    my $severity;
    $severity = "unhandled";

    my $raw_uri = $apache->the_request;
    my $uri = $apache->uri;
    my $user = '(not logged in)';
    my $headers = join("\n", map { "  $_: " . $pxt->header_in($_) } sort keys %{$pxt->headers_in});
    my $date = scalar localtime time;
    my $initial_request = "  " . ($apache->is_initial_req ? "Yes" : "No");
    my $error_notes = $apache->notes->get('error-notes') || '  (none)';

    if ($pxt->user) {
      $user = sprintf("  User %s (id %d, org_id %d)", $pxt->user->login, $pxt->user->id, $pxt->user->org->id);
    }

    my $formvars;
    my $pairs = join("\n", map { "  $_ => " . $pxt->passthrough_param($_) } sort $pxt->param);
    $formvars = <<EOS;
Form variables:
$pairs
EOS

    my $body = <<EOB;
The following exception occurred while executing this request:
 $raw_uri (from browser)
 $uri (from Apache)

Date:
  $date

Headers:
$headers

$formvars
User Information:
$user

Error notes:
$error_notes

Initial Request:
$initial_request

Error message:
  $E
EOB

    RHN::Mail->send(to => $to, subject => $subject, body => $body,
                    headers => {"X-RHN-Traceback-Severity" => $severity});
    warn "Traceback sent to $to";
  }

  return;
}

my $last_profile_pt;
sub clear_profile_timer {
  $last_profile_pt = 0;
}

1;

