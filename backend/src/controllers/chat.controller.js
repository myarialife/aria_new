const logger = require('../utils/logger');
const { Configuration, OpenAIApi } = require('openai');
const axios = require('axios');

// Initialize OpenAI API (if available)
let openai;
try {
  const configuration = new Configuration({
    apiKey: process.env.OPENAI_API_KEY,
  });
  openai = new OpenAIApi(configuration);
  logger.info('OpenAI API initialized successfully');
} catch (error) {
  logger.warn('OpenAI API not initialized. Using fallback responses.');
}

/**
 * Send a message to AI assistant and get a response
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.sendMessage = async (req, res) => {
  try {
    const { message } = req.body;
    const userId = req.user?.id || 'anonymous';
    const modelPreference = req.body.model || process.env.DEFAULT_AI_MODEL || 'gpt';
    
    logger.info(`Received message from user ${userId}: ${message.substring(0, 50)}...`);
    logger.info(`Using model preference: ${modelPreference}`);
    
    let aiResponse;
    
    // Try to use preferred AI model
    switch (modelPreference.toLowerCase()) {
      case 'deepseek':
        aiResponse = await getDeepSeekResponse(message, userId);
        break;
      case 'gpt':
      default:
        aiResponse = await getOpenAIResponse(message, userId);
        break;
    }
    
    // If AI response is still empty, use fallback
    if (!aiResponse) {
      aiResponse = getFallbackResponse(message);
    }
    
    // In a real implementation, you would save the conversation to database
    
    res.status(200).json({
      success: true,
      message: aiResponse
    });
  } catch (error) {
    logger.error(`Chat error: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error processing your message',
      error: error.message
    });
  }
};

/**
 * Get response from OpenAI models
 * @param {String} message - User message
 * @param {String} userId - User ID
 * @returns {Promise<String>} - AI response
 */
async function getOpenAIResponse(message, userId) {
  if (!openai || !process.env.OPENAI_API_KEY) {
    logger.warn('OpenAI API not available, skipping');
    return null;
  }
  
  try {
    // Define system message for ARIA context
    const systemMessage = `You are ARIA, an AI personal assistant built on the Solana blockchain. 
    You focus on privacy and data sovereignty, allowing users to earn ARI tokens for sharing data.
    Be helpful, friendly, and concise in your responses. You can assist with information about 
    Solana, blockchain, ARI tokens, and general AI assistant tasks.`;
    
    // Use chat completion API (more advanced than the legacy completion API)
    const completion = await openai.createChatCompletion({
      model: process.env.OPENAI_MODEL || "gpt-3.5-turbo",
      messages: [
        { role: "system", content: systemMessage },
        { role: "user", content: message }
      ],
      max_tokens: 500,
      temperature: 0.7,
      user: userId
    });
    
    return completion.data.choices[0].message.content.trim();
  } catch (error) {
    logger.error(`OpenAI error: ${error.message}`);
    return null;
  }
}

/**
 * Get response from DeepSeek models
 * @param {String} message - User message
 * @param {String} userId - User ID
 * @returns {Promise<String>} - AI response
 */
async function getDeepSeekResponse(message, userId) {
  if (!process.env.DEEPSEEK_API_KEY) {
    logger.warn('DeepSeek API not available, skipping');
    return null;
  }
  
  try {
    // Define system message for ARIA context
    const systemPrompt = `You are ARIA, an AI personal assistant built on the Solana blockchain. 
    You focus on privacy and data sovereignty, allowing users to earn ARI tokens for sharing data.
    Be helpful, friendly, and concise in your responses. You can assist with information about 
    Solana, blockchain, ARI tokens, and general AI assistant tasks.`;
    
    // Call DeepSeek API
    const response = await axios.post(
      'https://api.deepseek.com/v1/chat/completions',
      {
        model: process.env.DEEPSEEK_MODEL || "deepseek-chat",
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: message }
        ],
        temperature: 0.7,
        max_tokens: 500
      },
      {
        headers: {
          'Authorization': `Bearer ${process.env.DEEPSEEK_API_KEY}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data.choices[0].message.content.trim();
  } catch (error) {
    logger.error(`DeepSeek API error: ${error.message}`);
    return null;
  }
}

/**
 * Get chat history for a user
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getChatHistory = async (req, res) => {
  try {
    const userId = req.user?.id || 'anonymous';
    
    // In a real implementation, you would fetch chat history from database
    // For MVP, we'll return mock data
    
    const mockChatHistory = [
      {
        id: '1',
        timestamp: new Date(Date.now() - 86400000).toISOString(), // 1 day ago
        messages: [
          { sender: 'user', content: 'Hello, how can you help me?', timestamp: new Date(Date.now() - 86400000).toISOString() },
          { sender: 'aria', content: 'I can help you manage your data, answer questions, and assist with tasks. What would you like to do today?', timestamp: new Date(Date.now() - 86390000).toISOString() }
        ]
      },
      {
        id: '2',
        timestamp: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
        messages: [
          { sender: 'user', content: 'Tell me about ARI tokens', timestamp: new Date(Date.now() - 3600000).toISOString() },
          { sender: 'aria', content: 'ARI tokens are the utility tokens for the ARIA platform. You can earn them by contributing data and use them to access premium features.', timestamp: new Date(Date.now() - 3590000).toISOString() }
        ]
      }
    ];
    
    res.status(200).json({
      success: true,
      data: mockChatHistory
    });
  } catch (error) {
    logger.error(`Get chat history error: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error retrieving chat history',
      error: error.message
    });
  }
};

