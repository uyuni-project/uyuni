-- oracle equivalent source sha1 1ee8e8a21916cc86a7665502d714c9e377de2952
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

-- Create new column for rhnServer
ALTER TABLE rhnServer ADD contact_method_id NUMERIC DEFAULT (0) NOT NULL
    CONSTRAINT rhn_server_cmid_fk
        REFERENCES suseServerContactMethod (id);

-- Create new column for rhnRegToken
ALTER TABLE rhnRegToken ADD contact_method_id NUMERIC DEFAULT (0) NOT NULL
    CONSTRAINT rhn_reg_token_cmid_fk
        REFERENCES suseServerContactMethod (id);

