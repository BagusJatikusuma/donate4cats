CREATE TABLE IF NOT EXISTS bank_accounts (
    id VARCHAR(50) PRIMARY KEY,
    member_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    bank VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(id)
);