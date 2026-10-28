# Prompts para o Copilot CLI, fase a fase

Abra o terminal na raiz do projeto que vai ser migrado e rode `copilot`. Cole o prompt da fase. A CLI pede permissão
antes de rodar comandos e alterar arquivos: leia o que ela vai fazer antes de aprovar.

- **Uma sessão por fase.** Ao fim de cada uma, revise o resumo, o diff e o `mvn verify` antes de seguir.
- Se o agente sugerir pular uma fase ou juntar duas, não aceite.

---

## Fase 0 — Diagnóstico

```text
Leia docs/kit-camadas/GUIA.md inteiro e docs/kit-camadas/MODELOS.md (índice e seções 1 a 12).
Execute só a Fase 0 do GUIA (seção 6): mapeie este projeto e escreva docs/migracao/diagnostico.md com
(1) o que existe, (2) a tabela de-para de cada classe de produção para a camada e o pacote alvo, com o papel,
(3) quais decisões da seção 2 se aplicam, não se aplicam ou já existem, e por quê,
(4) os desvios encontrados, usando a tabela da seção 7, e (5) riscos e perguntas para mim.
Não altere nenhum arquivo além do diagnóstico. Rode mvn verify para registrar o estado atual dos testes.
No fim, me mostre um resumo e as perguntas, e pare.
```

## Fase 1 — Rede de segurança

```text
Siga a Fase 1 do docs/kit-camadas/GUIA.md, usando docs/migracao/diagnostico.md e as minhas respostas.
1. Garanta ITs (@QuarkusIntegrationTest) para toda a API e as filas atuais: caminho feliz, erro de regra e não
   encontrado. Reaproveite os que existem. Depois de prontos, eles não mudam até o fim da migração.
2. Gere o OpenAPI de referência em docs/migracao/openapi-antes.
3. Copie docs/kit-camadas/AGENTS.md para a raiz, preenchendo os marcadores com os nomes reais, e
   docs/kit-camadas/copilot-instructions.md para .github/.
4. Instale docs/kit-camadas/ArquiteturaTest.java em src/test/java/<pacote-base> com MIGRACAO = true e o
   src/test/resources/archunit.properties; rode e faça commit do archunit_store gerado.
mvn verify verde, commit "Migração camadas — fase 1: rede de segurança", resumo e pare.
```

## Fase 2 — Build

```text
Siga a Fase 2 do docs/kit-camadas/GUIA.md: um módulo Maven só (git mv para manter o histórico), com o build do
pom.xml de docs/kit-camadas/MODELOS.md (hibernate-processor, failsafe com quarkus.test.arg-line, JaCoCo com as
quatro execuções). Não mude nenhuma classe de pacote ainda. Não altere os ITs da Fase 1.
mvn verify verde, commit "Migração camadas — fase 2: build", resumo e pare.
```

## Fase 3 — Camadas (repita para cada camada, na ordem)

```text
Siga a Fase 3 do docs/kit-camadas/GUIA.md, só a camada <modelo | persistencia | integracao | negocio | apresentacao>,
conforme a tabela de-para do diagnóstico. Mova com git mv, ajuste pacotes e nomes pelo papel, e siga a forma dos
arquivos correspondentes em docs/kit-camadas/MODELOS.md. Para código legado que não se encaixa, use a seção 7.
Não mude comportamento nem os ITs da Fase 1. Rode o ArquiteturaTest e me diga quais violações congeladas desta
camada sumiram e quais restam.
mvn verify verde, commit "Migração camadas — fase 3: <camada>", resumo e pare.
```

## Fase 4 — Padrões

```text
Siga a Fase 4 do docs/kit-camadas/GUIA.md, aplicando só os padrões que o diagnóstico marcou como "aplica", um commit
por padrão, na forma de docs/kit-camadas/MODELOS.md. Antes de qualquer mudança que altere o contrato (por exemplo, o
formato dos erros para Problem Details), pare e me pergunte.
mvn verify verde a cada commit, resumo e pare.
```

## Fase 5 — Testes e cobertura

```text
Siga a Fase 5 do docs/kit-camadas/GUIA.md: testes puros para regras, estados, value objects e integrações;
@QuarkusTest com nome *BancoTest para o motor e os repositories (o mesmo conjunto de @WithTestResource); cobertura
com as três fontes. Depois troque MIGRACAO para false no ArquiteturaTest e apague o archunit_store e o
archunit.properties. Me mostre a cobertura por pacote, a partir do target/site/jacoco/jacoco.xml.
mvn verify verde, commit "Migração camadas — fase 5: testes e cobertura", resumo e pare.
```

## Fase 6 — Documentação e fechamento

```text
Siga a Fase 6 do docs/kit-camadas/GUIA.md: AGENTS.md final (sem marcadores e sem a seção "Migração em andamento"),
ADRs em docs/adr/ para as decisões adotadas e README com como rodar e a tabela da API. Depois confira o
"Critério de pronto da migração", incluindo a comparação do OpenAPI atual com docs/migracao/openapi-antes, e me
mostre cada item marcado com a evidência.
mvn verify verde, commit "Migração camadas — fase 6: documentação", resumo final.
```

---

## Se o agente travar ou desviar

- **Build vermelho que ele não resolve:** peça "mostre o erro completo e a causa, sem mudar teste para passar".
- **Ele quer editar um IT da Fase 1:** recuse. Se o IT quebrou, a migração mudou o comportamento.
- **Ele propõe interface com uma implementação, mapper ou serviço que só repassa leitura:** aponte o
  `GUIA.md`, seção 2.4, 2.2 ou 2.3.
- **Dúvida de regra de negócio:** responda você. O agente não deve decidir.
