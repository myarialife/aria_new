// ARIA Token Contract
// A minimal token implementation for the Solana blockchain

use solana_program::{
    account_info::{next_account_info, AccountInfo},
    entrypoint,
    entrypoint::ProgramResult,
    msg,
    program_error::ProgramError,
    pubkey::Pubkey,
    sysvar::{rent::Rent, Sysvar},
};
use spl_token::{
    instruction::{initialize_mint, mint_to},
    state::{Account, Mint},
};
use borsh::{BorshDeserialize, BorshSerialize};

// Program entry point
entrypoint!(process_instruction);

// Token configuration
const TOKEN_DECIMALS: u8 = 9;
const TOTAL_SUPPLY: u64 = 100_000_000_000_000_000; // 100 million with 9 decimals

#[derive(BorshSerialize, BorshDeserialize, Debug)]
pub enum ARIAInstruction {
    /// Initialize a new ARIA token
    /// Accounts expected:
    /// 0. `[signer]` The authority that will mint tokens
    /// 1. `[writable]` The mint account to initialize
    /// 2. `[]` The rent sysvar
    /// 3. `[]` The token program
    InitializeMint,

    /// Mint tokens to an account
    /// Accounts expected:
    /// 0. `[signer]` The minting authority
    /// 1. `[writable]` The mint account
    /// 2. `[writable]` The destination account
    /// 3. `[]` The token program
    MintTokens { amount: u64 },
}

pub fn process_instruction(
    program_id: &Pubkey,
    accounts: &[AccountInfo],
    instruction_data: &[u8],
) -> ProgramResult {
    let instruction = ARIAInstruction::try_from_slice(instruction_data)
        .map_err(|_| ProgramError::InvalidInstructionData)?;

    match instruction {
        ARIAInstruction::InitializeMint => {
            msg!("Instruction: Initialize ARIA Mint");
            process_initialize_mint(program_id, accounts)
        }
        ARIAInstruction::MintTokens { amount } => {
            msg!("Instruction: Mint Tokens");
            process_mint_tokens(accounts, amount)
        }
    }
}

pub fn process_initialize_mint(program_id: &Pubkey, accounts: &[AccountInfo]) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    let authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;
    let rent_info = next_account_info(account_info_iter)?;
    let token_program_info = next_account_info(account_info_iter)?;

    let rent = &Rent::from_account_info(rent_info)?;

    // Create the initialize mint instruction
    let initialize_mint_instruction = initialize_mint(
        &spl_token::id(),
        mint_info.key,
        authority_info.key,
        Some(authority_info.key), // Freeze authority
        TOKEN_DECIMALS,
    )?;

    // Execute the initialize mint instruction
    solana_program::program::invoke_signed(
        &initialize_mint_instruction,
        &[
            mint_info.clone(),
            rent_info.clone(),
            authority_info.clone(),
            token_program_info.clone(),
        ],
        &[],
    )?;

    msg!("ARIA token mint initialized successfully");
    Ok(())
}

pub fn process_mint_tokens(accounts: &[AccountInfo], amount: u64) -> ProgramResult {
    let account_info_iter = &mut accounts.iter();
    let authority_info = next_account_info(account_info_iter)?;
    let mint_info = next_account_info(account_info_iter)?;
    let destination_info = next_account_info(account_info_iter)?;
    let token_program_info = next_account_info(account_info_iter)?;

    // Create the mint to instruction
    let mint_to_instruction = mint_to(
        &spl_token::id(),
        mint_info.key,
        destination_info.key,
        authority_info.key,
        &[],
        amount,
    )?;

    // Execute the mint to instruction
    solana_program::program::invoke_signed(
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

#[cfg(test)]
mod tests {
    // Tests will be added here
} 