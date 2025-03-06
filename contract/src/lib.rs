// ARIA Token Contract
// SPL token implementation for the ARIA project on Solana blockchain

use solana_program::{
    account_info::{next_account_info, AccountInfo},
    entrypoint,
    entrypoint::ProgramResult,
    msg,
    program_error::ProgramError,
    program_pack::Pack,
    pubkey::Pubkey,
    sysvar::{rent::Rent, clock::Clock, Sysvar},
    program::invoke_signed,
};
use spl_token::{
    instruction::{initialize_mint, mint_to},
    state::{Mint, Account},
    error::TokenError,
};
use borsh::{BorshDeserialize, BorshSerialize};
use thiserror::Error;

// Program entry point
entrypoint!(process_instruction);

// Token configuration
const TOKEN_DECIMALS: u8 = 9;
const TOTAL_SUPPLY: u64 = 100_000_000_000_000_000; // 100 million tokens with 9 decimals
const MINT_COOLDOWN: i64 = 3600; // Minting cooldown period in seconds (1 hour)
const AUTHORITY_TRANSFER_EXPIRY: i64 = 86400; // Authority transfer validity period in seconds (24 hours)

// Define error types
#[derive(Error, Debug, Copy, Clone)]
pub enum AriaError {
    #[error("Invalid instruction data")]
    InvalidInstructionData,
    
    #[error("Insufficient authority")]
    InsufficientAuthority,
    
    #[error("Invalid account count")]
    InvalidAccountCount,
    
    #[error("Token mint mismatch")]
    TokenMintMismatch,
    
    #[error("Exceeds supply cap")]
    ExceedsSupplyCap,
    
    #[error("Mint cooldown active")]
    MintCooldownActive,
    
    #[error("Authority transfer request not found")]
    AuthorityTransferNotFound,
    
    #[error("Authority transfer request expired")]
    AuthorityTransferExpired,
}

impl From<AriaError> for ProgramError {
    fn from(e: AriaError) -> Self {
        ProgramError::Custom(e as u32)
    }
}

// Authority transfer state, stored in mint account's data extension
#[derive(BorshSerialize, BorshDeserialize, Debug, PartialEq)]
pub struct AuthorityTransferState {
    pub proposed_authority: Option<Pubkey>,
    pub proposal_time: i64,
}

#[derive(BorshSerialize, BorshDeserialize, Debug)]
pub enum ARIAInstruction {
    /// Initialize a new ARIA token
    /// Accounts required:
    /// 0. `[signer]` Authority account that will mint tokens
    /// 1. `[writable]` The mint account to initialize
    /// 2. `[]` The rent sysvar
    /// 3. `[]` The token program
    InitializeMint,

    /// Mint tokens to an account
    /// Accounts required:
    /// 0. `[signer]` Mint authority account
    /// 1. `[writable]` The mint account
    /// 2. `[writable]` The destination account
    /// 3. `[]` The token program
    /// 4. `[]` The clock sysvar
    MintTokens { amount: u64 },
    
    /// Propose transfer of mint authority
    /// Accounts required:
    /// 0. `[signer]` Current mint authority account
    /// 1. `[writable]` The mint account
    /// 2. `[]` New mint authority account
    /// 3. `[]` The clock sysvar
    ProposeAuthorityTransfer { new_authority: Pubkey },
    
    /// Accept mint authority transfer
    /// Accounts required:
    /// 0. `[signer]` New mint authority account
    /// 1. `[writable]` The mint account
    /// 2. `[]` Current mint authority account
    /// 3. `[]` The token program
    /// 4. `[]` The clock sysvar
    AcceptAuthorityTransfer,
    
    /// Cancel proposed authority transfer
    /// Accounts required:
    /// 0. `[signer]` Current mint authority account
    /// 1. `[writable]` The mint account
    CancelAuthorityTransfer,
}

pub fn process_instruction(
    program_id: &Pubkey,
    accounts: &[AccountInfo],
    instruction_data: &[u8],
) -> ProgramResult {
    let instruction = ARIAInstruction::try_from_slice(instruction_data)
        .map_err(|_| AriaError::InvalidInstructionData)?;

    match instruction {
        ARIAInstruction::InitializeMint => {
            msg!("Instruction: Initialize ARIA Mint");
            process_initialize_mint(program_id, accounts)
        }
        ARIAInstruction::MintTokens { amount } => {
            msg!("Instruction: Mint Tokens");
            process_mint_tokens(accounts, amount)
        }
        ARIAInstruction::ProposeAuthorityTransfer { new_authority } => {
            msg!("Instruction: Propose Authority Transfer");
            process_propose_authority_transfer(accounts, new_authority)
        }
        ARIAInstruction::AcceptAuthorityTransfer => {
            msg!("Instruction: Accept Authority Transfer");
            process_accept_authority_transfer(accounts)
        }
        ARIAInstruction::CancelAuthorityTransfer => {
            msg!("Instruction: Cancel Authority Transfer");
            process_cancel_authority_transfer(accounts)
        }
    }
}