/**
 * Delete a chat conversation
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.deleteChat = async (req, res) => {
  try {
    const { chatId } = req.params;
    const userId = req.user?.id || 'anonymous';
    
    // In a real implementation, you would delete the chat from database
    
    logger.info(`User ${userId} deleted chat ${chatId}`);
    
    res.status(200).json({
      success: true,
      message: `Chat ${chatId} deleted successfully`
    });
  } catch (error) {
    logger.error(`Delete chat error: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error deleting chat',
      error: error.message
    });
  }
};

/**
 * Get AI models available for the user
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getAvailableModels = async (req, res) => {
  try {
    const models = [];
    
    // Check which models are available
    if (process.env.OPENAI_API_KEY) {
      models.push({
        id: 'gpt',
        name: 'GPT',
        description: 'OpenAI\'s GPT model',
        provider: 'OpenAI',
        isDefault: true
      });
    }
    
    if (process.env.DEEPSEEK_API_KEY) {
      models.push({
        id: 'deepseek',
        name: 'DeepSeek',
        description: 'DeepSeek\'s language model',
        provider: 'DeepSeek AI',
        isDefault: false
      });
    }
    
    // Always add fallback option
    models.push({
      id: 'fallback',
      name: 'Basic',
      description: 'Rule-based responses (no API key required)',
      provider: 'ARIA',
      isDefault: models.length === 0
    });
    
    res.status(200).json({
      success: true,
      data: models
    });
  } catch (error) {
    logger.error(`Error fetching available models: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Error retrieving available AI models',
      error: error.message
    });
  }
};

/**
 * Get a fallback response based on rule-based matching
 * @param {String} message - User message
 * @returns {String} AI response
 */
const getFallbackResponse = (message) => {
  const lowerCaseMessage = message.toLowerCase();
  
  if (lowerCaseMessage.includes('hello') || lowerCaseMessage.includes('hi')) {
    return 'Hello! How can I assist you today?';
  } else if (lowerCaseMessage.includes('name')) {
    return 'My name is ARIA, your AI personal assistant powered by Solana blockchain.';
  } else if (lowerCaseMessage.includes('ari token') || lowerCaseMessage.includes('token')) {
    return 'ARI tokens are the utility tokens for the ARIA platform. You can earn them by contributing data and use them to access premium features.';
  } else if (lowerCaseMessage.includes('privacy') || lowerCaseMessage.includes('data')) {
    return 'ARIA is designed with privacy at its core. Your data remains under your control, and you choose what to share.';
  } else if (lowerCaseMessage.includes('solana')) {
    return 'ARIA is built on the Solana blockchain, providing fast, low-cost transactions and secure data storage.';
  } else if (lowerCaseMessage.includes('help')) {
    return 'I can help with information about ARIA, assist with daily tasks, answer questions, and more. What would you like to know?';
  } else if (lowerCaseMessage.includes('defi') || lowerCaseMessage.includes('finance')) {
    return 'Solana has a thriving DeFi ecosystem. With ARI tokens, you can participate in various financial activities and earn rewards for your data contributions.';
  } else if (lowerCaseMessage.includes('wallet')) {
    return 'You can connect wallets like Phantom or Solflare to the ARIA app to manage your ARI tokens, view your balance, and participate in the ecosystem.';
  } else if (lowerCaseMessage.includes('ai') || lowerCaseMessage.includes('assistant')) {
    return 'As your AI assistant, I can help with managing information, providing insights, and assisting with various tasks while respecting your privacy.';
  } else {
    return 'I understand your message. How can I help you further with ARIA?';
  }
}; 