package com.budget.buddy.budget_buddy_api.user.me.preferences;

/**
 * Persistence view of a user's global preferences. Maps 1:1 to the {@code user_preferences} row.
 */
record UserPreferencesRow(String language, String currency, String timezone) {
}