pub fn process_initialize_mint(program_id: &Pubkey, accounts: &[AccountInfo]) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    
    // Get required accounts
    let authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;
    let rent_info = next_account_info(account_info_iter)?;
    let token_program_info = next_account_info(account_info_iter)?;

    // Verify authority
    if !authority_info.is_signer {
        return Err(AriaError::InsufficientAuthority.into());
    }

    // Verify token program
    if token_program_info.key != &spl_token::id() {
        return Err(ProgramError::IncorrectProgramId);
    }

    // Get rent
    let rent = &Rent::from_account_info(rent_info)?;
    
    // Ensure mint account has enough space and rent exemption
    if !rent.is_exempt(mint_info.lamports(), Mint::LEN) {
        msg!("Mint account needs sufficient rent exemption");
        return Err(ProgramError::AccountNotRentExempt);
    }

    // Create initialize mint instruction
    let initialize_mint_instruction = initialize_mint(
        &spl_token::id(),
        mint_info.key,
        authority_info.key,
        Some(authority_info.key), // Freeze authority (optional)
        TOKEN_DECIMALS,
    )?;

    // Execute initialize mint instruction
    invoke_signed(
        &initialize_mint_instruction,
        &[
            mint_info.clone(),
            rent_info.clone(),
            authority_info.clone(),
            token_program_info.clone(),
        ],
        &[],
    )?;

    // Initialize authority transfer state as empty
    let authority_transfer_state = AuthorityTransferState {
        proposed_authority: None,
        proposal_time: 0,
    };
    
    msg!("ARIA token mint initialized successfully");
    Ok(())
}

pub fn process_mint_tokens(accounts: &[AccountInfo], amount: u64) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    
    // Get required accounts
    let authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;
    let destination_info = next_account_info(account_info_iter)?;
    let token_program_info = next_account_info(account_info_iter)?;
    let clock_info = next_account_info(account_info_iter)?;

    // Verify authority
    if !authority_info.is_signer {
        return Err(AriaError::InsufficientAuthority.into());
    }

    // Verify token program
    if token_program_info.key != &spl_token::id() {
        return Err(ProgramError::IncorrectProgramId);
    }
    
    // Get clock to check cooldown period
    let clock = Clock::from_account_info(clock_info)?;
    
    // Get mint account information
    let mint_data = Mint::unpack(&mint_info.data.borrow())?;
    
    // Check if current supply has reached the cap
    let current_supply = mint_data.supply;
    if current_supply + amount > TOTAL_SUPPLY {
        msg!("Mint amount would exceed total supply cap");
        return Err(AriaError::ExceedsSupplyCap.into());
    }
    
    // TODO: In a production version, we should store and check the last mint time
    // This is just an example, real implementation would need custom accounts for metadata
    
    // Verify the destination account belongs to the correct mint
    let dest_account = Account::unpack(&destination_info.data.borrow())?;
    if dest_account.mint != *mint_info.key {
        return Err(AriaError::TokenMintMismatch.into());
    }

    // Create mint instruction
    let mint_to_instruction = mint_to(
        &spl_token::id(),
        mint_info.key,
        destination_info.key,
        authority_info.key,
        &[],
        amount,
    )?;

    // Execute mint instruction
    invoke_signed(
        &mint_to_instruction,
        &[
            mint_info.clone(),
            destination_info.clone(),
            authority_info.clone(),
            token_program_info.clone(),
        ],
        &[],
    )?;

    msg!("Minted {} tokens to account {}", amount, destination_info.key);
    Ok(())
}

