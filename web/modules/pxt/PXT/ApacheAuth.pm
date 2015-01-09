#
# Copyright (c) 2008--2013 Red Hat, Inc.
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

package PXT::ApacheAuth;
use PXT::Config;

use Apache2::Const qw/:common OK REDIRECT M_GET AUTH_REQUIRED/;
use Apache2::Access ();
use RHN::Exception;
use PXT::ACL;

use PXT::ApacheHandler ();
use PXT::Utils ();
use RHN::User ();
use RHN::DB ();

sub handler {
  my $r = shift;

  if ($r->main) {
    $r->push_handlers(PerlAuthzHandler => \&authz_handler);
    return OK;
  }

  my $dbh = RHN::DB->connect();
  $dbh->call_procedure('logging.clear_log_id');

  my $status = PXT::ApacheHandler->initialize_pxt($r);
  return $status if $status;

  unless ($r->pnotes('pxt_request') and $r->pnotes('pxt_request')->use_sessions()) {
    warn "in ApacheAuth, but sessions are disabled.  declining.";
    return DECLINED;
  }

  my ($username, $user_id);

  if ($r->pnotes('pxt_session')) {
    my $session = $r->pnotes('pxt_session');

    if (defined $session->uid and $session->uid > 0) {
      eval {
        $username = RHN::User->find_username_fast($session->uid);
      };

      if ($@ and catchable($@)) {
        warn "User lookup failed: $@";
        return AUTH_REQUIRED;
      }
      elsif ($@) {
        die $@;
      }
    }
    $user_id = $session->uid;
    $dbh->call_procedure('logging.set_log_auth', $user_id);
  }

  if (not $username) {
    my $pxt = $r->pnotes('pxt_request');

    my $destination = $r->uri;

    if ($r->args) {
        $destination .= "?" . $r->args;
    }
    $destination = PXT::Utils->escapeURI($destination);

    $destination =~ s(\&)(%26)g;

    my $url = "/rhn/ReLogin.do?url_bounce=" . $destination;

    $url = $pxt->derelative_url($url);
    $url = $url->canonical;

    $r->content_type('text/html');
    $r->err_headers_out->{'Location'} = $url;
    $r->method("GET");
    $r->method_number(M_GET);
    $r->headers_in->unset('content-length');
    $r->status(REDIRECT);
    return REDIRECT;
  }

  $r->user($username);
  $r->pnotes('pxt_auth_username', $username);
  $r->pnotes('pxt_auth_user_id', $user_id);
  $r->push_handlers(PerlAuthzHandler => \&authz_handler);

  return OK;
}

sub authz_handler {
  my $r = shift;
  my $acl_require = shift;      # apache 2.4

  return OK if $r->main;

  my $username = $r->pnotes('pxt_auth_username');
  my $user_id = $r->pnotes('pxt_auth_user_id');
  return DECLINED unless $username;

  my $session = $r->pnotes('pxt_session');
  my $pxt = $r->pnotes('pxt_request');

  my @requires = ($acl_require ? ($acl_require)
                               : map {$_->{requirement} =~ s/^acl\s+//;
                                      $_->{requirement};} @{$r->requires});

  foreach my $entry (@requires) {

    # support addition mixin'able acls directly from the .htaccess file...
    my @mixins;
    while ($entry =~ m/mixin\s+(.*?)\s+/g) {
      push @mixins, $1;
    }

    # clean up the string for the acl parser...
    $entry =~ s{mixin\s+.*?\s+}{}g;

    my $acl_parser = new PXT::ACL(mixins => \@mixins);

    if (not $acl_parser->eval_acl($pxt, $entry)) {
      warn "acl fail: $entry";
      return FORBIDDEN;
    }
  }

#  $r->log_reason('User ' . $user->login . ' not allowed by "require"');

  $session->uid($user_id);
  return OK;
}

1;
