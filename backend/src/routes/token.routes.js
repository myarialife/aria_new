const express = require('express');
const router = express.Router();
const { 
  getTokenBalance, 
  getTokenTransactions, 
  transferTokens,
  getRewardEstimate 
} = require('../controllers/token.controller');

/**
 * @route GET /api/tokens/balance
 * @desc Get user's ARI token balance
 * @access Private
 */
router.get('/balance', getTokenBalance);

/**
 * @route GET /api/tokens/transactions
 * @desc Get user's token transaction history
 * @access Private
 */
router.get('/transactions', getTokenTransactions);

/**
 * @route POST /api/tokens/transfer
 * @desc Transfer tokens to another wallet
 * @access Private
 */
router.post('/transfer', transferTokens);

/**
 * @route GET /api/tokens/reward-estimate
 * @desc Get estimated rewards for data contribution
 * @access Private
 */
router.get('/reward-estimate', getRewardEstimate);

module.exports = router; 