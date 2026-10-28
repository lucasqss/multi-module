# 0013. Monolito modular para o pagamento do crédito de consórcio

- **Status:** Aceita (a organização interna em módulos Maven foi substituída pela [0014](0014-camadas-num-modulo-so.md))
- **Data:** 2026-10-28

Complementa a [0001](0001-multi-module-com-deploy-unico.md): ela decide o formato (multi-module com deploy único);
esta justifica o estilo pela carga, pelo domínio e pela fase do projeto (domínio em descoberta), com Mark Richards e Kent Beck como referências.

## Contexto

O sistema paga o crédito de consórcio (imóvel, veículo): cria o processo, confere as regras, processa e libera.

**Os números:**

- No máximo 1.000 usuários por dia, das 9h às 17h: uns 125 por hora.
- Supondo uns 20 requests por usuário, são 20 mil por dia, **menos de 1 request por segundo** em média.
- Com um pico 10 vezes maior, ainda fica abaixo de 10 por segundo. Uma instância Quarkus atende milhares.
- Fora do horário não há uso: existe uma janela natural para deploy e manutenção.

**O momento do domínio:**

- O processo começa do zero. As modalidades de consórcio (imóvel e veículo hoje; outras depois) entram uma de cada
  vez.
- A cada modalidade, o modelo de dados e as regras mudam, inclusive no que é comum a todas.
- As fronteiras do domínio ainda estão sendo descobertas: o que hoje parece um módulo estável pode se dividir ou se
  juntar daqui a alguns meses.

**As características que decidem o estilo** (as *driving characteristics* de Richards e Ford, que são poucas de
propósito):

| Característica | Por quê |
|---|---|
| Integridade dos dados e transação | É dinheiro: liberar o crédito e mudar a situação do processo acontecem juntos, ou nenhum dos dois. |
| Auditabilidade | O consórcio é regulado pelo Bacen. |
| Evolutividade (facilidade de mudar) | O domínio e o modelo de dados mudam a cada modalidade nova; é a característica que mais pesa agora. |
| Manutenibilidade e testabilidade | Equipe pequena; as regras mudam por segmento. |
| Disponibilidade no horário comercial | Não precisa ser 24x7. |

Escalabilidade e elasticidade **não** estão na lista.

## Decisão

Um único *architecture quantum*: um jar, um banco, uma transação local. Por dentro, ele é particionado por domínio
em módulos Maven, com as fronteiras verificadas por teste (ADR 0001).

### Por que, em Mark Richards

1. **"Tudo em arquitetura é um trade-off" e "o porquê importa mais que o como"**: as duas leis de *Fundamentals of
   Software Architecture*. A escolha parte das características acima, não da moda.
2. **O quantum é a unidade de deploy.** Richards só divide o sistema em vários quanta quando as partes precisam de
   características diferentes. Aqui todas têm a mesma carga, a mesma janela e a mesma exigência de integridade,
   então um quantum basta.
3. **Os integradores de granularidade pesam mais que os desintegradores** (*Software Architecture: The Hard
   Parts*).
   - **Pesam para juntar:**
     - a transação de banco: pagamento e estado juntos, sem saga nem compensação;
     - o workflow: o motor coordena criar, processar e pagar;
     - o relacionamento dos dados: `PROCESSO` e as tabelas filhas.
   - **Não pesam para separar:**
     - escalabilidade e throughput;
     - tolerância a falha num pedaço só;
     - volatilidade isolada;
     - segurança com escopo próprio.
4. **As falácias da computação distribuída**, que Richards retoma em *Fundamentals*: a rede não é confiável, a
   latência não é zero, a transação distribuída é cara. Microsserviços trariam todas elas sem benefício nesta carga.
5. **Particionamento por domínio dentro do monolito.** Os módulos e os pacotes `segmento.*` e `cadastro.*` seguem o
   domínio: é o *modular monolith* de Richards. Ele aponta o risco desse estilo: as fronteiras se desgastam sem
   governança. Por isso elas são *fitness functions* executáveis: o Maven, o `ArquiteturaModulosTest` e o
   `ArquiteturaTest`.
6. **Nas tabelas de avaliação de Richards, os estilos monolíticos pontuam alto em simplicidade e custo.** É
   exatamente o que este cenário pede.
