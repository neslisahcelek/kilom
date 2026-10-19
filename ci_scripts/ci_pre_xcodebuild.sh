#!/bin/bash
set -e

# Repo kök dizinini belirle
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$SCRIPT_DIR/../gradlew" ]; then
    REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
elif [ -f "$SCRIPT_DIR/../../gradlew" ]; then
    REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
else
    REPO_ROOT="${CI_WORKSPACE:-$(pwd)}"
fi

# Otomatik Sürüm Kodu (Build Number) Senkronizasyonu
if [ -n "$CI_BUILD_NUMBER" ]; then
    echo "=========================================================="
    echo " [Kilom] ci_pre_xcodebuild: Xcode Cloud CI_BUILD_NUMBER = $CI_BUILD_NUMBER"
    echo "=========================================================="
    perl -pi -e "s/^CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=$CI_BUILD_NUMBER/" "$REPO_ROOT/iosApp/Configuration/Config.xcconfig"
    echo "Güncellenen Config.xcconfig:"
    grep "CURRENT_PROJECT_VERSION" "$REPO_ROOT/iosApp/Configuration/Config.xcconfig"
fi
