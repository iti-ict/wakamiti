CREATE TRIGGER CLIENT_ID ON client INSTEAD OF INSERT AS
DECLARE @id int
BEGIN
    IF exists (SELECT 1 FROM inserted WHERE id IS NULL OR id = 0)
       BEGIN
            SELECT @id = MAX(id)+1 FROM client;
            IF @id IS NULL BEGIN set @id = 1;
            END;
       END
       ELSE
       BEGIN
        SELECT @id = id FROM inserted;
       END;
    INSERT INTO client (id, first_name, second_name, active, birth_date, creation)
    SELECT @id, first_name, second_name, active, birth_date, creation FROM Inserted;
END;

GO

CREATE TRIGGER CITY_ID ON city INSTEAD OF INSERT AS
DECLARE @id int
BEGIN
    IF exists (SELECT 1 FROM inserted WHERE id IS NULL OR id = 0)
    BEGIN
        SELECT @id = MAX(id)+1 FROM city;
        IF @id IS NULL BEGIN set @id = 1;
        END;
    END
    ELSE
    BEGIN
        SELECT @id = id FROM inserted;
    END;
    INSERT INTO city (id, name, latitude, longitude)
    SELECT @id, name, latitude, longitude FROM Inserted;
END;
