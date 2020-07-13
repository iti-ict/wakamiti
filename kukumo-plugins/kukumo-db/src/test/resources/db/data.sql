
DELETE FROM Book;
INSERT INTO Book(id, title, creation_date) VALUES (1, 'Test 1', '2020-01-01 01:23:45');
INSERT INTO Book(id, title) VALUES (2, 'Test 2');

COMMIT;