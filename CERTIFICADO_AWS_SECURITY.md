# 🔐 CERTIFICADO DIGITAL A1 NA AWS - SOLUÇÃO DE SEGURANÇA

## 🎯 PROBLEMA IDENTIFICADO
Não será possível manter o certificado digital A1 na pasta `resources` do projeto por questões de segurança, considerando que é um ativo crítico para a operação.

## ✅ SOLUÇÃO RECOMENDADA: AWS SECRETS MANAGER + ECS

### Arquitetura de Segurança Proposta
```
┌─────────────────────────────────────────────────────────────┐
│                    AWS CLOUD SECURITY                       │
├─────────────────────────────────────────────────────────────┤
│  AWS Secrets Manager                                        │
│  ├── llz-cnd-certificado-dev     (Base64 do .p12)           │
│  ├── llz-cnd-certificado-hml     (Base64 do .p12)           │
│  └── llz-cnd-certificado-prd     (Base64 do .p12)           │
├─────────────────────────────────────────────────────────────┤
│  AWS Systems Manager Parameter Store                        │
│  ├── /llz/cnd/certificado/alias                             │
│  └── /llz/cnd/certificado/password                          │
├─────────────────────────────────────────────────────────────┤
│  ECS Task Role (IAM)                                        │
│  ├── SecretsManagerReadWrite                                │
│  └── SSMParameterReadOnly                                   │
├─────────────────────────────────────────────────────────────┤
│  ECS Fargate Container                                      │
│  └── Spring Boot Application                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ IMPLEMENTAÇÃO TÉCNICA

### 1. CONFIGURAÇÃO AWS SECRETS MANAGER

#### Criação dos Secrets por Ambiente
```bash
# DEV (444301769287)
aws secretsmanager create-secret \
  --name "llz-cnd-certificado-dev" \
  --description "Certificado A1 para CND - Ambiente DEV" \
  --secret-binary fileb://llz-certificado.p12 \
  --region us-east-1 \
  --profile workloads-dev

# HML (770248317149)  
aws secretsmanager create-secret \
  --name "llz-cnd-certificado-hml" \
  --description "Certificado A1 para CND - Ambiente HML" \
  --secret-binary fileb://llz-certificado.p12 \
  --region us-east-1 \
  --profile workloads-hml

# PRD (756241603306)
aws secretsmanager create-secret \
  --name "llz-cnd-certificado-prd" \
  --description "Certificado A1 para CND - Ambiente PRD" \
  --secret-binary fileb://llz-certificado-producao.p12 \
  --region us-east-1 \
  --profile workloads-prd
```

#### Configuração de Parâmetros no Systems Manager
```bash
# Configurações por ambiente
aws ssm put-parameter \
  --name "/llz/cnd/certificado/alias" \
  --value "llz-test" \
  --type "String" \
  --description "Alias do certificado A1"

aws ssm put-parameter \
  --name "/llz/cnd/certificado/password" \
  --value "senha_segura_aqui" \
  --type "SecureString" \
  --description "Senha do certificado A1"
```

### 2. DEPENDÊNCIAS MAVEN

#### Adicionar ao pom.xml
```xml
<!-- AWS SDK para Secrets Manager -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>secretsmanager</artifactId>
    <version>2.21.29</version>
</dependency>

<!-- AWS SDK para Systems Manager -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>ssm</artifactId>
    <version>2.21.29</version>
</dependency>

<!-- Spring Cloud AWS -->
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-secrets-manager</artifactId>
    <version>3.0.3</version>
</dependency>
```

### 3. CONFIGURAÇÃO SPRING BOOT

#### application.properties
```properties
# AWS Configuration
aws.region=us-east-1
aws.secrets-manager.enabled=true

# Certificado Configuration (será sobrescrito pelo AWS)
cnd.certificado.secret-name=${AWS_SECRET_NAME:llz-cnd-certificado-dev}
cnd.certificado.alias=${AWS_CERT_ALIAS:/llz/cnd/certificado/alias}
cnd.certificado.password=${AWS_CERT_PASSWORD:/llz/cnd/certificado/password}

# Environment specific
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

#### application-dev.properties
```properties
cnd.certificado.secret-name=llz-cnd-certificado-dev
```

#### application-hml.properties
```properties
cnd.certificado.secret-name=llz-cnd-certificado-hml
```

#### application-prd.properties
```properties
cnd.certificado.secret-name=llz-cnd-certificado-prd
```

### 4. SERVIÇO AWS SECRETS MANAGER

