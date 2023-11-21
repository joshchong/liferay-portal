/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection.helper;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.search.opensearch2.internal.index.MappingsFactory;

import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.common.settings.Settings;

/**
 * @author André de Oliveira
 */
public class LiferayIndexCreationHelper implements IndexCreationHelper {

	public LiferayIndexCreationHelper(
		OpenSearchClientResolver openSearchClientResolver) {

		_openSearchClientResolver = openSearchClientResolver;
	}

	@Override
	public void contribute(CreateIndexRequest createIndexRequest) {
		MappingsFactory mappingsFactory = _getLiferayDocumentTypeFactory();

		mappingsFactory.createRequiredDefaultTypeMappings(createIndexRequest);
	}

	@Override
	public void contributeIndexSettings(Settings.Builder builder) {
		MappingsFactory mappingsFactory = _getLiferayDocumentTypeFactory();

		mappingsFactory.createRequiredDefaultAnalyzers(builder);
	}

	@Override
	public void whenIndexCreated(String indexName) {
		MappingsFactory mappingsFactory = _getLiferayDocumentTypeFactory();

		mappingsFactory.addOptionalDefaultMappings(indexName);
	}

	private MappingsFactory _getLiferayDocumentTypeFactory() {
		OpenSearchClient openSearchClient =
			_openSearchConnectionManager.getOpenSearchClient();

		return new MappingsFactory(
			new JSONFactoryImpl(), openSearchClient.indices());
	}

	private final OpenSearchClientResolver _openSearchClientResolver;

}