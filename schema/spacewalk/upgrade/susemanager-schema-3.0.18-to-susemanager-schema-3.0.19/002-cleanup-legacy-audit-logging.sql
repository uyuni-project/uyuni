
SELECT .recreate_trigger('rhnserver');
SELECT .recreate_trigger('rhnservergroup');
SELECT .recreate_trigger('web_contact');

TRUNCATE log CASCADE;
