require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');

// Import routes
const authRoutes = require('./routes/auth.routes');
const userRoutes = require('./routes/user.routes');
const chatRoutes = require('./routes/chat.routes');
const tokenRoutes = require('./routes/token.routes');
const solanaRoutes = require('./routes/solana.routes');

// Import middleware
const { errorHandler } = require('./middleware/error.middleware');
const { authMiddleware } = require('./middleware/auth.middleware');

// Import config
const logger = require('./utils/logger');
const { connectDB } = require('./config/database');

// Initialize express app
const app = express();

// Set security-related middleware
app.use(helmet());
app.use(cors());

// Set up rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again after 15 minutes'
});
app.use('/api/', limiter);

// Set up request logging
app.use(morgan('combined', { stream: { write: message => logger.info(message.trim()) } }));

// Parse JSON body
app.use(express.json());

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', authMiddleware, userRoutes);
app.use('/api/chat', authMiddleware, chatRoutes);
app.use('/api/tokens', authMiddleware, tokenRoutes);
app.use('/api/solana', authMiddleware, solanaRoutes);

// Health check route
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'OK', timestamp: new Date() });
});

// Error handling middleware
app.use(errorHandler);

// Start server
const PORT = process.env.PORT || 5000;

// Connect to MongoDB if enabled
if (process.env.USE_MONGODB === 'true') {
  connectDB()
    .then(() => {
      logger.info('MongoDB connected successfully');
      startServer();
    })
    .catch(err => {
      logger.error(`MongoDB connection error: ${err.message}`);
      // Start server anyway, some features will be limited
      startServer();
    });
} else {
  startServer();
}

function startServer() {
  app.listen(PORT, () => {
    logger.info(`Server running on port ${PORT}`);
  });
}

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  logger.error(`Unhandled Rejection: ${err.message}`);
  // Close server & exit process
  // process.exit(1);
});

module.exports = app; 