const logger = require('../utils/logger');

/**
 * Get user profile information
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getUserProfile = async (req, res) => {
  try {
    const userId = req.user.id || req.user.email || req.user.walletAddress;
    
    // For testing purposes, return mock profile data
    logger.info(`Getting profile for user: ${userId}`);
    
    res.status(200).json({
      success: true,
      data: {
        id: userId,
        name: 'Test User',
        email: 'test@example.com',
        walletAddress: req.user.walletAddress || 'not connected',
        joinDate: '2024-10-05T00:00:00Z',
        lastActive: new Date().toISOString()
      }
    });
  } catch (error) {
    logger.error(`Error getting user profile: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error retrieving user profile',
      error: error.message
    });
  }
};

/**
 * Update user profile information
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.updateUserProfile = async (req, res) => {
  try {
    const userId = req.user.id || req.user.email || req.user.walletAddress;
    const { name, email, bio } = req.body;
    
    // For testing purposes, log and return updated mock data
    logger.info(`Updating profile for user: ${userId}`);
    
    res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      data: {
        id: userId,
        name: name || 'Test User',
        email: email || 'test@example.com',
        bio: bio || '',
        walletAddress: req.user.walletAddress || 'not connected',
        updatedAt: new Date().toISOString()
      }
    });
  } catch (error) {
    logger.error(`Error updating user profile: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error updating user profile',
      error: error.message
    });
  }
};

/**
 * Get user settings
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getUserSettings = async (req, res) => {
  try {
    const userId = req.user.id || req.user.email || req.user.walletAddress;
    
    // For testing purposes, return mock settings
    logger.info(`Getting settings for user: ${userId}`);
    
    res.status(200).json({
      success: true,
      data: {
        privacySettings: {
          shareUsageData: true,
          shareLocationData: false,
          shareChatHistory: true,
          enableNotifications: true
        },
        tokenSettings: {
          autoStake: false,
          dataContributionRewards: true,
          participateInGrowthProgram: true
        },
        aiPreferences: {
          enableVoiceResponse: false,
          personalityType: 'balanced',
          responseLength: 'medium'
        }
      }
    });
  } catch (error) {
    logger.error(`Error getting user settings: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error retrieving user settings',
      error: error.message
    });
  }
};

/**
 * Update user settings
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.updateUserSettings = async (req, res) => {
  try {
    const userId = req.user.id || req.user.email || req.user.walletAddress;
    const { privacySettings, tokenSettings, aiPreferences } = req.body;
    
    // For testing purposes, log and return updated mock data
    logger.info(`Updating settings for user: ${userId}`);
    
    res.status(200).json({
      success: true,
      message: 'Settings updated successfully',
      data: {
        privacySettings: privacySettings || {
          shareUsageData: true,
          shareLocationData: false,
          shareChatHistory: true,
          enableNotifications: true
        },
        tokenSettings: tokenSettings || {
          autoStake: false,
          dataContributionRewards: true,
          participateInGrowthProgram: true
        },
        aiPreferences: aiPreferences || {
          enableVoiceResponse: false,
          personalityType: 'balanced',
          responseLength: 'medium'
        },
        updatedAt: new Date().toISOString()
      }
    });
  } catch (error) {
    logger.error(`Error updating user settings: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error updating user settings',
      error: error.message
    });
  }
};

// Mock user data
const mockUsers = [
  {
    id: '1',
    username: 'test_user',
    email: 'test@example.com',
    wallet: '5XqXkgJGADUrvUjra45i6poCV6L1nj4zSwXJXzA4JoiL',
    balance: 500,
    joinDate: '2024-10-05T00:00:00Z',
    preferences: {
      notifications: true,
      privacyLevel: 'standard',
      theme: 'light',
      language: 'en'
    },
    activities: [
      { type: 'login', timestamp: '2024-11-20T10:22:31Z' },
      { type: 'transaction', timestamp: '2024-11-18T14:35:22Z' },
      { type: 'settings_update', timestamp: '2024-11-15T09:12:44Z' }
    ]
  },
  // More mock users would be here in a real application
];

// Get user by ID
exports.getUser = (req, res) => {
  const { id } = req.params;
  const user = mockUsers.find(user => user.id === id);
  
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }
  
  res.json(user);
};

// Get user by wallet address
exports.getUserByWallet = (req, res) => {
  const { wallet } = req.params;
  const user = mockUsers.find(user => user.wallet.toLowerCase() === wallet.toLowerCase());
  
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }
  
  res.json(user);
};

// Update user preferences
exports.updatePreferences = (req, res) => {
  const { id } = req.params;
  const { preferences } = req.body;
  
  const userIndex = mockUsers.findIndex(user => user.id === id);
  
  if (userIndex === -1) {
    return res.status(404).json({ error: 'User not found' });
  }
  
  // In a real application, validate the preferences data
  mockUsers[userIndex].preferences = {
    ...mockUsers[userIndex].preferences,
    ...preferences
  };
  
  res.json(mockUsers[userIndex]);
};

// Get user activities
exports.getUserActivities = (req, res) => {
  const { id } = req.params;
  const user = mockUsers.find(user => user.id === id);
  
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }
  
  res.json(user.activities);
};

// Create new user (for demonstration purposes)
exports.createUser = (req, res) => {
  // In a real application, validate input and handle errors
  const { username, email, wallet } = req.body;
  
  // Check if user with wallet already exists
  if (mockUsers.some(user => user.wallet.toLowerCase() === wallet.toLowerCase())) {
    return res.status(400).json({ error: 'User with this wallet already exists' });
  }
  
  const newUser = {
    id: (mockUsers.length + 1).toString(),
    username,
    email,
    wallet,
    balance: 0,
    joinDate: new Date().toISOString(),
    preferences: {
      notifications: true,
      privacyLevel: 'standard',
      theme: 'light',
      language: 'en'
    },
    activities: []
  };
  
  mockUsers.push(newUser);
  
  res.status(201).json(newUser);
}; 