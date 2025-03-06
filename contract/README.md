# ARIA Token - Solana SPL Contract

This directory contains the Solana smart contract implementation for the ARIA token. ARIA is the native token for a decentralized AI personal assistant project based on the Solana blockchain.

## Token Overview

- **Name**: ARIA Token
- **Symbol**: ARI
- **Decimals**: 9
- **Total Supply**: 100,000,000 (100 million)
- **Type**: Solana SPL Token

## Contract Features

The ARIA token contract implements the following core functions:

1. **Token Initialization**: Create SPL token and set token parameters (name, symbol, decimals)
2. **Token Minting**: Mint tokens to specified accounts
3. **Authority Transfer**: Transfer minting authority to new management accounts

## Technical Architecture

The contract is written in Rust, based on the Solana program development framework, with the following technical components:

- **solana-program**: Solana core program library
- **spl-token**: Solana token program library
- **borsh**: Serialization/deserialization library
- **thiserror**: Error handling library

## Contract Structure

```
contract/
├── Cargo.toml          # Rust project configuration and dependencies
└── src/
    └── lib.rs          # Contract main implementation code
```

## Instruction Set

The contract implements three main instructions:

### 1. InitializeMint

Initialize the token mint account and set mint and freeze authorities.

**Parameters**:
- None

**Required Accounts**:
- `[signer]` Authority account that will mint tokens
- `[writable]` The mint account to initialize
- `[]` The rent sysvar
- `[]` The token program

### 2. MintTokens

Mint a specified amount of tokens to a target account.

**Parameters**:
- `amount`: Amount of tokens to mint

**Required Accounts**:
- `[signer]` Mint authority account
- `[writable]` The mint account
- `[writable]` The destination account
- `[]` The token program

### 3. TransferAuthority

Transfer minting authority to a new account.

**Parameters**:
- None

**Required Accounts**:
- `[signer]` Current mint authority account
- `[writable]` The mint account
- `[]` New mint authority account
- `[]` The token program

## Security Design

The contract includes multiple layers of security:

1. **Signer Verification**: All key operations require authority account signature
2. **Account Verification**: Validate account ownership and type
3. **Error Handling**: Comprehensive error handling and return information
4. **Authority Separation**: Support separation of mint and freeze authorities

## Building and Deployment

### Prerequisites

- Rust 1.65+
- Solana command line tools 1.16+
- NodeJS 16+ (for deployment scripts)

### Building the Contract

```bash
# Navigate to contract directory
cd contract

# Build the contract
cargo build-bpf
```

### Deploying the Token

We provide automated scripts to deploy the token and create the initial supply:

```bash
# Navigate to scripts directory
cd ../scripts

# Install dependencies
npm install

# Deploy token to devnet
node deploy_token.js

# List on pump.fun
node pump_fun_listing.js
```

## Token Economics

The ARI token is a core component of the ARIA ecosystem:

### Distribution Plan

- **50%** - User incentives (rewards and airdrops)
- **20%** - Development team and operations
- **20%** - Community governance and ecosystem
- **10%** - Marketing and partnerships

### Utility

- **Data Contribution Rewards**: Users earn tokens by contributing anonymous data
- **Premium Feature Access**: Unlock advanced features by staking tokens
- **Governance Participation**: Vote on project development priorities
- **Network Fee Discounts**: Receive discounts on transaction fees

## Pump.fun Listing

The ARIA token is planned to be listed on the pump.fun platform. For detailed listing procedures, refer to the guidance in the `scripts/pump_fun_listing.js` script. The listing will include:

1. Creating token description and social media links
2. Setting up the initial liquidity pool
3. Arranging listing marketing activities
4. Establishing ongoing community engagement plans

## Contribution Guidelines

Contributions to the contract are welcome! Please follow this process:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Security Audit

Before mainnet deployment, we recommend a security audit of the token contract. Recommended audit services include:

- Certik
- Solana Foundation Audit
- Kudelski Security

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support requests, please ask through GitHub Issues.

[![Website](https://img.shields.io/badge/Website-www.myaria.life-blue)](https://www.myaria.life/)
[![Twitter](https://img.shields.io/badge/Twitter-@ARIA__Assistant-blue)](https://x.com/ARIA_Assistant) 