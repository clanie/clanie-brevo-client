/*
 * Copyright (C) 2026, Claus Nielsen, clausn999@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package dk.clanie.brevo;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for the Brevo API client.
 */
@Data
@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {

	/** Brevo API key. */
	private String apiKey;

	/** Brevo API base URL. Defaults to the official v3 endpoint. */
	private String url = "https://api.brevo.com/v3";

	/** Whether to enable wiretap logging of HTTP requests/responses. */
	private boolean wiretap = false;

	/**
	 * Whether to use Brevo's sandbox mode.
	 * In sandbox mode, emails are accepted but not actually delivered.
	 * Useful for testing.
	 */
	private boolean sandbox = false;

	/**
	 * Secret token that Brevo includes in the configured request header on every
	 * webhook call. The controller compares the received header value against this
	 * secret and rejects the request if they don't match. Leave blank to skip
	 * verification (not recommended in production).
	 */
	private String webhookSecret;

	/**
	 * Name of the HTTP header that carries the webhook secret.
	 * Must match what is configured in the Brevo webhook settings.
	 * Defaults to {@code X-Brevo-Webhook-Secret}.
	 */
	private String webhookSecretHeader = "X-Brevo-Webhook-Secret";

	/**
	 * Named sender configurations, keyed by purpose (e.g. {@code transactional}).
	 * <p>
	 * Example YAML:
	 * <pre>
	 * brevo:
	 *   senders:
	 *     transactional:
	 *       email: noreply@clanie.dk
	 *       senderNameKey: brevo.sender.transactional.name
	 * </pre>
	 * The {@code senderNameKey} is a message-source key resolved at send time
	 * using the recipient's language, allowing localised sender names.
	 */
	private Map<String, SenderConfig> senders = Map.of();


	/**
	 * Returns the {@link SenderConfig} for the given purpose.
	 *
	 * @param purpose sender purpose key (e.g. {@code "transactional"})
	 * @return the matching config
	 * @throws IllegalArgumentException if no sender is configured for the purpose
	 */
	public SenderConfig getSender(String purpose) {
		SenderConfig config = senders.get(purpose);
		if (config == null) {
			throw new IllegalArgumentException(
					"No Brevo sender configured for purpose '" + purpose + "'. "
					+ "Add brevo.senders." + purpose + " to your configuration.");
		}
		return config;
	}


	@Data
	public static class SenderConfig {

		/** Email address shown as the sender. */
		private String email;

		/**
		 * Message-source key whose value is the sender display name.
		 * Resolved using the recipient's language at send time.
		 */
		private String senderNameKey;

	}

}