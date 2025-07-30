-- Criação das tabelas conforme documentação

-- Tabela de unidades (simulando estrutura do projeto original)
CREATE TABLE IF NOT EXISTS unidade (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    bloco VARCHAR(10),
    condominio_id BIGINT NOT NULL,
    situacao VARCHAR(20) DEFAULT 'ATIVO',
    reg_ativo BOOLEAN DEFAULT TRUE,
    dt_criacao TIMESTAMP DEFAULT NOW(),
    dt_alteracao TIMESTAMP DEFAULT NOW()
);

-- Tabela de condomínios
CREATE TABLE IF NOT EXISTS condominio (
    id BIGSERIAL PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    endereco_id BIGINT,
    reg_ativo BOOLEAN DEFAULT TRUE,
    dt_criacao TIMESTAMP DEFAULT NOW()
);

-- Tabela de endereços
CREATE TABLE IF NOT EXISTS endereco (
    id BIGSERIAL PRIMARY KEY,
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    cep VARCHAR(10),
    dt_criacao TIMESTAMP DEFAULT NOW()
);

-- Tabela principal da CND (conforme documentação)
CREATE TABLE IF NOT EXISTS unidade_cnd (
    id BIGSERIAL PRIMARY KEY,
    codigo_validacao VARCHAR(20) UNIQUE NOT NULL,
    unidade_id BIGINT NOT NULL,
    hash_parametros VARCHAR(64) NOT NULL,
    status VARCHAR(20) DEFAULT 'PROCESSANDO',
    canal_emissao VARCHAR(20) NOT NULL,
    documento_pdf BYTEA,
    documento_assinado BYTEA,
    codigo_plataforma VARCHAR(100),
    dados_assinatura JSONB,
    dt_criacao TIMESTAMP DEFAULT NOW(),
    dt_assinatura TIMESTAMP,
    dt_expiracao TIMESTAMP,
    tentativas_emissao INTEGER DEFAULT 1,
    ip_origem VARCHAR(45),
    reg_ativo BOOLEAN DEFAULT TRUE,
    dt_alteracao TIMESTAMP DEFAULT NOW(),
    usr_criacao BIGINT,
    usr_alteracao BIGINT,
    
    CONSTRAINT fk_unidade_cnd_unidade FOREIGN KEY (unidade_id) REFERENCES unidade(id),
    CONSTRAINT uk_unidade_hash UNIQUE (unidade_id, hash_parametros)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_unidade_cnd_codigo ON unidade_cnd(codigo_validacao);
CREATE INDEX IF NOT EXISTS idx_unidade_cnd_status ON unidade_cnd(status);
CREATE INDEX IF NOT EXISTS idx_unidade_cnd_data ON unidade_cnd(dt_criacao);

-- Dados de teste
INSERT INTO endereco (id, logradouro, numero, bairro, cidade, uf, cep) VALUES
(1, 'Rua das Flores', '123', 'Centro', 'Belo Horizonte', 'MG', '30112-000'),
(2, 'Avenida Brasil', '456', 'Savassi', 'Belo Horizonte', 'MG', '30140-000'),
(3, 'Rua da Paz', '789', 'Funcionários', 'Belo Horizonte', 'MG', '30130-000');

INSERT INTO condominio (id, razao_social, endereco_id) VALUES
(1, 'Condomínio Residencial Jardim das Flores', 1),
(2, 'Edifício Comercial Brasil Tower', 2),
(3, 'Condomínio Residencial Paz e Amor', 3);

INSERT INTO unidade (id, codigo, bloco, condominio_id) VALUES
(1, '101', 'A', 1),
(2, '102', 'A', 1),
(3, '201', 'B', 1),
(4, '301', 'A', 2),
(5, '401', 'B', 2),
(6, '101', 'C', 3),
(7, '102', 'C', 3),
(8, '201', 'D', 3);

-- Resetar sequences
SELECT setval('endereco_id_seq', 3, true);
SELECT setval('condominio_id_seq', 3, true);
SELECT setval('unidade_id_seq', 8, true);