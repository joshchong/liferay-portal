/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.aggregation;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.aggregation.pipeline.PipelineAggregationTranslator;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.DateHistogramAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.DateRangeAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.FilterAggregationTranslator;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.FilterAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.FiltersAggregationTranslator;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.FiltersAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.GeoDistanceAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.HistogramAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.RangeAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.SignificantTermsAggregationTranslator;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.SignificantTermsAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.SignificantTextAggregationTranslator;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.SignificantTextAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.bucket.TermsAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.metrics.ScriptedMetricAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.metrics.TopHitsAggregationTranslator;
import com.liferay.portal.search.opensearch2.internal.aggregation.metrics.TopHitsAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.metrics.WeightedAverageAggregationTranslatorImpl;
import com.liferay.portal.search.opensearch2.internal.aggregation.pipeline.OpenSearchPipelineAggregationTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.query.OpenSearchQueryTranslatorFixture;
import com.liferay.portal.search.opensearch2.internal.sort.OpenSearchSortFieldTranslatorFixture;

import org.opensearch.search.aggregations.PipelineAggregationBuilder;

/**
 * @author Michael C. Han
 */
public class OpenSearchAggregationTranslatorFixture {

	public OpenSearchAggregationTranslatorFixture() {
		OpenSearchPipelineAggregationTranslatorFixture
			pipelineAggregationTranslatorFixture =
				new OpenSearchPipelineAggregationTranslatorFixture();

		OpenSearchQueryTranslatorFixture openSearchQueryTranslatorFixture =
			new OpenSearchQueryTranslatorFixture();

		PipelineAggregationTranslator<PipelineAggregationBuilder>
			pipelineAggregationTranslator =
				pipelineAggregationTranslatorFixture.
					getOpenSearchPipelineAggregationTranslator();

		AggregationBuilderAssemblerFactory aggregationBuilderAssemblerFactory =
			new AggregationBuilderAssemblerFactoryImpl();

		ReflectionTestUtil.setFieldValue(
			aggregationBuilderAssemblerFactory,
			"_pipelineAggregationTranslator", pipelineAggregationTranslator);

		OpenSearchAggregationTranslator openSearchAggregationTranslator =
			new OpenSearchAggregationTranslator();

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_aggregationBuilderAssemblerFactory",
			aggregationBuilderAssemblerFactory);
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_dateHistogramAggregationTranslator",
			new DateHistogramAggregationTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_dateRangeAggregationTranslator",
			new DateRangeAggregationTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_histogramAggregationTranslator",
			new HistogramAggregationTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_pipelineAggregationTranslator",
			pipelineAggregationTranslator);
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_rangeAggregationTranslator",
			new RangeAggregationTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_termsAggregationTranslator",
			new TermsAggregationTranslatorImpl());

		_injectGeoAggregationTranslators(openSearchAggregationTranslator);
		_injectQueryAggregationTranslators(
			openSearchAggregationTranslator, openSearchQueryTranslatorFixture);
		_injectScriptAggregationTranslators(openSearchAggregationTranslator);
		_injectTopHitsAggregationTranslators(
			openSearchAggregationTranslator, openSearchQueryTranslatorFixture);

		_openSearchAggregationTranslator = openSearchAggregationTranslator;
	}

	public OpenSearchAggregationTranslator
		getOpenSearchAggregationTranslator() {

		return _openSearchAggregationTranslator;
	}

	private void _injectGeoAggregationTranslators(
		OpenSearchAggregationTranslator openSearchAggregationTranslator) {

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_geoDistanceAggregationTranslator",
			new GeoDistanceAggregationTranslatorImpl());
	}

	private void _injectQueryAggregationTranslators(
		OpenSearchAggregationTranslator openSearchAggregationTranslator,
		OpenSearchQueryTranslatorFixture openSearchQueryTranslatorFixture) {

		FilterAggregationTranslator filterAggregationTranslator =
			new FilterAggregationTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			filterAggregationTranslator, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_filterAggregationTranslator",
			filterAggregationTranslator);

		FiltersAggregationTranslator filtersAggregationTranslator =
			new FiltersAggregationTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			filtersAggregationTranslator, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_filtersAggregationTranslator",
			filtersAggregationTranslator);

		SignificantTermsAggregationTranslator
			significantTermsAggregationTranslator =
				new SignificantTermsAggregationTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			significantTermsAggregationTranslator, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_significantTermsAggregationTranslator",
			significantTermsAggregationTranslator);

		SignificantTextAggregationTranslator
			significantTextAggregationTranslator =
				new SignificantTextAggregationTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			significantTextAggregationTranslator, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_significantTextAggregationTranslator",
			significantTextAggregationTranslator);
	}

	private void _injectScriptAggregationTranslators(
		OpenSearchAggregationTranslator openSearchAggregationTranslator) {

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_scriptedMetricAggregationTranslator",
			new ScriptedMetricAggregationTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator,
			"_weightedAvgAggregationTranslator",
			new WeightedAverageAggregationTranslatorImpl());
	}

	private void _injectTopHitsAggregationTranslators(
		OpenSearchAggregationTranslator openSearchAggregationTranslator,
		OpenSearchQueryTranslatorFixture openSearchQueryTranslatorFixture) {

		OpenSearchSortFieldTranslatorFixture
			openSearchSortFieldTranslatorFixture =
				new OpenSearchSortFieldTranslatorFixture(
					openSearchQueryTranslatorFixture.
						getOpenSearchQueryTranslator());

		TopHitsAggregationTranslator topHitsAggregationTranslator =
			new TopHitsAggregationTranslatorImpl();

		ReflectionTestUtil.setFieldValue(
			topHitsAggregationTranslator, "_queryTranslator",
			openSearchQueryTranslatorFixture.getOpenSearchQueryTranslator());
		ReflectionTestUtil.setFieldValue(
			topHitsAggregationTranslator, "_sortFieldTranslator",
			openSearchSortFieldTranslatorFixture.
				getOpenSearchSortFieldTranslator());

		ReflectionTestUtil.setFieldValue(
			openSearchAggregationTranslator, "_topHitsAggregationTranslator",
			topHitsAggregationTranslator);
	}

	private final OpenSearchAggregationTranslator
		_openSearchAggregationTranslator;

}