--
-- Copyright (c) 2008--2013 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
--
--
--

insert into rhnSnapshotInvalidReason (id, label, name)
	values (sequence_nextval('rhn_ssinvalid_id_seq'), 'channel_removed',
		'A channel this snapshot was associated with no longer exists');

insert into rhnSnapshotInvalidReason (id, label, name)
	values (sequence_nextval('rhn_ssinvalid_id_seq'), 'channel_modified',
		'A channel this snapshot is associated with has been modified');

insert into rhnSnapshotInvalidReason (id, label, name)
	values (sequence_nextval('rhn_ssinvalid_id_seq'), 'sg_removed',
		'A server group this snapshot was associated with no longer exists');

insert into rhnSnapshotInvalidReason (id, label, name)
	values (sequence_nextval('rhn_ssinvalid_id_seq'), 'ns_removed',
		'A namespace this snapshot was associated with no longer exists');

insert into rhnSnapshotInvalidReason (id, label, name)
	values (sequence_nextval('rhn_ssinvalid_id_seq'), 'cr_removed',
		'A config revision this snapshot was associated with no longer exists');

insert into rhnSnapshotInvalidReason (id, label, name)
	values (sequence_nextval('rhn_ssinvalid_id_seq'), 'cc_removed',
		'A config channel this snapshot was associated with no longer exists');

commit;

