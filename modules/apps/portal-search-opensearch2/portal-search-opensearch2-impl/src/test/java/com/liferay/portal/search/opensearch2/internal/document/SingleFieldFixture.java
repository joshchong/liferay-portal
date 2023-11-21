/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.document;

import com.liferay.portal.search.opensearch2.internal.connection.IndexName;
import com.liferay.portal.search.opensearch2.internal.query.QueryBuilderFactory;
import com.liferay.portal.search.opensearch2.internal.query.SearchAssert;

import java.io.IOException;

import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.index.query.QueryBuilder;

/**
 * @author André de Oliveira
 */
public class SingleFieldFixture {

	public SingleFieldFixture(
		OpenSearchClient openSearchClient, IndexName indexName, String type) {

		_openSearchClient = openSearchClient;
		_type = type;

		_index = indexName.getName();
	}

	public void assertNoHits(String text) throws Exception {
		SearchAssert.assertNoHits(
			_openSearchClient, _field, _createQueryBuilder(text));
	}

	public void assertSearch(String text, String... expected) throws Exception {
		SearchAssert.assertSearch(
			_openSearchClient, _field, _createQueryBuilder(text), expected);
	}

	public void indexDocument(String value) {
		IndexRequest indexRequest = Requests.indexRequest(_index);

		indexRequest.source(_field, value);

		try {
			_openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	public void setField(String field) {
		_field = field;
	}

	public void setQueryBuilderFactory(
		QueryBuilderFactory queryBuilderFactory) {

		_queryBuilderFactory = queryBuilderFactory;
	}

	private QueryBuilder _createQueryBuilder(String text) {
		return _queryBuilderFactory.create(_field, text);
	}

	private String _field;
	private final String _index;
	private final OpenSearchClient _openSearchClient;
	private QueryBuilderFactory _queryBuilderFactory;
	private final String _type;

}