7. **Volatilidade espalhada não é motivo para separar** (*The Hard Parts*).
   - A volatilidade do código só puxa para separar quando está concentrada numa parte: separar isola essa parte do
     resto estável.
   - Aqui a mudança está em todo o domínio, inclusive no modelo de dados. Separar agora congelaria fronteiras que
     ainda estão sendo descobertas.
   - Cada mudança de modelo viraria mudança de contrato entre serviços, mais uma migration coordenada em bancos
     separados. *The Hard Parts* trata a decomposição dos dados como a parte mais difícil.
   - No monolito, mover uma fronteira é uma refatoração na IDE mais uma migration.

### Por que, em Kent Beck

1. **Design simples e YAGNI** (*Extreme Programming Explained*). As quatro regras: passa nos testes, revela a
   intenção, não duplica, tem o menor número de elementos. Distribuir para menos de 1 request por segundo é
   construir para um futuro que pode não vir.
2. **Custo do software ≈ custo de mudar ≈ acoplamento** (*Tidy First?*, a partir de Constantine). Os módulos
   existem para baixar o acoplamento e subir a coesão onde a mudança acontece. Um segmento novo não toca no motor;
   um sistema externo novo não toca no core.
3. **Estrutura como opção** (*Tidy First?*). O software vale pelo que faz hoje e pelas opções que abre amanhã.
   - Com as fronteiras limpas, extrair um módulo para um serviço é mudar o deploy, não o desenho. A opção custa
     pouco e é exercida quando a carga, o time ou a regulação pedirem.
   - O caminho inverso, distribuir agora e juntar depois, é caro de desfazer.
4. **TDD e feedback rápido** (*Test-Driven Development: By Example*). O core roda sem Quarkus, com `Fakes`, e os
   unitários levam cerca de 30 s. Os contratos garantem que o fake e o Oracle se comportam igual.
5. **"Make the change easy, then make the easy change."** Interface só onde há costura real (ADR 0005): cada
   elemento de estrutura precisa se pagar.
6. **"Embrace change"**, o subtítulo de *Extreme Programming Explained*. O XP parte do princípio de que o
   requisito vai mudar e otimiza para que mudar seja barato. Esse é o cenário de um domínio que cresce modalidade
   a modalidade.
7. **Explore, Expand, Extract (3X).**
   - Na fase de exploração, o que vale é aprender rápido com experimentos baratos.
   - Investir em escala e em separação física é coisa da fase de extração, quando o domínio já estabilizou.
   - Distribuir agora seria pagar o custo da fase 3 durante a fase 1.
8. **Decisões reversíveis são baratas** (*Tidy First?*).
   - Uma fronteira entre módulos dentro do mesmo jar se desfaz numa refatoração. Uma fronteira entre serviços, com
     banco próprio, não se desfaz fácil.
   - Separe as mudanças de estrutura das de comportamento, em passos pequenos. A cada modalidade nova: primeiro
     arrumar o modelo para receber a mudança, depois fazer a mudança.

## Consequências

**Prós**

- **Uma transação ACID local para o que envolve dinheiro**: sem saga, sem outbox entre serviços, sem consistência
  eventual.
- **Um pipeline, um deploy e um banco para operar**: o custo mínimo de infraestrutura e plantão.
- **Deploy fora do horário**: a janela das 17h às 9h neutraliza a maior fraqueza do monolito, que é publicar tudo
  junto.
- **Fronteiras garantidas por compilação e por teste**, não por disciplina: quem chega, pessoa ou IA, erra menos.
- **Refatoração entre módulos numa IDE só**, sem versionar contratos de rede.
- **Uma mudança de domínio é um commit, um deploy e uma migration**, com o compilador apontando tudo o que quebrou
  (até o HQL das consultas é validado na compilação).
- **Uma modalidade nova é aditiva**: um segmento novo e uma tabela filha, sem tocar no motor nem nas outras
  modalidades (ADRs 0002 e 0003).

**Contras e como estão mitigados**