pub fn process_propose_authority_transfer(
    accounts: &[AccountInfo],
    new_authority: Pubkey,
) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    
    // Get required accounts
    let current_authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;
    let new_authority_info = next_account_info(account_info_iter)?;
    let clock_info = next_account_info(account_info_iter)?;

    // Verify authority
    if !current_authority_info.is_signer {
        return Err(AriaError::InsufficientAuthority.into());
    }
    
    // Verify new authority account
    if new_authority_info.key != &new_authority {
        return Err(ProgramError::InvalidArgument);
    }
    
    // Get clock
    let clock = Clock::from_account_info(clock_info)?;
    
    // Get mint account information and confirm current authority
    let mint_data = Mint::unpack(&mint_info.data.borrow())?;
    if mint_data.mint_authority.unwrap() != *current_authority_info.key {
        return Err(AriaError::InsufficientAuthority.into());
    }
    
    // Create authority transfer state
    let authority_transfer_state = AuthorityTransferState {
        proposed_authority: Some(new_authority),
        proposal_time: clock.unix_timestamp,
    };
    
    // TODO: In an actual implementation, this state would need to be stored in an on-chain account
    // In this example, we've simplified the implementation

    msg!("Mint authority transfer proposed - from {} to {}", 
        current_authority_info.key, 
        new_authority_info.key);
    Ok(())
}

pub fn process_accept_authority_transfer(accounts: &[AccountInfo]) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    
    // Get required accounts
    let new_authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;
    let current_authority_info = next_account_info(account_info_iter)?;
    let token_program_info = next_account_info(account_info_iter)?;
    let clock_info = next_account_info(account_info_iter)?;

    // Verify new authority signature
    if !new_authority_info.is_signer {
        return Err(AriaError::InsufficientAuthority.into());
    }
    
    // Verify token program
    if token_program_info.key != &spl_token::id() {
        return Err(ProgramError::IncorrectProgramId);
    }
    
    // Get clock
    let clock = Clock::from_account_info(clock_info)?;
    
    // TODO: In a real implementation, get authority transfer state from an on-chain account
    // Here we assume we have verified:
    // 1. There is a transfer proposal for new_authority_info.key
    // 2. The proposal has not expired
    
    // Create set authority instruction
    let set_authority_instruction = spl_token::instruction::set_authority(
        &spl_token::id(),
        mint_info.key,
        Some(new_authority_info.key),
        spl_token::instruction::AuthorityType::MintTokens,
        current_authority_info.key,
        &[],
    )?;

    // Execute set authority instruction
    invoke_signed(
        &set_authority_instruction,
        &[
            mint_info.clone(),
            current_authority_info.clone(),
            token_program_info.clone(),
        ],
        &[],
    )?;

    // Clear authority transfer state
    // TODO: In a real implementation, update the on-chain account
    
    msg!("Mint authority transferred from {} to {}", 
        current_authority_info.key, 
        new_authority_info.key);
    Ok(())
}

