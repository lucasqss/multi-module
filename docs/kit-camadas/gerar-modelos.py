#!/usr/bin/env python3
"""Gera MODELOS.md a partir do código do projeto de referência, para os modelos nunca divergirem do código que
compila e passa nos testes. Roda só no repositório de referência: python3 docs/kit-camadas/gerar-modelos.py"""
import pathlib

RAIZ = pathlib.Path(__file__).resolve().parents[2]
M = 'src/main/java/com/example/processo/'
T = 'src/test/java/com/example/processo/'

SECOES = [
    ('Modelo: entidades', 'A entidade pai (herança JOINED, discriminador só leitura, construtor de pedido, eventos de '
     'estado) e uma filha. A filha declara a constante do discriminador e preenche o pai pelo construtor.',
     [M + 'modelo/Processo.java', M + 'modelo/ProcessoVeicular.java']),
    ('Modelo: estados (State em enum)', 'Cada constante sobrescreve só as transições que aceita.',
     [M + 'modelo/Situacao.java', M + 'modelo/TransicaoInvalidaException.java']),
    ('Modelo: value objects e exceções comuns', 'Válidos por construção; as exceções viram 422 e 503.',
     [M + 'modelo/Cpf.java', M + 'modelo/Valor.java', M + 'modelo/RegraVioladaException.java',
      M + 'modelo/SistemaExternoIndisponivelException.java']),
    ('Negócio: regras', 'Especificação composta, regras comuns e regras de um tipo.',
     [M + 'negocio/processo/Especificacao.java', M + 'negocio/processo/RegrasComuns.java',
      M + 'negocio/veicular/RegrasVeiculares.java']),
    ('Negócio: ponto de extensão e implementações', 'A interface, a decisão do processamento e um tipo com regra que '
     'consulta o banco (por último na composição).',
     [M + 'negocio/processo/Modalidade.java', M + 'negocio/processo/Avaliacao.java',
      M + 'negocio/veicular/ModalidadeVeicular.java', M + 'negocio/imobiliario/ModalidadeImobiliaria.java']),
    ('Negócio: o motor', 'Fluxo único, idempotência, transações curtas (QuarkusTransaction), compare-and-set e efeito '
     'externo depois do commit.',
     [M + 'negocio/processo/ProcessoService.java', M + 'negocio/processo/ConflitoDeSituacaoException.java',
      M + 'negocio/processo/Relogio.java']),
    ('Negócio: CRUD pelo caminho curto', 'Entidade válida por construção, serviço com @Transactional, repository.',
     [M + 'modelo/Agencia.java', M + 'negocio/agencia/AgenciaService.java',
      M + 'negocio/agencia/AgenciaNaoEncontradaException.java']),
    ('Persistência: repositories', 'Insert/find polimórficos, sequence, compare-and-set em SQL nativo, entidade técnica '
     'escondida (idempotência), consulta própria de um tipo e CRUD.',
     [M + 'persistencia/repositorio/ProcessoRepository.java', M + 'persistencia/repositorio/ChaveIdempotencia.java',
      M + 'persistencia/repositorio/ProcessoImobiliarioRepository.java',
      M + 'persistencia/repositorio/AgenciaRepository.java']),
    ('Persistência: consultas para as telas', 'Projeções planas com select new e paginação; a apresentação usa direto.',
     [M + 'persistencia/consulta/ProcessoConsultas.java', M + 'persistencia/consulta/ResumoProcesso.java',
      M + 'persistencia/consulta/DetalheVeicular.java']),
    ('Integração com sistema externo', 'Cliente, simulação do perfil dev, formato externo e a classe que o negócio '
     'chama.',
     [M + 'integracao/cadastro/CadastroClient.java', M + 'integracao/cadastro/CadastroClientSimulado.java',
      M + 'integracao/cadastro/RespostaCadastro.java', M + 'integracao/cadastro/CadastroIntegracao.java']),
    ('Apresentação: REST', 'Resource que só traduz, request com paraModelo(), response com de(...), leitura direta '
     'das consultas, paginação com 400 e Problem Details.',
     [M + 'apresentacao/rest/ProcessoVeicularResource.java', M + 'apresentacao/rest/ProcessosResource.java',
      M + 'apresentacao/rest/AgenciasResource.java', M + 'apresentacao/rest/TratadorDeErros.java',
      M + 'apresentacao/rest/dto/NovoProcessoVeicularRequest.java', M + 'apresentacao/rest/dto/ProponenteRequest.java',
      M + 'apresentacao/rest/dto/ProcessoCriadoResponse.java', M + 'apresentacao/rest/dto/SituacaoResponse.java',
      M + 'apresentacao/rest/dto/AgenciaResponse.java', M + 'apresentacao/rest/dto/PaginaResponse.java',
      M + 'apresentacao/rest/dto/Problema.java']),
    ('Apresentação: fila', 'Consumidor AMQP com ack/nack por tipo de erro, idempotência pelo id da mensagem e payload '
     'em qualquer formato.',
     [M + 'apresentacao/mensageria/NovosProcessosImobiliarios.java',
      M + 'apresentacao/mensageria/dto/NovoProcessoImobiliarioMensagem.java',
      M + 'apresentacao/mensageria/dto/RespostaNovoProcesso.java']),
    ('package-info de uma camada', 'Cada camada diz o que contém e o que pode ou não usar.',
     [M + 'negocio/package-info.java']),
    ('Migrations', 'Tabela filha de um tipo (PK = FK) e uma tabela de CRUD.',
     ['src/main/resources/db/migration/V3__processo_veicular.sql', 'src/main/resources/db/migration/V5__agencia.sql']),
    ('Configuração', 'O POM completo (um módulo, hibernate-processor, JaCoCo com três fontes, failsafe) e o '
     'application.properties (sem URL de banco nem amqp-host).',
     ['pom.xml', 'src/main/resources/application.properties', 'docker-compose.yml']),
    ('Testes puros', 'Máquina de estados em tabela, regras e decisão de um tipo, integração com o cliente em lambda.',
     [T + 'modelo/SituacaoTest.java', T + 'negocio/veicular/ModalidadeVeicularTest.java',
      T + 'integracao/cadastro/CadastroIntegracaoTest.java']),
    ('Testes com o banco (@QuarkusTest, *BancoTest)', 'O motor e os repositories no Oracle do Dev Services, '
     'com o mesmo conjunto de @WithTestResource.',
     [T + 'negocio/processo/ProcessoServiceBancoTest.java', T + 'persistencia/ProcessoRepositoryBancoTest.java',
      T + 'NovosProcessosPorFilaBancoTest.java']),
    ('Infraestrutura de teste', 'Sistemas externos falsos (servidor HTTP na JVM do teste) e a fila em memória.',
     [T + 'CadastroFalso.java', T + 'SistemaDePdfFalso.java', T + 'FilaEmMemoria.java']),
    ('Testes de integração (contra o jar)', 'O mesmo ciclo de vida para cada tipo, inclusive concorrência.',
     [T + 'CicloDeVidaPorTipoIT.java', T + 'Listagem.java']),
]

