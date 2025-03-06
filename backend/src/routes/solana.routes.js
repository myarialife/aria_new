const express = require('express');
const router = express.Router();
const { 
  getSolBalance, 
  getWalletInfo, 
  createToken,
  getTokenInfo
} = require('../controllers/solana.controller');

/**
 * @route GET /api/solana/balance
 * @desc Get user's SOL balance
 * @access Private
 */
router.get('/balance', getSolBalance);

/**
 * @route GET /api/solana/wallet
 * @desc Get Solana wallet information
 * @access Private
 */
router.get('/wallet', getWalletInfo);

/**
 * @route POST /api/solana/token/create
 * @desc Create a new token (admin only)
 * @access Private
 */
router.post('/token/create', createToken);

/**
 * @route GET /api/solana/token/:address
 * @desc Get information about a token
 * @access Private
 */
router.get('/token/:address', getTokenInfo);

module.exports = router; 