/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.index;

import com.liferay.portal.search.opensearch2.internal.connection.IndexCreator;
import com.liferay.portal.search.opensearch2.internal.connection.IndexName;
import com.liferay.portal.search.opensearch2.internal.connection.OpenSearchFixture;
import com.liferay.portal.search.opensearch2.internal.index.constants.MappingsConstants;

import java.io.IOException;

import java.util.Map;

import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.opensearch.OpenSearchClient;

/**
 * @author André de Oliveira
 */
public class LiferayIndexFixture {

	public LiferayIndexFixture(String subdirName, IndexName indexName) {
		OpenSearchFixture openSearchFixture = new OpenSearchFixture();

		_openSearchFixture = openSearchFixture;

		_indexCreator = new IndexCreator() {
			{
				setLiferayMappingsAddedToIndex(true);
				setOpenSearchClientResolver(openSearchFixture);
			}
		};

		_indexName = indexName;
	}

	public void assertAnalyzer(String field, String analyzer) throws Exception {
		OpenSearchClient openSearchClient = getOpenSearchClient();

		FieldMappingAssert.assertAnalyzer(
			analyzer, field, MappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_indexName.getName(), openSearchClient.indices());
	}

	public void assertType(String field, String type) throws Exception {
		OpenSearchClient openSearchClient = getOpenSearchClient();

		FieldMappingAssert.assertType(
			type, field, MappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_indexName.getName(), openSearchClient.indices());
	}

	public OpenSearchClient getOpenSearchClient() {
		return _openSearchFixture.getOpenSearchClient();
	}

	public void index(Map<String, Object> map) {
		IndexRequest indexRequest = getIndexRequest();

		indexRequest.source(map);

		OpenSearchClient openSearchClient = getOpenSearchClient();

		try {
			openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	public void setUp() throws Exception {
		_openSearchFixture.setUp();

		_indexCreator.createIndex(_indexName);
	}

	public void tearDown() throws Exception {
		_indexCreator.deleteIndex(_indexName);

		_openSearchFixture.tearDown();
	}

	protected IndexRequest getIndexRequest() {
		return Requests.indexRequest(_indexName.getName());
	}

	private final IndexCreator _indexCreator;
	private final IndexName _indexName;
	private final OpenSearchFixture _openSearchFixture;

}