LINGUAGEM = {'.java': 'java', '.sql': 'sql', '.xml': 'xml', '.properties': 'properties', '.yml': 'yaml'}


def main():
    partes = [
        '# Modelos de código\n',
        'Arquivos completos do projeto de referência (pacote `com.example.processo`), que compilam e passam nos testes. '
        'Troque o pacote e os nomes do domínio; mantenha a forma. O porquê de cada um está no `GUIA.md`.\n',
        '> Gerado por `gerar-modelos.py` a partir do código do repositório de referência. Não edite à mão: mude o código\n'
        '> de lá e rode o script de novo.\n',
        '## Índice\n',
    ]
    partes += [f'{i}. [{titulo}](#{i}-{ancora(titulo)})' for i, (titulo, _, _) in enumerate(SECOES, 1)]
    partes.append('')
    for i, (titulo, explicacao, arquivos) in enumerate(SECOES, 1):
        partes.append(f'## {i}. {titulo}\n')
        partes.append(explicacao + '\n')
        for caminho in arquivos:
            arquivo = RAIZ / caminho
            partes.append(f'### `{caminho}`\n')
            partes.append(f'```{LINGUAGEM[arquivo.suffix]}\n{arquivo.read_text().rstrip()}\n```\n')
    (RAIZ / 'docs/kit-camadas/MODELOS.md').write_text('\n'.join(partes))


def ancora(titulo):
    # Como o GitHub monta a âncora de um título: minúsculas, sem pontuação (acentos ficam), espaço vira hífen
    return ''.join(c if c.isalnum() or c in ' -' else '' for c in titulo.lower()).replace(' ', '-')


if __name__ == '__main__':
    main()
