/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.document;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.opensearch2.internal.LiferayOpenSearchIndexingFixtureFactory;
import com.liferay.portal.search.opensearch2.internal.OpenSearchIndexingFixture;
import com.liferay.portal.search.opensearch2.internal.connection.helper.IndexCreationHelper;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.indices.PutMappingRequest;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.xcontent.XContentType;

/**
 * @author André de Oliveira
 */
public class GeoLocationPointFieldTest extends BaseIndexingTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCustomField() throws Exception {
		_assertGeoLocationPointField(_CUSTOM_FIELD);
	}

	@Test
	public void testDefaultField() throws Exception {
		_assertGeoLocationPointField(Field.GEO_LOCATION);
	}

	@Test
	public void testDefaultTemplate() throws Exception {
		_assertGeoLocationPointField(_CUSTOM_FIELD.concat("_geolocation"));
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		OpenSearchIndexingFixture openSearchIndexingFixture =
			LiferayOpenSearchIndexingFixtureFactory.builder(
			).build();

		openSearchIndexingFixture.setIndexCreationHelper(
			new CustomFieldLiferayIndexCreationHelper(
				openSearchIndexingFixture.getOpenSearchClientResolver()));

		return openSearchIndexingFixture;
	}

	private void _assertGeoLocationPointField(String fieldName) {
		double latitude = 33.99772698059678;
		double longitude = -117.814457193017;

		String expected = "(33.99772698059678,-117.814457193017)";

		addDocument(
			DocumentCreationHelpers.singleGeoLocation(
				fieldName, latitude, longitude));

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.search();

				indexingTestHelper.verifyResponse(
					searchResponse -> DocumentsAssert.assertValues(
						searchResponse.getRequestString(),
						searchResponse.getDocuments(), fieldName,
						"[" + expected + "]"));
			});
	}

	private static final String _CUSTOM_FIELD = "customField";

	private static class CustomFieldLiferayIndexCreationHelper
		implements IndexCreationHelper {

		public CustomFieldLiferayIndexCreationHelper(
			OpenSearchClientResolver openSearchClientResolver) {

			_openSearchClientResolver = openSearchClientResolver;
		}

		@Override
		public void contribute(CreateIndexRequest createIndexRequest) {
		}

		@Override
		public void contributeIndexSettings(Settings.Builder builder) {
		}

		@Override
		public void whenIndexCreated(String indexName) {
			PutMappingRequest putMappingRequest = new PutMappingRequest(
				indexName);

			String source = StringBundler.concat(
				"{ \"properties\": { \"", _CUSTOM_FIELD, "\" : { \"fields\": ",
				"{ \"geopoint\" : { \"store\": true, \"type\": \"keyword\" } ",
				"}, \"store\": true, \"type\": \"geo_point\" } } }");

			putMappingRequest.source(source, XContentType.JSON);

			OpenSearchClient openSearchClient =
				_openSearchConnectionManager.getOpenSearchClient();

			OpenSearchIndicesClient openSearchIndicesClient =
				openSearchClient.indices();

			try {
				openSearchIndicesClient.putMapping(
					putMappingRequest, RequestOptions.DEFAULT);
			}
			catch (IOException ioException) {
				throw new RuntimeException(ioException);
			}
		}

		private final OpenSearchClientResolver _openSearchClientResolver;

	}

}