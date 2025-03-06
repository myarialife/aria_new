const logger = require('../utils/logger');

/**
 * Get user's ARI token balance
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getTokenBalance = async (req, res) => {
  try {
    const walletAddress = req.user.walletAddress || req.query.address;
    
    // For testing purposes, return mock data
    logger.info(`Getting ARI token balance for wallet: ${walletAddress}`);
    
    res.status(200).json({
      success: true,
      data: {
        balance: 1000.75, // Mock ARI token balance
        currency: 'ARI',
        lastUpdated: '2024-11-25T13:45:22Z'
      }
    });
  } catch (error) {
    logger.error(`Error getting token balance: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error retrieving token balance',
      error: error.message
    });
  }
};

/**
 * Get user's token transaction history
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getTokenTransactions = async (req, res) => {
  try {
    const walletAddress = req.user.walletAddress || req.query.address;
    
    // For testing purposes, return mock data
    logger.info(`Getting token transactions for wallet: ${walletAddress}`);
    
    const mockTransactions = [
      {
        id: 1,
        type: 'Received',
        amount: 100,
        timestamp: '2024-11-15T10:30:00Z',
        status: 'Confirmed',
        from: '7XYzBnNFzoSbiHXr5PUKETBxz2SCeZHWpqioAc5XqsLB',
        to: '5XqXkgJGADUrvUjra45i6poCV6L1nj4zSwXJXzA4JoiL',
        txId: '4vJ2Gqt6nHNtQqUp5gvRQ4KCBEy2pYxo2gSFoLXFcWTWD3P9wSqQGSZ2efYLKofrTWLbCeAEJiyQAZYeYkzKrm87'
      },
      {
        id: 2,
        type: 'Sent',
        amount: -25.5,
        timestamp: '2024-11-13T15:45:00Z',
        status: 'Confirmed',
        from: '5XqXkgJGADUrvUjra45i6poCV6L1nj4zSwXJXzA4JoiL',
        to: '9XYzBnNFzoSbiHXr5PUKETBxz2SCeZHWpqioAc5XqsLB',
        txId: '5vBZcWbr4LPnc1o3YRzjwhNHUu3zXQY3pYnKmZMcFJmTaR9nYLAMUc9c2vhbKXBCVRJqeQ1o4FEY4FpAXqJZkMa2'
      },
      {
        id: 3,
        type: 'Staking Reward',
        amount: 5.25,
        timestamp: '2024-11-10T09:15:00Z',
        status: 'Confirmed',
        from: 'Stake Authority',
        to: '5XqXkgJGADUrvUjra45i6poCV6L1nj4zSwXJXzA4JoiL',
        txId: '2wBQhaiDTXvKtbM6zKZYyZeGTBwTvYW5DLMLHcL6uWnk4pXQRUDvQwhmPdq1SgEBV68jbgmcQEcxnQGYYD1BNYTK'
      },
      {
        id: 4,
        type: 'Data Contribution',
        amount: 10,
        timestamp: '2024-11-05T14:20:00Z',
        status: 'Confirmed',
        from: 'ARIA Rewards',
        to: '5XqXkgJGADUrvUjra45i6poCV6L1nj4zSwXJXzA4JoiL',
        txId: '3eYLnm9zZmawicuXgBHoL4XdCdXUkDvG9zCU9yCfKAnJ3Dz8U5aAkVKm6MFEizZU6rP14jWRiPj9V3tGjDnU2HHE'
      }
    ];
    
    res.status(200).json({
      success: true,
      data: mockTransactions
    });
  } catch (error) {
    logger.error(`Error getting token transactions: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error retrieving token transactions',
      error: error.message
    });
  }
};

/**
 * Transfer tokens to another wallet
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.transferTokens = async (req, res) => {
  try {
    const { destination, amount } = req.body;
    const sourceWallet = req.user.walletAddress;
    
    // For testing purposes, just log and return success
    logger.info(`Token transfer request: ${amount} ARI from ${sourceWallet} to ${destination}`);
    
    // Simulate async process
    setTimeout(() => {
      logger.info('Token transfer completed successfully');
    }, 1000);
    
    res.status(200).json({
      success: true,
      message: 'Token transfer initiated successfully',
      data: {
        txId: 'mock_transaction_' + Date.now(),
        source: sourceWallet,
        destination: destination,
        amount: amount,
        fee: 0.000005,
        status: 'Processing'
      }
    });
  } catch (error) {
    logger.error(`Error transferring tokens: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error transferring tokens',
      error: error.message
    });
  }
};

/**
 * Get estimated rewards for data contribution
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getRewardEstimate = async (req, res) => {
  try {
    const { dataType, dataSize } = req.query;
    
    // For testing purposes, return mock data
    logger.info(`Getting reward estimate for data type: ${dataType}, size: ${dataSize}`);
    
    // Simple calculation for demo purposes
    let estimatedReward = 0;
    
    switch (dataType) {
      case 'usage':
        estimatedReward = 5;
        break;
      case 'preferences':
        estimatedReward = 10;
        break;
      case 'location':
        estimatedReward = 15;
        break;
      default:
        estimatedReward = 2;
    }
    
    // Adjust by size if provided
    if (dataSize) {
      const size = parseInt(dataSize);
      if (!isNaN(size)) {
        estimatedReward = estimatedReward * (size / 100);
      }
    }
    
    res.status(200).json({
      success: true,
      data: {
        estimatedReward: estimatedReward,
        currency: 'ARI',
        dataType: dataType,
        dataSize: dataSize
      }
    });
  } catch (error) {
    logger.error(`Error getting reward estimate: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error calculating reward estimate',
      error: error.message
    });
  }
}; 