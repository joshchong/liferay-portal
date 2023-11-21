/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.filter;

import com.liferay.portal.kernel.search.query.QueryTranslator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;

import org.opensearch.index.query.QueryBuilder;

/**
 * @author Michael C. Han
 */
public class OpenSearchFilterTranslatorFixture {

	public OpenSearchFilterTranslatorFixture(
		QueryTranslator<QueryBuilder> queryTranslator) {

		_openSearchFilterTranslator = new OpenSearchFilterTranslator() {
			{
				booleanFilterTranslator = new BooleanFilterTranslatorImpl();
				dateRangeFilterTranslator = new DateRangeFilterTranslatorImpl();
				dateRangeTermFilterTranslator =
					new DateRangeTermFilterTranslatorImpl();
				existsFilterTranslator = new ExistsFilterTranslatorImpl();
				geoBoundingBoxFilterTranslator =
					new GeoBoundingBoxFilterTranslatorImpl();
				geoDistanceFilterTranslator =
					new GeoDistanceFilterTranslatorImpl();
				geoDistanceRangeFilterTranslator =
					new GeoDistanceRangeFilterTranslatorImpl();
				missingFilterTranslator = new MissingFilterTranslatorImpl();
				prefixFilterTranslator = new PrefixFilterTranslatorImpl();

				queryFilterTranslator = new QueryFilterTranslatorImpl();

				ReflectionTestUtil.setFieldValue(
					queryFilterTranslator, "_queryTranslator", queryTranslator);

				rangeTermFilterTranslator = new RangeTermFilterTranslatorImpl();
				termFilterTranslator = new TermFilterTranslatorImpl();
				termsFilterTranslator = new TermsFilterTranslatorImpl();
				termsSetFilterTranslator = new TermsSetFilterTranslatorImpl();
			}
		};
	}

	public OpenSearchFilterTranslator getOpenSearchFilterTranslator() {
		return _openSearchFilterTranslator;
	}

	private final OpenSearchFilterTranslator _openSearchFilterTranslator;

}