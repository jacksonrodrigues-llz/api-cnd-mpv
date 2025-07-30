#!/bin/bash

# Script de teste da API CND MVP

BASE_URL="http://localhost:8080/api/cnd"

echo "=== API CND MVP - Script de Teste ==="
echo ""

# Função para testar emissão de CND
test_emitir_cnd() {
    echo "1. Testando emissão de CND para unidade ID 1..."
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/emitir/1" \
        -H "Content-Type: application/json" \
        -d '{
            "comAssinatura": false,
            "comPeriodo": true,
            "canalEmissao": "WEB"
        }')
    
    echo "Resposta: $RESPONSE"
    
    # Extrair código de validação
    CODIGO=$(echo $RESPONSE | grep -o '"codigoValidacao":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$CODIGO" ]; then
        echo "✅ CND emitida com sucesso! Código: $CODIGO"
        echo ""
        
        # Aguardar processamento
        echo "2. Aguardando processamento da assinatura (5 segundos)..."
        sleep 5
        echo ""
        
        # Testar validação
        echo "3. Testando validação da CND..."
        curl -s "$BASE_URL/validar/$CODIGO" | jq '.' || echo "Resposta: $(curl -s "$BASE_URL/validar/$CODIGO")"
        echo ""
        
        # Testar download
        echo "4. Testando download da CND..."
        curl -s "$BASE_URL/download/$CODIGO" -o "cnd-$CODIGO.pdf"
        
        if [ -f "cnd-$CODIGO.pdf" ]; then
            echo "✅ PDF baixado com sucesso: cnd-$CODIGO.pdf"
            
            # Calcular hash
            HASH=$(sha256sum "cnd-$CODIGO.pdf" | cut -d' ' -f1)
            echo "Hash do arquivo: $HASH"
            echo ""
            
            # Testar validação de hash
            echo "5. Testando validação de hash..."
            HASH_VALID=$(curl -s -X POST "$BASE_URL/validar-hash/$CODIGO" \
                -H "Content-Type: application/json" \
                -d "\"$HASH\"")
            
            if [ "$HASH_VALID" = "true" ]; then
                echo "✅ Hash validado com sucesso!"
            else
                echo "❌ Falha na validação do hash"
            fi
        else
            echo "❌ Falha no download do PDF"
        fi
        
    else
        echo "❌ Falha na emissão da CND"
    fi
}

# Função para testar anti-fraude
test_anti_fraude() {
    echo ""
    echo "6. Testando controle anti-fraude (múltiplas tentativas)..."
    
    for i in {1..6}; do
        echo "Tentativa $i..."
        RESPONSE=$(curl -s -X POST "$BASE_URL/emitir/1" \
            -H "Content-Type: application/json" \
            -d '{
                "comAssinatura": false,
                "comPeriodo": true,
                "canalEmissao": "WEB"
            }')
        
        if echo "$RESPONSE" | grep -q "Muitas tentativas"; then
            echo "✅ Controle anti-fraude funcionando! Bloqueio ativado na tentativa $i"
            break
        fi
        
        sleep 1
    done
}

# Verificar se a API está rodando
echo "Verificando se a API está rodando..."
if curl -s "$BASE_URL/../swagger-ui.html" > /dev/null; then
    echo "✅ API está rodando!"
    echo ""
    
    # Executar testes
    test_emitir_cnd
    test_anti_fraude
    
    echo ""
    echo "=== Testes Concluídos ==="
    echo "Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "Arquivos PDF gerados neste diretório."
    
else
    echo "❌ API não está rodando!"
    echo "Execute: mvn spring-boot:run"
fi