INSERT INTO rhnCompsType
  SELECT 3, 'mediaproducts'
   WHERE NOT EXISTS ( SELECT 1 FROM rhnCompsType WHERE label = 'mediaproducts');

