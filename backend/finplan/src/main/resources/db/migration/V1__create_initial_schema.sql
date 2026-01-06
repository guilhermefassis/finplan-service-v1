CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    payment_frequency VARCHAR(20) NOT NULL,
    payment_details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE credit_cards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    brand VARCHAR(50),
    closing_day INTEGER NOT NULL,
    due_day INTEGER NOT NULL,
    credit_limit DECIMAL(15,2),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE credit_card_invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id) ON DELETE CASCADE,
    reference_month DATE NOT NULL,
    closing_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(15,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL,
    payment_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE credit_card_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id) ON DELETE CASCADE,
    invoice_id UUID REFERENCES credit_card_invoices(id) ON DELETE SET NULL,
    purchase_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    amount DECIMAL(15,2) NOT NULL,
    installments BOOLEAN DEFAULT FALSE,
    total_installments INTEGER,
    current_installment INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fixed_expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    amount DECIMAL(15,2) NOT NULL,
    due_day INTEGER NOT NULL,
    recurrence VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    institution VARCHAR(255),
    total_amount DECIMAL(15,2) NOT NULL,
    installment_amount DECIMAL(15,2) NOT NULL,
    total_installments INTEGER NOT NULL,
    paid_installments INTEGER DEFAULT 0,
    due_day INTEGER NOT NULL,
    start_date DATE,
    interest_rate DECIMAL(5,2),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE variable_expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE incomes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(15,2) NOT NULL,
    received_date DATE NOT NULL,
    recurring BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);