const express = require('express');
const router = express.Router();
const { sendMessage, getChatHistory, deleteChat, getAvailableModels } = require('../controllers/chat.controller');

/**
 * @route POST /api/chat/message
 * @desc Send a message to AI assistant
 * @access Private
 */
router.post('/message', sendMessage);

/**
 * @route GET /api/chat/history
 * @desc Get user's chat history
 * @access Private
 */
router.get('/history', getChatHistory);

/**
 * @route DELETE /api/chat/:chatId
 * @desc Delete a chat conversation
 * @access Private
 */
router.delete('/:chatId', deleteChat);

/**
 * @route GET /api/chat/models
 * @desc Get available AI models
 * @access Private
 */
router.get('/models', getAvailableModels);

module.exports = router; 