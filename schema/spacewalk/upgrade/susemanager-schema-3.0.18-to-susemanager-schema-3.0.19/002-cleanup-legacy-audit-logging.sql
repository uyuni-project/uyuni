-- oracle equivalent source sha1 2d8902f8b5c90b29029a6b59cacf1f71c64d43a0

SELECT logging.recreate_trigger('rhnserver');
SELECT logging.recreate_trigger('rhnservergroup');
SELECT logging.recreate_trigger('web_contact');

TRUNCATE log CASCADE;
