# ARIA - AI Personal Assistant on Solana

<div align="center">
  <a href="https://www.myaria.life">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="assets/logo.svg">
      <source media="(prefers-color-scheme: light)" srcset="assets/logo.svg">
      <img alt="ARIA Logo" src="assets/logo.svg" width="250">
    </picture>
  </a>
</div>

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Website](https://img.shields.io/badge/Website-www.myaria.life-blue)](https://www.myaria.life/)
[![Twitter](https://img.shields.io/badge/Twitter-@ARIA__Assistant-blue)](https://x.com/ARIA_Assistant)

ARIA is a decentralized AI personal assistant built on the Solana blockchain. The project integrates artificial intelligence capabilities with blockchain technology to create a privacy-focused, token-incentivized personal assistant accessible via Android and web applications.

## Overview

ARIA tackles two major challenges in the AI assistant space:
1. **Data Privacy**: Using Solana blockchain for secure, user-controlled data storage
2. **Value Distribution**: Rewarding users for their data contributions through the ARI token

The ARIA ecosystem is powered by the ARI token, which will be launched on pump.fun, a Solana-based token platform.

## Features

- **ARI Token**: SPL token on the Solana blockchain
- **Wallet Integration**: Connect to Solana wallets (Phantom, Solflare)
- **Token Dashboard**: View token balance and transaction history
- **AI Chat Interface**: Full-featured chat with GPT & DeepSeek LLM support
- **Data Sovereignty**: User-controlled data sharing
- **Cross-platform Support**: Android app and web interface

## Technical Architecture

### 1. Android Application (Client Layer)
- Built with Kotlin and Jetpack Compose
- Wallet integration via Mobile Wallet Adapter
- REST API integration with backend services
- Local data processing and encryption

### 2. Web Frontend (Optional Client Layer)
- React.js with modern component architecture
- Solana wallet adapter integration
- Responsive design for all device types

### 3. Backend Services (Processing Layer)
- Node.js Express server
- RESTful API design
- JWT authentication with wallet verification
- Multi-model AI integration (OpenAI, DeepSeek)
- Fallback rule-based responses when API services unavailable

### 4. AI Engine (Intelligence Layer)
- OpenAI GPT integration
- DeepSeek model support
- Custom prompting for ARIA persona
- Expandable to additional models

### 5. Solana Blockchain Integration (Security Layer)
- SPL token implementation (ARI)
- Smart contracts for token economics
- Secure data storage and access control
- Transparent transaction history

## Repository Structure

```
ARIA/
├── README.md                    # Project overview and documentation
├── docs/                        # Documentation
│   ├── whitepaper.md            # Detailed project description
│   └── tokenomics.md            # Token economics specifications
├── contract/                    # Solana smart contracts
│   ├── Cargo.toml               # Rust dependencies
│   └── src/
│       └── lib.rs               # Token contract implementation
├── app/                         # Android application
│   ├── build.gradle             # Project-level build file
│   ├── settings.gradle          # Project settings
│   └── app/
│       ├── build.gradle         # App-level build file
│       └── src/
│           └── main/
│               ├── java/        # Kotlin source files
│               ├── res/         # Android resources
│               └── AndroidManifest.xml
├── frontend/                    # Web frontend (React)
│   ├── package.json             # NPM dependencies
│   ├── public/                  # Static assets
│   └── src/                     # React components and logic
├── backend/                     # Node.js backend server
│   ├── package.json             # NPM dependencies
│   └── src/                     # Server-side code
│       ├── controllers/         # API controllers
│       ├── routes/              # API routes
│       ├── middleware/          # Express middleware
│       ├── utils/               # Utility functions
│       └── index.js             # Server entry point
├── scripts/                     # Deployment and utility scripts
│   ├── deploy_token.js          # Token deployment script
│   └── pump_fun_listing.js      # pump.fun listing setup
└── docker-compose.yml           # Docker configuration
```

## Getting Started

### Prerequisites

- Node.js (v14+)
- Rust and Cargo (latest stable)
- Solana CLI tools (v1.16+)
- Android Studio (latest version)
- Docker & Docker Compose (optional)
- Phantom or Solflare wallet

### Installation and Setup

#### 1. Clone the repository
```bash
git clone https://github.com/yourusername/ARIA.git
cd ARIA
```

#### 2. Deploy the ARI token (for development)
```bash
# Install dependencies
cd scripts
npm install

# Deploy token to Solana devnet
node deploy_token.js
```

#### 3. Set up the backend server
```bash
# Navigate to backend directory
cd ../backend

# Install dependencies
npm install

# Copy example environment file
cp .env.example .env

# Customize environment variables in .env file
# Start the server
npm run dev
```

#### 4. Run the web frontend (optional)
```bash
# Navigate to frontend directory
cd ../frontend

# Install dependencies
npm install

# Start development server
npm start
```

#### 5. Build and run the Android app
```bash
# Navigate to app directory
cd ../app

# Open with Android Studio or build directly
./gradlew installDebug
```

#### 6. Using Docker (alternative)
```bash
# From project root
docker-compose up
```

## Token Economics

The ARI token is the backbone of the ARIA ecosystem:

- **Name**: ARI (ARIA Token)
- **Type**: SPL Token on Solana
- **Total Supply**: 100,000,000 ARI
- **Decimals**: 9

### Distribution

- **50%** - User incentives (rewards and airdrops)
- **20%** - Development team and operations
- **20%** - Community governance and ecosystem
- **10%** - Marketing and partnerships

### Utility

- **Data Contribution Rewards**: Users earn tokens by contributing anonymous data
- **Premium Feature Access**: Unlock advanced features by staking tokens
- **Governance Participation**: Vote on project development priorities
- **Network Fee Discounts**: Receive discounts on transaction fees

## Development Roadmap

### Phase 1: Foundation (Current)
- ARI token deployment on Solana
- Basic Android app with wallet integration
- Initial backend infrastructure
- Simple AI assistant functionality

### Phase 2: Core Features
- Enhanced AI assistant capabilities
- Multiple LLM model support
- Data privacy controls
- Expanded token utility

### Phase 3: Ecosystem Expansion
- Third-party integrations
- Advanced AI features with specialized knowledge domains
- Community governance implementation
- Cross-platform support expansion

## Testing

For testing purposes, the project includes:

1. Unit tests for key components
2. Integration tests for API endpoints
3. Mock implementations for development without external dependencies

Run tests with:
```bash
# Backend tests
cd backend
npm test

# Android tests (using Android Studio or Gradle)
cd app
./gradlew test
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please open an issue on this repository.

## Acknowledgements

- Solana Foundation
- SPL Token Program
- Jetpack Compose
- Mobile Wallet Adapter Protocol 