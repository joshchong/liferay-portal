/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.legacy.query;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.search.filter.FilterTranslator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.opensearch2.internal.filter.OpenSearchFilterTranslatorFixture;

import org.opensearch.index.query.QueryBuilder;

/**
 * @author Michael C. Han
 */
public class OpenSearchQueryTranslatorFixture {

	public OpenSearchQueryTranslatorFixture() {
		_openSearchQueryTranslator = new OpenSearchQueryTranslator() {
			{
				OpenSearchFilterTranslatorFixture
					openSearchFilterTranslatorFixture =
						new OpenSearchFilterTranslatorFixture(this);

				booleanQueryTranslator = new BooleanQueryTranslatorImpl();

				ReflectionTestUtil.setFieldValue(
					booleanQueryTranslator, "_filterTranslatorSnapshot",
					new Snapshot<FilterTranslator<QueryBuilder>>(null, null) {

						public FilterTranslator<QueryBuilder> get() {
							return openSearchFilterTranslatorFixture.
								getOpenSearchFilterTranslator();
						}

					});

				disMaxQueryTranslator = new DisMaxQueryTranslatorImpl();
				fuzzyQueryTranslator = new FuzzyQueryTranslatorImpl();
				matchAllQueryTranslator = new MatchAllQueryTranslatorImpl();
				matchQueryTranslator = new MatchQueryTranslatorImpl();
				moreLikeThisQueryTranslator =
					new MoreLikeThisQueryTranslatorImpl();
				multiMatchQueryTranslator = new MultiMatchQueryTranslatorImpl();
				nestedQueryTranslator = new NestedQueryTranslatorImpl();
				stringQueryTranslator = new StringQueryTranslatorImpl();
				termQueryTranslator = new TermQueryTranslatorImpl();
				termRangeQueryTranslator = new TermRangeQueryTranslatorImpl();
				wildcardQueryTranslator = new WildcardQueryTranslatorImpl();
			}
		};
	}

	public OpenSearchQueryTranslator getOpenSearchQueryTranslator() {
		return _openSearchQueryTranslator;
	}

	private final OpenSearchQueryTranslator _openSearchQueryTranslator;

}