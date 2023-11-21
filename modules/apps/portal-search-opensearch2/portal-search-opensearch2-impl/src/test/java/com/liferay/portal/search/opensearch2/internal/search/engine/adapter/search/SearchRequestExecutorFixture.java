/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.search;

import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.search.query.QueryTranslator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.search.engine.adapter.search.SearchRequestExecutor;
import com.liferay.portal.search.filter.ComplexQueryBuilderFactory;
import com.liferay.portal.search.internal.aggregation.AggregationResultsImpl;
import com.liferay.portal.search.internal.document.DocumentBuilderFactoryImpl;
import com.liferay.portal.search.internal.facet.ModifiedFacetImpl;
import com.liferay.portal.search.internal.facet.NestedFacetImpl;
import com.liferay.portal.search.internal.filter.ComplexQueryBuilderFactoryImpl;
import com.liferay.portal.search.internal.geolocation.GeoBuildersImpl;
import com.liferay.portal.search.internal.groupby.GroupByResponseFactoryImpl;
import com.liferay.portal.search.internal.highlight.HighlightFieldBuilderFactoryImpl;
import com.liferay.portal.search.internal.hits.SearchHitBuilderFactoryImpl;
import com.liferay.portal.search.internal.hits.SearchHitsBuilderFactoryImpl;
import com.liferay.portal.search.internal.legacy.groupby.GroupByRequestFactoryImpl;
import com.liferay.portal.search.internal.legacy.stats.StatsRequestBuilderFactoryImpl;
import com.liferay.portal.search.internal.legacy.stats.StatsResultsTranslatorImpl;
import com.liferay.portal.search.internal.query.QueriesImpl;
import com.liferay.portal.search.internal.stats.StatsResponseBuilderFactoryImpl;
import com.liferay.portal.search.legacy.stats.StatsRequestBuilderFactory;
import com.liferay.portal.search.opensearch2.internal.aggregation.OpenSearchAggregationTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.aggregation.pipeline.OpenSearchPipelineAggregationTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.facet.FacetProcessor;
import com.liferay.portal.search.opensearch2.internal.facet.FacetTranslator;
import com.liferay.portal.search.opensearch2.internal.facet.FacetTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.facet.NestedFacetProcessor;
import com.liferay.portal.search.opensearch2.internal.facet.RangeFacetProcessor;
import com.liferay.portal.search.opensearch2.internal.filter.OpenSearchFilterTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.groupby.GroupByTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.highlight.HighlighterTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.legacy.hits.HitDocumentTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.legacy.sort.SortTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.query.OpenSearchQueryTranslator;
import com.liferay.portal.search.opensearch2.internal.query.OpenSearchQueryTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.search.response.SearchResponseTranslator;
import com.liferay.portal.search.opensearch2.internal.search.response.SearchResponseTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.sort.OpenSearchSortFieldTranslator;
import com.liferay.portal.search.opensearch2.internal.sort.OpenSearchSortFieldTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.stats.StatsTranslator;
import com.liferay.portal.search.opensearch2.internal.stats.StatsTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.suggest.OpenSearchSuggesterTranslatorFixture;
import com.liferay.portal.search.query.Queries;

import java.util.ArrayList;
import java.util.List;

import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.index.query.QueryBuilder;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Michael C. Han
 */
public class SearchRequestExecutorFixture {

	public SearchRequestExecutor getSearchRequestExecutor() {
		return _searchRequestExecutor;
	}

	public void setUp() {
		OpenSearchQueryTranslatorFixture openSearchQueryTranslatorFixture =
			new OpenSearchQueryTranslatorFixture();

		OpenSearchQueryTranslator openSearchQueryTranslator =
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator();

		OpenSearchSortFieldTranslatorFixture
			openSearchSortFieldTranslatorFixture =
				new OpenSearchSortFieldTranslatorFixture(
					openSearchQueryTranslator);

		StatsTranslator statsTranslator = new StatsTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			statsTranslator, "_statsResponseBuilderFactory",
			new StatsResponseBuilderFactoryImpl());

