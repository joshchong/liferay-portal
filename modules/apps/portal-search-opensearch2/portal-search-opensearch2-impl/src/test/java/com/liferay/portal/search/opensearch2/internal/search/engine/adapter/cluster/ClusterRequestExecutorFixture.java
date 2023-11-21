/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.cluster;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.engine.adapter.cluster.ClusterRequestExecutor;

/**
 * @author Dylan Rebelak
 */
public class ClusterRequestExecutorFixture {

	public ClusterRequestExecutor getClusterRequestExecutor() {
		return _clusterRequestExecutor;
	}

	public void setUp() {
		ClusterHealthStatusTranslator clusterHealthStatusTranslator =
			new ClusterHealthStatusTranslatorImpl();

		_clusterRequestExecutor = new OpenSearchClusterRequestExecutor();

		ReflectionTestUtil.setFieldValue(
			_clusterRequestExecutor, "_healthClusterRequestExecutor",
			_createHealthClusterRequestExecutor(
				clusterHealthStatusTranslator, _openSearchClientResolver));
		ReflectionTestUtil.setFieldValue(
			_clusterRequestExecutor, "_stateClusterRequestExecutor",
			_createStateClusterRequestExecutor(_openSearchClientResolver));
		ReflectionTestUtil.setFieldValue(
			_clusterRequestExecutor, "_statsClusterRequestExecutor",
			_createStatsClusterRequestExecutor(
				clusterHealthStatusTranslator, _openSearchClientResolver));
	}

	protected void setOpenSearchClientResolver(
		OpenSearchClientResolver openSearchClientResolver) {

		_openSearchClientResolver = openSearchClientResolver;
	}

	private HealthClusterRequestExecutor _createHealthClusterRequestExecutor(
		ClusterHealthStatusTranslator clusterHealthStatusTranslator,
		OpenSearchClientResolver openSearchClientResolver) {

		HealthClusterRequestExecutor healthClusterRequestExecutor =
			new HealthClusterRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			healthClusterRequestExecutor, "_clusterHealthStatusTranslator",
			clusterHealthStatusTranslator);
		ReflectionTestUtil.setFieldValue(
			healthClusterRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return healthClusterRequestExecutor;
	}

	private StateClusterRequestExecutor _createStateClusterRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver) {

		StateClusterRequestExecutor stateClusterRequestExecutor =
			new StateClusterRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			stateClusterRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return stateClusterRequestExecutor;
	}

	private StatsClusterRequestExecutor _createStatsClusterRequestExecutor(
		ClusterHealthStatusTranslator clusterHealthStatusTranslator,
		OpenSearchClientResolver openSearchClientResolver) {

		StatsClusterRequestExecutor statsClusterRequestExecutor =
			new StatsClusterRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			statsClusterRequestExecutor, "_clusterHealthStatusTranslator",
			clusterHealthStatusTranslator);
		ReflectionTestUtil.setFieldValue(
			statsClusterRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			statsClusterRequestExecutor, "_jsonFactory", new JSONFactoryImpl());

		return statsClusterRequestExecutor;
	}

	private ClusterRequestExecutor _clusterRequestExecutor;

}