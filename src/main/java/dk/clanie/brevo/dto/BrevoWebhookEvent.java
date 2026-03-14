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
package dk.clanie.brevo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single transactional email event posted by Brevo to our webhook endpoint.
 * <p>
 * Brevo sends one JSON object per event. The {@code event} field identifies the
 * event type using snake_case strings; the {@code message-id} field correlates
 * the event with a {@code SentEmail} record.
 *
 * <h3>Known event type strings ({@code event} field)</h3>
 * <ul>
 *   <li>{@code request}       — Brevo accepted the send request</li>
 *   <li>{@code delivered}     — accepted by the recipient's mail server</li>
 *   <li>{@code soft_bounce}   — temporary delivery failure</li>
 *   <li>{@code hard_bounce}   — permanent delivery failure</li>
 *   <li>{@code opened}        — recipient opened the message (may repeat)</li>
 *   <li>{@code first_opening} — first open of this message</li>
 *   <li>{@code unique_opened} — de-duplicated open event</li>
 *   <li>{@code loaded_by_proxy} — opened via a privacy proxy (e.g. Apple MPP)</li>
 *   <li>{@code click}         — recipient clicked a link (may repeat)</li>
 *   <li>{@code spam}          — recipient marked the message as spam</li>
 *   <li>{@code unsubscribed}  — recipient unsubscribed</li>
 *   <li>{@code invalid_email} — email address invalid; rejected without delivery attempt</li>
 *   <li>{@code blocked}       — blocked by Brevo (block-list / sending limit)</li>
 *   <li>{@code deferred}      — delivery temporarily deferred by the receiving server</li>
 *   <li>{@code error}         — SMTP-level delivery error</li>
 * </ul>
 *
 * @see <a href="https://developers.brevo.com/docs/transactional-webhooks">Brevo Transactional Webhooks</a>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrevoWebhookEvent {

	/** Event type string — snake_case, e.g. {@code "delivered"}, {@code "hard_bounce"}. */
	private String event;

	/** Recipient email address. */
	private String email;

	/**
	 * Brevo's message identifier, correlates with {@code SentEmail.extId}.
	 * Brevo does NOT wrap this in angle brackets in the webhook payload
	 * (unlike some SMTP {@code Message-ID} headers).
	 * Example: {@code "201798300811.5787683@relay.domain.com"}.
	 */
	@JsonProperty("message-id")
	private String messageId;

	/**
	 * Date/time of the event as a human-readable string
	 * (Brevo format: {@code "YYYY-MM-DD HH:mm:ss"}).
	 */
	private String date;

	/**
	 * Unix timestamp (seconds) of when the message was sent/accepted by Brevo.
	 * See also {@link #tsEvent} and {@link #tsEpoch}.
	 */
	private Long ts;

	/**
	 * Unix timestamp (seconds) of when this specific event occurred.
	 * Prefer {@link #tsEpoch} (milliseconds) when available.
	 */
	@JsonProperty("ts_event")
	private Long tsEvent;

	/**
	 * Unix timestamp in <em>milliseconds</em> of the event.
	 * This is the most precise timestamp field; use this to populate
	 * {@code EmailEvent.occurredAt}.
	 */
	@JsonProperty("ts_epoch")
	private Long tsEpoch;

	/** Subject line of the email. */
	private String subject;

	/** Failure reason — present on {@code soft_bounce}, {@code hard_bounce}, {@code error}, and {@code deferred} events. */
	private String reason;

	/** The URL that was clicked — present on {@code click} events. */
	private String link;

	/**
	 * User-agent string of the mail client — present on {@code opened} and {@code click} events.
	 */
	@JsonProperty("user-agent")
	private String userAgent;

	/** IP address of the recipient — present on {@code opened} and {@code click} events. */
	private String ip;

	/** IP address used by Brevo to send the message. */
	@JsonProperty("sending_ip")
	private String sendingIp;

	/** Brevo template ID, if the message was sent from a template. */
	@JsonProperty("template_id")
	private Long templateId;

	/** Tags associated with the message at send time. */
	private List<String> tags;

	/** Value of the {@code X-Mailin-custom} header set when sending. */
	@JsonProperty("X-Mailin-custom")
	private String xMailinCustom;

}
