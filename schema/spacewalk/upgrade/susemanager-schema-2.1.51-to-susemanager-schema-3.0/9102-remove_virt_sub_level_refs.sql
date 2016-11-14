DELETE FROM rhnException
WHERE label = 'invalid_virt_sub_level';

delete from rhnChannelFamilyVirtSubLevel;
delete from rhnSGTypeVirtSubLevel;
delete from rhnVirtSubLevel;

DROP TABLE rhnChannelFamilyVirtSubLevel;
DROP TABLE rhnSGTypeVirtSubLevel;
DROP TABLE rhnVirtSubLevel;
DROP SEQUENCE rhn_virt_sl_seq;
