--
-- Copyright (c) 2017 SUSE LLC
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
suseImageInfoInstalledProduct
(
    image_info_id         NUMERIC
                            CONSTRAINT suse_iiip_ii_id_fk
                            REFERENCES suseImageInfo (id)
                            ON DELETE CASCADE
                            not null,
    installed_product_id  NUMERIC
                            CONSTRAINT suse_iiip_ip_id_fk
                            REFERENCES suseInstalledProduct (id)
                            not null
);

CREATE UNIQUE INDEX suse_iiip_inprod_uq
    ON suseImageInfoInstalledProduct (image_info_id, installed_product_id)
    ;
