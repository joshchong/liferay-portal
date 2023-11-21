/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import com.liferay.petra.process.local.LocalProcessExecutor;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.search.opensearch2.configuration.OpenSearchConfiguration;
import com.liferay.portal.search.opensearch2.internal.configuration.OpenSearchConfigurationWrapper;
import com.liferay.portal.search.opensearch2.internal.connection.constants.ConnectionConstants;
import com.liferay.portal.search.opensearch2.internal.sidecar.PathUtil;
import com.liferay.portal.search.opensearch2.internal.sidecar.Sidecar;
import com.liferay.portal.search.opensearch2.internal.sidecar.SidecarManager;
import com.liferay.portal.util.PropsImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;

import org.opensearch.client.opensearch.OpenSearchClient;

/**
 * @author André de Oliveira
 */
public class OpenSearchConnectionFixture implements OpenSearchClientResolver {

	public static Builder builder() {
		return new Builder();
	}

	public void createNode() {
		createOpenSearchConnection();

		_openSearchConnection.connect();
	}

	public OpenSearchConnection createOpenSearchConnection() {
		PropsUtil.setProps(new PropsImpl());

		OpenSearchConfigurationWrapper openSearchConfigurationWrapper =
			new OpenSearchConfigurationWrapper() {
				{
					setOpenSearchConfiguration(
						ConfigurableUtil.createConfigurable(
							OpenSearchConfiguration.class,
							_openSearchConfigurationProperties));
				}
			};

		Sidecar sidecar = new Sidecar(
			Mockito.mock(ClusterExecutor.class), openSearchConfigurationWrapper,
			_createOpenSearchInstancePaths(), new LocalProcessExecutor(),
			() -> _TMP_PATH.resolve("lib-process-executor"),
			Mockito.mock(SidecarManager.class));

		OpenSearchConnectionBuilder openSearchConnectionBuilder =
			new OpenSearchConnectionBuilder();

		openSearchConnectionBuilder.active(
			true
		).connectionId(
			ConnectionConstants.SIDECAR_CONNECTION_ID
		).postCloseRunnable(
			sidecar::stop
		).preConnectOpenSearchConnectionConsumer(
			elasticsearchConnection -> {
				_deleteTmpDir();

				sidecar.start();

				elasticsearchConnection.setNetworkHostAddresses(
					new String[] {sidecar.getNetworkHostAddress()});
			}
		);

		_openSearchConnection = openSearchConnectionBuilder.build();

		return _openSearchConnection;
	}

	public void destroyNode() {
		if (_openSearchConnection != null) {
			_openSearchConnection.close();
		}

		_deleteTmpDir();
	}

	@Override
	public OpenSearchClient getOpenSearchClient() {
		return _openSearchConnection.getOpenSearchClient();
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
		return _openSearchConfigurationProperties;
	}

	public OpenSearchConnection getOpenSearchConnection() {
		return _openSearchConnection;
	}

	public static class Builder {

		public OpenSearchConnectionFixture build() {
			OpenSearchConnectionFixture openSearchConnectionFixture =
				new OpenSearchConnectionFixture();

			openSearchConnectionFixture._openSearchConfigurationProperties =
				createOpenSearchConfigurationProperties(
					_openSearchConfigurationProperties, _clusterName);
			openSearchConnectionFixture._workPath = _TMP_PATH.resolve(
				_clusterName);

			return openSearchConnectionFixture;
		}

		public OpenSearchConnectionFixture.Builder clusterName(
			String clusterName) {

			_clusterName = clusterName;

			return this;
		}

		public Builder elasticsearchConfigurationProperties(
			Map<String, Object> elasticsearchConfigurationProperties) {

			if (elasticsearchConfigurationProperties == null) {
				elasticsearchConfigurationProperties =
					Collections.<String, Object>emptyMap();
			}

			_openSearchConfigurationProperties =
				elasticsearchConfigurationProperties;

			return this;
		}

		protected static Map<String, Object>
			createOpenSearchConfigurationProperties(
				Map<String, Object> elasticsearchConfigurationProperties,
				String clusterName) {

			return HashMapBuilder.<String, Object>put(
				"clusterName", clusterName
			).put(
				"configurationPid", OpenSearchConfiguration.class.getName()
			).put(
				"httpCORSAllowOrigin", "*"
			).put(
				"logExceptionsOnly", false
			).put(
				"sidecarHttpPort", HttpPortRange.AUTO
			).put(
				"sidecarJVMOptions", "-Xmx256m"
			).putAll(
				elasticsearchConfigurationProperties
			).build();
		}

		private String _clusterName;
		private Map<String, Object> _openSearchConfigurationProperties =
			Collections.<String, Object>emptyMap();

	}

	private OpenSearchInstancePaths _createOpenSearchInstancePaths() {
		OpenSearchInstancePaths openSearchInstancePaths = Mockito.mock(
			OpenSearchInstancePaths.class);

		Mockito.doReturn(
			_TMP_PATH.resolve("sidecar-elasticsearch")
		).when(
			openSearchInstancePaths
		).getHomePath();

		Mockito.doReturn(
			_workPath
		).when(
			openSearchInstancePaths
		).getWorkPath();

		return openSearchInstancePaths;
	}

	private void _deleteTmpDir() {
		PathUtil.deleteDir(_workPath);
	}

	private static final Path _TMP_PATH = Paths.get("tmp");

	private Map<String, Object> _openSearchConfigurationProperties =
		Collections.<String, Object>emptyMap();
	private OpenSearchConnection _openSearchConnection;
	private Path _workPath;

}