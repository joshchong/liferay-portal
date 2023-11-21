/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.opensearch2.internal.legacy.hits.HitDocumentTranslator;
import com.liferay.portal.search.opensearch2.internal.legacy.hits.HitDocumentTranslatorImpl;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.opensearch.common.document.DocumentField;
import org.opensearch.search.SearchHit;

/**
 * @author Joshua Cords
 */
public class SearchHitDocumentTranslatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDocumentWithIgnoredField() {
		SearchHit searchHit = SearchHit.createFromMap(
			HashMapBuilder.<String, Object>putPersistent(
				"document_fields",
				() -> HashMapBuilder.putPersistent(
					"_ignored",
					() -> new DocumentField(
						"_ignored", Arrays.asList("value"),
						Arrays.asList("fieldName"))
				).build()
			).build());

		HitDocumentTranslator hitDocumentTranslator =
			new HitDocumentTranslatorImpl();

		hitDocumentTranslator.translateNestedSort(searchHit);
	}

}