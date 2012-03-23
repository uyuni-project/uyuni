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

create table
suseProductChannel
(
    product_id number        not null
                             CONSTRAINT spc_pid_fk
                             REFERENCES suseProducts (id),
    channel_id number        not null
                             CONSTRAINT spc_rhn_cid_fk
                             REFERENCES rhnChannel (id),
    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

CREATE UNIQUE INDEX suse_prd_chan_uq
    ON suseProductChannel (product_id, channel_id)
    TABLESPACE [[64k_tbs]];

CREATE INDEX suse_prd_chan_chan_idx
    ON suseProductChannel (channel_id)
    TABLESPACE [[64k_tbs]]
    NOLOGGING;

