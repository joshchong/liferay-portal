/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.document;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.engine.adapter.document.DocumentRequestExecutor;
import com.liferay.portal.search.internal.document.DocumentBuilderFactoryImpl;
import com.liferay.portal.search.internal.geolocation.GeoBuildersImpl;
import com.liferay.portal.search.internal.script.ScriptsImpl;
import com.liferay.portal.search.opensearch2.internal.document.OpenSearchDocumentFactory;
import com.liferay.portal.search.opensearch2.internal.query.OpenSearchQueryTranslatorFixture;
import com.liferay.portal.search.script.Scripts;

/**
 * @author Dylan Rebelak
 */
public class DocumentRequestExecutorFixture {

	public DocumentRequestExecutor getDocumentRequestExecutor() {
		return _documentRequestExecutor;
	}

	public void setUp() {
		_documentRequestExecutor = _createDocumentRequestExecutor(
			_openSearchClientResolver, _openSearchDocumentFactory);
	}

	protected void setOpenSearchClientResolver(
		OpenSearchClientResolver openSearchClientResolver) {

		_openSearchClientResolver = openSearchClientResolver;
	}

	protected void setOpenSearchDocumentFactory(
		OpenSearchDocumentFactory openSearchDocumentFactory) {

		_openSearchDocumentFactory = openSearchDocumentFactory;
	}

	private OpenSearchBulkableDocumentRequestTranslator
		_createBulkableDocumentRequestTranslator(
			OpenSearchDocumentFactory openSearchDocumentFactory) {

		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator =
				new OpenSearchBulkableDocumentRequestTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			openSearchBulkableDocumentRequestTranslator,
			"_openSearchDocumentFactory", openSearchDocumentFactory);

