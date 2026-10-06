INSERT INTO suseInternalState (id, name, label)
  SELECT 15, 'formulas', 'Formulas'
   WHERE NOT EXISTS (
        SELECT 1 FROM suseInternalState
         WHERE id = 15
   );
