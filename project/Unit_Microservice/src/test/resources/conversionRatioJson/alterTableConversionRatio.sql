SET SQL_SAFE_UPDATES = 0;
SET  @num := 0;
UPDATE conversionratio SET entity_id = @num := (@num+1);
ALTER TABLE conversionratio AUTO_INCREMENT =1;
SET SQL_SAFE_UPDATES = 1;