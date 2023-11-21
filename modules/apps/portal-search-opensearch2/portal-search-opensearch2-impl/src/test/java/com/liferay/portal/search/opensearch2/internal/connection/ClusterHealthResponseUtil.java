/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import java.io.IOException;

import java.util.concurrent.TimeUnit;

import org.opensearch.action.admin.cluster.health.ClusterHealthRequest;
import org.opensearch.action.admin.cluster.health.ClusterHealthResponse;
import org.opensearch.client.ClusterClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.core.TimeValue;

/**
 * @author André de Oliveira
 */
public class ClusterHealthResponseUtil {

	public static ClusterHealthResponse getClusterHealthResponse(
		OpenSearchClientResolver openSearchClientResolver,
		HealthExpectations healthExpectations) {

		OpenSearchClient openSearchClient =
			openSearchClientResolver.getOpenSearchClient();

		ClusterClient clusterClient = openSearchClient.cluster();

		ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest();

		clusterHealthRequest.masterNodeTimeout(
			new TimeValue(10, TimeUnit.MINUTES));
		clusterHealthRequest.timeout(new TimeValue(10, TimeUnit.MINUTES));
		clusterHealthRequest.waitForActiveShards(
			healthExpectations.getActiveShards());
		clusterHealthRequest.waitForNodes(
			String.valueOf(healthExpectations.getNumberOfNodes()));
		clusterHealthRequest.waitForNoRelocatingShards(true);
		clusterHealthRequest.waitForStatus(healthExpectations.getStatus());

		try {
			return clusterClient.health(
				clusterHealthRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

}