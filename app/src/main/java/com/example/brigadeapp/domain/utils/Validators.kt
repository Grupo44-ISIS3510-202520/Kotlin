package com.example.brigadeapp.domain.utils


private val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")

private fun hasEmoji(text: String) = emojiRegex.containsMatchIn(text)

fun validateEmailDomain(
    value: String?,
    domain: String = AuthConstants.ALLOWED_EMAIL_DOMAIN,
    maxLen: Int = 30
): String? {
    val v = value?.trim().orEmpty()
    if (v.isEmpty()) return "Email is required"
    if (hasEmoji(v)) return "No emojis are allowed"
    if (v.length > maxLen) return "Maximum $maxLen characters"
    val domainRegex = Regex("^[^@\\s]+@${Regex.escape(domain)}$", RegexOption.IGNORE_CASE)
    if (!domainRegex.matches(v)) return "Use your @$domain email"
    return null
}

fun validateName(v: String?): String? {
    val t = v?.trim().orEmpty()
    if (t.isEmpty()) return "Name is required"
    if (hasEmoji(t)) return "No emojis are allowed"
    if (t.length > 15) return "Maximum 15 characters"
    return null
}

fun validateLastName(v: String?): String? {
    val t = v?.trim().orEmpty()
    if (t.isEmpty()) return "Last name is required"
    if (hasEmoji(t)) return "No emojis are allowed"
    if (t.length > 15) return "Maximum 15 characters"
    return null
}

fun validateUniandesCode(v: String?): String? {
    val t = v?.trim().orEmpty()
    if (t.isEmpty()) return "Uniandes code is required"
    if (!Regex("^\\d{6,12}$").matches(t)) return "Invalid Uniandes code"
    return null
}

fun validateBloodGroup(v: String?): String? =
    if (v != null && AuthConstants.BLOOD_GROUPS.contains(v)) null else "Select a valid blood group"

fun validateRole(v: String?): String? =
    if (v != null && AuthConstants.ROLES.contains(v)) null else "Select a valid role"

fun validatePassword(v: String?): String? {
    val t = v ?: ""
    if (t.isEmpty()) return "Password is required"
    if (hasEmoji(t)) return "No emojis are allowed"
    if (t.length < 6) return "Minimum 6 characters"
    if (t.length > 20) return "Maximum 20 characters"
    return null
}

fun validatePasswordConfirm(confirm: String?, original: String): String? {
    val c = confirm ?: ""
    if (c.isEmpty()) return "Confirmation is required"
    if (c != original) return "Passwords do not match"
    return null
}
