/**
 * ARIA Token Devnet Integration Test Script
 * This script tests all token contract functionality on Solana devnet
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

// Network configuration - only using devnet for testing
const NETWORK = 'devnet';
const CLUSTER_URL = clusterApiUrl('devnet');

// Token configuration - test values
const TOKEN_NAME = "ARIA Test Token";
const TOKEN_SYMBOL = "tARI";
const TOKEN_DECIMALS = 9;
const TOTAL_SUPPLY = 1_000_000; // 1 million test tokens

// Test log
const testLog = {
  timestamp: new Date().toISOString(),
  network: NETWORK,
  tests: []
};

// Log test result
function logTest(name, success, details = {}) {
  const testCase = {
    name,
    success,
    timestamp: new Date().toISOString(),
    ...details
  };
  
  testLog.tests.push(testCase);
  
  // Print test result
  if (success) {
    console.log(`✅ Test passed: ${name}`);
  } else {
    console.log(`❌ Test failed: ${name}`);
    if (details.error) {
      console.log(`   Error: ${details.error}`);
    }
  }
  
  // Save log
  fs.writeFileSync(
    path.join(__dirname, 'devnet-test-log.json'),
    JSON.stringify(testLog, null, 2)
  );
}

// Delay function
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function main() {
  console.log('=== ARIA TOKEN DEVNET INTEGRATION TEST ===');
  console.log(`Network: ${NETWORK}`);
  
  // Create test wallet
  let deployer;
  const walletPath = path.join(__dirname, `test-wallet-${NETWORK}.json`);
  
  try {
    if (fs.existsSync(walletPath)) {
      const walletData = fs.readFileSync(walletPath, 'utf-8');
      deployer = Keypair.fromSecretKey(
        Uint8Array.from(JSON.parse(walletData))
      );
      console.log(`Using test wallet: ${deployer.publicKey.toString()}`);
    } else {
      console.log('Creating new test wallet...');
      deployer = Keypair.generate();
      fs.writeFileSync(
        walletPath,
        JSON.stringify(Array.from(deployer.secretKey))
      );
      console.log(`Generated test wallet: ${deployer.publicKey.toString()}`);
    }
  } catch (err) {
    console.error('Wallet handling error:', err);
    rl.close();
    return;
  }

  // Connect to Solana devnet
  console.log('Connecting to Solana devnet...');
  const connection = new Connection(CLUSTER_URL, 'confirmed');
  
  // Request SOL airdrop for test wallet
  try {
    const balance = await connection.getBalance(deployer.publicKey);
    console.log(`Current wallet balance: ${balance / 1_000_000_000} SOL`);
    
    if (balance < 1_000_000_000) {
      console.log('Requesting SOL airdrop...');
      try {
        const signature = await connection.requestAirdrop(deployer.publicKey, 2_000_000_000);
        await connection.confirmTransaction(signature);
        const newBalance = await connection.getBalance(deployer.publicKey);
        console.log(`Balance after airdrop: ${newBalance / 1_000_000_000} SOL`);
        logTest('SOL Airdrop Test', true, { signature });
      } catch (e) {
        console.log('Airdrop request failed, trying second request...');
        try {
          const signature = await connection.requestAirdrop(deployer.publicKey, 1_000_000_000);
          await connection.confirmTransaction(signature);
          const newBalance = await connection.getBalance(deployer.publicKey);
          console.log(`Balance after airdrop: ${newBalance / 1_000_000_000} SOL`);
          logTest('SOL Airdrop Test (retry)', true, { signature });
        } catch (e) {
          logTest('SOL Airdrop Test', false, { error: e.message });
          console.error('Unable to get SOL airdrop. Please manually send SOL to test wallet.');
          console.log('Test wallet address: ' + deployer.publicKey.toString());
          
          const proceed = await question('Continue testing anyway? (y/n): ');
          if (proceed.toLowerCase() !== 'y') {
            rl.close();
            return;
          }
        }
      }
    }
  } catch (e) {
    logTest('Connection Test', false, { error: e.message });
    console.error('Unable to connect to Solana devnet:', e);
    rl.close();
    return;
  }

  // Test 1: Create token mint account
  console.log('\n--- Test 1: Create Token Mint Account ---');
  let mintAccount;
  try {
    mintAccount = Keypair.generate();
    console.log(`Generated token mint address: ${mintAccount.publicKey.toString()}`);
    
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
    
    console.log(`Mint transaction successful: ${createMintSignature}`);
    logTest('Create Token Mint Account', true, { signature: createMintSignature, mint: mintAccount.publicKey.toString() });
  } catch (e) {
    logTest('Create Token Mint Account', false, { error: e.message });
    console.error('Failed to create token mint account:', e);
    rl.close();
    return;
  }
  
  // Wait for confirmation
  await sleep(2000);
  
  // Test 2: Create token account
  console.log('\n--- Test 2: Create Token Account ---');
  let tokenAccount;
  try {
    // Initialize Token object
    const token = new Token(
      connection,
      mintAccount.publicKey,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    // Create token account for deployer
    console.log('Creating token account for test wallet...');
    const deployerTokenAccount = await token.getOrCreateAssociatedAccountInfo(
      deployer.publicKey
    );
    
    console.log(`Token account created successfully: ${deployerTokenAccount.address.toString()}`);
    tokenAccount = deployerTokenAccount;
    logTest('Create Token Account', true, { tokenAccount: deployerTokenAccount.address.toString() });
  } catch (e) {
    logTest('Create Token Account', false, { error: e.message });
    console.error('Failed to create token account:', e);
  }
  
  // Wait for confirmation
  await sleep(2000);
  
  // Test 3: Mint tokens
  console.log('\n--- Test 3: Mint Tokens ---');
  try {
    // Initialize Token object
    const token = new Token(
      connection,
      mintAccount.publicKey,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    // Mint tokens
    console.log(`Minting ${TOTAL_SUPPLY} tokens to test account...`);
    const mintSignature = await token.mintTo(
      tokenAccount.address,
      deployer,
      [],
      TOTAL_SUPPLY * (10 ** TOKEN_DECIMALS)
    );
    
    console.log(`Mint transaction successful: ${mintSignature}`);
    
    // Verify balance after minting
    const tokenBalance = await token.getAccountInfo(tokenAccount.address);
    console.log(`Token balance: ${tokenBalance.amount / (10 ** TOKEN_DECIMALS)} ${TOKEN_SYMBOL}`);
    
    // Verify balance is correct
    const expectedAmount = TOTAL_SUPPLY * (10 ** TOKEN_DECIMALS);
    if (tokenBalance.amount.toString() === expectedAmount.toString()) {
      logTest('Mint Tokens', true, { 
        signature: mintSignature, 
        amount: TOTAL_SUPPLY,
        balance: tokenBalance.amount.toString() 
      });
    } else {
      logTest('Mint Tokens - Balance Verification', false, { 
        signature: mintSignature,
        expectedAmount: expectedAmount.toString(),
        actualAmount: tokenBalance.amount.toString()
      });
    }
  } catch (e) {
    logTest('Mint Tokens', false, { error: e.message });
    console.error('Failed to mint tokens:', e);
  }
  
  // Wait for confirmation
  await sleep(2000);
  
  // Test 4: Create second recipient account
  console.log('\n--- Test 4: Create Second Recipient Account ---');
  let receiver;
  let receiverTokenAccount;
  
  try {
    // Create receiver wallet
    receiver = Keypair.generate();
    console.log(`Generated receiver wallet: ${receiver.publicKey.toString()}`);
    
    // Provide some SOL to receiver wallet
    const transferSolTx = new Transaction().add(
      SystemProgram.transfer({
        fromPubkey: deployer.publicKey,
        toPubkey: receiver.publicKey,
        lamports: 10_000_000 // 0.01 SOL
      })
    );
    
    const transferSolSignature = await sendAndConfirmTransaction(
      connection,
      transferSolTx,
      [deployer],
      { commitment: 'confirmed' }
    );
    
    console.log(`SOL transfer transaction successful: ${transferSolSignature}`);
    
    // Initialize Token object
    const token = new Token(
      connection,
      mintAccount.publicKey,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    // Create token account for receiver
    console.log('Creating token account for receiver wallet...');
    receiverTokenAccount = await token.getOrCreateAssociatedAccountInfo(
      receiver.publicKey
    );
    
    console.log(`Receiver account created successfully: ${receiverTokenAccount.address.toString()}`);
    logTest('Create Second Recipient Account', true, { 
      receiver: receiver.publicKey.toString(),
      tokenAccount: receiverTokenAccount.address.toString() 
    });
  } catch (e) {
    logTest('Create Second Recipient Account', false, { error: e.message });
    console.error('Failed to create second recipient account:', e);
  }
  
  // Wait for confirmation
  await sleep(2000);
  
  // Test 5: Transfer tokens
  console.log('\n--- Test 5: Transfer Tokens ---');
  try {
    // Initialize Token object
    const token = new Token(
      connection,
      mintAccount.publicKey,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    // Token transfer amount
    const transferAmount = 1000 * (10 ** TOKEN_DECIMALS);
    
    // Transfer tokens
    console.log(`Transferring 1,000 tokens to receiver account...`);
    const transferSignature = await token.transfer(
      tokenAccount.address,
      receiverTokenAccount.address,
      deployer,
      [],
      transferAmount
    );
    
    console.log(`Transfer transaction successful: ${transferSignature}`);
    
    // Verify balance after transfer
    const receiverBalance = await token.getAccountInfo(receiverTokenAccount.address);
    console.log(`Receiver account balance: ${receiverBalance.amount / (10 ** TOKEN_DECIMALS)} ${TOKEN_SYMBOL}`);
    
    // Verify balance is correct
    if (receiverBalance.amount.toString() === transferAmount.toString()) {
      logTest('Transfer Tokens', true, { 
        signature: transferSignature, 
        amount: transferAmount.toString(),
        receiverBalance: receiverBalance.amount.toString() 
      });
    } else {
      logTest('Transfer Tokens - Balance Verification', false, { 
        signature: transferSignature,
        expectedAmount: transferAmount.toString(),
        actualAmount: receiverBalance.amount.toString()
      });
    }
  } catch (e) {
    logTest('Transfer Tokens', false, { error: e.message });
    console.error('Failed to transfer tokens:', e);
  }
  
  // Wait for confirmation
  await sleep(2000);
  
  // Test complete
  console.log('\n=== TESTING COMPLETE ===');
  const passedTests = testLog.tests.filter(test => test.success).length;
  const totalTests = testLog.tests.length;
  
  console.log(`Tests passed: ${passedTests}/${totalTests}`);
  console.log(`Success rate: ${Math.round((passedTests / totalTests) * 100)}%`);
  
  if (passedTests === totalTests) {
    console.log('All tests passed! 🎉');
  } else {
    console.log('Some tests failed, please check error log.');
  }
  
  rl.close();
}

// Run the main function
main().catch(err => {
  console.error('Runtime error:', err);
  rl.close();
}); 