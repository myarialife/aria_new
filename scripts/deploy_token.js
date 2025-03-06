/**
 * ARIA Token Deployment Script
 * This script handles the following tasks:
 * 1. Creating a new SPL token
 * 2. Initializing the mint
 * 3. Creating token accounts
 * 4. Minting initial supply
 * 5. Saving deployment records for later use
 */

const {
  Connection,
  Keypair,
  PublicKey,
  Transaction,
  SystemProgram,
  sendAndConfirmTransaction,
  clusterApiUrl,
} = require('@solana/web3.js');
const {
  Token,
  TOKEN_PROGRAM_ID,
  ASSOCIATED_TOKEN_PROGRAM_ID,
  MintLayout,
} = require('@solana/spl-token');
const fs = require('fs');
const path = require('path');
const readline = require('readline');

// Create readline interface for user input
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

// Helper function for prompting input
const question = (query) => new Promise((resolve) => rl.question(query, resolve));

// Network configuration
let NETWORK = 'devnet'; // Default to development network

// Token configuration
const TOKEN_NAME = "ARIA Token";
const TOKEN_SYMBOL = "ARI";
const TOKEN_DECIMALS = 9;
const TOTAL_SUPPLY = 100_000_000; // 100 million tokens
const TOKEN_LOGO_URL = "https://raw.githubusercontent.com/your-repo/ARIA/main/assets/logo.png"; // Update to actual logo URL

// Transaction logging information
const deploymentLog = {
  timestamp: new Date().toISOString(),
  network: NETWORK,
  transactions: [],
  token: {
    name: TOKEN_NAME,
    symbol: TOKEN_SYMBOL,
    decimals: TOKEN_DECIMALS,
    totalSupply: TOTAL_SUPPLY,
    logoUrl: TOKEN_LOGO_URL
  }
};

// Log transaction
function logTransaction(type, signature, details = {}) {
  const transactionEntry = {
    type,
    signature,
    timestamp: new Date().toISOString(),
    ...details
  };
  
  deploymentLog.transactions.push(transactionEntry);
  
  // Save log
  fs.writeFileSync(
    path.join(__dirname, 'deployment-log.json'),
    JSON.stringify(deploymentLog, null, 2)
  );
}

