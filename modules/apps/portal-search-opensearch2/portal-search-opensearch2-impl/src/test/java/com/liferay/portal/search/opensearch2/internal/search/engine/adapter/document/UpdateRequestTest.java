/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.document;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.opensearch2.internal.connection.OpenSearchFixture;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.OpenSearchClient;

/**
 * @author Adam Brandizzi
 */
public class UpdateRequestTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() throws Exception {
		_openSearchFixture = new OpenSearchFixture();

		_openSearchFixture.setUp();

		_openSearchClient = _openSearchFixture.getOpenSearchClient();

		_indicesClient = _openSearchClient.indices();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_openSearchFixture.tearDown();
	}

	@Before
	public void setUp() throws IOException {
		_indicesClient.create(
			new CreateIndexRequest(_INDEX_NAME), RequestOptions.DEFAULT);
	}

	@After
	public void tearDown() throws IOException {
		_indicesClient.delete(
			new DeleteIndexRequest(_INDEX_NAME), RequestOptions.DEFAULT);
	}

	@Test
	public void testUnsetValueWithArrayWithNull() throws IOException {
		String id = _indexAndGetId();

		_updateField(id, "field2", new Object[] {null});

		Map<String, Object> fields = _getFields(id);

		Assert.assertEquals("an example", fields.get("field1"));

		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)fields.get("field2");

		Assert.assertEquals(list.toString(), 1, list.size());
		Assert.assertNull(list.get(0));
	}

	@Test
	public void testUnsetValueWithEmptyArray() throws IOException {
		String id = _indexAndGetId();

		_updateField(id, "field2", new Object[0]);

		Map<String, Object> fields = _getFields(id);

		Assert.assertEquals("an example", fields.get("field1"));

		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)fields.get("field2");

		Assert.assertTrue(list.toString(), list.isEmpty());
	}

	@Test
	public void testUnsetValueWithNull() throws IOException {
		String id = _indexAndGetId();

		_updateField(id, "field2", null);

		Map<String, Object> fields = _getFields(id);

		Assert.assertEquals("an example", fields.get("field1"));
		Assert.assertNull(fields.get("field2"));
	}

	@Test
	public void testUpdateRequestWithMap() throws IOException {
		String id = _indexAndGetId();

		_updateField(id, "field2", "UPDATED FIELD");

		Map<String, Object> fields = _getFields(id);

		Assert.assertEquals("an example", fields.get("field1"));
		Assert.assertEquals("UPDATED FIELD", fields.get("field2"));
	}

	private Map<String, Object> _getFields(String id) throws IOException {
		GetRequest getRequest = new GetRequest(_INDEX_NAME, id);

		GetResponse getResponse = _openSearchClient.get(
			getRequest, RequestOptions.DEFAULT);

		return getResponse.getSource();
	}

	private String _indexAndGetId() throws IOException {
		IndexRequest indexRequest = new IndexRequest(_INDEX_NAME);

		indexRequest.source(
			HashMapBuilder.put(
				"field1", "an example"
			).put(
				"field2", "some test"
			).build());

		IndexResponse indexResponse = _openSearchClient.index(
			indexRequest, RequestOptions.DEFAULT);

		return indexResponse.getId();
	}

	private void _updateField(String id, String fieldName, Object fieldValue)
		throws IOException {

		UpdateRequest updateRequest = new UpdateRequest(_INDEX_NAME, id);

		updateRequest.doc(
			HashMapBuilder.put(
				fieldName, fieldValue
			).build());

		_openSearchClient.update(updateRequest, RequestOptions.DEFAULT);
	}

	private static final String _INDEX_NAME = "test_request_index";

	private static IndicesClient _indicesClient;
	private static OpenSearchClient _openSearchClient;
	private static OpenSearchFixture _openSearchFixture;

}