| Contra | Mitigação |
|---|---|
| Tudo sobe junto: um bug em qualquer módulo exige deploy de tudo. | `mvn verify` completo e deploy fora do horário. |
| Tolerância a falha baixa: memória esgotada ou threads presas num sistema externo lento derrubam tudo. | Timeouts de 2 s, PDF fora da transação, 503 isolado por integração. |
| Só escala por inteiro. | Irrelevante nesta carga; uma segunda instância resolve a disponibilidade, se preciso. |
| O multi-module tem custo próprio: 5 POMs, enums espelhados, test-jar, Jandex, JaCoCo agregado. | Pelo critério de Beck, ele se paga porque garante as fronteiras na compilação e acelera quem chega. Um módulo único com pacotes e ArchUnit funcionaria, com garantia mais fraca. |
| Banco compartilhado: os módulos se acoplam pelos dados, o que dificulta uma extração futura. | Só o core grava; o query só lê (ADR 0004). |
| Erosão das fronteiras, o risco que Richards aponta no modular monolith. | As fitness functions quebram o build. |
| Pilha única (Java, Quarkus, Oracle) para tudo. | Aceito: um time, uma pilha. |
| Um campo novo atravessa várias camadas: migration, entidade, record do domínio, mapper, projeção da consulta, DTO de entrada. Com o domínio mudando muito, esse é o custo que mais aparece no dia a dia. | É o preço de separar domínio e schema. As receitas do `AGENTS.md` listam cada passo; o compilador, o HQL validado na compilação e os contratos apontam o que faltou. |
| Os enums do domínio têm espelho no `jpa-core`: mudar um estado ou tipo é mudar dois enums e um CHECK. | O `EnumsEspelhadosTest` falha até os dois ficarem iguais. |
| Com o modelo mudando, as migrations se acumulam. | Antes da primeira ida para produção, elas podem ser consolidadas numa só, porque ainda não foram aplicadas em produção. Depois disso, só migration nova. |
| Mudar um campo comum a todas as modalidades mexe na tabela pai e em todos os segmentos. | É raro por desenho: o comum fica no motor, o específico na tabela filha. |
| O motor único (ADR 0002) é uma aposta: uma modalidade com ciclo de vida diferente não cabe nele. | Num monolito, rever a aposta é refatoração; o gatilho está abaixo. |
| A auditabilidade ainda não está atendida: não há histórico de transições. | Independe do estilo, mas é uma característica que decide: precisa entrar no backlog. |

**Quando rever** (cada um destes gatilhos pede uma ADR nova):

- **Uma parte passa a precisar de características diferentes** e pede um quantum separado. Exemplos: a confirmação
  de pagamento precisa ser 24x7 e o resto não; um canal aberto ao consorciado traz picos imprevisíveis.
- **Escopo de segurança ou compliance próprio** e separar reduz esse escopo. Exemplos: dados de cartão (PCI); um
  isolamento exigido pelo Bacen.
- **Vários times com cadências de release em conflito** no mesmo jar.
- **A carga medida, não a suposta, chega perto do limite** de uma instância.
- **Uma modalidade não cabe no ciclo de vida do motor.** Revê-se a ADR 0002 (o motor), não necessariamente o deploy.
- **O domínio estabilizou**: as fronteiras dos módulos não mudam há meses. Só então extrair um serviço passa a
  valer a pena, e só se outro gatilho desta lista também aparecer.

## Referências

- RICHARDS, M.; FORD, N. *Fundamentals of Software Architecture*. O'Reilly, 2020 (2ª ed., 2025): características
  arquiteturais, as duas leis, architecture quantum, falácias da computação distribuída, fitness functions,
  avaliação dos estilos.
- FORD, N.; RICHARDS, M.; SADALAGE, P.; DEHGHANI, Z. *Software Architecture: The Hard Parts*. O'Reilly, 2021:
  integradores e desintegradores de granularidade.
- RICHARDS, M. *Software Architecture Patterns*. 2ª ed. O'Reilly, 2022.
- BECK, K.; ANDRES, C. *Extreme Programming Explained*. 2ª ed. Addison-Wesley, 2004: design simples e YAGNI.
- BECK, K. *Test-Driven Development: By Example*. Addison-Wesley, 2002.
- BECK, K. *Tidy First?* O'Reilly, 2023: acoplamento, coesão, estrutura como opção, decisões reversíveis,
  separar estrutura de comportamento.
- BECK, K. Explore, Expand, Extract (3X): o modelo de fases que ele apresenta em palestras e textos a partir de 2016.