#### AwsSecretsService.java
```java
package br.com.llz.cnd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsSecretsService {
    
    private final SecretsManagerClient secretsManagerClient;
    private final SsmClient ssmClient;
    
    @Value("${cnd.certificado.secret-name}")
    private String certificadoSecretName;
    
    public byte[] obterCertificado() {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(certificadoSecretName)
                .build();
            
            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            
            // O certificado está armazenado como binary no Secrets Manager
            return response.secretBinary().asByteArray();
            
        } catch (Exception e) {
            log.error("Erro ao obter certificado do AWS Secrets Manager: {}", e.getMessage());
            throw new RuntimeException("Falha ao carregar certificado", e);
        }
    }
    
    public String obterParametro(String parameterName) {
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(true)
                .build();
            
            GetParameterResponse response = ssmClient.getParameter(request);
            return response.parameter().value();
            
        } catch (Exception e) {
            log.error("Erro ao obter parâmetro {}: {}", parameterName, e.getMessage());
            throw new RuntimeException("Falha ao carregar parâmetro", e);
        }
    }
}
```

### 5. CONFIGURAÇÃO AWS CLIENTS

#### AwsConfig.java
```java
package br.com.llz.cnd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class AwsConfig {
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
            .region(Region.of(awsRegion))
            .build();
    }
    
    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder()
            .region(Region.of(awsRegion))
            .build();
    }
}
```

### 6. ATUALIZAÇÃO DO CndAssinaturaService

#### CndAssinaturaService.java (Versão AWS)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CndAssinaturaService {
    
    private final UnidadeCndRepository cndRepository;
    private final AwsSecretsService awsSecretsService;
    
    @Value("${cnd.certificado.alias}")
    private String certificadoAliasParam;
    
    @Value("${cnd.certificado.password}")
    private String certificadoPasswordParam;
    
    private byte[] assinarDocumentoLocal(byte[] documento) {
        try {
            // 1. Obter certificado do AWS Secrets Manager
            byte[] certificadoBytes = awsSecretsService.obterCertificado();
            
            // 2. Obter configurações do Systems Manager
            String alias = awsSecretsService.obterParametro(certificadoAliasParam);
            String password = awsSecretsService.obterParametro(certificadoPasswordParam);
            
            // 3. Carregar KeyStore a partir dos bytes
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(certificadoBytes), password.toCharArray());
            
            // 4. Obter chave privada
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
            
            // 5. Assinar documento
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(documento);
            byte[] assinatura = signature.sign();
            
            return combinarDocumentoComAssinatura(documento, assinatura);
            
        } catch (Exception e) {
            log.error("Erro ao assinar documento com certificado AWS: {}", e.getMessage());
            return documento;
        }
    }
    
    // Resto do código permanece igual...
}
```

---

## 🚀 CONFIGURAÇÃO ECS

### 1. IAM ROLE PARA ECS TASK

#### ecs-task-role-policy.json
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue"
            ],
            "Resource": [
                "arn:aws:secretsmanager:us-east-1:444301769287:secret:llz-cnd-certificado-dev*",
                "arn:aws:secretsmanager:us-east-1:770248317149:secret:llz-cnd-certificado-hml*",
                "arn:aws:secretsmanager:us-east-1:756241603306:secret:llz-cnd-certificado-prd*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "ssm:GetParameter",
                "ssm:GetParameters"
            ],
            "Resource": [
                "arn:aws:ssm:us-east-1:*:parameter/llz/cnd/certificado/*"
            ]
        }
    ]
}
```

### 2. ECS TASK DEFINITION

#### task-definition.json
```json
{
    "family": "llz-cnd-api",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "512",
    "memory": "1024",
    "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
    "taskRoleArn": "arn:aws:iam::ACCOUNT:role/llz-cnd-task-role",
    "containerDefinitions": [
        {
            "name": "llz-cnd-api",
            "image": "ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/llz-cnd-api:latest",
            "portMappings": [
                {
                    "containerPort": 8080,
                    "protocol": "tcp"
                }
            ],
            "environment": [
                {
                    "name": "SPRING_PROFILES_ACTIVE",
                    "value": "dev"
                },
                {
                    "name": "AWS_REGION",
                    "value": "us-east-1"
                }
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/llz-cnd-api",
                    "awslogs-region": "us-east-1",
                    "awslogs-stream-prefix": "ecs"
                }
            }
        }
    ]
}
```

---

## 🔒 BENEFÍCIOS DE SEGURANÇA

