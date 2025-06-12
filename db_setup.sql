-- Garante que o banco de dados correto seja criado, caso ainda não exista.
CREATE DATABASE IF NOT EXISTS sistemapix_db;
-- Usa o banco de dados correto para todos os comandos seguintes.
USE sistemapix_db;

-- Apaga as tabelas antigas na ordem correta para evitar erros de chave estrangeira.
DROP TABLE IF EXISTS historico_operacoes;
DROP TABLE IF EXISTS chaves_pix;
DROP TABLE IF EXISTS contas;
DROP TABLE IF EXISTS clientes;

-- Tabela de Clientes
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    telefone VARCHAR(20) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL, 
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Contas
CREATE TABLE contas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    numero_conta VARCHAR(20) NOT NULL UNIQUE,
    saldo DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Tabela de Chaves Pix
CREATE TABLE chaves_pix (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_conta INT NOT NULL,
    tipo_chave ENUM('CPF', 'EMAIL', 'TELEFONE', 'ALEATORIA') NOT NULL,
    valor_chave VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (id_conta) REFERENCES contas(id) ON DELETE CASCADE
);

-- Tabela de Histórico de Operações
CREATE TABLE historico_operacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_conta_origem INT NOT NULL,
    id_conta_destino INT, 
    tipo_operacao VARCHAR(50) NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    data_operacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insere um utilizador e, CRUCIALMENTE, uma conta para ele.
INSERT INTO clientes (nome, cpf, telefone, email, senha) 
VALUES ('Ana Silva', '111.111.111-11', '(11) 98765-4321', 'ana.silva@email.com', '$2a$10$3gA5p.m3bF.Lp3gS4g.2y.g9E8w.Z2yU4U5i6g7H8i9j0K1l2m3n4');

-- A LINHA MAIS IMPORTANTE: Cria a conta para o cliente que acabámos de inserir.
INSERT INTO contas (id_cliente, numero_conta, saldo) VALUES (LAST_INSERT_ID(), '1001-1', 5000.00);

-- Opcional: Adiciona uma chave PIX de exemplo para teste.
INSERT INTO chaves_pix (id_conta, tipo_chave, valor_chave) VALUES ((SELECT id FROM contas WHERE id_cliente = 1), 'EMAIL', 'ana.silva@email.com');
