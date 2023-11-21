/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import java.io.IOException;

import java.util.Map;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.client.indices.GetIndexResponse;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.cluster.health.ClusterHealthStatus;

/**
 * @author André de Oliveira
 */
public class OpenSearchFixture implements OpenSearchClientResolver {

	public OpenSearchFixture() {
		this(
			_openSearchConnectionFixtureSingleton.
				getOpenSearchConnectionFixture(),
			true);
	}

	/**
	 * @deprecated As of Athanasius (7.3.x)
	 */
	@Deprecated
	public OpenSearchFixture(Class<?> clazz) {
		this();
	}

	public OpenSearchFixture(
		OpenSearchConnectionFixture openSearchConnectionFixture) {

		this(openSearchConnectionFixture, false);
	}

	public OpenSearchFixture(
		OpenSearchConnectionFixture openSearchConnectionFixture,
		boolean singleton) {

		_openSearchConnectionFixture = openSearchConnectionFixture;
		_singleton = singleton;
	}

	/**
	 * @deprecated As of Athanasius (7.3.x)
	 */
	@Deprecated
	public OpenSearchFixture(String subdirName) {
		this();
	}

	public GetIndexResponse getIndex(String... indices) {
		OpenSearchClient openSearchClient = getOpenSearchClient();

		OpenSearchIndicesClient openSearchIndicesClient =
			openSearchClient.indices();

		GetIndexRequest getIndexRequest = new GetIndexRequest(indices);

		try {
			return openSearchIndicesClient.get(
				getIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	@Override
	public OpenSearchClient getOpenSearchClient() {
		return _openSearchConnectionFixture.getOpenSearchClient();
	}

	@Override
	public OpenSearchClient getOpenSearchClient(String connectionId) {
		return getOpenSearchClient();
	}

	@Override
	public OpenSearchClient getOpenSearchClient(
		String connectionId, boolean preferLocalCluster) {

		return getOpenSearchClient();
	}

	public Map<String, Object> getOpenSearchConfigurationProperties() {
		return _openSearchConnectionFixture.
			getOpenSearchConfigurationProperties();
	}

	public OpenSearchConnection getOpenSearchConnection() {
		return _openSearchConnectionFixture.getOpenSearchConnection();
	}

	public void setUp() throws Exception {
		if (_singleton) {
			_openSearchConnectionFixtureSingleton.start();

			return;
		}

		_openSearchConnectionFixture.createNode();
	}

	public void tearDown() throws Exception {
		if (!_singleton) {
			_openSearchConnectionFixture.destroyNode();
		}
	}

	public void waitForOpenSearchToStart() {
		ClusterHealthResponseUtil.getClusterHealthResponse(
			this,
			new HealthExpectations() {
				{
					setActivePrimaryShards(0);
					setActiveShards(0);
					setNumberOfDataNodes(1);
					setNumberOfNodes(1);
					setStatus(ClusterHealthStatus.GREEN);
					setUnassignedShards(0);
				}
			});
	}

	private static final OpenSearchConnectionFixtureSingleton
		_openSearchConnectionFixtureSingleton =
			new OpenSearchConnectionFixtureSingleton();

	private final OpenSearchConnectionFixture _openSearchConnectionFixture;
	private final boolean _singleton;

	private static class OpenSearchConnectionFixtureSingleton {

		public void start() {
			if (!_connected) {
				_openSearchConnectionFixture.createNode();

				_connected = true;
			}
		}

		protected OpenSearchConnectionFixture getOpenSearchConnectionFixture() {
			return _openSearchConnectionFixture;
		}

		private OpenSearchConnectionFixtureSingleton() {
			_openSearchConnectionFixture = OpenSearchConnectionFixture.builder(
			).clusterName(
				OpenSearchFixture.class.getSimpleName()
			).build();
		}

		private boolean _connected;
		private final OpenSearchConnectionFixture _openSearchConnectionFixture;

	}

}