/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;
import java.util.function.Consumer;

import org.apache.http.HttpHost;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.opensearch.client.Node;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;

/**
 * @author André de Oliveira
 */
public class OpenSearchConnectionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_openSearchConnection = new OpenSearchConnection();
	}

	@Test
	public void testConnectAndClose() {
		_openSearchConnection.setNetworkHostAddresses(
			new String[] {"http://localhost:9200"});

		Runnable postCloseRunnable = Mockito.mock(Runnable.class);

		_openSearchConnection.setPostCloseRunnable(postCloseRunnable);

		Consumer<OpenSearchConnection> preConnectOpenSearchConnectionConsumer =
			Mockito.mock(Consumer.class);

		_openSearchConnection.setPreConnectOpenSearchConnectionConsumer(
			preConnectOpenSearchConnectionConsumer);

		Assert.assertFalse(_openSearchConnection.isConnected());

		_openSearchConnection.connect();

		Assert.assertTrue(_openSearchConnection.isConnected());

		Mockito.verify(
			preConnectOpenSearchConnectionConsumer
		).accept(
			Mockito.any()
		);

		_assertNetworkHostAddress("localhost", 9200);

		_openSearchConnection.close();

		Assert.assertFalse(_openSearchConnection.isConnected());

		Mockito.verify(
			postCloseRunnable
		).run();

		_openSearchConnection.setNetworkHostAddresses(
			new String[] {"http://127.0.0.1:9999"});

		_openSearchConnection.connect();

		Assert.assertTrue(_openSearchConnection.isConnected());

		_assertNetworkHostAddress("127.0.0.1", 9999);
	}

	private void _assertNetworkHostAddress(String hostString, int port) {
		OpenSearchClient openSearchClient =
			_openSearchConnection.getOpenSearchClient();

		RestClient restClient = openSearchClient.getLowLevelClient();

		List<Node> nodes = restClient.getNodes();

		Assert.assertEquals(nodes.toString(), 1, nodes.size());

		Node node = nodes.get(0);

		HttpHost httpHost = node.getHost();

		Assert.assertEquals(hostString, httpHost.getHostName());
		Assert.assertEquals(port, httpHost.getPort());
	}

	private OpenSearchConnection _openSearchConnection;

}