UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/baseos' WHERE label LIKE 'External - AlmaLinux 8 (%)';
UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/appstream' WHERE label LIKE 'External - AlmaLinux 8 AppStream (%)';
UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/extras' WHERE label LIKE 'External - AlmaLinux 8 Extras (%)';
UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/ha' WHERE label LIKE 'External - AlmaLinux 8 High Availability (%)';
UPDATE rhnContentSource SET source_url = 'https://mirrors.almalinux.org/mirrorlist/8/powertools' WHERE label LIKE 'External - AlmaLinux 8 PowerTools (%)';

