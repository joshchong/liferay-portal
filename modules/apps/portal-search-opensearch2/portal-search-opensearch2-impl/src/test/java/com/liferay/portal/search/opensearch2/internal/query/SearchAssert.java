/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.query;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Assert;

import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.common.document.DocumentField;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;

/**
 * @author André de Oliveira
 */
public class SearchAssert {

	public static void assertNoHits(
			OpenSearchClient openSearchClient, String field,
			QueryBuilder queryBuilder)
		throws Exception {

		assertSearch(openSearchClient, field, queryBuilder, new String[0]);
	}

	public static void assertSearch(
			OpenSearchClient openSearchClient,
			SearchSourceBuilder searchSourceBuilder,
			SearchRequest searchRequest, String field, String... expectedValues)
		throws Exception {

		assertSearch(
			() -> search(openSearchClient, searchSourceBuilder, searchRequest),
			field, expectedValues);
	}

	public static void assertSearch(
			OpenSearchClient openSearchClient, String field,
			QueryBuilder queryBuilder, String... expectedValues)
		throws Exception {

		assertSearch(
			() -> search(openSearchClient, queryBuilder), field,
			expectedValues);
	}

	protected static void assertSearch(
			Supplier<SearchHits> supplier, String field,
			String... expectedValues)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> Assert.assertEquals(
				_sort(Arrays.asList(expectedValues)),
				_sort(getValues(supplier.get(), field))));
	}

	protected static List<String> getValues(
		SearchHits searchHits, String field) {

		List<String> values = new ArrayList<>();

		for (SearchHit searchHit : searchHits.getHits()) {
			DocumentField documentField = searchHit.field(field);

			values.add(documentField.getValue());
		}

		return values;
	}

	protected static SearchHits search(
		OpenSearchClient openSearchClient, QueryBuilder queryBuilder) {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(queryBuilder);

		return search(
			openSearchClient, searchSourceBuilder, new SearchRequest());
	}

	protected static SearchHits search(
		OpenSearchClient openSearchClient,
		SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

		searchSourceBuilder.storedField(StringPool.STAR);

		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse searchResponse = openSearchClient.search(
				searchRequest, RequestOptions.DEFAULT);

			return searchResponse.getHits();
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private static String _sort(Collection<String> collection) {
		List<String> list = new ArrayList<>(collection);

		Collections.sort(list);

		return list.toString();
	}

}