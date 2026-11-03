#!/usr/bin/env bash
# Verifica a regra de dependência entre camadas (sem ports and adapters: application pode
# injetar adapter; domain não depende de ninguém) e sinais de domínio anêmico.
# Uso: .github/skills/revisao-arquitetura/verificar-camadas.sh   (a partir da raiz do repositório)
# Sai com código 1 se encontrar violação.
set -uo pipefail

BASE="src/main/java/com/example/boleto"
falhas=0

checar() { # descrição, diretório, padrão (regex de import proibido)
  local descricao="$1" dir="$2" padrao="$3"
  [ -d "$dir" ] || return 0
  local achados
  achados=$(grep -rnE "$padrao" "$dir" --include='*.java' || true)
  if [ -n "$achados" ]; then
    echo "✗ $descricao"
    echo "$achados" | sed 's/^/    /'
    falhas=$((falhas + 1))
  else
    echo "✓ $descricao"
  fi
}

echo "== Regra de dependência =="
checar "domain não depende de framework" "$BASE/domain" \
  '^import (static )?(jakarta|javax|io\.quarkus|org\.hibernate|com\.fasterxml|org\.eclipse)\.'
checar "domain não depende de application/adapter/entrypoint" "$BASE/domain" \
  '^import (static )?com\.example\.boleto\.(application|adapter|entrypoint)\.'
checar "application não depende de entrypoint" "$BASE/application" \
  '^import (static )?com\.example\.boleto\.entrypoint\.'
checar "application não usa JPA/Hibernate" "$BASE/application" \
  '^import (static )?(jakarta\.persistence|org\.hibernate|io\.quarkus\.hibernate)\.'
checar "adapter não depende de application/entrypoint" "$BASE/adapter" \
  '^import (static )?com\.example\.boleto\.(application|entrypoint)\.'
checar "entrypoint não depende de adapter" "$BASE/entrypoint" \
  '^import (static )?com\.example\.boleto\.adapter\.'

echo "== Domínio rico =="
checar "entidades sem setters públicos" "$BASE/domain/entity" \
  'public\s+void\s+set[A-Z]'
checar "entidades sem construtor público" "$BASE/domain/entity" \
  'public\s+[A-Z][A-Za-z0-9]*\s*\([^)]*\)\s*\{'
checar "domínio sem relógio implícito (use Clock)" "$BASE/domain" \
  '(LocalDate|LocalDateTime|Instant)\.now\(\)'
checar "application sem relógio implícito (use Clock)" "$BASE/application" \
  '(LocalDate|LocalDateTime|Instant)\.now\(\)'
checar "domínio sem if/switch em status (use State)" "$BASE/domain/entity" \
  '(status|getStatus\(\))\s*(==|!=)|switch\s*\(\s*(status|getStatus\(\))'
checar "use cases sem if/switch em status (use State)" "$BASE/application" \
  '(getStatus\(\)|status\(\))\s*(==|!=)|switch\s*\(.*getStatus\(\)'

echo
if [ "$falhas" -gt 0 ]; then
  echo "$falhas verificação(ões) falharam."
  exit 1
fi
echo "Todas as verificações passaram."
