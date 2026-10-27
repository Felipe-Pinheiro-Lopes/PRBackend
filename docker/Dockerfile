FROM lscr.io/linuxserver/webtop:ubuntu-xfce

# Atualiza pacotes e instala OpenJDK 21, Git, ferramentas de compilação e dependências de GUI
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk \
    git \
    curl \
    wget \
    ca-certificates \
    build-essential \
    mesa-utils \
    libgl1-mesa-dri \
    libglx-mesa0 \
    pulseaudio-utils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Instalação oficial do VS Code (Visual Studio Code) via pacote .deb direto
RUN curl -L -o /tmp/vscode.deb "https://update.code.visualstudio.com/latest/linux-deb-x64/stable" && \
    apt-get update && \
    apt-get install -y --no-install-recommends /tmp/vscode.deb && \
    rm -f /tmp/vscode.deb && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Script de inicialização automática: cria atalhos no Desktop para o VS Code e Workspace do projeto
RUN mkdir -p /etc/cont-init.d && \
    echo '#!/bin/bash\n\
mkdir -p /config/Desktop\n\
# Atalho do VS Code no Desktop\n\
cat << "DESKTOP_ENTRY" > /config/Desktop/code.desktop\n\
[Desktop Entry]\n\
Version=1.0\n\
Type=Application\n\
Name=Visual Studio Code\n\
Comment=Editor de Código Java\n\
Exec=/usr/bin/code --no-sandbox --unity-launch /config/workspace\n\
Icon=vscode\n\
Path=/config/workspace\n\
Terminal=false\n\
StartupNotify=true\n\
DESKTOP_ENTRY\n\
chmod +x /config/Desktop/code.desktop\n\
chown -R 911:911 /config/Desktop\n\
' > /etc/cont-init.d/99-spacedefender-setup && \
    chmod +x /etc/cont-init.d/99-spacedefender-setup
