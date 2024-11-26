#!/bin/bash
# install-thruststand.sh - Installation/Uninstallation script for ThrustStand

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root"
    exit 1
fi

# Function to show usage
show_usage() {
    echo "Usage: $0 [--uninstall]"
    echo "  No flags    Install ThrustStand (default)"
    echo "  --uninstall Remove ThrustStand"
    exit 1
}

# Application details
APP_NAME="thruststand"
INSTALL_DIR="/opt/$APP_NAME"
BIN_DIR="/usr/local/bin"
PRIMARY_LOGO_URL="https://wmich.edu/sites/default/files/attachments/u171/2020/WMU%20primary-digital.png"
STACKED_LOGO_URL="https://wmich.edu/sites/default/files/attachments/u171/2020/WMU%20stacked-digital.png"

# Function to install Java
install_java() {
    echo "Installing BellSoft JDK 21 (includes JavaFX)..."
    
    # Install required dependencies
    apt-get install -y wget

    # Download JDK
    wget https://download.bell-sw.com/java/21.0.4+9/bellsoft-jdk21.0.4+9-linux-aarch64-full.deb

    # Install JDK
    if ! dpkg -i bellsoft-jdk21.0.4+9-linux-aarch64-full.deb; then
        echo "Error during JDK installation, trying to fix dependencies..."
        apt-get -f install -y
        if ! dpkg -i bellsoft-jdk21.0.4+9-linux-aarch64-full.deb; then
            echo "Failed to install JDK"
            return 1
        fi
    fi

    # Clean up
    rm bellsoft-jdk21.0.4+9-linux-aarch64-full.deb

    return 0
}

# Function to perform uninstallation
uninstall_thruststand() {
    echo "Uninstalling ThrustStand..."

    # Remove desktop shortcut
    SUDO_USER=$(logname || echo $SUDO_USER)
    if [ ! -z "$SUDO_USER" ]; then
        USER_DESKTOP="/home/$SUDO_USER/Desktop"
        if [ -f "$USER_DESKTOP/ThrustStand.desktop" ]; then
            rm -f "$USER_DESKTOP/ThrustStand.desktop"
        fi
    fi

    # Remove .desktop file
    rm -f /usr/share/applications/thruststand.desktop

    # Remove binary
    rm -f "$BIN_DIR/$APP_NAME"

    # Remove installation directory
    rm -rf "$INSTALL_DIR"

    echo "Uninstallation completed successfully!"
    return 0
}

