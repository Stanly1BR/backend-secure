CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    operadora_id INTEGER REFERENCES operadoras(id),
    perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('ADMIN', 'OPERADORA', 'ANALISTA')),
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
CREATE TABLE IF NOT EXISTS logs_operacao (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuarios(id),
    operacao VARCHAR(50) NOT NULL CHECK (operacao IN ('INSERT', 'UPDATE', 'DELETE')),
    tabela VARCHAR(50) NOT NULL,
    registro_id INTEGER,
    dados_anteriores JSONB,
    dados_novos JSONB,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE operadora IS 'Cadastro de operadoras de planos de saúde';
COMMENT ON COLUMN operadora.cnpj IS 'CNPJ da operadora (somente números)';