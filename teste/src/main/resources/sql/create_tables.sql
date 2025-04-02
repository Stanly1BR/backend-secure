CREATE TABLE IF NOT EXISTS operadoras (
    id SERIAL PRIMARY KEY,
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    data_registro DATE NOT NULL,
    modalidade VARCHAR(100) NOT NULL,
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf CHAR(2),
    cep VARCHAR(8),
    telefone VARCHAR(20),
    email VARCHAR(100),
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    despesa_saude DECIMAL(15,2) DEFAULT 0.00
    );

CREATE TABLE IF NOT EXISTS demonstracoes_contabeis (
    id SERIAL PRIMARY KEY,
    cnpj VARCHAR(14) NOT NULL,
    operadora_id INTEGER NOT NULL REFERENCES operadora(id) ON DELETE CASCADE,
    data DATE NOT NULL,
    ano_referencia INTEGER NOT NULL,
    despesa_hospitalar DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    despesa_ambulatorial DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    despesa_odontologica DECIMAL(15,2) DEFAULT 0.00,
    receita_operacional DECIMAL(15,2) DEFAULT 0.00,
    lucro_liquido DECIMAL(15,2) DEFAULT 0.00,
    data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_aprovacao TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDENTE',
    CONSTRAINT uk_demonstracao UNIQUE (operadora_id, data)
    );

-- Índices
CREATE INDEX IF NOT EXISTS idx_demonstracao_operadora_data ON demonstracoes_contabeis(operadora_id, data);
CREATE INDEX IF NOT EXISTS idx_demonstracao_ano_data ON demonstracoes_contabeis(ano_referencia, data);
CREATE INDEX IF NOT EXISTS idx_operadora_uf ON operadoras(uf);
CREATE INDEX IF NOT EXISTS idx_operadora_modalidade ON operadoras(modalidade);