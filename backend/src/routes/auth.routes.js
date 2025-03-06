const express = require('express');
const router = express.Router();
const { register, login, refreshToken, verifyWallet } = require('../controllers/auth.controller');

/**
 * @route POST /api/auth/register
 * @desc Register a new user
 * @access Public
 */
router.post('/register', register);

/**
 * @route POST /api/auth/login
 * @desc Login user and get token
 * @access Public
 */
router.post('/login', login);

/**
 * @route POST /api/auth/refresh
 * @desc Refresh access token
 * @access Public (with refresh token)
 */
router.post('/refresh', refreshToken);

/**
 * @route POST /api/auth/verify-wallet
 * @desc Verify and authenticate a Solana wallet
 * @access Public
 */
router.post('/verify-wallet', verifyWallet);

module.exports = router; 