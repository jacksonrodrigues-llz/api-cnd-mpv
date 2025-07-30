-- Inserir endereços
INSERT INTO endereco (id, logradouro, numero, bairro, cidade, uf, cep) VALUES
(1, 'Rua das Flores', '123', 'Centro', 'Belo Horizonte', 'MG', '30112-000'),
(2, 'Av. Brasil', '456', 'Centro', 'Belo Horizonte', 'MG', '30140-000'),
(3, 'Rua da Paz', '789', 'Centro', 'Belo Horizonte', 'MG', '30160-000')
ON CONFLICT (id) DO NOTHING;

-- Inserir condomínios
INSERT INTO condominio (id, razao_social, endereco_id, reg_ativo) VALUES
(1, 'Condomínio Residencial Jardim das Flores', 1, true),
(2, 'Edifício Brasil Tower', 2, true),
(3, 'Condomínio Paz e Amor', 3, true)
ON CONFLICT (id) DO NOTHING;

-- Inserir unidades
INSERT INTO unidade (id, codigo, bloco, condominio_id, reg_ativo) VALUES
(1, '101', 'A', 1, true),
(2, '102', 'A', 1, true),
(3, '201', 'B', 1, true),
(4, '301', 'A', 2, true),
(5, '401', 'B', 2, true),
(6, '101', 'C', 3, true),
(7, '102', 'C', 3, true),
(8, '201', 'D', 3, true)
ON CONFLICT (id) DO NOTHING;