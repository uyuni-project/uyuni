UPDATE rhnChannel
SET label = 'oes24.4-sle-manager-tools15-debuginfo-updates-x86_64',
name = 'OES24.4-SLE-Manager-Tools15-Debuginfo-Updates for x86_64',
summary = 'OES24.4-SLE-Manager-Tools15-Debuginfo-Updates for x86_64'
WHERE label = 'oes22.4-sle-manager-tools15-debuginfo-updates-x86_64';

UPDATE suseChannelTemplate
SET channel_label = 'oes24.4-sle-manager-tools15-debuginfo-updates-x86_64',
channel_name = 'OES24.4-SLE-Manager-Tools15-Debuginfo-Updates for x86_64'
WHERE channel_label = 'oes22.4-sle-manager-tools15-debuginfo-updates-x86_64';