		return openSearchBulkableDocumentRequestTranslator;
	}

	private BulkDocumentRequestExecutor _createBulkDocumentRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator) {

		BulkDocumentRequestExecutor bulkDocumentRequestExecutor =
			new BulkDocumentRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			bulkDocumentRequestExecutor,
			"_openSearchBulkableDocumentRequestTranslator",
			openSearchBulkableDocumentRequestTranslator);
		ReflectionTestUtil.setFieldValue(
			bulkDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return bulkDocumentRequestExecutor;
	}

	private DeleteByQueryDocumentRequestExecutor
		_createDeleteByQueryDocumentRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		DeleteByQueryDocumentRequestExecutor
			deleteByQueryDocumentRequestExecutor =
				new DeleteByQueryDocumentRequestExecutorImpl();

		com.liferay.portal.search.opensearch2.internal.legacy.query.
			OpenSearchQueryTranslatorFixture
				legacyOpenSearchQueryTranslatorFixture =
					new com.liferay.portal.search.opensearch2.internal.legacy.
						query.OpenSearchQueryTranslatorFixture();

		OpenSearchQueryTranslatorFixture openSearchQueryTranslatorFixture =
			new OpenSearchQueryTranslatorFixture();

		ReflectionTestUtil.setFieldValue(
			deleteByQueryDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			deleteByQueryDocumentRequestExecutor, "_legacyQueryTranslator",
			legacyOpenSearchQueryTranslatorFixture.
				getOpenSearchQueryTranslator());
		ReflectionTestUtil.setFieldValue(
			deleteByQueryDocumentRequestExecutor, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());

		return deleteByQueryDocumentRequestExecutor;
	}

	private DeleteDocumentRequestExecutor _createDeleteDocumentRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator) {

		DeleteDocumentRequestExecutor deleteDocumentRequestExecutor =
			new DeleteDocumentRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			deleteDocumentRequestExecutor,
			"_openSearchBulkableDocumentRequestTranslator",
			openSearchBulkableDocumentRequestTranslator);
		ReflectionTestUtil.setFieldValue(
			deleteDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return deleteDocumentRequestExecutor;
	}

	private DocumentRequestExecutor _createDocumentRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchDocumentFactory openSearchDocumentFactory) {

		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator =
				_createBulkableDocumentRequestTranslator(
					openSearchDocumentFactory);

		DocumentRequestExecutor documentRequestExecutor =
			new OpenSearchDocumentRequestExecutor();

		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_bulkDocumentRequestExecutor",
			_createBulkDocumentRequestExecutor(
				openSearchClientResolver,
				openSearchBulkableDocumentRequestTranslator));
		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_deleteByQueryDocumentRequestExecutor",
			_createDeleteByQueryDocumentRequestExecutor(
				openSearchClientResolver));
		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_deleteDocumentRequestExecutor",
			_createDeleteDocumentRequestExecutor(
				openSearchClientResolver,
				openSearchBulkableDocumentRequestTranslator));
		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_getDocumentRequestExecutor",
			_createGetDocumentRequestExecutor(
				openSearchClientResolver,
				openSearchBulkableDocumentRequestTranslator));
		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_indexDocumentRequestExecutor",
			_createIndexDocumentRequestExecutor(
				openSearchClientResolver,
				openSearchBulkableDocumentRequestTranslator));
		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_updateByQueryDocumentRequestExecutor",
			_createUpdateByQueryDocumentRequestExecutor(
				openSearchClientResolver));
		ReflectionTestUtil.setFieldValue(
			documentRequestExecutor, "_updateDocumentRequestExecutor",
			_createUpdateDocumentRequestExecutor(
				openSearchClientResolver,
				openSearchBulkableDocumentRequestTranslator));

		return documentRequestExecutor;
	}

	private GetDocumentRequestExecutor _createGetDocumentRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator) {

		GetDocumentRequestExecutor getDocumentRequestExecutor =
			new GetDocumentRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			getDocumentRequestExecutor,
			"_openSearchBulkableDocumentRequestTranslator",
			openSearchBulkableDocumentRequestTranslator);
		ReflectionTestUtil.setFieldValue(
			getDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			getDocumentRequestExecutor, "_documentBuilderFactory",
			new DocumentBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			getDocumentRequestExecutor, "_geoBuilders", new GeoBuildersImpl());

		return getDocumentRequestExecutor;
	}

	private IndexDocumentRequestExecutor _createIndexDocumentRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator) {

		IndexDocumentRequestExecutor indexDocumentRequestExecutor =
			new IndexDocumentRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			indexDocumentRequestExecutor,
			"_openSearchBulkableDocumentRequestTranslator",
			openSearchBulkableDocumentRequestTranslator);
		ReflectionTestUtil.setFieldValue(
			indexDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return indexDocumentRequestExecutor;
	}

	private UpdateByQueryDocumentRequestExecutor
		_createUpdateByQueryDocumentRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		UpdateByQueryDocumentRequestExecutor
			updateByQueryDocumentRequestExecutor =
				new UpdateByQueryDocumentRequestExecutorImpl();

		com.liferay.portal.search.opensearch2.internal.legacy.query.
			OpenSearchQueryTranslatorFixture
				lecacyOpenSearchQueryTranslatorFixture =
					new com.liferay.portal.search.opensearch2.internal.legacy.
						query.OpenSearchQueryTranslatorFixture();

		OpenSearchQueryTranslatorFixture openSearchQueryTranslatorFixture =
			new OpenSearchQueryTranslatorFixture();

		ReflectionTestUtil.setFieldValue(
			updateByQueryDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			updateByQueryDocumentRequestExecutor, "_legacyQueryTranslator",
			lecacyOpenSearchQueryTranslatorFixture.
				getOpenSearchQueryTranslator());
		ReflectionTestUtil.setFieldValue(
			updateByQueryDocumentRequestExecutor, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());
		ReflectionTestUtil.setFieldValue(
			updateByQueryDocumentRequestExecutor, "_scripts", _scripts);

		return updateByQueryDocumentRequestExecutor;
	}

	private UpdateDocumentRequestExecutor _createUpdateDocumentRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchBulkableDocumentRequestTranslator
			openSearchBulkableDocumentRequestTranslator) {

		UpdateDocumentRequestExecutor updateDocumentRequestExecutor =
			new UpdateDocumentRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			updateDocumentRequestExecutor,
			"_openSearchBulkableDocumentRequestTranslator",
			openSearchBulkableDocumentRequestTranslator);
		ReflectionTestUtil.setFieldValue(
			updateDocumentRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return updateDocumentRequestExecutor;
	}

	private static final Scripts _scripts = new ScriptsImpl();

	private DocumentRequestExecutor _documentRequestExecutor;
	private OpenSearchDocumentFactory _openSearchDocumentFactory;

}