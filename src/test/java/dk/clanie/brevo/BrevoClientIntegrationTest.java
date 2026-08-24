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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestClient;

import dk.clanie.brevo.dto.SendEmailResponse;
import dk.clanie.test.env.EnvFileExtension;
import dk.clanie.web.RestClientFactory;

/**
 * Integration test for {@link BrevoClient} against the real Brevo sandbox API.
 * <p>
 * This module configures no surefire {@code excludedGroups}, so the
 * {@code "integration"} tag alone does not keep the test out of a plain
 * {@code mvn test}. The test is therefore opt-in via the key instead: it is
 * <em>skipped</em> unless {@code BREVO_API_KEY} is supplied as an environment
 * variable, a system property, or a {@code .env.local} entry.
 * <p>
 * That guard is deliberate. Brevo webhooks are configured per <em>account</em>,
 * so a send from this test — sandbox or not — delivers {@code request} and
 * {@code blocked} events to whatever webhook URL the account points at, which
 * in practice is production. Production has no {@code SentEmail} for a message
 * it never sent, so each run left a pair of "No SentEmail found" warnings in the
 * production log (portfolio issue #251). Never let this test run against the
 * production Brevo account by accident.
 * <p>
 * Brevo's sandbox mode accepts requests and returns a {@code messageId} without
 * actually delivering the email.
 */
@Tag("integration")
@ExtendWith(EnvFileExtension.class)
class BrevoClientIntegrationTest {

	private static final String BREVO_API_URL = "https://api.brevo.com/v3";

	private BrevoClient brevoClient;


	@BeforeEach
	void setUp() {
		String apiKey = System.getProperty("BREVO_API_KEY", System.getenv("BREVO_API_KEY"));
		assumeTrue(apiKey != null && !apiKey.isBlank(),
				"BREVO_API_KEY not set — skipping the live Brevo sandbox call.");

		RestClientFactory restClientFactory = new RestClientFactory(RestClient.builder());

		BrevoProperties properties = new BrevoProperties();
		properties.setApiKey(apiKey);
		properties.setUrl(BREVO_API_URL);
		properties.setSandbox(true); // Sandbox: accepted but not delivered
		properties.setWiretap(false);

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
