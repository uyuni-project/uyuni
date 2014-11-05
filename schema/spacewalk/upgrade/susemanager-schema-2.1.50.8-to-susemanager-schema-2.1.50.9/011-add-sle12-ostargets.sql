insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-12-ppc64le', 'sle-12-ppc64le', LOOKUP_CHANNEL_ARCH('channel-ppc64le'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-12-s390x', 'sle-12-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-12-x86_64', 'sle-12-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));
