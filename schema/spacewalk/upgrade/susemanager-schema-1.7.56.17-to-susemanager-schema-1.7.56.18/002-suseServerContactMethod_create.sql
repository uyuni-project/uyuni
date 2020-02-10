-- oracle equivalent source sha1 34259c7f3029e12f883d9e64d36999840bb6263c
--
-- Copyright (c) 2013 Novell, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseServerContactMethod
(
    id        NUMERIC NOT NULL
                  CONSTRAINT suse_cont_meth_id_pk PRIMARY KEY,
    label     VARCHAR(64) NOT NULL,
    name      VARCHAR(128) NOT NULL,
    rank      NUMERIC NOT NULL
);

CREATE INDEX suse_cont_meth_label_id_idx
    ON suseServerContactMethod (label, id);

ALTER TABLE suseServerContactMethod
    ADD CONSTRAINT suse_cont_meth_label_uq UNIQUE (label);

