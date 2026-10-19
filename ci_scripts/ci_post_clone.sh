#!/bin/bash
set -e

echo "=========================================================="
echo " [Kilom] Xcode Cloud ci_post_clone başlatılıyor..."
echo "=========================================================="

# 1. Repo kök dizinini belirle
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$SCRIPT_DIR/../gradlew" ]; then
    REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
elif [ -f "$SCRIPT_DIR/../../gradlew" ]; then
    REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
else
    REPO_ROOT="${CI_WORKSPACE:-$(pwd)}"
fi

echo "Repository Root: $REPO_ROOT"

# 2. gradlew çalıştırma iznini garantiye al
if [ -f "$REPO_ROOT/gradlew" ]; then
    chmod +x "$REPO_ROOT/gradlew"
fi

# 3. Yüksek Hızlı JDK 17 Kurulumu (Adoptium Temurin aarch64)
# Homebrew yerine doğrudan tarball indirerek derleme süresi 4 dakika yerine 15 saniyeye indirilir (Ücretsiz 25 saat kotasını korur).
JAVA_BASE_DIR="$HOME/jdk-17"
JAVA_HOME_DIR="$JAVA_BASE_DIR/Contents/Home"

if [ ! -x "$JAVA_HOME_DIR/bin/java" ]; then
    ARCH="$(uname -m)"
    echo "JDK 17 bulunamadı, mimari ($ARCH) için Adoptium Temurin 17 indiriliyor..."
    mkdir -p "$JAVA_BASE_DIR"
    if [ "$ARCH" = "x86_64" ]; then
        curl -sL "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_x64_mac_hotspot_17.0.12_7.tar.gz" | tar -xz -C "$JAVA_BASE_DIR" --strip-components=1
    else
        curl -sL "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.12_7.tar.gz" | tar -xz -C "$JAVA_BASE_DIR" --strip-components=1
    fi
fi

export JAVA_HOME="$JAVA_HOME_DIR"
export PATH="$JAVA_HOME/bin:$PATH"

# 4. Sistem geneline symlink oluştur (Xcode build scripti doğrudan erişebilsin)
sudo mkdir -p /Library/Java/JavaVirtualMachines 2>/dev/null || true
sudo ln -sfn "$JAVA_BASE_DIR" /Library/Java/JavaVirtualMachines/temurin-17.jdk 2>/dev/null || true
sudo ln -sf "$JAVA_HOME/bin/java" /usr/local/bin/java 2>/dev/null || true

# 5. Xcode Build Phase için ortam dosyasını yaz
echo "export JAVA_HOME=\"$JAVA_HOME\"" > "$REPO_ROOT/.xcode_cloud_env"
echo "export PATH=\"$JAVA_HOME/bin:\$PATH\"" >> "$REPO_ROOT/.xcode_cloud_env"

echo "Doğrulanan Java:"
"$JAVA_HOME/bin/java" -version
echo "JAVA_HOME=$JAVA_HOME"

# 6. Gradle ön ısıtma (Pre-warm / cache)
cd "$REPO_ROOT"
./gradlew --version

# 7. Otomatik Sürüm Kodu (Build Number) Senkronizasyonu
if [ -n "$CI_BUILD_NUMBER" ]; then
    echo "Xcode Cloud CI_BUILD_NUMBER tespit edildi: $CI_BUILD_NUMBER"
    perl -pi -e "s/^CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=$CI_BUILD_NUMBER/" "$REPO_ROOT/iosApp/Configuration/Config.xcconfig"
    echo "Config.xcconfig güncellendi:"
    grep "CURRENT_PROJECT_VERSION" "$REPO_ROOT/iosApp/Configuration/Config.xcconfig"
fi

echo "=========================================================="
echo " [Kilom] ci_post_clone başarıyla tamamlandı!"
echo "=========================================================="

