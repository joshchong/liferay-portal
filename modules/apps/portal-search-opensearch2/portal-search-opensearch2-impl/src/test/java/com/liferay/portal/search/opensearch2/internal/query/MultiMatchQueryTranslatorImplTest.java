/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.query;

import com.liferay.portal.search.internal.query.MultiMatchQueryImpl;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Bryan Engler
 */
public class MultiMatchQueryTranslatorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testTranslate() {
		MultiMatchQueryTranslatorImpl multiMatchQueryTranslatorImpl =
			new MultiMatchQueryTranslatorImpl();

		Map<String, Float> fieldsBoosts = new HashMap<>();

		fieldsBoosts.put("test", null);

		MultiMatchQuery multiMatchQuery = new MultiMatchQueryImpl(
			"test", fieldsBoosts);

		multiMatchQueryTranslatorImpl.translateNestedSort(multiMatchQuery);
	}

}