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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import dk.clanie.web.RestClientFactory;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for clanie-brevo-client.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "brevo", name = {"apiKey", "url"})
@EnableConfigurationProperties(BrevoProperties.class)
public class BrevoAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	BrevoClient brevoClient(BrevoProperties properties, RestClientFactory restClientFactory) {
		return new BrevoClient(properties, restClientFactory);
	}

}