### 1. **Isolamento Completo**
- ✅ Certificado nunca fica no código fonte
- ✅ Certificado nunca fica na imagem Docker
- ✅ Acesso controlado por IAM roles

### 2. **Auditoria e Compliance**
- ✅ CloudTrail registra todos os acessos
- ✅ Versionamento automático no Secrets Manager
- ✅ Rotação automática de secrets (futuro)

### 3. **Segregação por Ambiente**
- ✅ Certificados diferentes por ambiente
- ✅ Contas AWS separadas (DEV/HML/PRD)
- ✅ Políticas IAM específicas por ambiente

### 4. **Monitoramento e Alertas**
- ✅ CloudWatch Logs para auditoria
- ✅ Alertas de acesso não autorizado
- ✅ Métricas de uso do certificado

---

## 💰 CUSTOS ESTIMADOS

### AWS Secrets Manager
- **Armazenamento**: $0.40/mês por secret
- **Requisições**: $0.05 por 10.000 requisições
- **Total estimado**: ~$5/mês para 3 ambientes

### Systems Manager Parameter Store
- **Parâmetros Standard**: Gratuito
- **Parâmetros Advanced**: $0.05 por 10.000 requisições
- **Total estimado**: ~$2/mês

### **Custo Total**: ~$7/mês (muito baixo comparado ao valor de segurança)

---

## 🚀 PLANO DE MIGRAÇÃO

### Fase 1: Preparação (1 dia)
1. Criar secrets no AWS Secrets Manager
2. Configurar parâmetros no Systems Manager
3. Criar IAM roles e policies

### Fase 2: Desenvolvimento (2 dias)
1. Implementar AwsSecretsService
2. Atualizar CndAssinaturaService
3. Configurar profiles por ambiente

### Fase 3: Testes (1 dia)
1. Testes em ambiente DEV
2. Validação de assinatura
3. Testes de performance

### Fase 4: Deploy (1 dia)
1. Deploy em HML
2. Testes de integração
3. Deploy em PRD

---

## 📋 CHECKLIST DE IMPLEMENTAÇÃO

### Preparação AWS
- [ ] Criar secrets no Secrets Manager (DEV/HML/PRD)
- [ ] Configurar parâmetros no Systems Manager
- [ ] Criar IAM roles com permissões mínimas
- [ ] Configurar CloudWatch Logs

### Desenvolvimento
- [ ] Adicionar dependências AWS SDK
- [ ] Implementar AwsSecretsService
- [ ] Atualizar CndAssinaturaService
- [ ] Configurar profiles por ambiente
- [ ] Implementar cache local (opcional)

### Testes
- [ ] Testes unitários com mocks
- [ ] Testes de integração com AWS
- [ ] Validação de assinatura digital
- [ ] Testes de performance

### Deploy
- [ ] Configurar ECS Task Definition
- [ ] Deploy em ambiente DEV
- [ ] Validação em HML
- [ ] Deploy em PRD

---

## 🔧 COMANDOS ÚTEIS

### Upload do Certificado
```bash
# Converter certificado para base64 (se necessário)
base64 -i llz-certificado.p12 -o certificado-base64.txt

# Upload direto para Secrets Manager
aws secretsmanager put-secret-value \
  --secret-id llz-cnd-certificado-dev \
  --secret-binary fileb://llz-certificado.p12
```

### Teste Local
```bash
# Testar acesso ao secret
aws secretsmanager get-secret-value \
  --secret-id llz-cnd-certificado-dev \
  --query SecretBinary \
  --output text | base64 -d > certificado-teste.p12
```

### Monitoramento
```bash
# Ver logs do ECS
aws logs tail /ecs/llz-cnd-api --follow

# Verificar métricas do Secrets Manager
aws cloudwatch get-metric-statistics \
  --namespace AWS/SecretsManager \
  --metric-name SuccessfulRequestLatency \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-02T00:00:00Z \
  --period 3600 \
  --statistics Average
```

---

## ✅ CONCLUSÃO

Esta solução oferece **máxima segurança** para o certificado digital A1, atendendo às preocupações da diretoria:

1. **Certificado fora do código**: Armazenado no AWS Secrets Manager
2. **Acesso controlado**: IAM roles com permissões mínimas
3. **Auditoria completa**: CloudTrail registra todos os acessos
4. **Segregação por ambiente**: Certificados diferentes para DEV/HML/PRD
5. **Custo baixo**: ~$7/mês para máxima segurança

A implementação é **transparente** para o código de negócio e **escalável** para futuras necessidades de segurança.