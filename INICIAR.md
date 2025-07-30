# 🚀 Como Iniciar o MVP da API CND

## Passos Rápidos

### 1. Subir o Banco de Dados
```bash
docker-compose up -d
```

### 2. Executar a Aplicação
```bash
mvn spring-boot:run
```

### 3. Testar a API
```bash
./test-api.sh
```

## 🎯 URLs Importantes

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Base**: http://localhost:8080/api/cnd
- **Banco**: localhost:5432 (cnd_mvp/cnd_user/cnd_pass)

## 📋 Teste Manual Rápido

### 1. Emitir CND
```bash
curl -X POST "http://localhost:8080/api/cnd/emitir/1" \
  -H "Content-Type: application/json" \
  -d '{"comAssinatura": false, "comPeriodo": true, "canalEmissao": "WEB"}'
```

### 2. Validar (use o código retornado)
```bash
curl "http://localhost:8080/api/cnd/validar/CND240315001"
```

### 3. Download PDF
```bash
curl "http://localhost:8080/api/cnd/download/CND240315001" -o cnd.pdf
```

## 🔧 Troubleshooting

### Erro de Porta
Se a porta 8080 estiver ocupada, altere em `application.properties`:
```properties
server.port=8081
```

### Erro de Banco
Verifique se o Docker está rodando:
```bash
docker ps
```

### Erro de Certificado
O certificado está em `src/main/resources/llz-test.p12` com senha `123456`.

## 📊 Dados de Teste

- **Unidades**: IDs 1-8 disponíveis
- **Todas adimplentes** (para permitir emissão de CND)
- **Dados simulados** de condomínios e endereços

## 🎨 Funcionalidades Demonstradas

✅ **PDF Moderno**: Layout profissional com logo  
✅ **Assinatura Digital**: Certificado A1 autoassinado  
✅ **QR Code**: Para validação online  
✅ **Hash SHA-256**: Integridade do documento  
✅ **Anti-Fraude**: Controle de tentativas  
✅ **Validação**: Independente da plataforma  

## 📱 Importar no Postman

1. Importe o arquivo `postman-collection.json`
2. Execute a collection em sequência
3. Os códigos são automaticamente capturados

---

**MVP pronto para demonstração! 🎉**