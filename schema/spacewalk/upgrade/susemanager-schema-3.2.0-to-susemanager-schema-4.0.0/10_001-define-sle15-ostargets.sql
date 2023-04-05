insert into suseOSTarget (id, os, target, channel_arch_id)
  select sequence_nextval('suse_ostarget_id_seq'), 'sle-15-x86_64', 'sle-15-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64') from dual
   where not exists (select 1 from suseOSTarget where target = 'sle-15-x86_64');

insert into suseOSTarget (id, os, target, channel_arch_id)
  select sequence_nextval('suse_ostarget_id_seq'), 'sle-15-s390x', 'sle-15-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x') from dual
   where not exists (select 1 from suseOSTarget where target = 'sle-15-s390x');

insert into suseOSTarget (id, os, target, channel_arch_id)
  select sequence_nextval('suse_ostarget_id_seq'), 'sle-15-ppc64le', 'sle-15-ppc64le', LOOKUP_CHANNEL_ARCH('channel-ppc64le') from dual
   where not exists (select 1 from suseOSTarget where target = 'sle-15-ppc64le');

insert into suseOSTarget (id, os, target, channel_arch_id)
  select sequence_nextval('suse_ostarget_id_seq'), 'sle-15-aarch64', 'sle-15-aarch64', LOOKUP_CHANNEL_ARCH('channel-aarch64') from dual
   where not exists (select 1 from suseOSTarget where target = 'sle-15-aarch64');

insert into suseOSTarget (id, os, target, channel_arch_id)
  select sequence_nextval('suse_ostarget_id_seq'), 'sle-12-aarch64', 'sle-12-aarch64', LOOKUP_CHANNEL_ARCH('channel-aarch64') from dual
   where not exists (select 1 from suseOSTarget where target = 'sle-12-aarch64');
