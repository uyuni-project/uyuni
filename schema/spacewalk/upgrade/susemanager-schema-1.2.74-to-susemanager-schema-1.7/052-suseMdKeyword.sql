--
-- Copyright (c) 2012 Novell
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
suseMdKeyword
(
    id                number PRIMARY KEY,
    label             varchar(128) NOT NULL,
    created           date default(sysdate) not null,
    modified          date default(sysdate) not null
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_mdkeyword_id_seq;

create or replace trigger
susemdkeyw_mod_trig
before insert or update on suseMdKeyword
for each row
begin
    :new.modified := sysdate;
end;
/
show errors

