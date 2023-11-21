/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.suggest;

import com.liferay.portal.kernel.test.ReflectionTestUtil;

/**
 * @author Michael C. Han
 */
public class OpenSearchSuggesterTranslatorFixture {

	public OpenSearchSuggesterTranslatorFixture() {
		ReflectionTestUtil.setFieldValue(
			_openSearchSuggesterTranslator, "_completionSuggesterTranslator",
			new CompletionSuggesterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchSuggesterTranslator, "_phraseSuggesterTranslator",
			new PhraseSuggesterTranslatorImpl());
		ReflectionTestUtil.setFieldValue(
			_openSearchSuggesterTranslator, "_termSuggesterTranslator",
			new TermSuggesterTranslatorImpl());
	}

	public OpenSearchSuggesterTranslator getOpenSearchSuggesterTranslator() {
		return _openSearchSuggesterTranslator;
	}

	private final OpenSearchSuggesterTranslator _openSearchSuggesterTranslator =
		new OpenSearchSuggesterTranslator();

}