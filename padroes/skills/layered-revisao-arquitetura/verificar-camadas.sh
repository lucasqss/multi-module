#!/usr/bin/env bash
# Verifica a regra de dependência entre camadas e sinais de domínio anêmico
# em projetos Java organizados em domain / application / adapter / entrypoint.
#
# Uso (a partir da raiz do repositório):
#   verificar-camadas.sh                       # descobre o(s) pacote(s) base sozinho
#   verificar-camadas.sh src/main/java/br/com/empresa/sistema   # ou informa o diretório
#
# Pacote base = diretório que contém "domain" e "application" (adapter/entrypoint são opcionais).
# Sai com código 1 se encontrar violação, 2 se não encontrar nenhum pacote base.
set -uo pipefail

falhas=0

checar() { # descrição, diretório, regex proibida
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

verificar_base() {
  local base="${1%/}"
  local pacote
  pacote=$(echo "$base" | sed -E 's#.*src/main/java/##; s#/#.#g')
  local p="${pacote//./\\.}"
  local entidades="$base/domain/entity"
  [ -d "$entidades" ] || entidades="$base/domain"

  echo "######## $pacote"
  echo "== Regra de dependência =="
  checar "domain não depende de framework" "$base/domain" \
    '^import (static )?(jakarta|javax|io\.quarkus|org\.hibernate|org\.springframework|com\.fasterxml|org\.eclipse|io\.micronaut)\.'
  checar "domain não depende de application/adapter/entrypoint" "$base/domain" \
    "^import (static )?$p\\.(application|adapter|entrypoint)\\."
  checar "application não depende de adapter/entrypoint" "$base/application" \
    "^import (static )?$p\\.(adapter|entrypoint)\\."
  checar "application não usa JPA/Hibernate" "$base/application" \
    '^import (static )?(jakarta\.persistence|javax\.persistence|org\.hibernate|io\.quarkus\.hibernate|org\.springframework\.data)\.'
  checar "adapter não depende de application/entrypoint" "$base/adapter" \
    "^import (static )?$p\\.(application|entrypoint)\\."
  checar "entrypoint não depende de adapter" "$base/entrypoint" \
    "^import (static )?$p\\.adapter\\."

  echo "== Domínio rico =="
  checar "entidades sem setters públicos" "$entidades" 'public\s+void\s+set[A-Z]'
  checar "entidades sem construtor público" "$base/domain/entity" 'public\s+[A-Z][A-Za-z0-9]*\s*\([^)]*\)\s*\{'
  checar "domínio sem relógio implícito (use Clock)" "$base/domain" '(LocalDate|LocalDateTime|Instant|ZonedDateTime)\.now\(\)'
  checar "application sem relógio implícito (use Clock)" "$base/application" '(LocalDate|LocalDateTime|Instant|ZonedDateTime)\.now\(\)'
  checar "entidades sem if/switch em status (use State)" "$entidades" \
    '(status|getStatus\(\))\s*(==|!=)|switch\s*\(\s*(status|getStatus\(\))'
  checar "use cases sem if/switch em status (use State)" "$base/application" \
    '(getStatus\(\)|status\(\))\s*(==|!=)|switch\s*\(.*getStatus\(\)'
  echo
}

if [ $# -gt 0 ]; then
  bases=("$@")
else
  mapfile -t bases < <(find . -path '*/src/main/java/*' -type d -name domain -not -path '*/target/*' \
    -exec sh -c '[ -d "$(dirname "$1")/application" ] && dirname "$1"' _ {} \; | sed 's#^\./##' | sort -u)
fi

if [ ${#bases[@]} -eq 0 ]; then
  echo "Nenhum pacote base encontrado (diretório com 'domain' e 'application' sob src/main/java)."
  echo "Informe o diretório: $0 src/main/java/<pacote/base>"
  exit 2
fi

for base in "${bases[@]}"; do
  verificar_base "$base"
done

if [ "$falhas" -gt 0 ]; then
  echo "$falhas verificação(ões) falharam."
  exit 1
fi
echo "Todas as verificações passaram."
