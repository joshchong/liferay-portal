/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.query;

import com.liferay.portal.kernel.test.ReflectionTestUtil;

/**
 * @author Michael C. Han
 */
public class OpenSearchQueryTranslatorFixture {

	public OpenSearchQueryTranslatorFixture() {
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_booleanQueryTranslator",
			new BooleanQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_boostingQueryTranslator",
			new BoostingQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_commonTermsQueryTranslator",
			new CommonTermsQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_constantScoreQueryTranslator",
			new ConstantScoreQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_dateRangeTermQueryTranslator",
			new DateRangeTermQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_disMaxQueryTranslator",
			new DisMaxQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_existsQueryTranslator",
			new ExistsQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_functionScoreQueryTranslator",
			new FunctionScoreQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_fuzzyQueryTranslator",
			new FuzzyQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_geoBoundingBoxQueryTranslator",
			new GeoBoundingBoxQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_geoDistanceQueryTranslator",
			new GeoDistanceQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_geoDistanceRangeQueryTranslator",
			new GeoDistanceRangeQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_geoPolygonQueryTranslator",
			new GeoPolygonQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_geoShapeQueryTranslator",
			new GeoShapeQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_idsQueryTranslator",
			new IdsQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_matchAllQueryTranslator",
			new MatchAllQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_matchPhrasePrefixQueryTranslator",
			new MatchPhrasePrefixQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_matchPhraseQueryTranslator",
			new MatchPhraseQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_matchQueryTranslator",
			new MatchQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_moreLikeThisQueryTranslator",
			new MoreLikeThisQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_multiMatchQueryTranslator",
			new MultiMatchQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_nestedQueryTranslator",
			new NestedQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_percolateQueryTranslator",
			new PercolateQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_prefixQueryTranslator",
			new PrefixQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_rangeTermQueryTranslator",
			new RangeTermQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_regexQueryTranslator",
			new RegexQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_scriptQueryTranslator",
			new ScriptQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_simpleQueryStringQueryTranslator",
			new SimpleStringQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_stringQueryTranslator",
			new StringQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_termQueryTranslator",
			new TermQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_termsQueryTranslator",
			new TermsQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_termsSetQueryTranslator",
			new TermsSetQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_wildcardQueryTranslator",
			new WildcardQueryTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchQueryTranslator, "_wrapperQueryTranslator",
			new WrapperQueryTranslatorImpl());
	}

	public OpenSearchQueryTranslator getOpenSearchQueryTranslator() {
		return _openSearchQueryTranslator;
	}

	private final OpenSearchQueryTranslator _openSearchQueryTranslator =
		new OpenSearchQueryTranslator();

}