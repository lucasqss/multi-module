---
name: layered-iniciar-projeto
description: Prepara um projeto Java (Quarkus) para o padrão layered com domínio rico, criando a base genérica (Specification, RegraNegocio, ValidadorRegras, famílias de exceção, Dinheiro, Motivo, paginação, handler único de erros HTTP, Clock no fuso do negócio) a partir dos templates desta skill, e o "Mapa do projeto" nas instruções do Copilot. Use em projeto novo ou existente que ainda não tem essa base, ou quando outra skill layered-* não encontrar os papéis de que precisa.
---

# Iniciar projeto no padrão layered

## 1. Descubra o que já existe (não duplique)

1. **Pacote base**: o diretório sob `src/main/java` que contém (ou vai conter) `domain/`, `application/`,
   `adapter/`, `entrypoint/`, `shared/`. Se não estiver claro, **pergunte ao usuário**.
2. Para cada template em `templates/` (nesta pasta da skill), verifique se o projeto já tem uma classe
   com o mesmo papel (mesmo que com outro nome). Se tiver, **mantenha a do projeto** e anote no mapa.
3. Framework: os templates usam **Quarkus** (`@ServerExceptionMapper`, CDI, MicroProfile Config).
   Em Spring, adapte só o entrypoint/config: `@RestControllerAdvice` + `@ExceptionHandler`,
   `@Configuration` + `@Bean`, `@Value`. Domínio e application não mudam.

## 2. Copie os templates

Para cada `templates/<caminho>/<Nome>.java.tmpl`:
- destino: `src/main/java/<pacote/base>/<caminho>/<Nome>.java`
  (o template de teste vai para `src/test/java/<pacote/base>/<caminho sem "test/">/`);
- troque `__PACOTE__` pelo pacote base (ex.: `br.com.empresa.sistema`).

| Template | Papel |
|---|---|
| `domain/specification/{Specification,RegraNegocio,ValidadorRegras}` | Specification pattern componível, regra com exceção específica, validador fail-fast |
| `domain/exception/DomainException` + `DadoInvalido`/`RecursoNaoEncontrado`/`Conflito`/`RegraNegocio` | As quatro famílias (400/404/409/422) |
| `domain/exception/{ValorMonetarioInvalido,MotivoInvalido}Exception` | Usadas por `Dinheiro` e `Motivo` |
| `domain/valueobject/{Dinheiro,Motivo}` | VOs genéricos |
| `shared/Pagina` | Paginação do repositório até a resposta |
| `shared/ClockProducer` | `Clock` no fuso do negócio (`app.fuso-horario`) |
| `entrypoint/api/v1/handler/{ApiExceptionHandler,ErroResponse}` | Formato único de erro, status por família |
| `entrypoint/api/v1/request/MotivoRequest`, `response/PaginaResponse` | Contratos HTTP reutilizáveis |
| `test/domain/specification/SpecificationTest` | Testa a base de specs |

Ajuste o `v1` dos caminhos se o projeto versiona a API de outro jeito.

## 3. Dependências (Quarkus)

Garanta no `pom.xml`: `quarkus-rest-jackson`, `quarkus-hibernate-validator`; para testes,
`quarkus-junit` (ou `quarkus-junit5`, conforme a versão) e `rest-assured`.
Persistência (`quarkus-hibernate-orm-panache` + driver) só quando houver adapter de banco.

## 4. Mapa do projeto

Copie `mapa-do-projeto.md` (nesta pasta) para o fim de `.github/copilot-instructions.md` (crie o
arquivo se não existir) e preencha com os nomes **reais** do projeto. As outras skills `layered-*`
leem esse mapa primeiro. Acrescente também, antes do mapa, as regras inegociáveis:

```markdown
## Regras inegociáveis
1. Domínio rico: entidade sem setters/construtor público; muda só por métodos de negócio.
2. Use case orquestra, não decide: nenhum `if` de regra de negócio em `*UC`.
3. Regras de negócio = Specification no catálogo; specs puras (fatos chegam no candidato).
4. Status = State pattern (classe por estado, sealed); nunca `if`/`switch` em status.
5. Exceções específicas por situação, nas famílias 400/404/409/422; um handler HTTP por família.
6. Value objects para conceitos com regra de formato; tempo sempre via `Clock`.
7. Contratos separados: Request/Response ≠ Input/Output ≠ entidade ≠ JpaEntity.
8. Ordem na entidade: estado autoriza (409) → specs validam (422) → muda estado.
9. Sem ports and adapters: use cases injetam classes concretas de `adapter/` (repositórios e
   integrações que falam domínio). Interface só com mais de uma implementação real.
10. Estrutura por tipo e enxuta: domain só com `entity`, `valueobject`, `exception`, `specification`,
    `state`; nada de `factory`, `model`, `enums`, `repository`, `service`; `shared/` sem subpastas.
11. Quem chama quem: Resource → UC → (repositório/integração, entidade); entidade → estado e regras.
    UC não chama UC; nada de `*Service`/`*Utils` genéricos.
12. Caso de uso = uma intenção do usuário: uma classe, um `executar`, uma transação. Nada de UC com
    vários métodos nem UC por entidade-parte; seção editada como bloco = um UC `Salvar<Secao>` (`PUT`);
    agregado que pode ficar incompleto nasce `EM_EDICAO` e só a finalização exige completude.
```

## 5. Valide

1. Compile e rode os testes (`mvn test`): o `SpecificationTest` copiado deve passar.
2. Rode `verificar-camadas.sh` da skill `layered-revisao-arquitetura`.
3. Próximo passo: o primeiro agregado com a skill `layered-novo-agregado-crud`. Se o projeto já tem
   pastas fora da estrutura alvo, rode antes a skill `layered-enxugar-estrutura`.
