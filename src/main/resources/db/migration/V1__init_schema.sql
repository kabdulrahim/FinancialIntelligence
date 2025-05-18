-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Create companies table
CREATE TABLE IF NOT EXISTS companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(10) NOT NULL,
    tax_id VARCHAR(50),
    registration_number VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(50),
    website VARCHAR(255),
    industry VARCHAR(100),
    fiscal_year_end VARCHAR(20),
    currency_code VARCHAR(3) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    company_id BIGINT REFERENCES companies(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Create user_roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Create currencies table
CREATE TABLE IF NOT EXISTS currencies (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10),
    exchange_rate DECIMAL(19, 6) NOT NULL,
    is_base_currency BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_updated_rate_at TIMESTAMP
);

-- Create cash_accounts table
CREATE TABLE IF NOT EXISTS cash_accounts (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    account_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(50),
    account_type VARCHAR(50) NOT NULL,
    bank_name VARCHAR(255),
    balance DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    balance_base_currency DECIMAL(19, 4),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create accounts_receivable table
CREATE TABLE IF NOT EXISTS accounts_receivable (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    customer_name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    amount_base_currency DECIMAL(19, 4),
    invoice_number VARCHAR(50),
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    payment_terms VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create accounts_payable table
CREATE TABLE IF NOT EXISTS accounts_payable (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    vendor_name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    amount_base_currency DECIMAL(19, 4),
    invoice_number VARCHAR(50),
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    category VARCHAR(100),
    notes TEXT,
    payment_terms VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    item_name VARCHAR(255) NOT NULL,
    item_code VARCHAR(50),
    item_type VARCHAR(20),
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(19, 4) NOT NULL,
    total_value DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    acquisition_date DATE,
    location VARCHAR(255),
    description TEXT,
    reorder_level INTEGER,
    status VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create short_term_liabilities table
CREATE TABLE IF NOT EXISTS short_term_liabilities (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    description VARCHAR(255) NOT NULL,
    liability_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    amount_base_currency DECIMAL(19, 4),
    due_date DATE NOT NULL,
    interest_rate DECIMAL(5, 2),
    creditor VARCHAR(255),
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create invoices table
CREATE TABLE IF NOT EXISTS invoices (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    invoice_number VARCHAR(50) NOT NULL,
    invoice_type VARCHAR(20) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    tax_amount DECIMAL(19, 4),
    total_amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    total_amount_base_currency DECIMAL(19, 4),
    payment_terms VARCHAR(100),
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    invoice_id BIGINT REFERENCES invoices(id),
    payment_date DATE NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    amount_base_currency DECIMAL(19, 4),
    payment_method VARCHAR(50) NOT NULL,
    reference_number VARCHAR(50),
    notes TEXT,
    cash_account_id BIGINT REFERENCES cash_accounts(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 6),
    amount_base_currency DECIMAL(19, 4),
    reference_number VARCHAR(50),
    category VARCHAR(100),
    notes TEXT,
    cash_account_id BIGINT REFERENCES cash_accounts(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    trigger_metric VARCHAR(100),
    trigger_threshold VARCHAR(100),
    trigger_value VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    read_at TIMESTAMP,
    dismissed_at TIMESTAMP
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    company_id BIGINT REFERENCES companies(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    description TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'Administrator with all privileges'),
    ('ROLE_OWNER', 'Company owner with full access to company data'),
    ('ROLE_CFO', 'Chief Financial Officer with full access to financial data'),
    ('ROLE_ACCOUNTANT', 'Accountant with access to financial transactions'),
    ('ROLE_USER', 'Regular user with limited access')
ON CONFLICT (name) DO NOTHING;

-- Insert default currencies
INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, is_active, created_at, updated_at) VALUES
    ('USD', 'US Dollar', '$', 1.000000, TRUE, TRUE, NOW(), NOW()),
    ('EUR', 'Euro', '€', 1.100000, FALSE, TRUE, NOW(), NOW()),
    ('GBP', 'British Pound', '£', 1.300000, FALSE, TRUE, NOW(), NOW()),
    ('JPY', 'Japanese Yen', '¥', 0.009000, FALSE, TRUE, NOW(), NOW()),
    ('CAD', 'Canadian Dollar', 'C$', 0.750000, FALSE, TRUE, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Create admin user (password: admin123)
INSERT INTO companies (name, type, currency_code, is_active, created_at, updated_at) VALUES
    ('System Admin', 'SME', 'USD', TRUE, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password, first_name, last_name, is_active, company_id, created_at, updated_at) VALUES
    ('admin', 'admin@wcm.com', '$2a$10$TdKCfW3A7J9ZrfQbJ3BdEuXV5qQcD568k.mMRpcGHwKLcYeE.nYji', 'System', 'Admin', TRUE, 
        (SELECT id FROM companies WHERE name = 'System Admin'), NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
    ((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'))
ON CONFLICT (user_id, role_id) DO NOTHING;
