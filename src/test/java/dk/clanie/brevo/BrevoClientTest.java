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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import dk.clanie.brevo.dto.SendEmailResponse;
import dk.clanie.web.RestClientFactory;

/**
 * Unit tests for {@link BrevoClient}.
 * Uses {@link MockRestServiceServer} to stub the Brevo HTTP endpoint.
 */
class BrevoClientTest {

	private static final String BASE_URL = "https://api.brevo.com/v3";
	private static final String API_KEY = "test-api-key";

	private MockRestServiceServer mockServer;
	private BrevoClient brevoClient;


	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();

		RestClientFactory restClientFactory = new RestClientFactory(builder);

		BrevoProperties properties = new BrevoProperties();
		properties.setApiKey(API_KEY);
		properties.setUrl(BASE_URL);

		brevoClient = new BrevoClient(properties, restClientFactory);
		brevoClient.init();
	}


	@Test
	void sendEmail_postsToBrevoWithApiKeyHeader() {
		mockServer.expect(requestTo(BASE_URL + "/smtp/email"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("api-key", API_KEY))
				.andRespond(withSuccess("""
						{"messageId": "<msg-123@brevo>"}
						""", MediaType.APPLICATION_JSON));

		SendEmailResponse response = brevoClient.sendEmail(
				"noreply@example.com", "Test Sender",
				"user@example.com", "Test User", "Subject", "<p>Body</p>");

		assertThat(response.getMessageId()).isEqualTo("<msg-123@brevo>");
		mockServer.verify();
	}


	@Test
	void sendEmail_includesSenderRecipientSubjectAndHtml() {
		mockServer.expect(requestTo(BASE_URL + "/smtp/email"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.sender.email").value("noreply@example.com"))
				.andExpect(jsonPath("$.sender.name").value("Test Sender"))
				.andExpect(jsonPath("$.to[0].email").value("user@example.com"))
				.andExpect(jsonPath("$.to[0].name").value("Test User"))
				.andExpect(jsonPath("$.subject").value("My Subject"))
				.andExpect(jsonPath("$.htmlContent").value("<p>Hello</p>"))
				.andRespond(withSuccess("""
						{"messageId": "<abc@brevo>"}
						""", MediaType.APPLICATION_JSON));

		brevoClient.sendEmail("noreply@example.com", "Test Sender",
				"user@example.com", "Test User", "My Subject", "<p>Hello</p>");
		mockServer.verify();
	}


	@Test
	void sendEmail_sandboxMode_includesSandboxFlag() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer sandboxMockServer = MockRestServiceServer.bindTo(builder).build();

		RestClientFactory restClientFactory = new RestClientFactory(builder);
		BrevoProperties sandboxProps = new BrevoProperties();
		sandboxProps.setApiKey(API_KEY);
		sandboxProps.setUrl(BASE_URL);
		sandboxProps.setSandbox(true);

		BrevoClient sandboxClient = new BrevoClient(sandboxProps, restClientFactory);
		sandboxClient.init();

		sandboxMockServer.expect(requestTo(BASE_URL + "/smtp/email"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.sandbox.enabled").value(true))
				.andRespond(withSuccess("""
						{"messageId": "<sandbox-msg@brevo>"}
						""", MediaType.APPLICATION_JSON));

		sandboxClient.sendEmail("noreply@example.com", "Test Sender",
				"user@example.com", "Test User", "Subject", "<p>Body</p>");
		sandboxMockServer.verify();
	}


	@Test
	void sendEmail_noSandbox_sandboxFieldAbsent() {
		mockServer.expect(requestTo(BASE_URL + "/smtp/email"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.sandbox").doesNotExist())
				.andRespond(withSuccess("""
						{"messageId": "<live-msg@brevo>"}
						""", MediaType.APPLICATION_JSON));

		brevoClient.sendEmail("noreply@example.com", "Test Sender",
				"user@example.com", "Test User", "Subject", "<p>Body</p>");
		mockServer.verify();
	}

}
