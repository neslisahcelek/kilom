#!/bin/bash
set -e

# Kilom sürüm kodu (CURRENT_PROJECT_VERSION) artırıcı
CONFIG_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/../iosApp/Configuration" && pwd)/Config.xcconfig"

CURRENT_VERSION=$(grep "^CURRENT_PROJECT_VERSION=" "$CONFIG_FILE" | cut -d'=' -f2)
NEW_VERSION=$((CURRENT_VERSION + 1))

perl -pi -e "s/^CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=$NEW_VERSION/" "$CONFIG_FILE"

echo "=========================================================="
echo " [Kilom] Build numarası güncellendi: $CURRENT_VERSION -> $NEW_VERSION"
echo " Dosya: $CONFIG_FILE"
echo "=========================================================="