# Function to perform installation
install_thruststand() {
    # Check for existing installation
    if [ -d "$INSTALL_DIR" ]; then
        echo "Previous installation detected at $INSTALL_DIR"
        read -p "Do you want to remove it and continue? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            uninstall_thruststand
        else
            echo "Installation cancelled"
            exit 1
        fi
    fi

    # Verify required directories exist
    echo "Checking for required directories..."
    if [ ! -d "src" ]; then
        echo "Error: src directory not found"
        exit 1
    fi

    if [ ! -d "src/fxml" ]; then
        echo "Error: src/fxml directory not found"
        exit 1
    fi

    if [ ! -d "src/styles" ]; then
        echo "Error: src/styles directory not found"
        exit 1
    fi

    # Create lib and bin directories if they don't exist
    if [ ! -d "lib" ]; then
        echo "Creating lib directory..."
        mkdir -p lib
    fi

    if [ ! -d "bin" ]; then
        echo "Creating bin directory..."
        mkdir -p bin
    fi

    # Create resources directory if it doesn't exist
    if [ ! -d "src/resources" ]; then
        echo "Creating resources directory..."
        mkdir -p src/resources
    fi

    # Install dependencies
    echo "Updating package lists..."
    apt-get update
    apt-get install -y wget

    # Download jSerialComm if not present
    if [ ! -f "lib/jserialcomm-2.10.4.jar" ]; then
        echo "Downloading jSerialComm library..."
        wget "https://github.com/Fazecast/jSerialComm/releases/download/v2.10.4/jserialcomm-2.10.4.jar" -P "lib"
    fi

    # Download WMU logos if not present
    if [ ! -f "src/resources/WMU_primary-digital.png" ]; then
        echo "Downloading WMU primary logo..."
        wget -O "src/resources/WMU_primary-digital.png" "$PRIMARY_LOGO_URL"
    fi

    if [ ! -f "src/resources/WMU_stacked-digital.png" ]; then
        echo "Downloading WMU stacked logo..."
        wget -O "src/resources/WMU_stacked-digital.png" "$STACKED_LOGO_URL"
    fi

    # Check and install Java
    echo "Checking Java installation..."
    if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "openjdk version \"2[1-9]" 2>/dev/null; then
        if ! install_java; then
            echo "Failed to install Java. Please check error messages above."
            exit 1
        fi
    fi

    # Compile Java source files
    echo "Compiling Java source files..."
    # Build classpath for compilation
    COMPILE_CLASSPATH="."
    for jar in lib/*.jar; do
        COMPILE_CLASSPATH="$COMPILE_CLASSPATH:$jar"
    done

    # Find all Java files in src directory
    find src -name "*.java" > sources.txt

    # Compile with JavaFX modules
    javac --module-path /usr/share/openjfx/lib \
          --add-modules javafx.controls,javafx.fxml \
          -d bin \
          -cp "$COMPILE_CLASSPATH" \
          @sources.txt

    if [ $? -ne 0 ]; then
        echo "Compilation failed!"
        rm sources.txt
        exit 1
    fi

    rm sources.txt

    # Create installation directory structure
    echo "Creating installation directories..."
    mkdir -p $INSTALL_DIR/bin
    mkdir -p $INSTALL_DIR/lib
    mkdir -p $INSTALL_DIR/src/fxml
    mkdir -p $INSTALL_DIR/src/styles
    mkdir -p $INSTALL_DIR/src/resources

    # Copy application files
    echo "Copying application files..."
    cp -r bin/* $INSTALL_DIR/bin/
    cp -r lib/* $INSTALL_DIR/lib/
    cp -r src/fxml/* $INSTALL_DIR/src/fxml/
    cp -r src/styles/* $INSTALL_DIR/src/styles/
    cp -r src/resources/* $INSTALL_DIR/src/resources/

    # Create launcher script
    echo "Creating launcher script..."
    cat > $BIN_DIR/$APP_NAME << EOF
#!/bin/bash

# Build classpath
CLASSPATH="$INSTALL_DIR/bin"
CLASSPATH="\$CLASSPATH:$INSTALL_DIR/lib/*"
CLASSPATH="\$CLASSPATH:$INSTALL_DIR/src"

# Run application
cd $INSTALL_DIR  # Change to install directory to ensure relative paths work
java --add-modules javafx.controls,javafx.fxml \\
     -cp "\$CLASSPATH" \\
     ThrustStand "\$@"
EOF

    # Make launcher executable
    chmod +x $BIN_DIR/$APP_NAME

    # Set appropriate permissions
    chown -R root:root $INSTALL_DIR
    chmod -R 755 $INSTALL_DIR

    # Create desktop shortcut
    echo "Creating desktop shortcut..."

    # Create .desktop file
    cat > /usr/share/applications/thruststand.desktop << EOF
[Desktop Entry]
Version=1.0
Name=ThrustStand
Comment=WMU Motor Thrust Stand Application
Exec=thruststand
Icon=$INSTALL_DIR/src/resources/WMU_stacked-digital.png
Terminal=false
Type=Application
Categories=Utility;Application;
StartupNotify=true
NoDisplay=false
X-GNOME-SingleWindow=true
EOF

    # Make .desktop file executable
    chmod +x /usr/share/applications/thruststand.desktop

    # Create symlink on desktop for current user
    SUDO_USER=$(logname || echo $SUDO_USER)
    if [ ! -z "$SUDO_USER" ]; then
        USER_DESKTOP="/home/$SUDO_USER/Desktop"
        if [ -d "$USER_DESKTOP" ]; then
            ln -sf /usr/share/applications/thruststand.desktop "$USER_DESKTOP/ThrustStand.desktop"
            chown $SUDO_USER:$SUDO_USER "$USER_DESKTOP/ThrustStand.desktop"
            # Mark as trusted
            gio set "$USER_DESKTOP/ThrustStand.desktop" "metadata::trusted" yes
        fi
    fi

    echo "Installation completed successfully!"
    echo ""
    echo "Directory structure installed to:"
    echo "- $INSTALL_DIR/"
    echo "  ├── bin/ (compiled class files)"
    echo "  ├── lib/ (jserialcomm JAR)"
    echo "  └── src/"
    echo "      ├── fxml/"
    echo "      ├── styles/"
    echo "      └── resources/"
    echo "          └── WMU_primary-digital.png"
    echo ""
    echo "Verifying Java installation:"
    which java
    java -version
    echo ""
    echo "You can run the application by typing 'thruststand' in the terminal"
    echo ""
    echo "Installation directory: $INSTALL_DIR"
    echo "Launcher location: $BIN_DIR/$APP_NAME"
}

# Main script execution
case "$1" in
    --uninstall)
        uninstall_thruststand
        ;;
    --help)
        show_usage
        ;;
    "")
        # No arguments provided, default to install
        install_thruststand
        ;;
    *)
        echo "Invalid option: $1"
        show_usage
        ;;
esac