# Decisões de arquitetura (ADRs)

Cada arquivo registra uma decisão: o contexto, o que foi decidido e as consequências. Curtas de propósito.
Uma ADR aceita não se edita; se a decisão mudar, escreve-se outra que a substitui. Para uma decisão nova, copie o
[modelo](0000-modelo.md) com o próximo número.

| Nº | Decisão | Status |
|---|---|---|
| [0001](0001-multi-module-com-deploy-unico.md) | Maven multi-module com deploy único | Substituída por 0014 |
| [0002](0002-motor-unico-com-spi-segmento.md) | Motor único com SPI `Segmento` | Aceita |
| [0003](0003-heranca-joined-e-genericos-no-dominio.md) | Herança JOINED no banco, genéricos no domínio | Substituída por 0014 |
| [0004](0004-escrita-e-leitura-separadas.md) | Escrita e leitura separadas | Substituída por 0014 |
| [0005](0005-interface-so-onde-ha-costura-real.md) | Interface só onde há costura real | Substituída por 0014 |
| [0006](0006-maquina-de-estados-state-em-enum.md) | Máquina de estados: padrão State num enum | Aceita |
| [0007](0007-compare-and-set-em-vez-de-lock.md) | Compare-and-set em vez de lock pessimista | Aceita |
| [0008](0008-sistemas-externos-atras-de-ports.md) | Sistemas externos atrás de ports em `integracoes` | Substituída por 0014 |
| [0009](0009-value-objects-validos-por-construcao.md) | Value objects válidos por construção | Aceita |
| [0010](0010-erros-em-problem-details.md) | Erros em Problem Details (RFC 9457) | Aceita |
| [0011](0011-entradas-por-canal-em-adapter-inbound.md) | Entradas por canal em `adapter/inbound` (REST, fila, agendamento) | Substituída por 0014 |
| [0012](0012-cadastro-simples-sem-motor.md) | Cadastro simples pelo caminho curto, sem motor | Aceita |
| [0013](0013-monolito-modular-para-o-consorcio.md) | Monolito modular para o pagamento do crédito de consórcio | Aceita |
| [0014](0014-camadas-num-modulo-so.md) | Camadas num módulo só, com as entidades como modelo | Aceita |
