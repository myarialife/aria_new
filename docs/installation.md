# ARIA Project Installation Guide

This guide provides detailed instructions for setting up the ARIA project development environment, including all necessary dependencies for working with the token contract, deployment scripts, and application components.

## Prerequisites

Before starting, ensure your system has:

- Git (version 2.20+)
- A text editor or IDE (VSCode recommended)
- Terminal/Command Line access

## 1. Setting Up the Rust Environment

The token contract requires Rust and the Solana BPF toolchain:

### 1.1 Install Rust
```bash
# Install Rustup (Rust installer)
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Follow the on-screen instructions, typically selecting option 1 for default installation

# Update your current shell
source $HOME/.cargo/env

# Verify installation
rustc --version
cargo --version
```

### 1.2 Install Solana Tool Suite
```bash
# Install Solana tools
sh -c "$(curl -sSfL https://release.solana.com/v1.16.0/install)"

# Add Solana to your PATH (add this to your .bashrc or .zshrc)
export PATH="$HOME/.local/share/solana/install/active_release/bin:$PATH"

# Verify installation
solana --version
```

### 1.3 Install Solana BPF Toolchain
```bash
# This allows building programs for the Solana blockchain
rustup component add rust-src
solana-install install 1.16.0
```

## 2. Setting Up Node.js Environment

The deployment scripts and applications require Node.js:

### 2.1 Install Node.js and npm
```bash
# Using NVM (recommended for managing Node.js versions)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

# Reload your shell configuration
source ~/.bashrc  # or source ~/.zshrc for Zsh users

# Install and use the latest LTS version of Node.js
nvm install --lts
nvm use --lts

# Verify installation
node --version  # Should show v16+ for compatibility
npm --version   # Should show v8+
```

### 2.2 Install Project Dependencies
```bash
# Navigate to the scripts directory
cd scripts

# Install dependencies
npm install @solana/web3.js @solana/spl-token

# Navigate to the backend directory
cd ../backend

# Install backend dependencies
npm install

# Navigate to the frontend directory (if applicable)
cd ../frontend

# Install frontend dependencies
npm install
```

## 3. Setting Up Android Development Environment (Optional)

If you plan to build the Android app:

### 3.1 Install Android Studio
1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Follow the installation instructions for your operating system
3. Install the Android SDK via Android Studio's SDK Manager
4. Set up an Android emulator or connect a physical device for testing

## 4. Environment Configuration

### 4.1 Solana Network Configuration
```bash
# Configure Solana to use the development network by default
solana config set --url https://api.devnet.solana.com

# Create a development wallet for testing (optional, scripts can create one)
solana-keygen new --no-bip39-passphrase -o devwallet.json

# Airdrop some SOL to your development wallet
solana airdrop 2 $(solana-keygen pubkey devwallet.json) --url https://api.devnet.solana.com
```

### 4.2 Environment Variables
Create appropriate `.env` files for each component:

For Backend:
```bash
# Create backend .env file
cat > backend/.env << EOL
PORT=5000
SOLANA_NETWORK=devnet
OPENAI_API_KEY=your_openai_key_here
DEEPSEEK_API_KEY=your_deepseek_key_here
JWT_SECRET=your_jwt_secret_here
EOL
```

For Scripts (if needed):
```bash
# Create scripts .env file
cat > scripts/.env << EOL
SOLANA_NETWORK=devnet
EOL
```

## 5. Building the Contract

```bash
# Navigate to the contract directory
cd contract

# Build the contract
cargo build-bpf

# Run tests
cargo test -- --nocapture
```

## 6. Running the Project

### 6.1 Running the Backend Server
```bash
# Navigate to backend directory
cd backend

# Install dependencies (if not done earlier)
npm install

# Start the development server
npm run dev
```

### 6.2 Running the Frontend (Web)
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (if not done earlier)
npm install

# Start the development server
npm start
```

### 6.3 Running the Android App
```bash
# Navigate to app directory
cd app

# Open with Android Studio and run from there
# OR
./gradlew installDebug  # to build and install on connected device
```

## 7. Deploying the Token

Once your development environment is set up, you can deploy the token:

```bash
# Navigate to scripts directory
cd scripts

# Run the deployment script
node deploy_token.js

# Follow the interactive prompts in the script
```

## 8. Troubleshooting

### Common Issues and Solutions

#### Rust Toolchain Issues
```bash
# Update Rust
rustup update

# Clean and rebuild
cargo clean
cargo build-bpf
```

#### Solana Connection Issues
```bash
# Check your network connection and Solana RPC status
curl -X POST -H "Content-Type: application/json" -d '{"jsonrpc":"2.0","id":1,"method":"getHealth"}' https://api.devnet.solana.com

# If devnet is having issues, try:
solana config set --url https://api.testnet.solana.com
```

#### Node.js Dependency Issues
```bash
# Clear npm cache and reinstall
npm cache clean --force
rm -rf node_modules
npm install
```

## 9. Next Steps

After installation:

1. Review the project documentation in the docs directory
2. Explore the token contract code
3. Try deploying a test token on devnet
4. Start developing new features

## 10. Additional Resources

- [Solana Documentation](https://docs.solana.com/)
- [Rust Book](https://doc.rust-lang.org/book/)
- [SPL Token Documentation](https://spl.solana.com/token)
- [Solana Web3.js Documentation](https://solana-labs.github.io/solana-web3.js/)

---

*Last Updated: November 28, 2024*

If you encounter any issues during installation or setup, please create an issue in the GitHub repository. 