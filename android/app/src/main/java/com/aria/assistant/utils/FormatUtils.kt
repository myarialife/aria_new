package com.aria.assistant.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Format ARIA token amount
 */
fun formatAriaTokenAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%.4f ARIA", amount)
}

/**
 * Format SOL amount
 */
fun formatSolAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%.6f SOL", amount)
}

/**
 * Format timestamp to date
 */
fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

/**
 * Format timestamp to relative time
 */
fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 30 * 24 * 60 * 60 * 1000L -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> formatTimestamp(timestamp)
    }
}

/**
 * Format file size
 */
fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    
    return String.format(
        Locale.getDefault(),
        "%.1f %s",
        size / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}

/**
 * Format wallet address (shorten display)
 */
fun formatWalletAddress(address: String): String {
    if (address.length <= 12) return address
    return "${address.substring(0, 6)}...${address.substring(address.length - 6)}"
}

/**
 * Format transaction ID (shorten display)
 */
fun formatTransactionId(txId: String): String {
    if (txId.length <= 12) return txId
    return "${txId.substring(0, 6)}...${txId.substring(txId.length - 6)}"
}

/**
 * Format data type to readable text
 */
fun formatDataType(type: String): String {
    return when (type.lowercase()) {
        "location" -> "Location Data"
        "contacts" -> "Contacts Data"
        "calendar" -> "Calendar Data"
        "sms" -> "SMS Data"
        "other" -> "Other Data"
        else -> type
    }
}

/**
 * Format privacy level to readable text
 */
fun formatPrivacyLevel(level: String): String {
    return when (level.lowercase()) {
        "public" -> "Public"
        "protected" -> "Protected"
        "private" -> "Private"
        "anonymized" -> "Anonymized"
        else -> level
    }
}