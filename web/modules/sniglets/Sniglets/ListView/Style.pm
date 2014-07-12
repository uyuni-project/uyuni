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

package Sniglets::ListView::Style;
use strict;

sub new {
  my $class = shift;
  my $style_name = shift;
  my $set_label = shift;

  if ($style_name) {
    return bless { style_name => $style_name, set_label => $set_label }, "${class}::$style_name";
  }
  else {
    return bless { style_name => "standard", set_label => $set_label }, "${class}::standard";
  }
}

package Sniglets::ListView::Style::standard;

sub header_column {
  my $self = shift;

  my $ret = <<EOQ;
    <th>{column_name}</th>
EOQ

  return $ret;
}

sub header {
  my $self = shift;

  my $ret =<<EOQ;
<div class="spacewalk-list">
  <div class="spacewalk-list-top-addons">
    {alphabar}
    {pagination}
  </div>
  <div class="panel panel-default">
    <div class="panel-heading">
      <div class="spacewalk-list-head-addons">
        {control_area}
      </div>
    </div>
EOQ

  return $ret;
}

sub table_header {
  my $self = shift;

  my $ret =<<EOQ;
    <table class="table table-striped">
      <!-- Begin Header Row -->
      <thead>
        <tr>
          {header_row}
        </tr>
      </thead>
      <!-- End Header Row -->
EOQ

  return $ret;
}

sub select_column_header {
  my $self = shift;

  my $set_label = qq{'$self->{set_label}'} || '';

  my $ret = <<EOQ;
    <th class="list-checkbox-header" width="5%">
      <input type="checkbox" id="rhn_javascriptenabled_checkall_checkbox" style="display: none" name="checkall" title="Select or deselect all {list_of} on this page" onClick="check_all_on_page(this.form, $set_label)"{checkall_checked}{checkall_disabled}/>
      <noscript>Select</noscript>
    </th>
EOQ

  return $ret;
}

sub table_footer {
  my $self = shift;

  my $ret =<<EOQ;
    </table>
EOQ
  return $ret;
}

sub footer {
  my $self = shift;

  my $ret =<<EOQ;
    <div class="panel-footer">
      <div class="spacewalk-list-footer-addons">
      {control_area}
      </div>
    </div>
  </div>
  <div class="spacewalk-list-bottom-addons">
    {pagination}
  </div>

</div>
EOQ

  return $ret;
}

sub pagination {
  my $self = shift;

  my $ret =<<EOQ;
  <div class="spacewalk-list-pagination">
  <!-- Begin Pagination Buttons -->
  <span id="list_min">{current_lower}</span> - <span id="list_max">{current_upper}</span> of <span id="list_total">{total}</span>
  <div class="spacewalk-list-pagination-btns btn-group">
    {back_buttons_str} {forward_buttons_str}
  </div>
  {hidden_vars}
  </div>
  <!-- End Pagination Buttons -->
EOQ

  return $ret;
}

sub row {
  my $self = shift;

  my $ret =<<EOQ;
  <tr class="{row-class}">
{checkbox}
{columns}
  </tr>
EOQ

  return $ret;
}

sub row_class_odd {
  my $self = shift;

  return 'list-row-odd';
}

sub row_class_even {
  my $self = shift;

  return 'list-row-even';
}

sub column {
  my $self = shift;

  my $ret =<<EOQ;
    <td>
      {col_data}
    </td>
EOQ

  return $ret;
}

sub checkbox {
  my $self = shift;

  my $ret =<<EOQ;
    <td class="list-checkbox">
      {checkbox}
    </td>
EOQ

  return $ret;
}

sub empty_list_wrapper {
  my $self = shift;

  my $ret =<<EOQ;
    <div class="panel-body">
      <div class="list-empty-message">{empty_list_message}</div>
    </div>
EOQ

  return $ret;
}

sub alphabar {
  my $self = shift;

  my $ret =<<EOQ;
  <div class="spacewalk-list-alphabar">
    {alphabar_content}
  </div>
EOQ

  return $ret;
}

package Sniglets::ListView::Style::blank;

sub header_column { return '' }

sub header { return '' }

sub select_column_header { return '' }

sub footer { return '' }

sub pagination { return '' }

sub row { return '' }

sub row_class_even { return '' }

sub row_class_odd { return '' }

sub column { return '' }

sub checkbox { return '' }

sub empty_list_wrapper { return '' }

sub alphabar { return '' }


package Sniglets::ListView::Style::your_rhn_summary;

use base qw/Sniglets::ListView::Style::blank/;

sub column {
  my $self = shift;

  my $ret =<<EOQ;
    <td{width_str}{align_str}{nowrap_str}{class_str}>
      {col_data}
    </td>
EOQ

  return $ret;
}

sub row {
  my $self = shift;

  my $ret =<<EOQ;
  <tr class="{row-class}">
{checkbox}
{columns}
  </tr>
EOQ

  return $ret;
}

sub row_class_odd {
  my $self = shift;

  return 'list-row-odd';
}

sub row_class_even {
  my $self = shift;

  return 'list-row-even';
}

sub empty_list_wrapper {
  my $self = shift;

  my $ret =<<EOQ;
    <tr class="list-row-odd"><td class="only-column" align="center" colspan="4"><strong>{empty_list_message}</strong></td></tr>
EOQ

  return $ret;
}

package Sniglets::ListView::Style::channel_tree;

use base qw/Sniglets::ListView::Style::blank/;

sub header_column {
  my $self = shift;

  my $ret =<<EOQ;
    <th{attr_string}>{column_name}</th>
EOQ

  return $ret;
}

sub header {
  my $self = shift;

  my $ret =<<EOQ;
<table width="100%" cellspacing="0" cellpadding="0" class="list" align="center">
<!-- Begin Header Row -->
  <thead>
  <tr>
{header_row}
  </tr>
  </thead>
<!-- End Header Row -->

EOQ

  return $ret;
}

sub row {
  my $self = shift;

  my $ret =<<EOQ;
  <tr class="{row-class}">
{columns}
  </tr>
EOQ

  return $ret;
}

sub row_class_odd {
  my $self = shift;

  return 'table-tree-even';
}

sub row_class_even {
  my $self = shift;

  return 'table-tree-odd';
}

sub column {
  my $self = shift;

  my $ret =<<EOQ;
    <td{width_str}{align_str}{nowrap_str}{class_str}>
      {col_data}
    </td>
EOQ

  return $ret;
}

sub table_footer {
  my $self = shift;

  my $ret =<<EOQ;
    </table>
EOQ
  return $ret;
}

sub footer {
  my $self = shift;

  my $ret =<<EOQ;
  </div>
  <div class="panel-footer">
    <div class="spacewalk-list-footer-addons">
    {control_area}
    </div>
  </div>

  <div class="spacewalk-list-bottom-addons">
    {pagination}
  </div>

</div>
EOQ

  return $ret;
}

sub empty_list_wrapper {
  my $self = shift;

  my $ret =<<EOQ;
    <strong>{empty_list_message}</strong>
EOQ

  return $ret;
}

1;
