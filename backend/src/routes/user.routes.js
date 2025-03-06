const express = require('express');
const router = express.Router();
const { 
  getUserProfile, 
  updateUserProfile, 
  getUserSettings, 
  updateUserSettings 
} = require('../controllers/user.controller');

/**
 * @route GET /api/users/profile
 * @desc Get user profile information
 * @access Private
 */
router.get('/profile', getUserProfile);

/**
 * @route PUT /api/users/profile
 * @desc Update user profile information
 * @access Private
 */
router.put('/profile', updateUserProfile);

/**
 * @route GET /api/users/settings
 * @desc Get user settings
 * @access Private
 */
router.get('/settings', getUserSettings);

/**
 * @route PUT /api/users/settings
 * @desc Update user settings
 * @access Private
 */
router.put('/settings', updateUserSettings);

module.exports = router; 