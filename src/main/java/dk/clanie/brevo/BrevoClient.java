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

import java.util.List;

import org.springframework.web.client.RestClient;

import dk.clanie.brevo.dto.SendEmailRequest;
import dk.clanie.brevo.dto.SendEmailRequest.EmailContact;
import dk.clanie.brevo.dto.SendEmailRequest.Sandbox;
import dk.clanie.brevo.dto.SendEmailResponse;
import dk.clanie.web.RestClientFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for the Brevo transactional email API.
 * <p>
 * Wraps Brevo's {@code POST /smtp/email} endpoint. When {@code brevo.sandbox=true}
 * the request is sent with the sandbox flag so Brevo accepts it without delivering.
 */
@Slf4j
@RequiredArgsConstructor
public class BrevoClient {

	private final BrevoProperties properties;
	private final RestClientFactory restClientFactory;

	private RestClient restClient;


	@PostConstruct
	public void init() {
		restClient = restClientFactory.newRestClient(
				properties.getUrl(),
				builder -> builder.defaultHeader("api-key", properties.getApiKey()),
				properties.isWiretap());
	}


	/**
	 * Sends a transactional email via the Brevo API.
	 *
	 * @param senderEmail    sender email address (from address)
	 * @param senderName     sender display name (from name)
	 * @param recipientEmail recipient email address
	 * @param recipientName  recipient display name
	 * @param subject        email subject line
	 * @param htmlContent    rendered HTML body
	 * @return Brevo's response containing the message ID
	 */
	public SendEmailResponse sendEmail(String senderEmail, String senderName,
			String recipientEmail, String recipientName,
			String subject, String htmlContent) {
		SendEmailRequest.SendEmailRequestBuilder builder = SendEmailRequest.builder()
				.sender(EmailContact.builder()
						.email(senderEmail)
						.name(senderName)
						.build())
				.to(List.of(EmailContact.builder()
						.email(recipientEmail)
						.name(recipientName)
						.build()))
				.subject(subject)
				.htmlContent(htmlContent);

		if (properties.isSandbox()) {
			builder.sandbox(Sandbox.builder().enabled(true).build());
		}

		SendEmailRequest request = builder.build();

		log.debug("Sending email from {} to {} via Brevo (sandbox={})", senderEmail, recipientEmail, properties.isSandbox());

		return restClient.post()
				.uri("/smtp/email")
				.body(request)
				.retrieve()
				.body(SendEmailResponse.class);
	}

}
