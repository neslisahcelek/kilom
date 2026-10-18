#!/bin/bash
set -e

# Delegate to the root ci_post_clone.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_SCRIPT="$(cd "$SCRIPT_DIR/../.." && pwd)/ci_scripts/ci_post_clone.sh"

if [ -f "$ROOT_SCRIPT" ]; then
    chmod +x "$ROOT_SCRIPT"
    exec "$ROOT_SCRIPT"
else
    echo "Hata: Root ci_post_clone.sh bulunamadı: $ROOT_SCRIPT"
    exit 1
fi
