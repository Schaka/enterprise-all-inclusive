CREATE TABLE customers(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255)
);

CREATE TABLE accounts(
     id             BIGSERIAL       PRIMARY KEY,
     name           VARCHAR(255),
     customer_id    BIGINT          REFERENCES customers(id),
     balance        NUMERIC(12, 4)
);

CREATE TABLE transactions(
     id             BIGSERIAL       PRIMARY KEY,
     account_id     BIGINT          REFERENCES accounts(id),
     amount         NUMERIC(12, 4),
     created        TIMESTAMP,
     modified       TIMESTAMP,
     status         VARCHAR(10),
     type           VARCHAR(10)
);