async function main() {
  console.log('--- ARIA TOKEN DEPLOYMENT ---');
  
  // Ask which network to use
  try {
    const networkInput = await question('Select network (devnet/mainnet, default is devnet): ');
    if (networkInput && (networkInput.toLowerCase() === 'mainnet' || networkInput.toLowerCase() === 'mainnet-beta')) {
      NETWORK = 'mainnet-beta';
    }
  } catch (err) {
    console.log('Using default network: devnet');
  }
  
  deploymentLog.network = NETWORK;
  console.log(`Network: ${NETWORK}`);
  
  // Confirm mint amount
  const confirmSupply = await question(`Confirm token total supply of ${TOTAL_SUPPLY} ${TOKEN_SYMBOL}? (y/n, default is y): `);
  if (confirmSupply.toLowerCase() === 'n') {
    rl.close();
    return;
  }
  
  // Load or create deployer wallet
  let deployer;
  const walletPath = path.join(__dirname, `deployer-wallet-${NETWORK}.json`);
  
  try {
    if (fs.existsSync(walletPath)) {
      const walletData = fs.readFileSync(walletPath, 'utf-8');
      deployer = Keypair.fromSecretKey(
        Uint8Array.from(JSON.parse(walletData))
      );
      console.log(`Using existing wallet: ${deployer.publicKey.toString()}`);
    } else {
      console.log('Creating new deployment wallet...');
      deployer = Keypair.generate();
      fs.writeFileSync(
        walletPath,
        JSON.stringify(Array.from(deployer.secretKey))
      );
      console.log(`Generated new wallet: ${deployer.publicKey.toString()}`);
    }
    
    deploymentLog.wallet = deployer.publicKey.toString();
  } catch (err) {
    console.error('Wallet handling error:', err);
    rl.close();
    return;
  }

  // Connect to Solana network
  const CLUSTER_URL = NETWORK === 'mainnet-beta' ? clusterApiUrl('mainnet-beta') : clusterApiUrl('devnet');
  const connection = new Connection(CLUSTER_URL, 'confirmed');
  
  // Check account balance
  try {
    const balance = await connection.getBalance(deployer.publicKey);
    console.log(`Wallet balance: ${balance / 1_000_000_000} SOL`);
    
    if (balance < 1_000_000_000) {
      console.log('Warning: Low balance. Please add SOL to your wallet to continue.');
      if (NETWORK === 'devnet') {
        console.log('Requesting airdrop...');
        const signature = await connection.requestAirdrop(deployer.publicKey, 2_000_000_000);
        await connection.confirmTransaction(signature);
        const newBalance = await connection.getBalance(deployer.publicKey);
        console.log(`Balance after airdrop: ${newBalance / 1_000_000_000} SOL`);
        logTransaction('airdrop', signature, { amount: 2 });
      } else {
        console.log('Please add SOL to this address and run the script again:');
        console.log(deployer.publicKey.toString());
        rl.close();
        return;
      }
    }
  } catch (error) {
    console.error('Unable to get balance:', error);
    rl.close();
    return;
  }

  try {
    // Create new token mint account
    console.log('Creating token mint account...');
    const mintAccount = Keypair.generate();
    console.log(`Token mint address: ${mintAccount.publicKey.toString()}`);
    
    // Save mint address for later use
    fs.writeFileSync(
      path.join(__dirname, `${TOKEN_SYMBOL.toLowerCase()}-mint-address-${NETWORK}.json`),
      JSON.stringify({ 
        address: mintAccount.publicKey.toString(),
        network: NETWORK,
        name: TOKEN_NAME,
        symbol: TOKEN_SYMBOL,
        decimals: TOKEN_DECIMALS
      })
    );

    // Calculate minimum rent exemption for mint
    const mintRent = await connection.getMinimumBalanceForRentExemption(
      MintLayout.span
    );

    // Create transaction to allocate space for mint
    const createMintTx = new Transaction().add(
      SystemProgram.createAccount({
        fromPubkey: deployer.publicKey,
        newAccountPubkey: mintAccount.publicKey,
        lamports: mintRent,
        space: MintLayout.span,
        programId: TOKEN_PROGRAM_ID,
      })
    );

    // Initialize mint
    createMintTx.add(
      Token.createInitMintInstruction(
        TOKEN_PROGRAM_ID,
        mintAccount.publicKey,
        TOKEN_DECIMALS,
        deployer.publicKey, // mint authority
        deployer.publicKey  // freeze authority (optional)
      )
    );

    // Send transaction
    console.log('Sending transaction to create and initialize token...');
    const createMintSignature = await sendAndConfirmTransaction(
      connection,
      createMintTx,
      [deployer, mintAccount],
      { commitment: 'confirmed' }
    );
    
    console.log(`Transaction confirmed: ${createMintSignature}`);
    logTransaction('create_mint', createMintSignature, { mintAddress: mintAccount.publicKey.toString() });
    console.log('Token mint created and initialized!');

    // Create token account for deployer
    console.log('Creating token account for deployer...');
    const token = new Token(
      connection,
      mintAccount.publicKey,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    const deployerTokenAccount = await token.getOrCreateAssociatedAccountInfo(
      deployer.publicKey
    );
    
    console.log(`Deployer token account: ${deployerTokenAccount.address.toString()}`);
    logTransaction('create_token_account', 'N/A', { tokenAccount: deployerTokenAccount.address.toString() });

    // Mint total supply to deployer
    console.log(`Minting ${TOTAL_SUPPLY} tokens to deployer account...`);
    const mintSignature = await token.mintTo(
      deployerTokenAccount.address,
      deployer,
      [],
      TOTAL_SUPPLY * (10 ** TOKEN_DECIMALS)
    );
    console.log(`Mint transaction confirmed: ${mintSignature}`);
    logTransaction('mint_tokens', mintSignature, { amount: TOTAL_SUPPLY });
    
    // Check balance to confirm
    const tokenBalance = await token.getAccountInfo(deployerTokenAccount.address);
    console.log(`Token balance: ${tokenBalance.amount / (10 ** TOKEN_DECIMALS)} ${TOKEN_SYMBOL}`);
    
    console.log('--- TOKEN DEPLOYMENT COMPLETE ---');
    console.log('Token details:');
    console.log(`- Name: ${TOKEN_NAME} (${TOKEN_SYMBOL})`);
    console.log(`- Mint address: ${mintAccount.publicKey.toString()}`);
    console.log(`- Decimals: ${TOKEN_DECIMALS}`);
    console.log(`- Total supply: ${TOTAL_SUPPLY}`);
    console.log(`- Owner: ${deployer.publicKey.toString()}`);
    
    // Next steps instructions
    console.log('\nNext steps:');
    console.log('1. Add the token to a wallet using the mint address');
    console.log('2. Run the pump_fun_listing.js script to set up the listing');
    console.log('3. Distribute tokens according to the tokenomics plan');
    
    if (NETWORK === 'mainnet-beta') {
      console.log('\nSteps to add token to pump.fun:');
      console.log('1. Visit https://pump.fun/create');
      console.log('2. Fill in the following details:');
      console.log(`   - Token mint address: ${mintAccount.publicKey.toString()}`);
      console.log(`   - Name: ${TOKEN_NAME}`);
      console.log(`   - Symbol: ${TOKEN_SYMBOL}`);
      console.log('   - Description: AI Personal Assistant on Solana Blockchain');
      console.log('   - Logo: Upload project logo');
      console.log('   - Website: Your project website');
      console.log('   - Twitter: Your project Twitter account');
      console.log('   - Discord: Your project Discord invitation link');
      console.log('3. Prepare enough tokens for liquidity pool');
    }
    
  } catch (error) {
    console.error('Deployment failed:', error);
    logTransaction('error', 'N/A', { error: error.message });
  }
  
  rl.close();
}

main().catch(err => {
  console.error('Runtime error:', err);
  rl.close();
}); 