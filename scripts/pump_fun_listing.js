/**
 * Script to list the ARIA token on pump.fun
 * This script helps with preparing the token for listing on pump.fun
 */

const {
  Connection,
  Keypair,
  PublicKey,
  Transaction,
  sendAndConfirmTransaction,
} = require('@solana/web3.js');
const {
  Token,
  TOKEN_PROGRAM_ID,
} = require('@solana/spl-token');
const fs = require('fs');
const path = require('path');
const readline = require('readline');

// Network configuration
const NETWORK = 'mainnet-beta'; // pump.fun requires mainnet
const CLUSTER_URL = `https://api.${NETWORK}.solana.com`;

// Create readline interface for user input
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

// Helper function to prompt for input
const question = (query) => new Promise((resolve) => rl.question(query, resolve));

async function main() {
  console.log('--- ARIA TOKEN PUMP.FUN LISTING PREPARATION ---');
  console.log(`Network: ${NETWORK}`);
  
  // Load deployer wallet
  let deployer;
  const walletPath = path.join(__dirname, 'deployer-wallet.json');
  
  try {
    if (fs.existsSync(walletPath)) {
      const walletData = fs.readFileSync(walletPath, 'utf-8');
      deployer = Keypair.fromSecretKey(
        Uint8Array.from(JSON.parse(walletData))
      );
      console.log(`Using wallet: ${deployer.publicKey.toString()}`);
    } else {
      console.error('Deployer wallet not found. Please run deploy_token.js first.');
      return;
    }
  } catch (err) {
    console.error('Error with wallet:', err);
    return;
  }

  // Load token mint address
  let mintAddress;
  const mintAddressPath = path.join(__dirname, 'mint-address.json');
  
  try {
    if (fs.existsSync(mintAddressPath)) {
      const mintData = JSON.parse(fs.readFileSync(mintAddressPath, 'utf-8'));
      mintAddress = new PublicKey(mintData.address);
      console.log(`Using token mint: ${mintAddress.toString()}`);
    } else {
      mintAddress = new PublicKey(await question('Enter your token mint address: '));
    }
  } catch (err) {
    console.error('Error with mint address:', err);
    return;
  }

  // Connect to the Solana network
  const connection = new Connection(CLUSTER_URL, 'confirmed');
  
  // Check account balance
  const balance = await connection.getBalance(deployer.publicKey);
  console.log(`Wallet balance: ${balance / 1_000_000_000} SOL`);
  
  if (balance < 0.1 * 1_000_000_000) {
    console.error('Warning: Low balance. Please fund your wallet with SOL to proceed.');
    return;
  }

  try {
    // Initialize the token object
    const token = new Token(
      connection,
      mintAddress,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    // Get token supply information
    const mintInfo = await token.getMintInfo();
    console.log('\nToken Information:');
    console.log(`- Decimals: ${mintInfo.decimals}`);
    console.log(`- Supply: ${mintInfo.supply / (10 ** mintInfo.decimals)}`);
    
    console.log('\n--- PUMP.FUN LISTING INSTRUCTIONS ---');
    console.log('To list your token on pump.fun:');
    console.log('1. Visit https://pump.fun/create');
    console.log(`2. Enter the token mint address: ${mintAddress.toString()}`);
    console.log('3. Complete the following information:');
    console.log('   - Token Name: ARIA Token');
    console.log('   - Token Symbol: ARI');
    console.log('   - Description: AI Personal Assistant on Solana Blockchain');
    console.log('   - Logo: Upload the ARIA logo from your assets folder');
    console.log('   - Website: Your project website URL');
    console.log('   - Twitter: Your project Twitter handle');
    console.log('   - Discord: Your project Discord invite');
    
    console.log('\n--- LIQUIDITY POOL SETUP ---');
    console.log('To ensure successful trading on pump.fun:');
    console.log('1. Allocate at least 20% of total supply for liquidity');
    console.log('2. Create a liquidity pool with SOL/ARI pair');
    console.log('3. Set a reasonable initial price considering market conditions');
    
    // Get LP allocation information from user
    const lpAllocationPercent = await question('What percentage of tokens will you allocate to liquidity (recommended 20-30%)? ');
    const totalSupply = mintInfo.supply / (10 ** mintInfo.decimals);
    const lpAllocation = totalSupply * (parseInt(lpAllocationPercent) / 100);
    
    console.log(`\nBased on ${lpAllocationPercent}% allocation, you should provide ${lpAllocation} ARI tokens for liquidity.`);
    
    // Prepare LP instructions
    console.log('\n--- LP SETUP CHECKLIST ---');
    console.log('☐ Transfer LP allocation tokens to a separate wallet');
    console.log('☐ Prepare SOL for initial liquidity (recommended 5-10 SOL)');
    console.log('☐ Create liquidity pool on Raydium or Orca');
    console.log('☐ Lock liquidity for at least 3 months for community trust');
    console.log('☐ Announce pool creation on your social channels');
    
    console.log('\n--- MARKETING PREPARATION ---');
    console.log('For a successful launch, prepare:');
    console.log('☐ Announcement posts for Twitter, Discord, Telegram');
    console.log('☐ Graphics showing pump.fun listing details');
    console.log('☐ AMAs scheduled around the launch time');
    console.log('☐ Community contests or airdrops to increase engagement');
    
    console.log('\nGood luck with your pump.fun listing!');
    
  } catch (error) {
    console.error('Error:', error);
  }
  
  rl.close();
}

main().catch(console.error); 