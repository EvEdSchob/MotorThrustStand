#!/bin/bash
# install-thruststand.sh - Installation script for ThrustStand

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root"
    exit 1
fi

# Application details
APP_NAME="thruststand"
INSTALL_DIR="/opt/$APP_NAME"
BIN_DIR="/usr/local/bin"
WMU_LOGO_URL="https://wmich.edu/sites/default/files/attachments/u171/2020/WMU%20primary-digital.png"

# Verify required directories exist
echo "Checking for required directories..."
if [ ! -d "bin" ]; then
    echo "Error: bin directory not found"
    exit 1
fi

if [ ! -d "src" ]; then
    echo "Error: src directory not found"
    exit 1
fi

if [ ! -d "src/fxml" ]; then
    echo "Error: src/fxml directory not found"
    exit 1
fi

# Create lib directory if it doesn't exist
if [ ! -d "lib" ]; then
    echo "Creating lib directory..."
    mkdir -p lib
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

# Download WMU logo if not present
if [ ! -f "src/resources/WMU_primary-digital.png" ]; then
    echo "Downloading WMU logo..."
    wget -O "src/resources/WMU_primary-digital.png" "$WMU_LOGO_URL"
fi

# Download and install Bellsoft JDK 21 if not present
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "version \"21"; then
    echo "Installing BellSoft JDK 21 (includes JavaFX)..."
    wget https://download.bell-sw.com/java/21.0.1+12/bellsoft-jdk21.0.1+12-linux-arm32-vfp-hflt.deb
    dpkg -i bellsoft-jdk21.0.1+12-linux-arm32-vfp-hflt.deb
    rm bellsoft-jdk21.0.1+12-linux-arm32-vfp-hflt.deb
fi

# Create installation directory structure
echo "Creating installation directories..."
mkdir -p $INSTALL_DIR/bin
mkdir -p $INSTALL_DIR/lib
mkdir -p $INSTALL_DIR/src/fxml
mkdir -p $INSTALL_DIR/src/resources

# Copy application files
echo "Copying application files..."
cp -r bin/* $INSTALL_DIR/bin/
cp -r lib/* $INSTALL_DIR/lib/
cp -r src/fxml/* $INSTALL_DIR/src/fxml/
cp -r src/resources/* $INSTALL_DIR/src/resources/

# Configure serial port access
echo "Configuring serial port access..."

# Create dialout group if it doesn't exist
if ! getent group dialout > /dev/null; then
    groupadd dialout
fi

# Add current user to dialout group
SUDO_USER=$(logname || echo $SUDO_USER)
if [ ! -z "$SUDO_USER" ]; then
    usermod -a -G dialout "$SUDO_USER"
    echo "Added user $SUDO_USER to dialout group"
fi

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

echo "Installation completed successfully!"
echo ""
echo "Directory structure installed to:"
echo "- $INSTALL_DIR/"
echo "  ├── bin/ (compiled class files)"
echo "  ├── lib/ (jserialcomm JAR)"
echo "  └── src/"
echo "      ├── fxml/"
echo "      └── resources/"
echo "          └── WMU_primary-digital.png"
echo ""
echo "IMPORTANT: For serial port access to take effect, please either:"
echo "1. Log out and log back in, or"
echo "2. Reboot the Raspberry Pi"
echo ""
echo "You can run the application by typing 'thruststand' in the terminal"
echo ""
echo "Installed Java version:"
java -version
echo "Installation directory: $INSTALL_DIR"
echo "Launcher location: $BIN_DIR/$APP_NAME"