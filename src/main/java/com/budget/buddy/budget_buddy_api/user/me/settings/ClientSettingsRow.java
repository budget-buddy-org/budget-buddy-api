package com.budget.buddy.budget_buddy_api.user.me.settings;

import java.util.Map;

/**
 * Persistence view of one per-client settings row. {@code settings} is an opaque JSON object owned
 * by the client app; the server stores it verbatim and does not validate its shape.
 */
record ClientSettingsRow(String clientId, Map<String, Object> settings) {
}
