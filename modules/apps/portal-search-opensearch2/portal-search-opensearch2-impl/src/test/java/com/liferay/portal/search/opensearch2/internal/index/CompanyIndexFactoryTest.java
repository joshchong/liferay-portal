/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.index;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.opensearch2.internal.configuration.OpenSearchConfigurationWrapper;
import com.liferay.portal.search.opensearch2.internal.connection.IndexName;
import com.liferay.portal.search.opensearch2.internal.connection.OpenSearchFixture;
import com.liferay.portal.search.opensearch2.internal.document.SingleFieldFixture;
import com.liferay.portal.search.opensearch2.internal.index.constants.MappingsConstants;
import com.liferay.portal.search.opensearch2.internal.query.QueryBuilderFactories;
import com.liferay.portal.search.opensearch2.internal.util.ResourceUtil;
import com.liferay.portal.search.spi.model.index.contributor.IndexContributor;
import com.liferay.portal.search.spi.settings.IndexSettingsContributor;
import com.liferay.portal.search.spi.settings.IndexSettingsHelper;
import com.liferay.portal.search.spi.settings.TypeMappingsHelper;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.mockito.Mockito;

import org.opensearch.action.ingest.PutPipelineRequest;
import org.opensearch.client.IngestClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.client.indices.GetIndexResponse;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.cluster.metadata.MappingMetadata;
import org.opensearch.common.bytes.BytesArray;
import org.opensearch.common.settings.Settings;
import org.opensearch.xcontent.XContentType;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author André de Oliveira
 */
public class CompanyIndexFactoryTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() throws Exception {
		_openSearchFixture = new OpenSearchFixture(
			CompanyIndexFactoryTest.class.getSimpleName());

		_openSearchFixture.setUp();

