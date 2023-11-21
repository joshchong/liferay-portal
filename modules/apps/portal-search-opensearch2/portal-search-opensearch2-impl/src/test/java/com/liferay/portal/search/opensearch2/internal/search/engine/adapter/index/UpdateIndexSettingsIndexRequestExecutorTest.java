/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.index;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.engine.adapter.index.UpdateIndexSettingsIndexRequest;
import com.liferay.portal.search.opensearch2.internal.connection.OpenSearchFixture;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.opensearch.action.admin.indices.settings.put.UpdateSettingsRequest;

/**
 * @author Michael C. Han
 */
public class UpdateIndexSettingsIndexRequestExecutorTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_openSearchFixture = new OpenSearchFixture(
			UpdateIndexSettingsIndexRequestExecutorTest.class.getSimpleName());

		_openSearchFixture.setUp();

		_indicesOptionsTranslator = new IndicesOptionsTranslatorImpl();
	}

	@After
	public void tearDown() throws Exception {
		_openSearchFixture.tearDown();
	}

	@Test
	public void testIndexRequestTranslation() {
		UpdateIndexSettingsIndexRequest updateIndexSettingsIndexRequest =
			new UpdateIndexSettingsIndexRequest(_INDEX_NAME);

		updateIndexSettingsIndexRequest.setSettings(
			StringBundler.concat(
				"{\n", "    \"analysis\": {\n", "        \"analyzer\": {\n",
				"            \"content\": {\n",
				"                \"tokenizer\": \"whitespace\",\n",
				"                \"type\": \"custom\"\n", "            }\n",
				"        }\n", "    }\n", "}"));

		UpdateIndexSettingsIndexRequestExecutorImpl
			updateIndexSettingsIndexRequestExecutorImpl =
				new UpdateIndexSettingsIndexRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			updateIndexSettingsIndexRequestExecutorImpl,
			"_openSearchClientResolver", _openSearchFixture);
		ReflectionTestUtil.setFieldValue(
			updateIndexSettingsIndexRequestExecutorImpl,
			"_indicesOptionsTranslator", _indicesOptionsTranslator);

		UpdateSettingsRequest updateSettingsRequest =
			updateIndexSettingsIndexRequestExecutorImpl.
				createUpdateSettingsRequest(updateIndexSettingsIndexRequest);

		String[] indices = updateSettingsRequest.indices();

		Assert.assertEquals(Arrays.toString(indices), 1, indices.length);
		Assert.assertEquals(_INDEX_NAME, indices[0]);
	}

	private static final String _INDEX_NAME = "test_request_index";

	private IndicesOptionsTranslator _indicesOptionsTranslator;
	private OpenSearchFixture _openSearchFixture;

}