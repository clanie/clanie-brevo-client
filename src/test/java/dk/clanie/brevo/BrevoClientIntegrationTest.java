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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import dk.clanie.brevo.dto.SendEmailResponse;
import dk.clanie.web.RestClientFactory;

/**
 * Integration test for {@link BrevoClient} against the real Brevo sandbox API.
 * <p>
 * Requires a valid {@code BREVO_API_KEY} environment variable.
 * Tagged {@code "integration"} so it is excluded from the default build.
 * Run with: {@code mvn test -Dgroups=integration}
 * <p>
 * Brevo's sandbox mode accepts requests and returns a {@code messageId} without
 * actually delivering the email.
 */
@Tag("integration")
class BrevoClientIntegrationTest {

	private static final String BREVO_API_URL = "https://api.brevo.com/v3";

	private BrevoClient brevoClient;


	@BeforeEach
	void setUp() {
		String apiKey = System.getenv("BREVO_API_KEY");
		if (apiKey == null || apiKey.isBlank()) {
			// Fall back to a placeholder that will cause a 401 — test will still exercise the HTTP path
			apiKey = "placeholder-key";
		}

		RestClientFactory restClientFactory = new RestClientFactory(RestClient.builder());

		BrevoProperties properties = new BrevoProperties();
		properties.setApiKey(apiKey);
		properties.setUrl(BREVO_API_URL);
		properties.setSandbox(true); // Sandbox: accepted but not delivered

		brevoClient = new BrevoClient(properties, restClientFactory);
		brevoClient.init();
	}


	@Test
	void sendEmail_sandboxMode_returnsMessageId() {
		SendEmailResponse response = brevoClient.sendEmail(
				"noreply@clanie.dk",
				"Portfolio Manager (Test)",
				"integration-test@clanie.dk",
				"Integration Test Recipient",
				"Portfolio Manager — integration test",
				"<p>This is an automated integration test email sent in sandbox mode.</p>");

		assertThat(response).isNotNull();
		assertThat(response.getMessageId()).isNotBlank();
	}

}