		_putTimestampPipeline(_openSearchFixture.getOpenSearchClient());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_openSearchFixture.tearDown();
	}

	@Before
	public void setUp() throws Exception {
		_companyIndexFactoryFixture = new CompanyIndexFactoryFixture(
			_openSearchFixture, testName.getMethodName());

		_companyIndexFactory =
			_companyIndexFactoryFixture.getCompanyIndexFactory();

		IndexHelper indexHelper =
			_companyIndexFactoryFixture.getCompanyIndexFactoryHelper();

		Mockito.reset(_openSearchConfigurationWrapper);

		ReflectionTestUtil.setFieldValue(
			indexHelper, "_openSearchConfigurationWrapper",
			_openSearchConfigurationWrapper);

		ReflectionTestUtil.setFieldValue(
			_companyIndexFactory, "_companyIndexFactoryHelper", indexHelper);
		ReflectionTestUtil.setFieldValue(
			_companyIndexFactory, "_openSearchConfigurationWrapper",
			_openSearchConfigurationWrapper);

		Mockito.when(
			_openSearchConfigurationWrapper.indexMaxResultWindow()
		).thenReturn(
			10000
		);

		_singleFieldFixture = new SingleFieldFixture(
			_openSearchFixture.getOpenSearchClient(),
			new IndexName(_companyIndexFactoryFixture.getIndexName()),
			MappingsConstants.LIFERAY_DOCUMENT_TYPE);

		_singleFieldFixture.setQueryBuilderFactory(QueryBuilderFactories.MATCH);
	}

	@After
	public void tearDown() {
		_companyIndexFactoryFixture.tearDown();

		if (_serviceRegistrations.isEmpty()) {
			return;
		}

		for (ServiceRegistration<?> serviceRegistration :
				_serviceRegistrations) {

			serviceRegistration.unregister();
		}

		_serviceRegistrations.clear();
	}

	@Test
	public void testAdditionalIndexConfigurations() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			"index.number_of_replicas: 1\nindex.number_of_shards: 2"
		);

		createIndices();

		Settings settings = _getIndexSettings();

		Assert.assertEquals("1", settings.get("index.number_of_replicas"));
		Assert.assertEquals("2", settings.get("index.number_of_shards"));
	}

	@Test
	public void testAdditionalTypeMappings() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalTypeMappings()
		).thenReturn(
			loadAdditionalTypeMappings()
		);

		_assertAdditionalTypeMappings();
	}

	@Test
	public void testAdditionalTypeMappingsWithLegacyRootType()
		throws Exception {

		Mockito.when(
			_openSearchConfigurationWrapper.additionalTypeMappings()
		).thenReturn(
			_loadAdditionalTypeMappingsWithLegacyRootType()
		);

		_assertAdditionalTypeMappings();
	}

	@Test
	public void testAddMultipleIndexSettingsContributors() throws Exception {
		_serviceRegistrations.add(
			_bundleContext.registerService(
				IndexSettingsContributor.class,
				new TestIndexSettingsContributor(), null));

		_serviceRegistrations.add(
			_bundleContext.registerService(
				IndexSettingsContributor.class,
				new TestIndexSettingsContributor(), null));
	}

	@Test
	public void testCreateIndicesWithBlankStrings() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			StringPool.BLANK
		);

		Mockito.when(
			_openSearchConfigurationWrapper.additionalTypeMappings()
		).thenReturn(
			StringPool.SPACE
		);

		Mockito.when(
			_openSearchConfigurationWrapper.indexNumberOfReplicas()
		).thenReturn(
			StringPool.BLANK
		);

		Mockito.when(
			_openSearchConfigurationWrapper.indexNumberOfShards()
		).thenReturn(
			StringPool.SPACE
		);

		createIndices();
	}

	@Test
	public void testCreateIndicesWithEmptyConfiguration() throws Exception {
		createIndices();
	}

	@Test
	public void testDefaultIndexSettings() throws Exception {
		createIndices();

		Settings settings = _getIndexSettings();

		Assert.assertEquals("0", settings.get("index.number_of_replicas"));
		Assert.assertEquals("1", settings.get("index.number_of_shards"));
	}

	@Test
	public void testDefaultIndices() throws Exception {
		createIndices();

		_assertMappings(Field.COMPANY_ID, Field.ENTRY_CLASS_NAME);
	}

	@Test
	public void testIndexConfigurations() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.indexNumberOfReplicas()
		).thenReturn(
			"1"
		);

		Mockito.when(
			_openSearchConfigurationWrapper.indexNumberOfShards()
		).thenReturn(
			"2"
		);

		createIndices();

		Settings settings = _getIndexSettings();

		Assert.assertEquals("1", settings.get("index.number_of_replicas"));
		Assert.assertEquals("2", settings.get("index.number_of_shards"));
	}

	@Test
	public void testIndexContributors() throws Exception {
		ReflectionTestUtil.setFieldValue(
			_companyIndexFactoryFixture, "_indexName", "other");

		ReflectionTestUtil.setFieldValue(
			_companyIndexFactoryFixture.getCompanyIndexFactoryHelper(),
			"_indexContributorServiceTrackerList",
			ServiceTrackerListFactory.open(
				_bundleContext, IndexContributor.class, null,
				new ServiceTrackerCustomizer
					<IndexContributor, IndexContributor>() {

					@Override
					public IndexContributor addingService(
						ServiceReference<IndexContributor> serviceReference) {

						return null;
					}

					@Override
					public void modifiedService(
						ServiceReference<IndexContributor> serviceReference,
						IndexContributor indexContributor) {
					}

					@Override
					public void removedService(
						ServiceReference<IndexContributor> serviceReference,
						IndexContributor indexContributor) {
					}

				}));

		addIndexContributor(
			new IndexContributor() {

				@Override
				public void onAfterCreate(String indexName) {
					_companyIndexFactoryFixture.createIndices();
				}

				@Override
				public void onBeforeRemove(String indexName) {
					_companyIndexFactoryFixture.deleteIndices();
				}

			});

		createIndices();

		_assertHasIndex(_companyIndexFactoryFixture.getIndexName());

		deleteIndices();

		_assertNoIndex(_companyIndexFactoryFixture.getIndexName());
	}

	@Test
	public void testIndexContributorsThrowsException() throws Exception {
		addIndexContributor(
			new IndexContributor() {

				@Override
				public void onAfterCreate(String indexName) {
					throw new RuntimeException();
				}

				@Override
				public void onBeforeRemove(String indexName) {
					throw new RuntimeException();
				}

			});

		createIndices();
	}

	@Test
	public void testIndexSettingsContributor() throws Exception {
		_serviceRegistrations.add(
			_bundleContext.registerService(
				IndexSettingsContributor.class,
				new IndexSettingsContributor() {

					@Override
					public void contribute(
						String indexName,
						TypeMappingsHelper typeMappingsHelper) {
					}

					@Override
					public void populate(
						IndexSettingsHelper indexSettingsHelper) {

						indexSettingsHelper.put(
							"index.number_of_replicas", "2");
						indexSettingsHelper.put("index.number_of_shards", "3");
					}

				},
				null));

		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			"index.number_of_replicas: 0\nindex.number_of_shards: 0"
		);

		createIndices();

		Settings settings = _getIndexSettings();

		Assert.assertEquals("2", settings.get("index.number_of_replicas"));
		Assert.assertEquals("3", settings.get("index.number_of_shards"));
	}

	@Test
	public void testIndexSettingsContributorTypeMappings() throws Exception {
		String mappings = loadAdditionalTypeMappings();

		_serviceRegistrations.add(
			_bundleContext.registerService(
				IndexSettingsContributor.class,
				new IndexSettingsContributor() {

					@Override
					public void contribute(
						String indexName,
						TypeMappingsHelper typeMappingsHelper) {

						typeMappingsHelper.addTypeMappings(
							indexName, _replaceAnalyzer(mappings, "brazilian"));
					}

					@Override
					public void populate(
						IndexSettingsHelper indexSettingsHelper) {
					}

				},
				null));

		Mockito.when(
			_openSearchConfigurationWrapper.additionalTypeMappings()
		).thenReturn(
			_replaceAnalyzer(mappings, "portuguese")
		);

		createIndices();

		String field = RandomTestUtil.randomString() + "_ja";

		_indexOneDocument(field);

		assertAnalyzer(field, "brazilian");
	}

	@Test
	public void testOptionalDefaultTemplateIsAlwaysAfterContributedTemplates()
		throws Exception {

		Mockito.when(
			_openSearchConfigurationWrapper.additionalTypeMappings()
		).thenReturn(
			loadAdditionalTypeMappings()
		);

		createIndices();

		_indexOneDocument("match_additional_mapping");
		_indexOneDocument("match_catch_all");

		assertType("match_additional_mapping", "keyword");
		assertType("match_catch_all", "text");
	}

	@Test
	public void testOverrideLegacyTypeMappings() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			_loadAdditionalAnalyzers()
		);

		Mockito.when(
			_openSearchConfigurationWrapper.overrideTypeMappings()
		).thenReturn(
			_loadOverrideLegacyTypeMappings()
		);

		createIndices();

		String field1 = "title";

		_indexOneDocument(field1);

		assertAnalyzer(field1, "kuromoji_liferay_custom");

		String field2 = "description";

		_indexOneDocument(field2);

		_assertNoAnalyzer(field2);
	}

	@Test
	public void testOverrideTypeMappings() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			_loadAdditionalAnalyzers()
		);

		Mockito.when(
			_openSearchConfigurationWrapper.overrideTypeMappings()
		).thenReturn(
			_loadOverrideTypeMappings()
		);

		createIndices();

		String field1 = "title";

		_indexOneDocument(field1);

		assertAnalyzer(field1, "kuromoji_liferay_custom");

		String field2 = "description";

		_indexOneDocument(field2);

		_assertNoAnalyzer(field2);
	}

	@Test
	public void testOverrideTypeMappingsHonorDefaultIndices() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			_loadAdditionalAnalyzers()
		);

		Mockito.when(
			_openSearchConfigurationWrapper.overrideTypeMappings()
		).thenReturn(
			_loadOverrideTypeMappings()
		);

		createIndices();

		_assertMappings(Field.TITLE);
	}

	@Test
	public void testRemoveIndexSettingsContributor() {
		ServiceRegistration<IndexSettingsContributor> serviceRegistration =
			_bundleContext.registerService(
				IndexSettingsContributor.class,
				new TestIndexSettingsContributor(), null);

		serviceRegistration.unregister();
	}

	@Rule
	public TestName testName = new TestName();

	protected void addIndexContributor(IndexContributor indexContributor) {
		_serviceRegistrations.add(
			_bundleContext.registerService(
				IndexContributor.class, indexContributor, null));
	}

	protected void assertAnalyzer(String field, String analyzer)
		throws Exception {

		OpenSearchClient openSearchClient =
			_openSearchFixture.getOpenSearchClient();

		FieldMappingAssert.assertAnalyzer(
			analyzer, field, MappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_companyIndexFactoryFixture.getIndexName(),
			openSearchClient.indices());
	}

	protected void assertType(String field, String type) throws Exception {
		OpenSearchClient openSearchClient =
			_openSearchFixture.getOpenSearchClient();

		FieldMappingAssert.assertType(
			type, field, MappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_companyIndexFactoryFixture.getIndexName(),
			openSearchClient.indices());
	}

	protected void createIndices() throws Exception {
		OpenSearchClient openSearchClient =
			_openSearchFixture.getOpenSearchClient();

		OpenSearchIndicesClient openSearchIndicesClient =
			openSearchClient.indices();

		_companyIndexFactory.createIndices(
			openSearchIndicesClient, RandomTestUtil.randomLong());
	}

	protected void deleteIndices() {
		OpenSearchClient openSearchClient =
			_openSearchFixture.getOpenSearchClient();

		OpenSearchIndicesClient openSearchIndicesClient =
			openSearchClient.indices();

		_companyIndexFactory.deleteIndices(
			openSearchIndicesClient, RandomTestUtil.randomLong());
	}

	protected boolean hasIndex(String indexName) {
		OpenSearchClient openSearchClient =
			_openSearchFixture.getOpenSearchClient();

		OpenSearchIndicesClient openSearchIndicesClient =
			openSearchClient.indices();

		GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

		try {
			return openSearchIndicesClient.exists(
				getIndexRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	protected String loadAdditionalTypeMappings() {
		try {
			return ResourceUtil.getResourceAsString(
				getClass(),
				"CompanyIndexFactoryTest-additionalTypeMappings.json");
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	protected static class TestIndexSettingsContributor
		implements IndexSettingsContributor {

		@Override
		public void contribute(
			String indexName, TypeMappingsHelper typeMappingsHelper) {
		}

		@Override
		public void populate(IndexSettingsHelper indexSettingsHelper) {
		}

	}

	private static void _putTimestampPipeline(OpenSearchClient openSearchClient)
		throws Exception {

		IngestClient ingestClient = openSearchClient.ingest();

		String source = JSONUtil.put(
			"description", "Adds timestamp to documents"
		).put(
			"processors",
			JSONUtil.put(
				JSONUtil.put(
					"set",
					JSONUtil.put(
						"field", "_source.timestamp"
					).put(
						"value", "{{{_ingest.timestamp}}}"
					)))
		).toString();

		PutPipelineRequest putPipelineRequest = new PutPipelineRequest(
			"timestamp",
			new BytesArray(source.getBytes(StandardCharsets.UTF_8)),
			XContentType.JSON);

		ingestClient.putPipeline(putPipelineRequest, RequestOptions.DEFAULT);
	}

	private void _assertAdditionalTypeMappings() throws Exception {
		Mockito.when(
			_openSearchConfigurationWrapper.additionalIndexConfigurations()
		).thenReturn(
			_loadAdditionalAnalyzers()
		);

		createIndices();

		String contributedKeywordFieldName = "orderStatus";

		assertType(contributedKeywordFieldName, "keyword");

		String contributedTextFieldName = "productDescription";

		assertType(contributedTextFieldName, "text");

		String liferayKeywordFieldName = "status";

		assertType(liferayKeywordFieldName, "keyword");

		String liferayTextFieldName = "subtitle";

		assertType(liferayTextFieldName, "text");

		String intactFieldName = RandomTestUtil.randomString() + "_en";

		_indexOneDocument(intactFieldName);

		assertAnalyzer(intactFieldName, "english");

		String replacedFieldName = RandomTestUtil.randomString() + "_ja";

		_indexOneDocument(replacedFieldName);

		assertAnalyzer(replacedFieldName, "kuromoji_liferay_custom");
	}

	private void _assertHasIndex(String indexName) {
		Assert.assertTrue(
			"Index " + indexName + " does not exist", hasIndex(indexName));
	}

	private void _assertMappings(String... fieldNames) {
		String indexName = _companyIndexFactoryFixture.getIndexName();

		GetIndexResponse getIndexResponse = _openSearchFixture.getIndex(
			indexName);

		Map<String, MappingMetadata> mappings =
			getIndexResponse.getMappingsJSONObject();

		MappingMetadata mappingMetadata = mappings.get(indexName);

		Map<String, Object> map = _getPropertiesMap(mappingMetadata);

		Set<String> set = map.keySet();

		Assert.assertThat(set, CoreMatchers.hasItems(fieldNames));
	}

	private void _assertNoAnalyzer(String field) throws Exception {
		assertAnalyzer(field, null);
	}

	private void _assertNoIndex(String indexName) {
		Assert.assertFalse(
			"Index " + indexName + " exists", hasIndex(indexName));
	}

	private Settings _getIndexSettings() {
		String name = _companyIndexFactoryFixture.getIndexName();

		GetIndexResponse getIndexResponse = _openSearchFixture.getIndex(name);

		Map<String, Settings> map = getIndexResponse.getSettingsJSONObject();

		return map.get(name);
	}

	private Map<String, Object> _getPropertiesMap(
		MappingMetadata mappingMetadata) {

		Map<String, Object> map = mappingMetadata.getSourceAsMap();

		return (Map<String, Object>)map.get("properties");
	}

	private void _indexOneDocument(String field) {
		_indexOneDocument(field, RandomTestUtil.randomString());
	}

	private void _indexOneDocument(String field, String value) {
		_singleFieldFixture.setField(field);

		_singleFieldFixture.indexDocument(value);
	}

	private String _loadAdditionalAnalyzers() throws Exception {
		return ResourceUtil.getResourceAsString(
			getClass(), "CompanyIndexFactoryTest-additionalAnalyzers.json");
	}

	private String _loadAdditionalTypeMappingsWithLegacyRootType() {
		try {
			return ResourceUtil.getResourceAsString(
				getClass(),
				"CompanyIndexFactoryTest-additionalTypeMappings-with-legacy-" +
					"root-type.json");
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private String _loadOverrideLegacyTypeMappings() throws Exception {
		return ResourceUtil.getResourceAsString(
			getClass(),
			"CompanyIndexFactoryTest-overrideLegacyTypeMappings.json");
	}

	private String _loadOverrideTypeMappings() throws Exception {
		return ResourceUtil.getResourceAsString(
			getClass(), "CompanyIndexFactoryTest-overrideTypeMappings.json");
	}

	private String _replaceAnalyzer(String mappings, String analyzer) {
		return StringUtil.replace(
			mappings, "kuromoji_liferay_custom", analyzer);
	}

	private static final BundleContext _bundleContext =
		SystemBundleUtil.getBundleContext();
	private static OpenSearchFixture _openSearchFixture;

	private CompanyIndexFactory _companyIndexFactory;
	private CompanyIndexFactoryFixture _companyIndexFactoryFixture;
	private final OpenSearchConfigurationWrapper
		_openSearchConfigurationWrapper = Mockito.mock(
			OpenSearchConfigurationWrapper.class);
	private final List<ServiceRegistration<?>> _serviceRegistrations =
		new ArrayList<>();
	private SingleFieldFixture _singleFieldFixture;

}