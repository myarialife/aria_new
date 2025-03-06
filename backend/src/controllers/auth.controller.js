const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const { Connection, PublicKey } = require('@solana/web3.js');
const logger = require('../utils/logger');

/**
 * Register a new user
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.register = async (req, res) => {
  try {
    const { email, password, name } = req.body;
    
    // In a real implementation, you would check if the user already exists
    // and save to database. For MVP, we'll just return a success message.
    
    logger.info(`User registration attempt for ${email}`);
    
    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, 10);
    
    // Generate JWT token
    const token = generateToken({ email });
    
    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      token
    });
  } catch (error) {
    logger.error(`Registration error: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Server error during registration',
      error: error.message
    });
  }
};

/**
 * Login user and generate token
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;
    
    // In a real implementation, you would fetch the user from database
    // and compare passwords. For MVP, we'll just simulate this.
    
    logger.info(`Login attempt for ${email}`);
    
    // Check if user exists (mock validation)
    if (email !== 'test@example.com') {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials'
      });
    }
    
    // Generate JWT token
    const token = generateToken({ email });
    
    res.status(200).json({
      success: true,
      message: 'Login successful',
      token
    });
  } catch (error) {
    logger.error(`Login error: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Server error during login',
      error: error.message
    });
  }
};

/**
 * Refresh JWT token
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.refreshToken = async (req, res) => {
  try {
    const { refreshToken } = req.body;
    
    // Verify refresh token
    const decoded = jwt.verify(refreshToken, process.env.JWT_SECRET);
    
    // Generate new access token
    const newToken = generateToken({ email: decoded.email });
    
    res.status(200).json({
      success: true,
      message: 'Token refreshed successfully',
      token: newToken
    });
  } catch (error) {
    logger.error(`Token refresh error: ${error.message}`);
    res.status(401).json({
      success: false,
      message: 'Invalid or expired refresh token',
      error: error.message
    });
  }
};

/**
 * Verify wallet signature and authenticate
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.verifyWallet = async (req, res) => {
  try {
    const { publicKey, signature, message } = req.body;
    
    // In a production environment, you would verify the signature
    // For MVP, we'll just generate a token based on the public key
    
    logger.info(`Wallet verification attempt for ${publicKey}`);
    
    // Generate JWT token
    const token = generateToken({ walletAddress: publicKey });
    
    res.status(200).json({
      success: true,
      message: 'Wallet verified successfully',
      token
    });
  } catch (error) {
    logger.error(`Wallet verification error: ${error.message}`);
    res.status(500).json({
      success: false,
      message: 'Server error during wallet verification',
      error: error.message
    });
  }
};

/**
 * Generate JWT token
 * @param {Object} payload - Data to include in the token
 * @returns {String} JWT token
 */
const generateToken = (payload) => {
  return jwt.sign(
    payload,
    process.env.JWT_SECRET || 'default_secret_key',
    { expiresIn: process.env.JWT_EXPIRE || '30d' }
  );
}; 