/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.document;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.opensearch2.internal.connection.OpenSearchFixture;
import com.liferay.portal.search.opensearch2.internal.document.OpenSearchDocumentFactory;
import com.liferay.portal.search.opensearch2.internal.document.OpenSearchDocumentFactoryImpl;
import com.liferay.portal.search.test.util.indexing.DocumentFixture;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.common.xcontent.XContentHelper;
import org.opensearch.xcontent.XContentType;

/**
 * @author Michael C. Han
 */
public class OpenSearchBulkableDocumentRequestTranslatorTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() throws Exception {
		_openSearchFixture = new OpenSearchFixture(
			OpenSearchBulkableDocumentRequestTranslatorTest.class);

		_openSearchFixture.setUp();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_openSearchFixture.tearDown();
	}

	@Before
	public void setUp() throws Exception {
		OpenSearchDocumentFactory openSearchDocumentFactory =
			_createOpenSearchDocumentFactory();

		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator =
				_createOpenSearchBulkableDocumentRequestTranslator(
					openSearchDocumentFactory);

		_openSearchBulkableDocumentRequestTranslator =
			openSearchBulkableDocumentRequestTranslator;

		_openSearchDocumentFactory = openSearchDocumentFactory;

		_documentFixture.setUp();
	}

	@After
	public void tearDown() throws Exception {
		_documentFixture.tearDown();
	}

	@Test
	public void testDeleteDocumentRequestTranslationWithNoRefresh() {
		_testDeleteDocumentRequestTranslation(
			false, WriteRequest.RefreshPolicy.NONE);
	}

	@Test
	public void testDeleteDocumentRequestTranslationWithRefresh() {
		_testDeleteDocumentRequestTranslation(
			true, WriteRequest.RefreshPolicy.IMMEDIATE);
	}

	@Test
	public void testIndexDocumentRequestTranslationWithNoRefresh()
		throws Exception {

		_testIndexDocumentRequestTranslation(
			"1", false, WriteRequest.RefreshPolicy.NONE);
	}

	@Test
	public void testIndexDocumentRequestTranslationWithNoRefreshNoId()
		throws Exception {

		_testIndexDocumentRequestTranslation(
			null, false, WriteRequest.RefreshPolicy.NONE);
	}

	@Test
	public void testIndexDocumentRequestTranslationWithRefresh()
		throws Exception {

		_testIndexDocumentRequestTranslation(
			"1", true, WriteRequest.RefreshPolicy.IMMEDIATE);
	}

	@Test
	public void testIndexDocumentRequestTranslationWithRefreshNoId()
		throws Exception {

		_testIndexDocumentRequestTranslation(
			null, true, WriteRequest.RefreshPolicy.IMMEDIATE);
	}

	@Test
	public void testUpdateDocumentRequestTranslationWithNoRefresh()
		throws Exception {

		_testUpdateDocumentRequestTranslation(
			"1", false, WriteRequest.RefreshPolicy.NONE);
	}

	@Test
	public void testUpdateDocumentRequestTranslationWithNoRefreshNoId()
		throws Exception {

		_testUpdateDocumentRequestTranslation(
			null, false, WriteRequest.RefreshPolicy.NONE);
	}

	@Test
	public void testUpdateDocumentRequestTranslationWithRefresh()
		throws Exception {

		_testUpdateDocumentRequestTranslation(
			"1", true, WriteRequest.RefreshPolicy.IMMEDIATE);
	}

	@Test
	public void testUpdateDocumentRequestTranslationWithRefreshNoId()
		throws Exception {

		_testUpdateDocumentRequestTranslation(
			null, true, WriteRequest.RefreshPolicy.IMMEDIATE);
	}

	private OpenSearchBulkableDocumentRequestTranslator
		_createOpenSearchBulkableDocumentRequestTranslator(
			OpenSearchDocumentFactory openSearchDocumentFactory) {

		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator =
				new OpenSearchBulkableDocumentRequestTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			openSearchBulkableDocumentRequestTranslator,
			"_openSearchDocumentFactory", openSearchDocumentFactory);

		return openSearchBulkableDocumentRequestTranslator;
	}

	private OpenSearchDocumentFactory _createOpenSearchDocumentFactory() {
		return new OpenSearchDocumentFactoryImpl();
	}

	private void _setUid(Document document, String uid) {
		if (!Validator.isBlank(uid)) {
			document.addKeyword(Field.UID, uid);
		}
	}

	private void _testDeleteDocumentRequestTranslation(
		boolean refreshPolicy,
		WriteRequest.RefreshPolicy expectedRefreshPolicy) {

		String id = "1";

		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			_INDEX_NAME, id);

		deleteDocumentRequest.setRefresh(refreshPolicy);
		deleteDocumentRequest.setType(_MAPPING_NAME);

		DeleteRequest deleteRequest =
			_openSearchBulkableDocumentRequestTranslator.translate(
				deleteDocumentRequest);

		Assert.assertEquals(
			expectedRefreshPolicy, deleteRequest.getRefreshPolicy());
		Assert.assertEquals(_INDEX_NAME, deleteRequest.index());
		Assert.assertEquals(_MAPPING_NAME, deleteRequest.type());
		Assert.assertEquals(id, deleteRequest.id());

		BulkRequest bulkRequest = new BulkRequest();

		bulkRequest.add(
			_openSearchBulkableDocumentRequestTranslator.translate(
				deleteDocumentRequest));

		Assert.assertEquals(1, bulkRequest.numberOfActions());
	}

	private void _testIndexDocumentRequestTranslation(
			String id, boolean refreshPolicy,
			WriteRequest.RefreshPolicy expectedRefreshPolicy)
		throws Exception {

		Document document = new DocumentImpl();

		_setUid(document, id);

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			_INDEX_NAME, document);

		indexDocumentRequest.setRefresh(refreshPolicy);
		indexDocumentRequest.setType(_MAPPING_NAME);

		IndexRequest indexRequest =
			_openSearchBulkableDocumentRequestTranslator.translate(
				indexDocumentRequest);

		Assert.assertEquals(
			expectedRefreshPolicy, indexRequest.getRefreshPolicy());
		Assert.assertEquals(_INDEX_NAME, indexRequest.index());
		Assert.assertEquals(id, indexRequest.id());

		String source = XContentHelper.convertToJson(
			indexRequest.source(), false, XContentType.JSON);

		Assert.assertEquals(
			_openSearchDocumentFactory.getOpenSearchDocument(document), source);

		BulkRequest bulkRequest = new BulkRequest();

		bulkRequest.add(
			_openSearchBulkableDocumentRequestTranslator.translate(
				indexDocumentRequest));

		Assert.assertEquals(1, bulkRequest.numberOfActions());
	}

	private void _testUpdateDocumentRequestTranslation(
			String id, boolean refreshPolicy,
			WriteRequest.RefreshPolicy expectedRefreshPolicy)
		throws Exception {

		Document document = new DocumentImpl();

		_setUid(document, id);

		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			_INDEX_NAME, id, document);

		updateDocumentRequest.setRefresh(refreshPolicy);
		updateDocumentRequest.setType(_MAPPING_NAME);

		UpdateRequest updateRequest =
			_openSearchBulkableDocumentRequestTranslator.translate(
				updateDocumentRequest);

		Assert.assertEquals(
			expectedRefreshPolicy, updateRequest.getRefreshPolicy());
		Assert.assertEquals(_INDEX_NAME, updateRequest.index());
		Assert.assertEquals(id, updateRequest.id());

		IndexRequest indexRequest = updateRequest.doc();

		String source = XContentHelper.convertToJson(
			indexRequest.source(), false, XContentType.JSON);

		Assert.assertEquals(
			_openSearchDocumentFactory.getOpenSearchDocument(document), source);

		BulkRequest bulkRequest = new BulkRequest();

		bulkRequest.add(
			_openSearchBulkableDocumentRequestTranslator.translate(
				updateDocumentRequest));

		Assert.assertEquals(1, bulkRequest.numberOfActions());
	}

	private static final String _INDEX_NAME = "test_request_index";

	private static final String _MAPPING_NAME = "testMapping";

	private static OpenSearchFixture _openSearchFixture;

	private final DocumentFixture _documentFixture = new DocumentFixture();
	private OpenSearchBulkableDocumentRequestTranslator
		_openSearchBulkableDocumentRequestTranslator;
	private OpenSearchDocumentFactory _openSearchDocumentFactory;

}