UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/baseos'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 (%)' LIMIT 1) AND
  NOT EXISTS (SELECT id FROM rhnContentSource WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/baseos');

UPDATE rhnChannelContentSource SET source_id=(SELECT id FROM rhnContentSource
  WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/baseos')
  WHERE source_id IN (SELECT id FROM rhnContentSource WHERE rhnContentSource.label LIKE 'External - AlmaLinux 8 (%)');

DELETE FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 (%)' AND NOT source_url='https://mirrors.almalinux.org/mirrorlist/8/baseos';

UPDATE rhnContentSource SET label = 'External - AlmaLinux 8'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 (%)');


UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/appstream'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 AppStream (%)' LIMIT 1) AND
  NOT EXISTS (SELECT id FROM rhnContentSource WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/appstream');

UPDATE rhnChannelContentSource SET source_id=(SELECT id FROM rhnContentSource
  WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/appstream')
  WHERE source_id IN (SELECT id FROM rhnContentSource WHERE rhnContentSource.label LIKE 'External - AlmaLinux 8 AppStream (%)');

DELETE FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 AppStream (%)' AND NOT source_url='https://mirrors.almalinux.org/mirrorlist/8/appstream';

UPDATE rhnContentSource SET label = 'External - AlmaLinux 8 AppStream'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 AppStream (%)');


UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/extras'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 Extras (%)' LIMIT 1) AND
  NOT EXISTS (SELECT id FROM rhnContentSource WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/extras');

UPDATE rhnChannelContentSource SET source_id=(SELECT id FROM rhnContentSource
  WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/extras')
  WHERE source_id IN (SELECT id FROM rhnContentSource WHERE rhnContentSource.label LIKE 'External - AlmaLinux 8 Extras (%)');

DELETE FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 Extras (%)' AND NOT source_url='https://mirrors.almalinux.org/mirrorlist/8/extras';

UPDATE rhnContentSource SET label = 'External - AlmaLinux 8 Extras'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 Extras (%)');


UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/ha'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 High Availability (%)' LIMIT 1) AND
  NOT EXISTS (SELECT id FROM rhnContentSource WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/ha');

UPDATE rhnChannelContentSource SET source_id=(SELECT id FROM rhnContentSource
  WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/ha')
  WHERE source_id IN (SELECT id FROM rhnContentSource WHERE rhnContentSource.label LIKE 'External - AlmaLinux 8 High Availability (%)');

DELETE FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 High Availability (%)' AND NOT source_url='https://mirrors.almalinux.org/mirrorlist/8/ha';

UPDATE rhnContentSource SET label = 'External - AlmaLinux 8 High Availability'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 High Availability (%)');


UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/powertools'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 PowerTools (%)' LIMIT 1) AND
  NOT EXISTS (SELECT id FROM rhnContentSource WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/powertools');

UPDATE rhnChannelContentSource SET source_id=(SELECT id FROM rhnContentSource
  WHERE source_url='https://mirrors.almalinux.org/mirrorlist/8/powertools')
  WHERE source_id IN (SELECT id FROM rhnContentSource WHERE rhnContentSource.label LIKE 'External - AlmaLinux 8 PowerTools (%)');

DELETE FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 PowerTools (%)' AND NOT source_url='https://mirrors.almalinux.org/mirrorlist/8/powertools';

UPDATE rhnContentSource SET label = 'External - AlmaLinux 8 PowerTools'
  WHERE id = (SELECT id FROM rhnContentSource WHERE label LIKE 'External - AlmaLinux 8 PowerTools (%)');

