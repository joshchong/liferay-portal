/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.opensearch2.configuration.OpenSearchConfiguration;
import com.liferay.portal.search.opensearch2.internal.configuration.OpenSearchConfigurationWrapper;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Adam Brandizzi
 * @author André de Oliveira
 */
public class HttpPortRangeTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testEmbeddedHttpPort() {
		_mockEmbeddedHttpPort(4400);

		_assertSidecarHttpPort("4400");
	}

	@Test
	public void testHasDefaultHttpPort() {
		_assertSidecarHttpPort("9201");
	}

	@Test
	public void testSidecarHttpPort() {
		_mockSidecarHttpPort("3100-3199");

		_assertSidecarHttpPort("3100-3199");
	}

	@Test
	public void testSidecarHttpPortAuto() {
		_mockEmbeddedHttpPort(4400);
		_mockSidecarHttpPort(HttpPortRange.AUTO);

		_assertSidecarHttpPort("9201-9300");
	}

	@Test
	public void testSidecarHttpPortHasPrecedenceOverEmbeddedHttpPort() {
		_mockEmbeddedHttpPort(4400);
		_mockSidecarHttpPort("3100-3199");

		_assertSidecarHttpPort("3100-3199");
	}

	private void _assertSidecarHttpPort(String expected) {
		OpenSearchConfigurationWrapper openSearchConfigurationWrapper =
			new OpenSearchConfigurationWrapper() {
				{
					setOpenSearchConfiguration(
						ConfigurableUtil.createConfigurable(
							OpenSearchConfiguration.class,
							HashMapBuilder.<String, Object>put(
								"embeddedHttpPort", _embeddedHttpPort
							).put(
								"sidecarHttpPort", _sidecarHttpPort
							).build()));
				}
			};

		HttpPortRange httpPortRange = new HttpPortRange(
			openSearchConfigurationWrapper);

		Assert.assertEquals(expected, httpPortRange.toSettingsString());
	}

	private void _mockEmbeddedHttpPort(int embeddedHttpPort) {
		_embeddedHttpPort = embeddedHttpPort;
	}

	private void _mockSidecarHttpPort(String sidecarHttpPort) {
		_sidecarHttpPort = sidecarHttpPort;
	}

	private Integer _embeddedHttpPort;
	private String _sidecarHttpPort;

}