		_searchRequestExecutor = _createSearchRequestExecutor(
			createComplexQueryBuilderFactory(new QueriesImpl()),
			_openSearchClientResolver, openSearchQueryTranslator,
			openSearchSortFieldTranslatorFixture.
				getOpenSearchSortFieldTranslator(),
			_facetProcessor, new StatsRequestBuilderFactoryImpl(),
			statsTranslator);
	}

	public void tearDown() {
		_serviceRegistrations.forEach(
			serviceRegistration -> serviceRegistration.unregister());

		ReflectionTestUtil.invoke(
			_facetTranslatorImpl, "deactivate", new Class<?>[0]);
	}

	protected static CommonSearchRequestBuilderAssembler
		createCommonSearchSourceBuilderAssembler(
			OpenSearchQueryTranslator openSearchQueryTranslator,
			FacetProcessor<?> facetProcessor, StatsTranslator statsTranslator,
			ComplexQueryBuilderFactory complexQueryBuilderFactory) {

		com.liferay.portal.search.opensearch2.internal.legacy.query.
			OpenSearchQueryTranslatorFixture
				legacyOpenSearchQueryTranslatorFixture =
					new com.liferay.portal.search.opensearch2.internal.legacy.
						query.OpenSearchQueryTranslatorFixture();

		com.liferay.portal.search.opensearch2.internal.legacy.query.
			OpenSearchQueryTranslator legacyOpenSearchQueryTranslator =
				legacyOpenSearchQueryTranslatorFixture.
					getOpenSearchQueryTranslator();

		OpenSearchAggregationTranslatorFixture
			openSearchAggregationTranslatorFixture =
				new OpenSearchAggregationTranslatorFixture();

		OpenSearchFilterTranslatorFixture openSearchFilterTranslatorFixture =
			new OpenSearchFilterTranslatorFixture(
				legacyOpenSearchQueryTranslator);

		OpenSearchPipelineAggregationTranslatorFixture
			openSearchPipelineAggregationTranslatorFixture =
				new OpenSearchPipelineAggregationTranslatorFixture();

		CommonSearchRequestBuilderAssembler
			commonSearchRequestBuilderAssembler =
				new CommonSearchRequestBuilderAssemblerImpl();

		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_aggregationTranslator",
			openSearchAggregationTranslatorFixture.
				getOpenSearchAggregationTranslator());
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_complexQueryBuilderFactory",
			complexQueryBuilderFactory);
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_facetTranslator",
			_createFacetTranslator(
				facetProcessor, legacyOpenSearchQueryTranslator));
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_filterTranslator",
			openSearchFilterTranslatorFixture.getOpenSearchFilterTranslator());
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_legacyQueryTranslator",
			legacyOpenSearchQueryTranslator);
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler,
			"_pipelineAggregationTranslator",
			openSearchPipelineAggregationTranslatorFixture.
				getOpenSearchPipelineAggregationTranslator());
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_queryTranslator",
			openSearchQueryTranslator);
		ReflectionTestUtil.setFieldValue(
			commonSearchRequestBuilderAssembler, "_statsTranslator",
			statsTranslator);

		return commonSearchRequestBuilderAssembler;
	}

	protected static ComplexQueryBuilderFactory
		createComplexQueryBuilderFactory(Queries queries) {

		ComplexQueryBuilderFactoryImpl complexQueryBuilderFactoryImpl =
			new ComplexQueryBuilderFactoryImpl();

		ReflectionTestUtil.setFieldValue(
			complexQueryBuilderFactoryImpl, "_queries", queries);

		return complexQueryBuilderFactoryImpl;
	}

	protected void setFacetProcessor(FacetProcessor<?> facetProcessor) {
		_facetProcessor = facetProcessor;
	}

	protected void setOpenSearchClientResolver(
		OpenSearchClientResolver openSearchClientResolver) {

		_openSearchClientResolver = openSearchClientResolver;
	}

	private static FacetTranslator _createFacetTranslator(
		FacetProcessor<?> facetProcessor,
		QueryTranslator<QueryBuilder> queryTranslator) {

		_facetTranslatorImpl = new FacetTranslatorImpl();

		ReflectionTestUtil.invoke(
			_facetTranslatorImpl, "activate",
			new Class<?>[] {BundleContext.class}, _bundleContext);

		if (facetProcessor != null) {
			ReflectionTestUtil.setFieldValue(
				_facetTranslatorImpl, "_defaultFacetProcessor",
				(FacetProcessor<SearchRequestBuilder>)facetProcessor);
		}
		else {
			_serviceRegistrations.add(
				_bundleContext.registerService(
					(Class<FacetProcessor<SearchRequestBuilder>>)
						(Class<?>)FacetProcessor.class,
					new RangeFacetProcessor(),
					MapUtil.singletonDictionary(
						"class.name", ModifiedFacetImpl.class.getName())));

			_serviceRegistrations.add(
				_bundleContext.registerService(
					(Class<FacetProcessor<SearchRequestBuilder>>)
						(Class<?>)FacetProcessor.class,
					new NestedFacetProcessor(),
					MapUtil.singletonDictionary(
						"class.name", NestedFacetImpl.class.getName())));
		}

		OpenSearchFilterTranslatorFixture openSearchFilterTranslatorFixture =
			new OpenSearchFilterTranslatorFixture(queryTranslator);

		ReflectionTestUtil.setFieldValue(
			_facetTranslatorImpl, "_filterTranslator",
			openSearchFilterTranslatorFixture.getOpenSearchFilterTranslator());

		return _facetTranslatorImpl;
	}

	private ClosePointInTimeRequestExecutor
		_createClosePointInTimeRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		ClosePointInTimeRequestExecutor closePointInTimeRequestExecutor =
			new ClosePointInTimeRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			closePointInTimeRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return closePointInTimeRequestExecutor;
	}

	private CountSearchRequestExecutor _createCountSearchRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		CommonSearchRequestBuilderAssembler commonSearchRequestBuilderAssembler,
		StatsTranslator statsTranslator) {

		CountSearchRequestExecutor countSearchRequestExecutor =
			new CountSearchRequestExecutorImpl();

		CommonSearchResponseAssembler commonSearchResponseAssembler =
			new CommonSearchResponseAssemblerImpl();

		ReflectionTestUtil.setFieldValue(
			commonSearchResponseAssembler, "_statsTranslator", statsTranslator);

		ReflectionTestUtil.setFieldValue(
			countSearchRequestExecutor, "_commonSearchResponseAssembler",
			commonSearchResponseAssembler);

		ReflectionTestUtil.setFieldValue(
			countSearchRequestExecutor, "_commonSearchSourceBuilderAssembler",
			commonSearchRequestBuilderAssembler);
		ReflectionTestUtil.setFieldValue(
			countSearchRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return countSearchRequestExecutor;
	}

	private MultisearchSearchRequestExecutor
		_createMultisearchSearchRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver,
			SearchSearchRequestAssembler searchSearchRequestAssembler,
			SearchSearchResponseAssembler searchSearchResponseAssembler) {

		MultisearchSearchRequestExecutor multisearchSearchRequestExecutor =
			new MultisearchSearchRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			multisearchSearchRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			multisearchSearchRequestExecutor, "_searchSearchRequestAssembler",
			searchSearchRequestAssembler);
		ReflectionTestUtil.setFieldValue(
			multisearchSearchRequestExecutor, "_searchSearchResponseAssembler",
			searchSearchResponseAssembler);

		return multisearchSearchRequestExecutor;
	}

	private OpenPointInTimeRequestExecutor
		_createOpenPointInTimeRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		OpenPointInTimeRequestExecutor openPointInTimeRequestExecutor =
			new OpenPointInTimeRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			openPointInTimeRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return openPointInTimeRequestExecutor;
	}

	private SearchRequestExecutor _createSearchRequestExecutor(
		ComplexQueryBuilderFactory complexQueryBuilderFactory,
		OpenSearchClientResolver openSearchClientResolver,
		OpenSearchQueryTranslator openSearchQueryTranslator,
		OpenSearchSortFieldTranslator openSearchSortFieldTranslator,
		FacetProcessor<?> facetProcessor,
		StatsRequestBuilderFactory statsRequestBuilderFactory,
		StatsTranslator statsTranslator) {

		CommonSearchRequestBuilderAssembler
			commonSearchRequestBuilderAssembler =
				createCommonSearchSourceBuilderAssembler(
					openSearchQueryTranslator, facetProcessor, statsTranslator,
					complexQueryBuilderFactory);

		SearchSearchRequestAssembler searchSearchRequestAssembler =
			_createSearchSearchRequestAssembler(
				openSearchQueryTranslator, openSearchSortFieldTranslator,
				commonSearchRequestBuilderAssembler, statsRequestBuilderFactory,
				statsTranslator);

		SearchSearchResponseAssembler searchSearchResponseAssembler =
			_createSearchSearchResponseAssembler(
				statsRequestBuilderFactory, statsTranslator);

		SearchRequestExecutor searchRequestExecutor =
			new OpenSearchSearchRequestExecutor();

		ReflectionTestUtil.setFieldValue(
			searchRequestExecutor, "_closePointInTimeRequestExecutor",
			_createClosePointInTimeRequestExecutor(openSearchClientResolver));
		ReflectionTestUtil.setFieldValue(
			searchRequestExecutor, "_countSearchRequestExecutor",
			_createCountSearchRequestExecutor(
				openSearchClientResolver, commonSearchRequestBuilderAssembler,
				statsTranslator));
		ReflectionTestUtil.setFieldValue(
			searchRequestExecutor, "_multisearchSearchRequestExecutor",
			_createMultisearchSearchRequestExecutor(
				openSearchClientResolver, searchSearchRequestAssembler,
				searchSearchResponseAssembler));
		ReflectionTestUtil.setFieldValue(
			searchRequestExecutor, "_openPointInTimeRequestExecutor",
			_createOpenPointInTimeRequestExecutor(openSearchClientResolver));
		ReflectionTestUtil.setFieldValue(
			searchRequestExecutor, "_searchSearchRequestExecutor",
			_createSearchSearchRequestExecutor(
				openSearchClientResolver, searchSearchRequestAssembler,
				searchSearchResponseAssembler));
		ReflectionTestUtil.setFieldValue(
			searchRequestExecutor, "_suggestSearchRequestExecutor",
			_createSuggestSearchRequestExecutor(openSearchClientResolver));

		return searchRequestExecutor;
	}

	private SearchSearchRequestAssembler _createSearchSearchRequestAssembler(
		OpenSearchQueryTranslator openSearchQueryTranslator,
		OpenSearchSortFieldTranslator openSearchSortFieldTranslator,
		CommonSearchRequestBuilderAssembler commonSearchRequestBuilderAssembler,
		StatsRequestBuilderFactory statsRequestBuilderFactory,
		StatsTranslator statsTranslator) {

		SearchSearchRequestAssembler searchSearchRequestAssembler =
			new SearchSearchRequestAssemblerImpl();

		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_commonSearchSourceBuilderAssembler",
			commonSearchRequestBuilderAssembler);
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_groupByRequestFactory",
			new GroupByRequestFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_groupByTranslator",
			new GroupByTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_highlighterTranslator",
			new HighlighterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_queryTranslator",
			openSearchQueryTranslator);
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_sortFieldTranslator",
			openSearchSortFieldTranslator);
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_sortTranslator",
			new SortTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_statsRequestBuilderFactory",
			statsRequestBuilderFactory);
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestAssembler, "_statsTranslator", statsTranslator);

		return searchSearchRequestAssembler;
	}

	private SearchSearchRequestExecutor _createSearchSearchRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver,
		SearchSearchRequestAssembler searchSearchRequestAssembler,
		SearchSearchResponseAssembler searchSearchResponseAssembler) {

		SearchSearchRequestExecutor searchSearchRequestExecutor =
			new SearchSearchRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			searchSearchRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestExecutor, "_searchSearchRequestAssembler",
			searchSearchRequestAssembler);
		ReflectionTestUtil.setFieldValue(
			searchSearchRequestExecutor, "_searchSearchResponseAssembler",
			searchSearchResponseAssembler);

		return searchSearchRequestExecutor;
	}

	private SearchSearchResponseAssembler _createSearchSearchResponseAssembler(
		StatsRequestBuilderFactory statsRequestBuilderFactory,
		StatsTranslator statsTranslator) {

		SearchSearchResponseAssembler searchSearchResponseAssembler =
			new SearchSearchResponseAssemblerImpl();

		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_aggregationResults",
			new AggregationResultsImpl());

		CommonSearchResponseAssembler commonSearchResponseAssembler =
			new CommonSearchResponseAssemblerImpl();

		ReflectionTestUtil.setFieldValue(
			commonSearchResponseAssembler, "_statsTranslator", statsTranslator);

		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_commonSearchResponseAssembler",
			commonSearchResponseAssembler);

		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_documentBuilderFactory",
			new DocumentBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_geoBuilders",
			new GeoBuildersImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_highlightFieldBuilderFactory",
			new HighlightFieldBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_searchHitBuilderFactory",
			new SearchHitBuilderFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_searchHitsBuilderFactory",
			new SearchHitsBuilderFactoryImpl());

		SearchResponseTranslator searchResponseTranslator =
			new SearchResponseTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			searchResponseTranslator, "_groupByResponseFactory",
			new GroupByResponseFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			searchResponseTranslator, "_searchHitDocumentTranslator",
			new HitDocumentTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			searchResponseTranslator, "_statsRequestBuilderFactory",
			statsRequestBuilderFactory);
		ReflectionTestUtil.setFieldValue(
			searchResponseTranslator, "_statsResultsTranslator",
			new StatsResultsTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			searchResponseTranslator, "_statsTranslator", statsTranslator);

		ReflectionTestUtil.setFieldValue(
			searchSearchResponseAssembler, "_searchResponseTranslator",
			searchResponseTranslator);

		return searchSearchResponseAssembler;
	}

	private SuggestSearchRequestExecutor _createSuggestSearchRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver) {

		SuggestSearchRequestExecutor suggestSearchRequestExecutor =
			new SuggestSearchRequestExecutorImpl();

		OpenSearchSuggesterTranslatorFixture
			openSearchSuggesterTranslatorFixture =
				new OpenSearchSuggesterTranslatorFixture();

		ReflectionTestUtil.setFieldValue(
			suggestSearchRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);
		ReflectionTestUtil.setFieldValue(
			suggestSearchRequestExecutor, "_suggesterTranslator",
			openSearchSuggesterTranslatorFixture.
				getOpenSearchSuggesterTranslator());

		return suggestSearchRequestExecutor;
	}

	private static final BundleContext _bundleContext =
		SystemBundleUtil.getBundleContext();
	private static FacetTranslatorImpl _facetTranslatorImpl;
	private static final List
		<ServiceRegistration<FacetProcessor<SearchRequestBuilder>>>
			_serviceRegistrations = new ArrayList<>();

	private FacetProcessor<?> _facetProcessor;
	private SearchRequestExecutor _searchRequestExecutor;

}