#!/bin/bash
set -e

# Delegate to the root ci_pre_xcodebuild.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_SCRIPT="$(cd "$SCRIPT_DIR/../.." && pwd)/ci_scripts/ci_pre_xcodebuild.sh"

if [ -f "$ROOT_SCRIPT" ]; then
    chmod +x "$ROOT_SCRIPT"
    exec "$ROOT_SCRIPT"
fi
