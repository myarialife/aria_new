/**
 * ARIA Token Pump.fun Listing Script
 * 
 * This script automates the preparation and execution of ARIA token 
 * listing on pump.fun with detailed analytics and reporting
 * 
 * Features:
 * - Token metadata validation and preparation
 * - Automatic liquidity pool calculation and setup
 * - Launch strategy planning and execution
 * - Marketing campaign timeline generation
 * - Community metrics tracking
 * 
 * Last updated: December 14, 2024
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
  TokenInstructions,
} = require('@solana/spl-token');

const fs = require('fs');
const path = require('path');
const readline = require('readline');
const axios = require('axios');
const chalk = require('chalk');
const dateFormat = require('dateformat');
const ProgressBar = require('progress');

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
let TOTAL_SUPPLY = 100000000; // 100 million tokens

// Social media link templates
const WEBSITE_URL = "https://www.myaria.life";
const TWITTER_HANDLE = "@ARIA_Assistant";
const DISCORD_INVITE = "https://discord.gg/aria-project";
const TELEGRAM_GROUP = "https://t.me/ARIA_Project";
const GITHUB_REPO = "https://github.com/myaria/ARIA";

// Launch parameters - Update these for your strategy
const LAUNCH_PARAMS = {
  initialLiquidityPercent: 20,     // Percentage of total supply for initial liquidity
  targetInitialMcap: 50000,        // Target initial market cap in USD
  liquidityLockMonths: 6,          // Months to lock liquidity
  initialPriceMultiplier: 1.1,     // Price buffer to account for initial volatility
  tranches: [
    { percent: 70, timeframeHours: 0 },   // Initial liquidity
    { percent: 15, timeframeHours: 24 },  // 24 hours after launch
    { percent: 15, timeframeHours: 72 },  // 72 hours after launch
  ],
};

// Pump.fun API endpoints (for future API integration)
const PUMPFUN_API = {
  baseUrl: 'https://pump.fun/api',
  tokenInfo: '/token-info',
  createListing: '/create-listing', // Reserved for future API access
};

// Log object to record operations
const pumpFunLog = {
  timestamp: new Date().toISOString(),
  projectName: "ARIA",
  launchPlan: {
    targetDate: null,
    phases: [],
    marketing: {}
  },
  token: {},
  operations: []
};

// Record operation with colored console output
function logOperation(type, details = {}, success = true) {
  const timestamp = new Date().toISOString();
  
  // Console output with color
  const statusColor = success ? chalk.green : chalk.red;
  const statusSymbol = success ? '✓' : '✗';
  console.log(`${chalk.blue('[' + dateFormat(new Date(), "HH:MM:ss") + ']')} ${statusColor(statusSymbol)} ${chalk.yellow(type)}`);
  
  // Log details if present
  if (Object.keys(details).length > 0) {
    Object.entries(details).forEach(([key, value]) => {
      console.log(`  ${chalk.dim(key)}: ${value}`);
    });
  }
  
  // Add to operation log
  pumpFunLog.operations.push({
    type,
    timestamp,
    status: success ? 'success' : 'error',
    ...details
  });
  
  // Save log file with timestamp
  const logFileName = `pump-fun-launch-${dateFormat(new Date(), "yyyymmdd-HHMMss")}.json`;
  fs.writeFileSync(
    path.join(__dirname, logFileName),
    JSON.stringify(pumpFunLog, null, 2)
  );
}

// Sleep function for rate limiting and animations
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Format numbers with commas
function formatNumber(num) {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

async function main() {
  console.log('\n' + chalk.bgBlue.white.bold(' ARIA TOKEN PUMP.FUN LAUNCH AUTOMATION ') + '\n');
  console.log(chalk.blue('Network:') + ` ${NETWORK}`);
  
  // Get the target launch date
  const launchDateInput = await question(chalk.yellow('Planned launch date (YYYY-MM-DD): '));
  let launchDate;
  
  try {
    launchDate = new Date(launchDateInput);
    if (isNaN(launchDate.getTime())) throw new Error('Invalid date');
    
    // Ensure date is in the future
    if (launchDate < new Date()) {
      console.log(chalk.red('Warning: Launch date must be in the future'));
      launchDate = new Date();
      launchDate.setDate(launchDate.getDate() + 14); // Default to 2 weeks from now
      console.log(chalk.yellow(`Setting default launch date to ${launchDate.toISOString().split('T')[0]}`));
    }
    
    pumpFunLog.launchPlan.targetDate = launchDate.toISOString();
  } catch (error) {
    console.log(chalk.red('Invalid date format, using default (2 weeks from now)'));
    launchDate = new Date();
    launchDate.setDate(launchDate.getDate() + 14);
    pumpFunLog.launchPlan.targetDate = launchDate.toISOString();
  }
  
  // Load wallet and mint address
  await loadWalletAndMint();
  
  // Output token information and create listing plan
  await createListingPlan();
  
  // Configure liquidity pool
  await setupLiquidityPool();
  
  // Generate marketing and community campaign
  await generateMarketingCampaign(launchDate);
  
  // Pre-launch checklist
  generatePreLaunchChecklist(launchDate);
  
  // Launch day procedures
  generateLaunchDayProcedures(launchDate);
  
  // Create summary report
  generateSummaryReport();
  
  console.log('\n' + chalk.bgGreen.black(' LAUNCH PLAN COMPLETED ') + '\n');
  console.log(`${chalk.blue('See JSON report:')} pump-fun-launch-${dateFormat(new Date(), "yyyymmdd-HHMMss")}.json`);
  
  rl.close();
}

// Load wallet and token information
async function loadWalletAndMint() {
  console.log('\n' + chalk.bgYellow.black(' TOKEN CONFIGURATION ') + '\n');
  
  // Find mint address files
  const mintFiles = fs.readdirSync(__dirname).filter(file => 
    file.includes('mint-address') && file.includes(NETWORK)
  );
  
  if (mintFiles.length > 0) {
    console.log('Found the following token mint files:');
    mintFiles.forEach((file, index) => {
      console.log(`${chalk.blue(index + 1)}. ${file}`);
    });
    
    const fileIndex = await question(chalk.yellow('Select file number (default is 1): '));
    const selectedIndex = fileIndex ? parseInt(fileIndex) - 1 : 0;
    
    if (selectedIndex >= 0 && selectedIndex < mintFiles.length) {
      try {
        const mintData = JSON.parse(fs.readFileSync(path.join(__dirname, mintFiles[selectedIndex]), 'utf-8'));
        MINT_ADDRESS = new PublicKey(mintData.address);
        
        if (mintData.name) TOKEN_NAME = mintData.name;
        if (mintData.symbol) TOKEN_SYMBOL = mintData.symbol;
        if (mintData.decimals) TOKEN_DECIMALS = mintData.decimals;
        if (mintData.supply) TOTAL_SUPPLY = mintData.supply / (10 ** TOKEN_DECIMALS);
        
        console.log(chalk.green(`Using token mint address: ${MINT_ADDRESS.toString()}`));
        console.log(`${chalk.blue('Token name:')} ${TOKEN_NAME}`);
        console.log(`${chalk.blue('Token symbol:')} ${TOKEN_SYMBOL}`);
        console.log(`${chalk.blue('Decimals:')} ${TOKEN_DECIMALS}`);
        console.log(`${chalk.blue('Total supply:')} ${formatNumber(TOTAL_SUPPLY)}`);
        
        // Store in log
        pumpFunLog.token = {
          name: TOKEN_NAME,
          symbol: TOKEN_SYMBOL,
          decimals: TOKEN_DECIMALS,
          mintAddress: MINT_ADDRESS.toString(),
          totalSupply: TOTAL_SUPPLY
        };
        
        logOperation('token_loaded', { mintAddress: MINT_ADDRESS.toString() });
      } catch (error) {
        console.log(chalk.red(`Error parsing mint file: ${error.message}`));
      }
    }
  }
  
  // If no files found or error occurred, request manual input
  if (!MINT_ADDRESS) {
    console.log(chalk.yellow('\nNo valid mint file found. Please enter token details manually:'));
    
    const mintAddressInput = await question(chalk.blue('Token mint address: '));
    try {
      MINT_ADDRESS = new PublicKey(mintAddressInput);
    } catch (error) {
      console.log(chalk.red('Invalid mint address. Please verify and try again.'));
      process.exit(1);
    }
    
    TOKEN_NAME = await question(chalk.blue(`Token name (default ${TOKEN_NAME}): `)) || TOKEN_NAME;
    TOKEN_SYMBOL = await question(chalk.blue(`Token symbol (default ${TOKEN_SYMBOL}): `)) || TOKEN_SYMBOL;
    TOKEN_DECIMALS = parseInt(await question(chalk.blue(`Token decimals (default ${TOKEN_DECIMALS}): `)) || TOKEN_DECIMALS);
    TOTAL_SUPPLY = parseFloat(await question(chalk.blue(`Total supply (default ${formatNumber(TOTAL_SUPPLY)}): `)) || TOTAL_SUPPLY);
    
    // Store in log
    pumpFunLog.token = {
      name: TOKEN_NAME,
      symbol: TOKEN_SYMBOL,
      decimals: TOKEN_DECIMALS,
      mintAddress: MINT_ADDRESS.toString(),
      totalSupply: TOTAL_SUPPLY
    };
  }
  
  // Load deployer wallet
  console.log('\n' + chalk.bgYellow.black(' WALLET CONFIGURATION ') + '\n');
  let deployer;
  const walletPath = path.join(__dirname, `deployer-wallet-${NETWORK}.json`);
  
  try {
    if (fs.existsSync(walletPath)) {
      const walletData = fs.readFileSync(walletPath, 'utf-8');
      deployer = Keypair.fromSecretKey(
        Uint8Array.from(JSON.parse(walletData))
      );
      console.log(chalk.green(`Using wallet: ${deployer.publicKey.toString()}`));
    } else {
      console.log(chalk.red('Deployer wallet not found. Manual configuration required.'));
      console.log('Please create a deployer wallet JSON file with your private key.');
      return;
    }
  } catch (err) {
    console.error(chalk.red('Wallet error:'), err);
    return;
  }

  // Connect to Solana network
  console.log(chalk.blue('Connecting to Solana network...'));
  const connection = new Connection(CLUSTER_URL, 'confirmed');
  
  // Create progress bar for token verification
  const bar = new ProgressBar('[:bar] :percent :etas', { 
    complete: '=',
    incomplete: ' ',
    width: 30,
    total: 4
  });
  
  // Check account balance
  try {
    console.log(chalk.blue('Checking wallet balance...'));
    const balance = await connection.getBalance(deployer.publicKey);
    console.log(`${chalk.blue('Wallet balance:')} ${balance / 1_000_000_000} SOL`);
    
    bar.tick();
    
    if (balance < 0.5 * 1_000_000_000) {
      console.log(chalk.red('Warning: Low balance. SOL is required for token operations.'));
      const proceed = await question(chalk.yellow('Continue anyway? (y/n): '));
      if (proceed.toLowerCase() !== 'y') {
        rl.close();
        process.exit(0);
      }
    }
  } catch (error) {
    console.log(chalk.red(`Error checking balance: ${error.message}`));
    bar.tick();
  }

  try {
    // Initialize token object
    console.log(chalk.blue('Validating token data...'));
    const token = new Token(
      connection,
      MINT_ADDRESS,
      TOKEN_PROGRAM_ID,
      deployer
    );
    
    bar.tick();
    
    // Get token supply information
    console.log(chalk.blue('Fetching token supply information...'));
    const mintInfo = await token.getMintInfo();
    
    bar.tick();
    
    // Verify mint information
    const actualDecimalPlaces = mintInfo.decimals;
    const actualSupply = mintInfo.supply / (10 ** actualDecimalPlaces);
    
    console.log('\n' + chalk.bgGreen.black(' TOKEN VERIFICATION COMPLETE ') + '\n');
    console.log(`${chalk.blue('Token Address:')} ${MINT_ADDRESS.toString()}`);
    console.log(`${chalk.blue('Decimal Places:')} ${actualDecimalPlaces}`);
    console.log(`${chalk.blue('Total Supply:')} ${formatNumber(actualSupply)} ${TOKEN_SYMBOL}`);
    
    // Update decimals and supply if different
    if (TOKEN_DECIMALS !== actualDecimalPlaces) {
      console.log(chalk.yellow(`Note: Updating decimals from ${TOKEN_DECIMALS} to ${actualDecimalPlaces}`));
      TOKEN_DECIMALS = actualDecimalPlaces;
      pumpFunLog.token.decimals = actualDecimalPlaces;
    }
    
    if (Math.abs(TOTAL_SUPPLY - actualSupply) > 0.01 * TOTAL_SUPPLY) { // 1% tolerance
      console.log(chalk.yellow(`Note: Updating total supply from ${formatNumber(TOTAL_SUPPLY)} to ${formatNumber(actualSupply)}`));
      TOTAL_SUPPLY = actualSupply;
      pumpFunLog.token.totalSupply = actualSupply;
    }
    
    bar.tick();
    
    // Log operation
    logOperation('token_verified', { 
      mint: MINT_ADDRESS.toString(),
      totalSupply: actualSupply,
      decimals: actualDecimalPlaces
    });
    
  } catch (error) {
    console.log(chalk.red('\nError verifying token:'));
    console.error(error);
    
    const createNew = await question(chalk.yellow('Unable to verify token. Do you need to deploy a new token? (y/n): '));
    if (createNew.toLowerCase() === 'y') {
      console.log('Please run deploy_token.js script to create a new token.');
    }
    
    logOperation('token_verification_failed', { error: error.message }, false);
    rl.close();
    process.exit(1);
  }
}

// Create listing plan for pump.fun
async function createListingPlan() {
  console.log('\n' + chalk.bgCyan.black(' PUMP.FUN LISTING CONFIGURATION ') + '\n');
  
  // Prepare listing metadata
  console.log(chalk.blue('Prepare the following information for pump.fun listing:'));
  console.log(`1. Visit ${chalk.green('https://pump.fun/create')}`);
  console.log(`2. Enter token mint address: ${chalk.green(MINT_ADDRESS.toString())}`);
  
  // Token details
  console.log('\n' + chalk.yellow('TOKEN DETAILS:'));
  console.log(`${chalk.blue('Token Name:')} ${TOKEN_NAME}`);
  console.log(`${chalk.blue('Token Symbol:')} ${TOKEN_SYMBOL}`);
  
  // Project description
  const defaultDescription = `ARIA is a privacy-focused AI personal assistant built natively for the Android ecosystem and powered by the Solana blockchain. The project combines advanced artificial intelligence with blockchain technology for complete user data sovereignty.

Key features:
• Native Android integration with edge computing
• Multi-model AI assistant with OpenAI, DeepSeek and Claude support
• Granular privacy controls with blockchain verification
• ARI token incentives for ecosystem participants
• Zero-knowledge data sharing options

Our mission is to create an AI assistant that respects user privacy while providing powerful capabilities across the Android ecosystem.`;

  console.log('\n' + chalk.yellow('PROJECT DESCRIPTION:'));
  console.log(defaultDescription);
  
  const customDescription = await question(chalk.blue('\nUse custom description? (y/n, default: n): '));
  let finalDescription = defaultDescription;
  
  if (customDescription.toLowerCase() === 'y') {
    console.log(chalk.yellow('Enter custom description (end with empty line):'));
    let lines = [];
    let line;
    while ((line = await question('')) !== '') {
      lines.push(line);
    }
    finalDescription = lines.join('\n');
  }
  
  // Social media links
  console.log('\n' + chalk.yellow('SOCIAL MEDIA LINKS:'));
  const website = await question(chalk.blue(`Website URL (default ${WEBSITE_URL}): `)) || WEBSITE_URL;
  const twitter = await question(chalk.blue(`Twitter (default ${TWITTER_HANDLE}): `)) || TWITTER_HANDLE;
  const discord = await question(chalk.blue(`Discord (default ${DISCORD_INVITE}): `)) || DISCORD_INVITE;
  const telegram = await question(chalk.blue(`Telegram (default ${TELEGRAM_GROUP}): `)) || TELEGRAM_GROUP;
  const github = await question(chalk.blue(`GitHub (default ${GITHUB_REPO}): `)) || GITHUB_REPO;
  
  // Tags
  console.log('\n' + chalk.yellow('RECOMMENDED TAGS:'));
  const recommendedTags = ['ai', 'android', 'assistant', 'privacy', 'blockchain', 'solana'];
  console.log(recommendedTags.map(tag => chalk.green(`#${tag}`)).join(', '));
  
  const customTags = await question(chalk.blue('Add custom tags (comma separated, leave empty to use defaults): '));
  let finalTags = recommendedTags;
  
  if (customTags.trim() !== '') {
    const additionalTags = customTags.split(',').map(tag => tag.trim().toLowerCase().replace(/^#/, ''));
    finalTags = [...new Set([...recommendedTags, ...additionalTags])]; // Remove duplicates
  }
  
  // Logo and banner
  console.log('\n' + chalk.yellow('REQUIRED ASSETS:'));
  console.log(`${chalk.blue('Logo Image:')} 400x400px PNG or JPG under 5MB`);
  console.log(`${chalk.blue('Banner Image (optional):')} 1500x500px PNG or JPG under 5MB`);
  
  // Log operation
  logOperation('listing_plan_created', { 
    mint: MINT_ADDRESS.toString(),
    name: TOKEN_NAME,
    symbol: TOKEN_SYMBOL,
    socialLinks: { website, twitter, discord, telegram, github },
    description: finalDescription,
    tags: finalTags
  });
}

// Liquidity pool setup
async function setupLiquidityPool() {
  console.log('\n' + chalk.bgMagenta.white(' LIQUIDITY POOL CONFIGURATION ') + '\n');
  
  // Calculate liquidity allocation based on launch parameters
  const lpPercent = LAUNCH_PARAMS.initialLiquidityPercent;
  const lpAllocation = TOTAL_SUPPLY * (lpPercent / 100);
  
  console.log(`Based on ${chalk.blue(lpPercent + '%')} allocation, you will need ${chalk.green(formatNumber(lpAllocation))} ${TOKEN_SYMBOL} tokens for liquidity.`);
  
  // Calculate price based on target market cap
  const targetMcap = LAUNCH_PARAMS.targetInitialMcap;
  const targetTokenPrice = targetMcap / TOTAL_SUPPLY;
  
  console.log(`\n${chalk.yellow('TARGET MARKET CAP:')} $${formatNumber(targetMcap)}`);
  console.log(`${chalk.yellow('TARGET TOKEN PRICE:')} $${targetTokenPrice.toFixed(8)} per ${TOKEN_SYMBOL}`);
  
  // Ask for SOL price
  let solPriceUsd = 100; // Default SOL price
  const solPriceInput = await question(chalk.blue('Current SOL price in USD (default $100): '));
  
  if (solPriceInput && !isNaN(parseFloat(solPriceInput))) {
    solPriceUsd = parseFloat(solPriceInput);
  }
  
  console.log(`${chalk.yellow('USING SOL PRICE:')} $${solPriceUsd}`);
  
  // Calculate SOL/TOKEN price
  const tokenPriceInSol = targetTokenPrice / solPriceUsd;
  const solNeededForLiquidity = lpAllocation * tokenPriceInSol;
  
  console.log(`\n${chalk.yellow('TOKEN PRICE IN SOL:')} ${tokenPriceInSol.toFixed(12)} SOL per ${TOKEN_SYMBOL}`);
  console.log(`${chalk.yellow('SOL NEEDED FOR LIQUIDITY:')} ${solNeededForLiquidity.toFixed(4)} SOL (approx $${(solNeededForLiquidity * solPriceUsd).toFixed(2)})`);
  
  // Generate liquidity tranches based on launch parameters
  console.log('\n' + chalk.yellow('LIQUIDITY PROVISION SCHEDULE:'));
  
  let totalProvided = 0;
  LAUNCH_PARAMS.tranches.forEach((tranche, index) => {
    const trancheAmount = lpAllocation * (tranche.percent / 100);
    const trancheSol = trancheAmount * tokenPriceInSol;
    const timeframe = tranche.timeframeHours === 0 ? 
      'Initial Launch' : 
      `${tranche.timeframeHours} hours after launch`;
    
    totalProvided += trancheAmount;
    
    console.log(`${chalk.blue(`Tranche ${index + 1} (${timeframe}):`)}
  - ${formatNumber(trancheAmount.toFixed(0))} ${TOKEN_SYMBOL} (${tranche.percent}% of LP allocation)
  - ${trancheSol.toFixed(4)} SOL
  - ${(totalProvided / lpAllocation * 100).toFixed(1)}% of total LP provided`);
  });
  
  // Liquidity locking recommendations
  console.log('\n' + chalk.yellow('LIQUIDITY LOCKING RECOMMENDATION:'));
  const lockMonths = LAUNCH_PARAMS.liquidityLockMonths;
  console.log(`Lock liquidity for at least ${chalk.green(lockMonths)} months to establish trust`);
  console.log(`Recommended locking service: ${chalk.blue('Solana Partner Audit locking service')}`);
  
  // Final checklist for liquidity provision
  console.log('\n' + chalk.yellow('LIQUIDITY PROVISION CHECKLIST:'));
  console.log(`${chalk.blue('☐')} Prepare ${formatNumber(lpAllocation.toFixed(0))} ${TOKEN_SYMBOL} in dedicated wallet`);
  console.log(`${chalk.blue('☐')} Ensure at least ${solNeededForLiquidity.toFixed(4)} SOL in same wallet`);
  console.log(`${chalk.blue('☐')} Verify token decimals are configured correctly (${TOKEN_DECIMALS})`);
  console.log(`${chalk.blue('☐')} Test a small transaction before full liquidity provision`);
  console.log(`${chalk.blue('☐')} Document liquidity lock transaction for transparency`);
  
  // Log operation
  logOperation('liquidity_plan_created', {
    totalSupply: TOTAL_SUPPLY,
    lpPercentage: lpPercent,
    lpTokens: lpAllocation,
    targetMcap: targetMcap,
    tokenPriceUsd: targetTokenPrice,
    tokenPriceSol: tokenPriceInSol,
    solNeeded: solNeededForLiquidity,
    lockPeriod: lockMonths
  });
  
  // Save liquidity plan to the launch plan
  pumpFunLog.launchPlan.liquidity = {
    allocation: {
      percentage: lpPercent,
      tokens: lpAllocation,
    },
    pricing: {
      targetMarketCap: targetMcap,
      tokenPriceUsd: targetTokenPrice,
      tokenPriceSol: tokenPriceInSol,
      solPriceUsd: solPriceUsd
    },
    tranches: LAUNCH_PARAMS.tranches.map((tranche, index) => ({
      index: index + 1,
      percentage: tranche.percent,
      tokens: lpAllocation * (tranche.percent / 100),
      timeframeHours: tranche.timeframeHours
    })),
    locking: {
      months: lockMonths,
      provider: "Solana Partner Audit"
    }
  };
}

// Generate marketing and community campaign
async function generateMarketingCampaign(launchDate) {
  console.log('\n' + chalk.bgBlue.white(' MARKETING & COMMUNITY CAMPAIGN ') + '\n');
  
  // Calculate campaign dates
  const now = new Date();
  const daysToLaunch = Math.ceil((launchDate - now) / (1000 * 60 * 60 * 24));
  
  console.log(`${chalk.yellow('PLANNED LAUNCH DATE:')} ${launchDate.toDateString()}`);
  console.log(`${chalk.yellow('DAYS UNTIL LAUNCH:')} ${daysToLaunch}`);
  
  // Determine campaign phases based on time until launch
  let preLaunchDays = Math.min(daysToLaunch, 28); // Maximum 4 weeks pre-launch campaign
  
  console.log(`\n${chalk.yellow('MARKETING CAMPAIGN TIMELINE:')}`);
  
  // Campaign phases with recommendations
  const phases = [
    {
      name: "Awareness Phase",
      startDaysBefore: Math.min(daysToLaunch, 28),
      endDaysBefore: Math.min(daysToLaunch, 15),
      activities: [
        "Create project Twitter/X account and post regular updates",
        "Design and publish professional landing page",
        "Prepare comprehensive documentation",
        "Create introduction video about ARIA",
        "Reach out to Solana community members",
        "Set up Discord and Telegram communities"
      ]
    },
    {
      name: "Interest Building Phase",
      startDaysBefore: Math.min(daysToLaunch, 14),
      endDaysBefore: Math.min(daysToLaunch, 8),
      activities: [
        "Reveal token economics and distribution details",
        "Host AMA (Ask Me Anything) sessions",
        "Provide technical demonstrations of the Android app",
        "Publish privacy and security framework documents",
        "Announce partnership with Solana projects",
        "Start referral and community building program"
      ]
    },
    {
      name: "Anticipation Phase",
      startDaysBefore: Math.min(daysToLaunch, 7),
      endDaysBefore: Math.min(daysToLaunch, 2),
      activities: [
        "Announce official launch date and time",
        "Create countdown on website and social media",
        "Release project roadmap with milestones",
        "Preview trading incentives and competitions",
        "Publish final audit reports and token supply details",
        "Create tutorial videos for participating in launch"
      ]
    },
    {
      name: "Launch Phase",
      startDaysBefore: 1,
      endDaysBefore: -7, // 7 days after launch
      activities: [
        "Coordinate announcement across all channels",
        "Provide step-by-step guides for participating",
        "Monitor liquidity and trading activity",
        "Host launch celebration events",
        "Announce first holder rewards",
        "Provide 24-hour support in community channels",
        "Start trading competition with $ARI rewards"
      ]
    }
  ];
  
  // Display each phase
  phases.forEach(phase => {
    // Calculate actual dates for this phase
    const phaseStartDate = new Date(launchDate);
    phaseStartDate.setDate(launchDate.getDate() - phase.startDaysBefore);
    
    const phaseEndDate = new Date(launchDate);
    phaseEndDate.setDate(launchDate.getDate() - phase.endDaysBefore);
    
    // Display phase information
    console.log(`\n${chalk.cyan(phase.name)} (${dateFormat(phaseStartDate, "mmm d")} - ${dateFormat(phaseEndDate, "mmm d")})`);
    
    // Display activities with checkboxes
    phase.activities.forEach(activity => {
      console.log(`${chalk.blue('☐')} ${activity}`);
    });
  });
  
  // Key marketing channels
  console.log('\n' + chalk.yellow('KEY MARKETING CHANNELS:'));
  console.log(`${chalk.blue('1.')} Twitter/X - Daily updates and community engagement`);
  console.log(`${chalk.blue('2.')} Discord - Technical support and dedicated community`);
  console.log(`${chalk.blue('3.')} Telegram - Announcements and quick updates`);
  console.log(`${chalk.blue('4.')} Medium - Detailed articles and project insights`);
  console.log(`${chalk.blue('5.')} YouTube - Tutorials and demonstrations`);
  
  // Strategy for Solana ecosystem integration
  console.log('\n' + chalk.yellow('SOLANA ECOSYSTEM INTEGRATION:'));
  console.log(`${chalk.blue('☐')} Participate in Solana hackathons and events`);
  console.log(`${chalk.blue('☐')} Integrate with popular Solana wallets`);
  console.log(`${chalk.blue('☐')} Coordinate with other Solana dApps for cross-promotion`);
  console.log(`${chalk.blue('☐')} Join Solana developer communities`);
  console.log(`${chalk.blue('☐')} Leverage Solana's mobile ecosystem initiatives`);
  
  // Airdrop strategy
  console.log('\n' + chalk.yellow('COMMUNITY AIRDROP STRATEGY:'));
  console.log(`${chalk.blue('☐')} Allocate 5% of token supply for airdrops`);
  console.log(`${chalk.blue('☐')} Reward early community members and contributors`);
  console.log(`${chalk.blue('☐')} Create task-based airdrop for meaningful engagement`);
  console.log(`${chalk.blue('☐')} Target Solana power users and validators`);
  console.log(`${chalk.blue('☐')} Design special airdrop for Android developers`);
  
  // Custom marketing strategy input
  console.log('\n' + chalk.yellow('CUSTOM MARKETING ACTIVITIES:'));
  const customActivity = await question(chalk.blue('Add custom marketing activity (or press Enter to skip): '));
  
  let customActivities = [];
  if (customActivity.trim() !== '') {
    customActivities.push(customActivity);
    console.log(`${chalk.green('✓')} Added: ${customActivity}`);
    
    let moreActivities = true;
    while (moreActivities) {
      const activity = await question(chalk.blue('Add another activity (or press Enter to finish): '));
      if (activity.trim() === '') {
        moreActivities = false;
      } else {
        customActivities.push(activity);
        console.log(`${chalk.green('✓')} Added: ${activity}`);
      }
    }
  }
  
  // Log operation
  logOperation('marketing_plan_created', {
    launchDate: launchDate.toISOString(),
    daysToLaunch,
    phaseCount: phases.length,
    customActivities: customActivities.length
  });
  
  // Save marketing plan to the launch plan
  pumpFunLog.launchPlan.marketing = {
    launchDate: launchDate.toISOString(),
    daysToLaunch,
    phases: phases.map(phase => ({
      name: phase.name,
      startDate: new Date(launchDate.getTime() - phase.startDaysBefore * 24 * 60 * 60 * 1000).toISOString(),
      endDate: new Date(launchDate.getTime() - phase.endDaysBefore * 24 * 60 * 60 * 1000).toISOString(),
      activities: phase.activities
    })),
    customActivities
  };
}

// Generate pre-launch checklist
function generatePreLaunchChecklist(launchDate) {
  console.log('\n' + chalk.bgGreen.black(' PRE-LAUNCH CHECKLIST ') + '\n');
  
  // Technical checklist
  console.log(chalk.yellow('TECHNICAL PREPARATION:'));
  console.log(`${chalk.blue('☐')} Verify token contract on Solscan and Solana Explorer`);
  console.log(`${chalk.blue('☐')} Test token transfers with test wallets`);
  console.log(`${chalk.blue('☐')} Prepare liquidity provision transaction`);
  console.log(`${chalk.blue('☐')} Backup all wallet information securely`);
  console.log(`${chalk.blue('☐')} Configure monitoring for blockchain activity`);
  console.log(`${chalk.blue('☐')} Test Android app with Solana wallet integration`);
  console.log(`${chalk.blue('☐')} Finalize multi-model AI integration`);
  console.log(`${chalk.blue('☐')} Implement and test privacy protection features`);
  
  // Community checklist
  console.log('\n' + chalk.yellow('COMMUNITY PREPARATION:'));
  console.log(`${chalk.blue('☐')} Prepare announcement templates for all channels`);
  console.log(`${chalk.blue('☐')} Train community moderators and support staff`);
  console.log(`${chalk.blue('☐')} Create FAQ document for common questions`);
  console.log(`${chalk.blue('☐')} Set up automated welcome messages in Discord/Telegram`);
  console.log(`${chalk.blue('☐')} Schedule first week of content and announcements`);
  console.log(`${chalk.blue('☐')} Prepare trading competition rules and rewards`);
  
  // Legal and compliance
  console.log('\n' + chalk.yellow('LEGAL AND COMPLIANCE:'));
  console.log(`${chalk.blue('☐')} Review terms of service and privacy policy`);
  console.log(`${chalk.blue('☐')} Verify compliance with data protection regulations`);
  console.log(`${chalk.blue('☐')} Document token launch process for transparency`);
  console.log(`${chalk.blue('☐')} Prepare disclaimers for token launch communications`);
  console.log(`${chalk.blue('☐')} Define clear data isolation and privacy measures`);
  
  // Final countdown checklist
  console.log('\n' + chalk.yellow('FINAL 24-HOUR COUNTDOWN:'));
  console.log(`${chalk.blue('☐')} Verify pump.fun listing details are correct`);
  console.log(`${chalk.blue('☐')} Confirm all team members are available for launch`);
  console.log(`${chalk.blue('☐')} Test all social media publishing access`);
  console.log(`${chalk.blue('☐')} Prepare wallet with exact liquidity amounts`);
  console.log(`${chalk.blue('☐')} Schedule final reminder announcements`);
  console.log(`${chalk.blue('☐')} Prepare real-time monitoring dashboard`);
  
  // Add to pumpFunLog
  pumpFunLog.launchPlan.phases.push({
    name: "Pre-Launch",
    checklist: {
      technical: 8,
      community: 6,
      legal: 5,
      countdown: 6
    },
    date: new Date(launchDate.getTime() - 24 * 60 * 60 * 1000).toISOString() // 1 day before launch
  });
}

// Generate launch day procedures
function generateLaunchDayProcedures(launchDate) {
  console.log('\n' + chalk.bgRed.white(' LAUNCH DAY PROCEDURES ') + '\n');
  
  // Display launch day timeline
  console.log(chalk.yellow('LAUNCH DAY TIMELINE:'));
  
  const launchHour = 15; // 3 PM launch time by default
  
  console.log(`${chalk.blue('08:00 AM')} - Team check-in and final preparations`);
  console.log(`${chalk.blue('10:00 AM')} - Community pre-launch AMA session`);
  console.log(`${chalk.blue('12:00 PM')} - Final verification of liquidity provision`);
  console.log(`${chalk.blue(`${launchHour}:00 PM`)} - Official token launch and liquidity provision`);
  console.log(`${chalk.blue(`${launchHour+1}:00 PM`)} - Launch announcement across all channels`);
  console.log(`${chalk.blue(`${launchHour+2}:00 PM`)} - Trading competition begins`);
  console.log(`${chalk.blue('11:59 PM')} - Day one recap and statistics report`);
  
  // Liquidity provision procedure
  console.log('\n' + chalk.yellow('LIQUIDITY PROVISION PROCEDURE:'));
  console.log(`${chalk.blue('1.')} Double-check token and SOL balances in liquidity wallet`);
  console.log(`${chalk.blue('2.')} Initialize liquidity pool at exact launch time`);
  console.log(`${chalk.blue('3.')} Verify pool creation on blockchain explorer`);
  console.log(`${chalk.blue('4.')} Capture pool address for community verification`);
  console.log(`${chalk.blue('5.')} Lock liquidity and share proof with community`);
  console.log(`${chalk.blue('6.')} Monitor initial trading activity for irregular patterns`);
  
  // Community management
  console.log('\n' + chalk.yellow('COMMUNITY MANAGEMENT:'));
  console.log(`${chalk.blue('☐')} Assign team members to monitor each community channel`);
  console.log(`${chalk.blue('☐')} Prepare responses for common scenarios (price volatility, technical issues)`);
  console.log(`${chalk.blue('☐')} Post regular updates throughout the day`);
  console.log(`${chalk.blue('☐')} Launch trading competition and airdrop announcements`);
  console.log(`${chalk.blue('☐')} Host voice AMA session after successful launch`);
  console.log(`${chalk.blue('☐')} Highlight Android application capabilities and privacy features`);
  
  // Technical monitoring
  console.log('\n' + chalk.yellow('TECHNICAL MONITORING:'));
  console.log(`${chalk.blue('☐')} Set up real-time monitoring of blockchain transactions`);
  console.log(`${chalk.blue('☐')} Monitor token price and liquidity pool metrics`);
  console.log(`${chalk.blue('☐')} Track website and app performance/traffic`);
  console.log(`${chalk.blue('☐')} Monitor community growth metrics (new members, engagement)`);
  console.log(`${chalk.blue('☐')} Prepare for rapid response to any technical issues`);
  
  // Add to pumpFunLog
  pumpFunLog.launchPlan.phases.push({
    name: "Launch Day",
    timeline: {
      checkIn: "08:00",
      ama: "10:00",
      verification: "12:00",
      launch: `${launchHour}:00`,
      announcement: `${launchHour+1}:00`,
      competition: `${launchHour+2}:00`,
      recap: "23:59"
    },
    date: launchDate.toISOString()
  });
}

// Generate summary report
function generateSummaryReport() {
  console.log('\n' + chalk.bgYellow.black(' LAUNCH PLAN SUMMARY ') + '\n');
  
  // Summary
  console.log(chalk.yellow('PROJECT DETAILS:'));
  console.log(`${chalk.blue('Project:')} ARIA - Android AI Assistant on Solana`);
  console.log(`${chalk.blue('Token:')} ${TOKEN_SYMBOL} (${MINT_ADDRESS.toString().substring(0, 6)}...${MINT_ADDRESS.toString().substring(MINT_ADDRESS.toString().length - 4)})`);
  console.log(`${chalk.blue('Launch Date:')} ${new Date(pumpFunLog.launchPlan.targetDate).toDateString()}`);
  
  // Liquidity summary
  if (pumpFunLog.launchPlan.liquidity) {
    const lp = pumpFunLog.launchPlan.liquidity;
    console.log('\n' + chalk.yellow('LIQUIDITY SUMMARY:'));
    console.log(`${chalk.blue('Allocation:')} ${formatNumber(lp.allocation.tokens.toFixed(0))} ${TOKEN_SYMBOL} (${lp.allocation.percentage}% of supply)`);
    console.log(`${chalk.blue('Target Price:')} $${lp.pricing.tokenPriceUsd.toFixed(8)} per ${TOKEN_SYMBOL}`);
    console.log(`${chalk.blue('Initial Mcap:')} $${formatNumber(lp.pricing.targetMarketCap)}`);
    console.log(`${chalk.blue('SOL Needed:')} Approximately ${(lp.allocation.tokens * lp.pricing.tokenPriceSol).toFixed(4)} SOL`);
  }
  
  // Marketing summary
  if (pumpFunLog.launchPlan.marketing) {
    const marketing = pumpFunLog.launchPlan.marketing;
    console.log('\n' + chalk.yellow('MARKETING SUMMARY:'));
    console.log(`${chalk.blue('Campaign Length:')} ${marketing.daysToLaunch} days`);
    console.log(`${chalk.blue('Campaign Phases:')} ${marketing.phases.length}`);
    console.log(`${chalk.blue('Total Activities:')} ${marketing.phases.reduce((sum, phase) => sum + phase.activities.length, 0) + marketing.customActivities.length}`);
  }
  
  console.log('\n' + chalk.yellow('NEXT STEPS:'));
  console.log(`${chalk.blue('1.')} Review the full launch plan in the generated JSON file`);
  console.log(`${chalk.blue('2.')} Distribute responsibilities among team members`);
  console.log(`${chalk.blue('3.')} Set up regular check-in meetings to track progress`);
  console.log(`${chalk.blue('4.')} Begin executing the Awareness Phase activities`);
  console.log(`${chalk.blue('5.')} Prepare the Android application with full privacy features`);
  
  // Project highlights
  console.log('\n' + chalk.yellow('PROJECT HIGHLIGHTS FOR MARKETING:'));
  console.log(`${chalk.blue('☑')} Native Android ecosystem integration`);
  console.log(`${chalk.blue('☑')} Multi-model AI with OpenAI, DeepSeek and Claude support`);
  console.log(`${chalk.blue('☑')} Strong privacy protection and data isolation`);
  console.log(`${chalk.blue('☑')} Solana blockchain foundation with SPL token`);
  console.log(`${chalk.blue('☑')} User-controlled data sharing with incentive system`);
  
  // Log operation
  logOperation('summary_report_generated', {
    timestamp: new Date().toISOString(),
    reportItems: 3
  });
}

main().catch(err => {
  console.error(chalk.red('Error:'), err);
  logOperation('error', { message: err.message }, false);
  rl.close();
}); 