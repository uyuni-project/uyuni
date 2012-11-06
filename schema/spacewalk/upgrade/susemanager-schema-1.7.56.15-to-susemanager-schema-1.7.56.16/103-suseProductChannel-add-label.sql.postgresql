-- oracle equivalent source sha1 35ad842f538da2d4aa919d908388904370626368
--
-- Copyright (c) 2010-2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

drop index suse_prd_chan_uq;

alter table suseProductChannel alter column channel_id drop not null;

alter table suseProductChannel add column channel_label VARCHAR(128) NOT NULL DEFAULT '';
alter table suseProductChannel add column parent_channel_label VARCHAR(128);

update suseProductChannel
   set channel_label = c.label
  from rhnChannel c
 where suseProductChannel.channel_id = c.id;

update suseProductChannel
   set parent_channel_label = ( select c.label
                                  from rhnChannel c
                                 where c.id = (select c2.parent_channel
                                                 from rhnChannel c2
                                                where c2.id = suseProductChannel.channel_id));

CREATE UNIQUE INDEX suse_prd_chan_label_uq
ON suseProductChannel (product_id, channel_label);

alter table suseProductChannel alter column channel_label drop default;

CREATE INDEX suse_prd_chan_pcl_idx
ON suseProductChannel (parent_channel_label);
