/**
 * Script to deploy the ARIA token on Solana
 * This script handles:
 * 1. Creating a new SPL token
 * 2. Initializing the mint
 * 3. Creating token accounts
 * 4. Minting the initial supply
 */

const {
  Connection,
  Keypair,
  PublicKey,
  Transaction,
  SystemProgram,
  sendAndConfirmTransaction,
} = require('@solana/web3.js');
const {
  Token,
  TOKEN_PROGRAM_ID,
  ASSOCIATED_TOKEN_PROGRAM_ID,
  MintLayout,
} = require('@solana/spl-token');
const fs = require('fs');
const path = require('path');

// Network configuration (change to 'mainnet-beta' for production)
const NETWORK = 'devnet';
const CLUSTER_URL = `https://api.${NETWORK}.solana.com`;

// Token configuration
const TOKEN_DECIMALS = 9;
const TOTAL_SUPPLY = 100_000_000; // 100 million tokens

async function main() {
  console.log('--- ARIA TOKEN DEPLOYMENT ---');
  console.log(`Network: ${NETWORK}`);
  
  // Load or create deployer wallet
  let deployer;
  const walletPath = path.join(__dirname, 'deployer-wallet.json');
  
  try {
    if (fs.existsSync(walletPath)) {
      const walletData = fs.readFileSync(walletPath, 'utf-8');
      deployer = Keypair.fromSecretKey(
        Uint8Array.from(JSON.parse(walletData))
      );
      console.log(`Using existing wallet: ${deployer.publicKey.toString()}`);
    } else {
      deployer = Keypair.generate();
      fs.writeFileSync(
        walletPath,
        JSON.stringify(Array.from(deployer.secretKey))
      );
      console.log(`Generated new wallet: ${deployer.publicKey.toString()}`);
    }
  } catch (err) {
    console.error('Error with wallet:', err);
    return;
  }

  // Connect to the Solana network
  const connection = new Connection(CLUSTER_URL, 'confirmed');
  
  // Check account balance
  const balance = await connection.getBalance(deployer.publicKey);
  console.log(`Wallet balance: ${balance / 1_000_000_000} SOL`);
  
  if (balance < 1_000_000_000) {
    console.log('Warning: Low balance. Please fund your wallet with SOL to proceed.');
    if (NETWORK === 'devnet') {
      console.log('Requesting airdrop...');
      const signature = await connection.requestAirdrop(deployer.publicKey, 1_000_000_000);
      await connection.confirmTransaction(signature);
      console.log('Airdrop received!');
    } else {
      return;
    }
  }

  try {
    // Create a new token mint
    console.log('Creating token mint...');
    const mintAccount = Keypair.generate();
    console.log(`Token mint address: ${mintAccount.publicKey.toString()}`);
    
    // Save the mint address for later use
    fs.writeFileSync(
      path.join(__dirname, 'mint-address.json'),
      JSON.stringify({ address: mintAccount.publicKey.toString() })
    );

    // Calculate the rent-exempt reserve for the mint
    const mintRent = await connection.getMinimumBalanceForRentExemption(
      MintLayout.span
    );

    // Create a transaction to allocate space for the mint
    const createMintTx = new Transaction().add(
      SystemProgram.createAccount({
        fromPubkey: deployer.publicKey,
        newAccountPubkey: mintAccount.publicKey,
        lamports: mintRent,
        space: MintLayout.span,
        programId: TOKEN_PROGRAM_ID,
      })
    );

    // Initialize the mint
    createMintTx.add(
      Token.createInitMintInstruction(
        TOKEN_PROGRAM_ID,
        mintAccount.publicKey,
        TOKEN_DECIMALS,
        deployer.publicKey, // mint authority
        deployer.publicKey  // freeze authority (optional)
      )
    );

    // Send the transaction
    await sendAndConfirmTransaction(
      connection,
      createMintTx,
      [deployer, mintAccount],
      { commitment: 'confirmed' }
    );
    console.log('Token mint created and initialized!');

    // Create a token account for the deployer
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

    // Mint the total supply to the deployer
    console.log(`Minting ${TOTAL_SUPPLY} tokens to deployer...`);
    await token.mintTo(
      deployerTokenAccount.address,
      deployer,
      [],
      TOTAL_SUPPLY * (10 ** TOKEN_DECIMALS)
    );
    
    // Check the balance to confirm
    const tokenBalance = await token.getAccountInfo(deployerTokenAccount.address);
    console.log(`Token balance: ${tokenBalance.amount / (10 ** TOKEN_DECIMALS)} ARI`);
    
    console.log('--- TOKEN DEPLOYMENT COMPLETE ---');
    console.log('Token details:');
    console.log(`- Name: ARIA Token (ARI)`);
    console.log(`- Mint address: ${mintAccount.publicKey.toString()}`);
    console.log(`- Decimals: ${TOKEN_DECIMALS}`);
    console.log(`- Total supply: ${TOTAL_SUPPLY}`);
    console.log(`- Owner: ${deployer.publicKey.toString()}`);
    
    // Instructions for next steps
    console.log('\nNext steps:');
    console.log('1. Add the token to a wallet using the mint address');
    console.log('2. Run the pump_fun_listing.js script to set up the listing');
    console.log('3. Distribute tokens according to the tokenomics plan');
    
  } catch (error) {
    console.error('Deployment failed:', error);
  }
}

main().catch(console.error); 