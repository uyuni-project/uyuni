
SELECT logging.recreate_trigger('rhnserver');
SELECT logging.recreate_trigger('rhnservergroup');
SELECT logging.recreate_trigger('web_contact');

TRUNCATE log CASCADE;
