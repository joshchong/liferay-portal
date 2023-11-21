/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import com.liferay.portal.search.opensearch2.internal.connection.helper.IndexCreationHelper;
import com.liferay.portal.search.opensearch2.internal.connection.helper.LiferayIndexCreationHelper;

import java.io.IOException;

import org.mockito.Mockito;

import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.support.IndicesOptions;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.common.settings.Settings;

/**
 * @author André de Oliveira
 */
public class IndexCreator {

	public Index createIndex(IndexName indexName) {
		IndicesClient indicesClient = _getIndicesClient();

		String name = indexName.getName();

		deleteIndex(indicesClient, name);

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(name);

		IndexCreationHelper indexCreationHelper = _getIndexCreationHelper();

		indexCreationHelper.contribute(createIndexRequest);

		Settings.Builder builder = Settings.builder();

		builder.putPersistent("index.number_of_replicas", 0);
		builder.putPersistent("index.number_of_shards", 1);

		indexCreationHelper.contributeIndexSettings(builder);

		createIndexRequest.settings(builder);

		try {
			indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		indexCreationHelper.whenIndexCreated(name);

		return new Index(indexName);
	}

	public void deleteIndex(IndexName indexName) {
		deleteIndex(_getIndicesClient(), indexName.getName());
	}

	protected void deleteIndex(IndicesClient indicesClient, String name) {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(name);

		deleteIndexRequest.indicesOptions(IndicesOptions.lenientExpandOpen());

		try {
			indicesClient.delete(deleteIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	protected void setIndexCreationHelper(
		IndexCreationHelper indexCreationHelper) {

		_indexCreationHelper = indexCreationHelper;
	}

	protected void setLiferayMappingsAddedToIndex(
		boolean liferayMappingsAddedToIndex) {

		_liferayMappingsAddedToIndex = liferayMappingsAddedToIndex;
	}

	protected void setOpenSearchClientResolver(
		OpenSearchClientResolver openSearchClientResolver) {

		_openSearchClientResolver = openSearchClientResolver;
	}

	private IndexCreationHelper _getIndexCreationHelper() {
		if (!_liferayMappingsAddedToIndex) {
			if (_indexCreationHelper != null) {
				return _indexCreationHelper;
			}

			return Mockito.mock(IndexCreationHelper.class);
		}

		LiferayIndexCreationHelper liferayIndexCreationHelper =
			new LiferayIndexCreationHelper(_openSearchClientResolver);

		if (_indexCreationHelper == null) {
			return liferayIndexCreationHelper;
		}

		return new IndexCreationHelper() {

			@Override
			public void contribute(CreateIndexRequest createIndexRequest) {
				_indexCreationHelper.contribute(createIndexRequest);

				liferayIndexCreationHelper.contribute(createIndexRequest);
			}

			@Override
			public void contributeIndexSettings(Settings.Builder builder) {
				_indexCreationHelper.contributeIndexSettings(builder);

				liferayIndexCreationHelper.contributeIndexSettings(builder);
			}

			@Override
			public void whenIndexCreated(String indexName) {
				_indexCreationHelper.whenIndexCreated(indexName);

				liferayIndexCreationHelper.whenIndexCreated(indexName);
			}

		};
	}

	private final IndicesClient _getIndicesClient() {
		OpenSearchClient openSearchClient =
			_openSearchConnectionManager.getOpenSearchClient();

		return openSearchClient.indices();
	}

	private IndexCreationHelper _indexCreationHelper;
	private boolean _liferayMappingsAddedToIndex;
	private OpenSearchConnectionManager _openSearchConnectionManager;

}