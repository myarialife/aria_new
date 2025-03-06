# GitHub Setup Guide for ARIA Project

This guide will help you set up and push the ARIA project to GitHub. Follow these steps to properly configure your repository and upload all project files.

## Prerequisites

- Git installed on your machine
- GitHub account
- Basic knowledge of Git commands
- Terminal/Command Prompt access

## Step 1: Create a New GitHub Repository

1. Go to [GitHub](https://github.com) and log in to your account
2. Click the "+" button in the top-right corner and select "New repository"
3. Repository name: `ARIA` (or your preferred name)
4. Description: "Decentralized AI Personal Assistant on Solana Blockchain"
5. Choose "Public" or "Private" visibility as needed
6. Check "Add a README file"
7. Choose "MIT License" from the Add a license dropdown
8. Click "Create repository"

## Step 2: Clone the Repository Locally

```bash
# Clone the repository to your local machine
git clone https://github.com/YOUR_USERNAME/ARIA.git

# Navigate to the repository directory
cd ARIA
```

## Step 3: Organize the Project Structure

Our project structure should follow the organization we've created:

```
ARIA/
├── README.md                    # Project overview and documentation
├── .gitignore                   # Git ignore file
├── LICENSE                      # MIT License file
├── contract/                    # Solana smart contracts
│   ├── Cargo.toml               # Rust dependencies
│   ├── README.md                # Token contract documentation
│   └── src/                     # Contract source code
├── scripts/                     # Deployment and utility scripts
│   ├── deploy_token.js          # Token deployment script
│   └── pump_fun_listing.js      # pump.fun listing setup script
├── docs/                        # Documentation
│   ├── security_audit.md        # Security audit report
│   └── tokenomics.md            # Token economics specifications
├── app/                         # Android application
├── frontend/                    # Web frontend
├── backend/                     # Node.js backend server
└── docker-compose.yml           # Docker configuration
```

Ensure this structure is reflected in your repository.

## Step 4: Create .gitignore File

Create a comprehensive `.gitignore` file to exclude unnecessary files:

```bash
# Create or edit .gitignore file
cat > .gitignore << EOL
# Dependencies
node_modules/
package-lock.json
yarn.lock

# Build outputs
build/
dist/
target/
.next/

# Environment files
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# Debug logs
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# IDE and editor files
.idea/
.vscode/
*.swp
*.swo

# OS specific files
.DS_Store
Thumbs.db

# Wallet key files
*-wallet*.json
*-keypair*.json

# Deployment logs
deployment-log.json
pump-fun-log.json
devnet-test-log.json
EOL
```

## Step 5: Copy Project Files

Copy all your project files into the appropriate directories in the cloned repository:

```bash
# Copy each component to the right directory
# For example:
cp -r /path/to/your/contract/* ./contract/
cp -r /path/to/your/scripts/* ./scripts/
cp -r /path/to/your/docs/* ./docs/
# ... and so on for each project component
```

## Step 6: Add and Commit Files

```bash
# Add all files to git
git add .

# Commit with a descriptive message
git commit -m "Initial commit: ARIA - Decentralized AI Assistant on Solana"
```

## Step 7: Push to GitHub

```bash
# Push to the main branch
git push origin main
```

## Step 8: Security Considerations

1. **Protect Private Keys**: Ensure that any private keys, wallet files, or sensitive credentials are included in `.gitignore` and NOT committed to the repository.

2. **Environment Variables**: Use environment variables for sensitive configuration like API keys rather than hardcoding them in the source code.

3. **Audit Before Publishing**: Review all code for sensitive information before pushing to a public repository.

## Step 9: Set Up GitHub Pages (Optional)

If you want to create a project website:

1. Go to your repository on GitHub
2. Navigate to Settings > Pages
3. Under Source, select "main" branch and "/docs" folder
4. Click Save

Your project website will be available at: `https://YOUR_USERNAME.github.io/ARIA/`

## Step 10: Add Project Collaborators (Optional)

1. Go to your repository on GitHub
2. Navigate to Settings > Collaborators
3. Click "Add people" and enter the usernames or email addresses of your team members
4. Choose the appropriate permission level and send invitations

## Step 11: Set Up Branch Protection (Optional)

For better code quality and security:

1. Go to your repository on GitHub
2. Navigate to Settings > Branches
3. Click "Add rule" under "Branch protection rules"
4. Enter "main" in the Branch name pattern
5. Select options like "Require pull request reviews before merging" and "Require status checks to pass before merging"
6. Click "Create"

## Troubleshooting

### Large File Issues
If you encounter problems pushing large files:
```bash
git config http.postBuffer 524288000
```

### Authentication Issues
If you're having authentication problems:
```bash
git config --global credential.helper cache
git config --global credential.helper 'cache --timeout=3600'
```

### Merge Conflicts
If you encounter merge conflicts:
```bash
git pull
# Resolve conflicts in your editor
git add .
git commit -m "Resolved merge conflicts"
git push
```

## Next Steps

After pushing your code to GitHub, consider:

1. Creating detailed documentation in the README.md file
2. Setting up CI/CD with GitHub Actions for automated testing and deployment
3. Creating issues for tracking work and bugs
4. Establishing a contribution guideline document

Congratulations! Your ARIA project is now on GitHub and ready for collaboration. 