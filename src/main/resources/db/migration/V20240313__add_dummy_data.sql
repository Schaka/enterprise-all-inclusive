INSERT INTO customers VALUES(1, 'bank');
INSERT INTO customers VALUES(2, 'paypal');
INSERT INTO customers VALUES(3, 'wise');
INSERT INTO customers VALUES(4, 'revolut');
INSERT INTO customers VALUES(5, 'cashapp');

INSERT INTO accounts VALUES(1, 'savings', 1, 1000.0);
INSERT INTO accounts VALUES(2, 'salary', 1, 100.0);
INSERT INTO accounts VALUES(3, 'games', 1, 50.0);
INSERT INTO accounts VALUES(4, 'gambling', 1, 812.0);

INSERT INTO transactions VALUES(1, 1, 1000.0, '2023-06-23', '2023-06-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(2, 2, 100.0, '2023-06-24', '2023-06-24', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(3, 3, 25.0, '2023-06-29', '2023-06-29', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(4, 3, 25.0, '2023-06-29', '2023-06-29', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(5, 4, 1000.0, '2023-05-29', '2023-05-29', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(6, 4, 188.0, '2023-06-01', '2023-06-01', 'ACCEPTED', 'OUT');

INSERT INTO accounts VALUES(5, 'savings', 2, 500.0);
INSERT INTO accounts VALUES(6, 'salary', 2, 0.0);

INSERT INTO transactions VALUES(7, 5, 500.0, '2023-06-23', '2023-06-23', 'ACCEPTED', 'IN');

INSERT INTO accounts VALUES(7, 'savings', 3, 100.0);
INSERT INTO accounts VALUES(8, 'travel', 3, 300.0);
INSERT INTO accounts VALUES(9, 'eu', 3, 500.0);
INSERT INTO accounts VALUES(10, 'nok', 3, 1000.0);

INSERT INTO transactions VALUES(8, 7, 100.0, '2023-06-23', '2023-06-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(9, 8, 300.0, '2023-06-24', '2023-06-24', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(10, 9, 500.0, '2023-06-29', '2023-06-29', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(11, 10, 600.0, '2023-06-29', '2023-06-29', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(12, 10, 400.0, '2023-05-29', '2023-05-29', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(13, 10, 100.0, '2023-06-01', '2023-06-01', 'ACCEPTED', 'OUT');

INSERT INTO accounts VALUES(11, 'bank', 4, 100.0);
INSERT INTO accounts VALUES(12, 'gambling', 4, 1.5);

INSERT INTO transactions VALUES(14, 11, 100.0, '2023-06-23', '2023-06-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(15, 12, 100.0, '2023-06-23', '2023-06-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(16, 12, 98.5, '2023-06-23', '2023-06-23', 'ACCEPTED', 'OUT');

INSERT INTO accounts VALUES(13, 'savings', 3, 100000.0);
INSERT INTO accounts VALUES(14, 'travel', 3, 3.0);
INSERT INTO accounts VALUES(15, 'eu', 3, 0.0);

INSERT INTO transactions VALUES(17, 13, 100000.0, '2023-01-23', '2023-01-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(18, 14, 100.0, '2023-06-23', '2023-06-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(19, 14, 103.0, '2023-02-23', '2023-02-23', 'ACCEPTED', 'IN');
INSERT INTO transactions VALUES(20, 14, 200.0, '2023-02-23', '2023-02-23', 'ACCEPTED', 'OUT');
