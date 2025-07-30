# API CND MVP - Certidão Negativa de Débitos

MVP funcional da API de Certidão Negativa de Débitos com assinatura digital.

## 🚀 Funcionalidades

- ✅ Emissão de CND em PDF com layout moderno
- ✅ Assinatura digital com certificado A1 autoassinado
- ✅ Código de validação único
- ✅ QR Code para validação online
- ✅ Controle anti-fraude
- ✅ Validação de hash do documento
- ✅ Download de documentos assinados

## 🛠️ Tecnologias

- Java 17
- Spring Boot 3.1.6
- PostgreSQL
- iText 7 (geração PDF)
- BouncyCastle (certificado digital)
- ZXing (QR Code)

## 📋 Pré-requisitos

- Java 17
- Maven 3.6+
- Docker e Docker Compose

## 🚀 Como Executar

### 1. Subir o banco PostgreSQL
```bash
docker-compose up -d
```

### 2. Executar a aplicação
```bash
mvn spring-boot:run
```

### 3. Acessar o Swagger
```
http://localhost:8080/swagger-ui.html
```

## 📡 Endpoints Principais

### 1. Emitir CND
```http
POST /api/cnd/emitir/{unidadeId}
Content-Type: application/json

{
  "comAssinatura": false,
  "comPeriodo": true,
  "canalEmissao": "WEB"
}
```

**Exemplo com unidade ID 1:**
```bash
curl -X POST "http://localhost:8080/api/cnd/emitir/1" \
  -H "Content-Type: application/json" \
  -d '{
    "comAssinatura": false,
    "comPeriodo": true,
    "canalEmissao": "WEB"
  }'
```

### 2. Validar CND
```http
GET /api/cnd/validar/{codigo}
```

**Exemplo:**
```bash
curl "http://localhost:8080/api/cnd/validar/CND240315001"
```

### 3. Download CND
```http
GET /api/cnd/download/{codigo}
```

**Exemplo:**
```bash
curl "http://localhost:8080/api/cnd/download/CND240315001" -o cnd.pdf
```

### 4. Validar Hash
```http
POST /api/cnd/validar-hash/{codigo}
Content-Type: application/json

"hash_do_documento_aqui"
```

## 🎯 Fluxo de Teste Completo

### 1. Emitir uma CND
```bash
curl -X POST "http://localhost:8080/api/cnd/emitir/1" \
  -H "Content-Type: application/json" \
  -d '{
    "comAssinatura": false,
    "comPeriodo": true,
    "canalEmissao": "WEB"
  }'
```

**Resposta esperada:**
```json
{
  "codigoValidacao": "CND240315001",
  "status": "PROCESSANDO",
  "dataEmissao": "2024-03-15T10:30:00",
  "dataExpiracao": "2024-04-14T10:30:00",
  "urlValidacao": "http://localhost:8080/api/cnd/validar/CND240315001",
  "hashDocumento": "a1b2c3d4e5f6..."
}
```

### 2. Aguardar processamento (2-3 segundos)
A assinatura é processada de forma assíncrona.

### 3. Validar a CND
```bash
curl "http://localhost:8080/api/cnd/validar/CND240315001"
```

**Resposta esperada:**
```json
{
  "codigoValidacao": "CND240315001",
  "valido": true,
  "status": "ASSINADO",
  "dataEmissao": "2024-03-15T10:30:00",
  "dataAssinatura": "2024-03-15T10:30:03",
  "nomeCondominio": "Condomínio Residencial Jardim das Flores",
  "unidadeCodigo": "101",
  "bloco": "A",
  "hashDocumento": "a1b2c3d4e5f6...",
  "dadosAssinatura": {
    "algoritmo": "SHA256withRSA",
    "certificado": "CN=LLZ Garantidora, OU=TI, O=LLZ",
    "timestamp": "2024-03-15T10:30:03",
    "hash": "a1b2c3d4e5f6..."
  }
}
```

### 4. Fazer download do PDF
```bash
curl "http://localhost:8080/api/cnd/download/CND240315001" -o cnd-assinada.pdf
```

### 5. Validar hash do documento
```bash
# Calcular hash do arquivo baixado
sha256sum cnd-assinada.pdf

# Validar via API
curl -X POST "http://localhost:8080/api/cnd/validar-hash/CND240315001" \
  -H "Content-Type: application/json" \
  -d '"hash_calculado_aqui"'
```

## 📊 Dados de Teste

O sistema vem com dados pré-populados:

### Unidades Disponíveis:
- ID 1: Unidade 101, Bloco A - Condomínio Jardim das Flores
- ID 2: Unidade 102, Bloco A - Condomínio Jardim das Flores
- ID 3: Unidade 201, Bloco B - Condomínio Jardim das Flores
- ID 4: Unidade 301, Bloco A - Edifício Brasil Tower
- ID 5: Unidade 401, Bloco B - Edifício Brasil Tower
- ID 6: Unidade 101, Bloco C - Condomínio Paz e Amor
- ID 7: Unidade 102, Bloco C - Condomínio Paz e Amor
- ID 8: Unidade 201, Bloco D - Condomínio Paz e Amor

## 🔒 Segurança

### Controle Anti-Fraude
- Máximo 5 tentativas por hora por unidade
- Bloqueio temporário em caso de abuso
- Rastreamento por IP

### Certificado Digital
- Certificado A1 autoassinado para testes
- Algoritmo SHA256withRSA
- Validade de 1 ano

### Validação de Integridade
- Hash SHA-256 de todos os documentos
- Validação independente via API
- QR Code para verificação online

## 🎨 Layout do PDF

O PDF gerado inclui:
- Logo da empresa (LLZ Garantidora)
- Código de validação destacado
- Informações completas da unidade e condomínio
- Declaração de quitação
- Período verificado (se solicitado)
- Data de validade
- Dados da empresa
- QR Code para validação
- Hash do documento

## 🔧 Configurações

### Banco de Dados
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cnd_mvp
spring.datasource.username=cnd_user
spring.datasource.password=cnd_pass
```

### Certificado Digital
```properties
cnd.certificado.path=llz-test.p12
cnd.certificado.password=123456
cnd.certificado.alias=llz-test
```

### Empresa
```properties
company.name=LLZ Garantidora
company.address=Rua dos Guajajaras 1611, 6° andar
company.city=Belo Horizonte
company.state=MG
```

## 📝 Logs

A aplicação gera logs detalhados de:
- Emissão de CNDs
- Processamento de assinaturas
- Tentativas de fraude
- Erros de validação

## 🚨 Limitações do MVP

- Certificado autoassinado (apenas para testes)
- Dados simulados de inadimplência
- Assinatura simplificada (não é PDF/A-3)
- Sem integração com plataformas externas

## 📞 Suporte

Para dúvidas sobre este MVP, consulte a documentação técnica completa ou entre em contato com a equipe de desenvolvimento.

---

**Desenvolvido para demonstração da nova funcionalidade de CND com assinatura digital.**