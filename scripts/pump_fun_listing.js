/**
 * ARIA Token Pump.fun Listing Script
 * This script helps prepare the ARIA token for listing on pump.fun
 * and provides complete guidance and liquidity pool setup suggestions
 */

const {
  Connection,
  Keypair,
  PublicKey,
  Transaction,
  sendAndConfirmTransaction,
  clusterApiUrl,
} = require('@solana/web3.js');
const {
  Token,
  TOKEN_PROGRAM_ID,
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
const NETWORK = 'mainnet-beta'; // pump.fun requires mainnet
const CLUSTER_URL = clusterApiUrl('mainnet-beta');

// Token information
let TOKEN_NAME = "ARIA Token";
let TOKEN_SYMBOL = "ARI";
let TOKEN_DECIMALS = 9;
let MINT_ADDRESS = null;

// Social media link templates
const WEBSITE_URL = "https://aria-project.com"; // Update to your actual website
const TWITTER_HANDLE = "@ARIA_Project"; // Update to your Twitter account
const DISCORD_INVITE = "https://discord.gg/aria-project"; // Update to your Discord invite
const TELEGRAM_GROUP = "https://t.me/ARIA_Project"; // Update to your Telegram group
const GITHUB_REPO = "https://github.com/yourusername/ARIA"; // Update to your GitHub repository

// Log object to record operations
const pumpFunLog = {
  timestamp: new Date().toISOString(),
  operations: []
};

// Record operation
function logOperation(type, details = {}) {
  pumpFunLog.operations.push({
    type,
    timestamp: new Date().toISOString(),
    ...details
  });
  
  // Save log
  fs.writeFileSync(
    path.join(__dirname, 'pump-fun-log.json'),
    JSON.stringify(pumpFunLog, null, 2)
  );
}

async function main() {
  console.log('--- ARIA TOKEN PUMP.FUN LISTING PREPARATION ---');
  console.log(`Network: ${NETWORK}`);
  
  // Load wallet and mint address
  await loadWalletAndMint();
  
  // Output token information and create listing plan
  await createListingPlan();
  
  // Liquidity pool setup
  await setupLiquidityPool();
  
  // Community and marketing activities
  suggestMarketingStrategies();
  
  console.log('\nGood luck with your pump.fun listing!');
  rl.close();
}

// Load wallet and token information
async function loadWalletAndMint() {
  console.log('\n--- LOADING TOKEN INFORMATION ---');
  
  // Find mint address files
  const mintFiles = fs.readdirSync(__dirname).filter(file => 
    file.includes('mint-address') && file.includes(NETWORK)
  );
  
  if (mintFiles.length > 0) {
    console.log('Found the following token mint files:');
    mintFiles.forEach((file, index) => {
      console.log(`${index + 1}. ${file}`);
    });
    
    const fileIndex = await question('Select file to use (enter number, default is 1): ');
    const selectedIndex = fileIndex ? parseInt(fileIndex) - 1 : 0;
    
    if (selectedIndex >= 0 && selectedIndex < mintFiles.length) {
      const mintData = JSON.parse(fs.readFileSync(path.join(__dirname, mintFiles[selectedIndex]), 'utf-8'));
      MINT_ADDRESS = new PublicKey(mintData.address);
      
      if (mintData.name) TOKEN_NAME = mintData.name;
      if (mintData.symbol) TOKEN_SYMBOL = mintData.symbol;
      if (mintData.decimals) TOKEN_DECIMALS = mintData.decimals;
      
      console.log(`Using token mint address: ${MINT_ADDRESS.toString()}`);
      console.log(`Token name: ${TOKEN_NAME}`);
      console.log(`Token symbol: ${TOKEN_SYMBOL}`);
    }
  }
  
  // If no files found, request manual input
  if (!MINT_ADDRESS) {
    const mintAddressInput = await question('Please enter token mint address: ');
    MINT_ADDRESS = new PublicKey(mintAddressInput);
    
    TOKEN_NAME = await question(`Token name (default is ${TOKEN_NAME}): `) || TOKEN_NAME;
    TOKEN_SYMBOL = await question(`Token symbol (default is ${TOKEN_SYMBOL}): `) || TOKEN_SYMBOL;
    TOKEN_DECIMALS = parseInt(await question(`Token decimals (default is ${TOKEN_DECIMALS}): `) || TOKEN_DECIMALS);
  }
  
  // Load deployer wallet
  console.log('\n--- LOADING WALLET ---');
  let deployer;
  const walletPath = path.join(__dirname, `deployer-wallet-${NETWORK}.json`);
  
  try {
    if (fs.existsSync(walletPath)) {
      const walletData = fs.readFileSync(walletPath, 'utf-8');
      deployer = Keypair.fromSecretKey(
        Uint8Array.from(JSON.parse(walletData))
      );
      console.log(`Using wallet: ${deployer.publicKey.toString()}`);
    } else {
      console.log('Deployer wallet not found. Manual configuration will be required.');
      return;
    }
  } catch (err) {
    console.error('Wallet error:', err);
    return;
  }

  // Connect to Solana network
  const connection = new Connection(CLUSTER_URL, 'confirmed');
  
  // Check account balance
  const balance = await connection.getBalance(deployer.publicKey);
  console.log(`Wallet balance: ${balance / 1_000_000_000} SOL`);
  
  if (balance < 0.5 * 1_000_000_000) {
    console.error('Warning: Low balance. SOL is required for token operations on mainnet.');
    const proceed = await question('Continue anyway? (y/n): ');
    if (proceed.toLowerCase() !== 'y') {
      rl.close();
      process.exit(0);
    }
  }

  try {
    // Initialize token object
    const token = new Token(
      connection,
      MINT_ADDRESS,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    // Get token supply information
    const mintInfo = await token.getMintInfo();
    console.log('\nToken Information:');
    console.log(`- Decimals: ${mintInfo.decimals}`);
    console.log(`- Total Supply: ${mintInfo.supply / (10 ** mintInfo.decimals)}`);
    
    // Log operation
    logOperation('check_token', { 
      mint: MINT_ADDRESS.toString(),
      totalSupply: mintInfo.supply.toString(),
      decimals: mintInfo.decimals
    });
    
  } catch (error) {
    console.error('Error:', error);
    const createNew = await question('Unable to get token information. Do you need to deploy a new token? (y/n): ');
    if (createNew.toLowerCase() === 'y') {
      console.log('Please run deploy_token.js script to create a new token.');
    }
    rl.close();
    process.exit(1);
  }
}

// Create listing plan
async function createListingPlan() {
  console.log('\n--- PUMP.FUN LISTING GUIDE ---');
  console.log('To list your token on pump.fun:');
  console.log('1. Visit https://pump.fun/create');
  console.log(`2. Enter token mint address: ${MINT_ADDRESS.toString()}`);
  console.log('3. Complete the following information:');
  console.log(`   - Token Name: ${TOKEN_NAME}`);
  console.log(`   - Token Symbol: ${TOKEN_SYMBOL}`);
  console.log('   - Description: AI Personal Assistant on Solana Blockchain');
  console.log('   - Logo: Upload ARIA logo from your assets folder');
  
  // Social media links
  console.log('\n--- SOCIAL MEDIA LINKS ---');
  const website = await question(`Website URL (default ${WEBSITE_URL}): `) || WEBSITE_URL;
  const twitter = await question(`Twitter (default ${TWITTER_HANDLE}): `) || TWITTER_HANDLE;
  const discord = await question(`Discord (default ${DISCORD_INVITE}): `) || DISCORD_INVITE;
  const telegram = await question(`Telegram (default ${TELEGRAM_GROUP}): `) || TELEGRAM_GROUP;
  const github = await question(`GitHub (default ${GITHUB_REPO}): `) || GITHUB_REPO;
  
  console.log('\nSuggested social media links:');
  console.log(`- Website: ${website}`);
  console.log(`- Twitter: ${twitter}`);
  console.log(`- Discord: ${discord}`);
  console.log(`- Telegram: ${telegram}`);
  console.log(`- GitHub: ${github}`);
  
  // Token description suggestions
  console.log('\n--- TOKEN DESCRIPTION TEMPLATE ---');
  const description = `ARIA is a decentralized AI personal assistant built on the Solana blockchain, combining artificial intelligence with blockchain technology to provide privacy protection and user data sovereignty. Through the ARI token, ecosystem participation is incentivized, allowing users to earn rewards and participate in governance.

Key features:
• Multi-model AI assistant (OpenAI and DeepSeek)
• Cross-platform support (Android and Web)
• Blockchain secure data storage
• ARI token driven ecosystem
• User privacy and data control

Website: ${website}
Twitter: ${twitter}
Discord: ${discord}`;

  console.log(description);
  console.log('\nNote: Token description is crucial for attracting early users. Make sure to highlight your project\'s unique selling points and value proposition.');
  
  // Log operation
  logOperation('prepare_listing', { 
    mint: MINT_ADDRESS.toString(),
    name: TOKEN_NAME,
    symbol: TOKEN_SYMBOL,
    socialLinks: { website, twitter, discord, telegram, github }
  });
}

// Liquidity pool setup
async function setupLiquidityPool() {
  console.log('\n--- LIQUIDITY POOL SETUP ---');
  console.log('To ensure successful trading on pump.fun:');
  
  // Get total supply
  const totalSupply = await question('What is the total token supply? ');
  let totalSupplyNum = parseFloat(totalSupply);
  
  if (isNaN(totalSupplyNum)) {
    console.log('Invalid input, using default value 100,000,000');
    totalSupplyNum = 100000000;
  }
  
  // Get liquidity allocation information
  const lpAllocationPercent = await question('What percentage of tokens will you allocate to liquidity pool? (recommended 20-30%): ');
  let lpPercent = parseFloat(lpAllocationPercent);
  
  if (isNaN(lpPercent)) {
    console.log('Invalid input, using default value 20%');
    lpPercent = 20;
  }
  
  const lpAllocation = totalSupplyNum * (lpPercent / 100);
  console.log(`\nBased on ${lpPercent}% allocation, you should provide ${lpAllocation} ${TOKEN_SYMBOL} tokens for liquidity.`);
  
  // Liquidity pool strategy
  console.log('\nRecommended liquidity allocation strategy:');
  console.log(`1. Initial liquidity: ${(lpPercent * 0.7).toFixed(1)}% (${(lpAllocation * 0.7).toFixed(0)} ${TOKEN_SYMBOL})`);
  console.log(`2. Subsequent liquidity additions: ${(lpPercent * 0.3).toFixed(1)}% (${(lpAllocation * 0.3).toFixed(0)} ${TOKEN_SYMBOL})`);
  
  // Initial price suggestions
  console.log('\nInitial price suggestions:');
  const mcapOptions = [
    { mcap: 1000, price: (1000 / totalSupplyNum).toFixed(8) },
    { mcap: 10000, price: (10000 / totalSupplyNum).toFixed(8) },
    { mcap: 50000, price: (50000 / totalSupplyNum).toFixed(8) },
    { mcap: 100000, price: (100000 / totalSupplyNum).toFixed(8) },
  ];
  
  console.log('Based on different initial market cap targets:');
  mcapOptions.forEach(option => {
    console.log(`- $${option.mcap} market cap: ${option.price} SOL per ${TOKEN_SYMBOL}`);
  });
  
  // SOL pairing suggestions
  const selectedMcap = await question('Select desired initial market cap (1000, 10000, 50000, 100000, default 10000): ') || 10000;
  const parsedMcap = parseFloat(selectedMcap);
  const recommendedPrice = isNaN(parsedMcap) ? 
    (10000 / totalSupplyNum) : 
    (parsedMcap / totalSupplyNum);
  
  const recommendedSol = lpAllocation * recommendedPrice;
  console.log(`\nBased on $${parsedMcap} market cap, recommended initial liquidity parameters:`);
  console.log(`- Price: ${recommendedPrice.toFixed(8)} SOL per ${TOKEN_SYMBOL}`);
  console.log(`- Tokens: ${lpAllocation.toFixed(0)} ${TOKEN_SYMBOL}`);
  console.log(`- SOL: ${recommendedSol.toFixed(2)} SOL`);
  
  // Log operation
  logOperation('liquidity_plan', { 
    totalSupply: totalSupplyNum,
    lpPercentage: lpPercent,
    lpTokens: lpAllocation,
    targetMcap: parsedMcap,
    tokenPrice: recommendedPrice,
    solNeeded: recommendedSol
  });
  
  // Liquidity pool instructions
  console.log('\n--- LIQUIDITY POOL SETUP STEPS ---');
  console.log('1. Prepare two wallets:');
  console.log('   - Deployment wallet: Holds total token supply');
  console.log('   - Liquidity wallet: Will be used for providing liquidity');
  console.log('2. Transfer planned token amount from deployment wallet to liquidity wallet');
  console.log('3. Prepare necessary SOL amount in liquidity wallet');
  console.log('4. Create liquidity pool:');
  console.log('   a. Create trading pair on Raydium or Jupiter');
  console.log('   b. Set initial price');
  console.log('   c. Deposit tokens and SOL');
  console.log('5. Lock liquidity (recommended at least 3 months)');
  console.log('   - Use tools like Solana Partner Audit locking service');
  console.log('6. Publicly announce liquidity pool creation and lock proof');
  
  console.log('\nNote: Liquidity pool is critical for token success. Ensure sufficient liquidity to avoid large price fluctuations.');
}

// Marketing and community suggestions
function suggestMarketingStrategies() {
  console.log('\n--- MARKETING AND COMMUNITY DEVELOPMENT STRATEGIES ---');
  console.log('A successful pump.fun listing requires a comprehensive marketing strategy:');
  
  console.log('\n1. Pre-listing Preparation:');
  console.log('   ☐ Create high-quality website clearly showcasing project value');
  console.log('   ☐ Prepare whitepaper/brief documentation');
  console.log('   ☐ Create professional social media accounts');
  console.log('   ☐ Design eye-catching brand assets and graphics');
  console.log('   ☐ Create demo video showcasing product features');
  
  console.log('\n2. Community Building:');
  console.log('   ☐ Create and activate Discord and Telegram communities');
  console.log('   ☐ Write detailed FAQ and getting started guides');
  console.log('   ☐ Set up bots for real-time support and information');
  console.log('   ☐ Create community rewards program');
  
  console.log('\n3. Listing Announcement:');
  console.log('   ☐ Tease listing date and time');
  console.log('   ☐ Create countdown and listing events');
  console.log('   ☐ Prepare Twitter announcement posts and threads');
  console.log('   ☐ Schedule AMAs with community');
  
  console.log('\n4. Listing Day Activities:');
  console.log('   ☐ Organize trading competitions on listing day');
  console.log('   ☐ Create community airdrops or rewards program');
  console.log('   ☐ Launch first-holder exclusive rewards');
  console.log('   ☐ Track and report trading activity in real-time');
  
  console.log('\n5. Post-listing Maintenance:');
  console.log('   ☐ Daily/weekly development progress updates');
  console.log('   ☐ Ongoing community engagement activities');
  console.log('   ☐ Regular trading competitions and challenges');
  console.log('   ☐ Track and publish token performance metrics');
  
  console.log('\n6. Partnerships and Outreach:');
  console.log('   ☐ Establish partnerships with other Solana projects');
  console.log('   ☐ Participate in Solana ecosystem events');
  console.log('   ☐ Contact Solana influencers for promotion');
  console.log('   ☐ Explore cross-promotion opportunities');
  
  // Log operation
  logOperation('marketing_plan', { timestamp: new Date().toISOString() });
}

main().catch(err => {
  console.error('Error:', err);
  rl.close();
}); 