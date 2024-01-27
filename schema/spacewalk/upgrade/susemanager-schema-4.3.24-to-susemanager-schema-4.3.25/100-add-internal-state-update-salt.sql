INSERT INTO suseInternalState (id, name, label)
  SELECT 12, 'update-salt', 'Update Salt'
   WHERE NOT EXISTS (
	SELECT 1 FROM suseInternalState
         WHERE id = 12
   );