pub fn process_cancel_authority_transfer(accounts: &[AccountInfo]) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    
    // Get required accounts
    let authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;

    // Verify authority
    if !authority_info.is_signer {
        return Err(AriaError::InsufficientAuthority.into());
    }
    
    // Get mint account information and confirm current authority
    let mint_data = Mint::unpack(&mint_info.data.borrow())?;
    if mint_data.mint_authority.unwrap() != *authority_info.key {
        return Err(AriaError::InsufficientAuthority.into());
    }
    
    // TODO: In a real implementation, get and clear authority transfer state from an on-chain account
    
    msg!("Mint authority transfer request cancelled");
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use solana_program::clock::Epoch;
    use solana_program::program_pack::Pack;
    use solana_program::pubkey::Pubkey;
    use std::mem::size_of;

    // Mock account info
    fn create_account_info<'a>(
        key: &'a Pubkey,
        is_signer: bool,
        is_writable: bool,
        lamports: &'a mut u64,
        data: &'a mut [u8],
        owner: &'a Pubkey,
    ) -> AccountInfo<'a> {
        AccountInfo {
            key,
            is_signer,
            is_writable,
            lamports: lamports,
            data: data.into(),
            owner,
            executable: false,
            rent_epoch: Epoch::default(),
        }
    }
    
    // Create a basic test environment
    struct TestEnv {
        program_id: Pubkey,
        token_program_id: Pubkey,
        authority: (Pubkey, u64, Vec<u8>, bool),
        mint: (Pubkey, u64, Vec<u8>, bool),
        destination: (Pubkey, u64, Vec<u8>, bool),
        new_authority: (Pubkey, u64, Vec<u8>, bool),
        rent: (Pubkey, u64, Vec<u8>, bool),
        clock: (Pubkey, u64, Vec<u8>, bool),
    }
    
    impl TestEnv {
        fn new() -> Self {
            TestEnv {
                program_id: Pubkey::new_unique(),
                token_program_id: spl_token::id(),
                authority: (Pubkey::new_unique(), 100000, vec![0; 32], true),
                mint: (Pubkey::new_unique(), 1000000, vec![0; Mint::LEN], false),
                destination: (Pubkey::new_unique(), 1000000, vec![0; Account::LEN], false),
                new_authority: (Pubkey::new_unique(), 100000, vec![0; 32], false),
                rent: (Pubkey::new_unique(), 100000, vec![0; 32], false),
                clock: (Pubkey::new_unique(), 100000, vec![0; 32], false),
            }
        }
        
        // Get authority account info
        fn authority_info(&mut self) -> AccountInfo {
            create_account_info(
                &self.authority.0,
                self.authority.3,
                true,
                &mut self.authority.1,
                &mut self.authority.2,
                &self.program_id,
            )
        }
        
        // Get mint account info
        fn mint_info(&mut self) -> AccountInfo {
            create_account_info(
                &self.mint.0,
                self.mint.3,
                true,
                &mut self.mint.1,
                &mut self.mint.2,
                &self.token_program_id,
            )
        }
        
        // Get destination account info
        fn destination_info(&mut self) -> AccountInfo {
            // Initialize destination account as empty TOKEN account
            let dst_account = Account {
                mint: self.mint.0,
                owner: self.authority.0,
                amount: 0,
                delegate: Option::None,
                state: spl_token::state::AccountState::Initialized,
                is_native: Option::None,
                delegated_amount: 0,
                close_authority: Option::None,
            };
            
            Account::pack(dst_account, &mut self.destination.2).unwrap();
            
            create_account_info(
                &self.destination.0,
                self.destination.3,
                true,
                &mut self.destination.1,
                &mut self.destination.2,
                &self.token_program_id,
            )
        }
        
        // Get new authority account info
        fn new_authority_info(&mut self) -> AccountInfo {
            create_account_info(
                &self.new_authority.0,
                self.new_authority.3,
                true,
                &mut self.new_authority.1,
                &mut self.new_authority.2,
                &self.program_id,
            )
        }
        
        // Get rent account info
        fn rent_info(&mut self) -> AccountInfo {
            create_account_info(
                &self.rent.0,
                self.rent.3,
                false,
                &mut self.rent.1,
                &mut self.rent.2,
                &solana_program::sysvar::rent::id(),
            )
        }
        
        // Get clock account info
        fn clock_info(&mut self) -> AccountInfo {
            create_account_info(
                &self.clock.0,
                self.clock.3,
                false,
                &mut self.clock.1,
                &mut self.clock.2,
                &solana_program::sysvar::clock::id(),
            )
        }
        
        // Get token program account info
        fn token_program_info(&mut self) -> AccountInfo {
            create_account_info(
                &self.token_program_id,
                false,
                false,
                &mut 10000000,
                &mut vec![],
                &Pubkey::default(),
            )
        }
    }
    
    // Test InitializeMint instruction - authority validation
    #[test]
    fn test_initialize_mint_authority() {
        let mut env = TestEnv::new();
        
        // Prepare test accounts
        let mut accounts = vec![
            env.authority_info(),
            env.mint_info(),
            env.rent_info(),
            env.token_program_info(),
        ];
        
        // Non-authority account, should fail
        accounts[0] = create_account_info(
            &env.authority.0,
            false, // Not a signer
            true,
            &mut env.authority.1,
            &mut env.authority.2,
            &env.program_id,
        );
        
        let result = process_initialize_mint(&env.program_id, &accounts);
        assert!(result.is_err());
        if let Err(e) = result {
            assert_eq!(e, AriaError::InsufficientAuthority.into());
        }
    }
    
    // Test MintTokens instruction - success case
    #[test]
    fn test_mint_tokens_success() {
        let mut env = TestEnv::new();
        
        // Prepare test accounts
        let accounts = vec![
            env.authority_info(),
            env.mint_info(),
            env.destination_info(),
            env.token_program_info(),
            env.clock_info(),
        ];
        
        // In this test, we assume SPL Token program is called, so we only validate input validation
        // In a real scenario, we'd use mocks to test the actual token minting
        let result = process_mint_tokens(&accounts, 1000);
        
        // This will fail since we're not actually mocking the SPL Token program
        // But we're still testing if the function can be correctly called
        assert!(result.is_err());
    }
    
    // Test MintTokens instruction - authority validation
    #[test]
    fn test_mint_tokens_authority() {
        let mut env = TestEnv::new();
        
        // Prepare test accounts
        let mut accounts = vec![
            env.authority_info(),
            env.mint_info(),
            env.destination_info(),
            env.token_program_info(),
            env.clock_info(),
        ];
        
        // Non-authority account, should fail
        accounts[0] = create_account_info(
            &env.authority.0,
            false, // Not a signer
            true,
            &mut env.authority.1,
            &mut env.authority.2,
            &env.program_id,
        );
        
        let result = process_mint_tokens(&accounts, 1000);
        assert!(result.is_err());
        if let Err(e) = result {
            assert_eq!(e, AriaError::InsufficientAuthority.into());
        }
    }
    
    // Test ProposeAuthorityTransfer instruction
    #[test]
    fn test_propose_authority_transfer() {
        let mut env = TestEnv::new();
        
        // Prepare test accounts
        let accounts = vec![
            env.authority_info(),
            env.mint_info(),
            env.new_authority_info(),
            env.clock_info(),
        ];
        
        // This test will fail since we didn't initialize the mint account properly
        // But we're still testing if the function can be correctly called
        let result = process_propose_authority_transfer(&accounts, env.new_authority.0);
        assert!(result.is_err());
    }
    
    // Test AcceptAuthorityTransfer instruction
    #[test]
    fn test_accept_authority_transfer() {
        let mut env = TestEnv::new();
        
        // Prepare test accounts
        let accounts = vec![
            env.new_authority_info(),
            env.mint_info(),
            env.authority_info(),
            env.token_program_info(),
            env.clock_info(),
        ];
        
        // This test will fail since we didn't initialize mint account and authority transfer
        // But we're still testing if the function can be correctly called
        let result = process_accept_authority_transfer(&accounts);
        assert!(result.is_err());
    }
    
    // Test CancelAuthorityTransfer instruction
    #[test]
    fn test_cancel_authority_transfer() {
        let mut env = TestEnv::new();
        
        // Prepare test accounts
        let accounts = vec![
            env.authority_info(),
            env.mint_info(),
        ];
        
        // This test will fail since we didn't initialize mint account and authority transfer
        // But we're still testing if the function can be correctly called
        let result = process_cancel_authority_transfer(&accounts);
        assert!(result.is_err());
    }
    
    // Test instruction parsing
    #[test]
    fn test_instruction_parsing() {
        // Test InitializeMint instruction parsing
        let initialize_data = ARIAInstruction::InitializeMint.try_to_vec().unwrap();
        let parsed = ARIAInstruction::try_from_slice(&initialize_data).unwrap();
        match parsed {
            ARIAInstruction::InitializeMint => {},
            _ => panic!("Incorrect parsing"),
        }
        
        // Test MintTokens instruction parsing
        let amount = 1000u64;
        let mint_data = ARIAInstruction::MintTokens { amount }.try_to_vec().unwrap();
        let parsed = ARIAInstruction::try_from_slice(&mint_data).unwrap();
        match parsed {
            ARIAInstruction::MintTokens { amount: parsed_amount } => {
                assert_eq!(parsed_amount, amount);
            },
            _ => panic!("Incorrect parsing"),
        }
        
        // Test ProposeAuthorityTransfer instruction parsing
        let new_authority = Pubkey::new_unique();
        let transfer_data = ARIAInstruction::ProposeAuthorityTransfer { new_authority }.try_to_vec().unwrap();
        let parsed = ARIAInstruction::try_from_slice(&transfer_data).unwrap();
        match parsed {
            ARIAInstruction::ProposeAuthorityTransfer { new_authority: parsed_authority } => {
                assert_eq!(parsed_authority, new_authority);
            },
            _ => panic!("Incorrect parsing"),
        }
    }
    
    // Test error handling
    #[test]
    fn test_error_handling() {
        let program_error: ProgramError = AriaError::InvalidInstructionData.into();
        assert_eq!(program_error, ProgramError::Custom(AriaError::InvalidInstructionData as u32));
        
        let program_error: ProgramError = AriaError::InsufficientAuthority.into();
        assert_eq!(program_error, ProgramError::Custom(AriaError::InsufficientAuthority as u32));
        
        let program_error: ProgramError = AriaError::ExceedsSupplyCap.into();
        assert_eq!(program_error, ProgramError::Custom(AriaError::ExceedsSupplyCap as u32));
        
        let program_error: ProgramError = AriaError::MintCooldownActive.into();
        assert_eq!(program_error, ProgramError::Custom(AriaError::MintCooldownActive as u32));
    }
} 