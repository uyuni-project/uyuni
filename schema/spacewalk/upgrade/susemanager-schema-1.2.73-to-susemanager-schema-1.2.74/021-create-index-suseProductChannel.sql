--
-- Copyright (c) 2012 SUSE Linux Products GmbH, Nuremberg, Germany
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

declare
    cursor product_to_channel is
    select distinct  product_id, channel_id
    from suseProductCHannel;

begin

    for ps in product_to_channel loop

        delete from suseproductchannel where product_id = ps.product_id and channel_id = ps.channel_id;
        insert into suseproductchannel (product_id, channel_id) values (ps.product_id, ps.channel_id);

    end loop;

end;


CREATE UNIQUE INDEX suse_prd_chan_uq
    ON suseProductChannel (product_id, channel_id)
    TABLESPACE [[64k_tbs]];

CREATE INDEX suse_prd_chan_chan_idx
    ON suseProductChannel (channel_id)
    TABLESPACE [[64k_tbs]]
    NOLOGGING;

commit;
