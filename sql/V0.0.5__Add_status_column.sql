DELIMITER $$

CREATE PROCEDURE add_status_column_if_not_exists()
BEGIN
  IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'members'
      AND COLUMN_NAME = 'status'
  ) THEN
    ALTER TABLE members ADD COLUMN status ENUM('active', 'pending') NOT NULL;
  END IF;
END$$

DELIMITER ;

CALL add_status_column_if_not_exists();
DROP PROCEDURE add_status_column_if_not_exists;