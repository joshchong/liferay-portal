/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.search.engine.adapter.snapshot;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.search.engine.adapter.snapshot.SnapshotRequestExecutor;

/**
 * @author Michael C. Han
 */
public class SnapshotRequestExecutorFixture {

	public SnapshotRequestExecutor getSnapshotRequestExecutor() {
		return _snapshotRequestExecutor;
	}

	public void setUp() {
		_snapshotRequestExecutor = new OpenSearchSnapshotRequestExecutor() {
			{
				createSnapshotRepositoryRequestExecutor =
					_createCreateSnapshotRepositoryRequestExecutor(
						_openSearchClientResolver);
				createSnapshotRequestExecutor =
					_createCreateSnapshotRequestExecutor(
						_openSearchClientResolver);
				deleteSnapshotRequestExecutor =
					_createDeleteSnapshotRequestExecutor(
						_openSearchClientResolver);
				getSnapshotRepositoriesRequestExecutor =
					_createGetSnapshotRepositoriesRequestExecutor(
						_openSearchClientResolver);
				getSnapshotsRequestExecutor =
					_createGetSnapshotsRequestExecutor(
						_openSearchClientResolver);
				restoreSnapshotRequestExecutor =
					_createRestoreSnapshotRequestExecutor(
						_openSearchClientResolver);
			}
		};
	}

	protected void setOpenSearchClientResolver(
		OpenSearchClientResolver openSearchClientResolver) {

		_openSearchClientResolver = openSearchClientResolver;
	}

	private CreateSnapshotRepositoryRequestExecutor
		_createCreateSnapshotRepositoryRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		CreateSnapshotRepositoryRequestExecutor
			createSnapshotRepositoryRequestExecutor =
				new CreateSnapshotRepositoryRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			createSnapshotRepositoryRequestExecutor,
			"_openSearchClientResolver", openSearchClientResolver);

		return createSnapshotRepositoryRequestExecutor;
	}

	private CreateSnapshotRequestExecutor _createCreateSnapshotRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver) {

		CreateSnapshotRequestExecutor createSnapshotRequestExecutor =
			new CreateSnapshotRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			createSnapshotRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return createSnapshotRequestExecutor;
	}

	private DeleteSnapshotRequestExecutor _createDeleteSnapshotRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver) {

		DeleteSnapshotRequestExecutor deleteSnapshotRequestExecutor =
			new DeleteSnapshotRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			deleteSnapshotRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return deleteSnapshotRequestExecutor;
	}

	private GetSnapshotRepositoriesRequestExecutor
		_createGetSnapshotRepositoriesRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		GetSnapshotRepositoriesRequestExecutor
			getSnapshotRepositoriesRequestExecutor =
				new GetSnapshotRepositoriesRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			getSnapshotRepositoriesRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return getSnapshotRepositoriesRequestExecutor;
	}

	private GetSnapshotsRequestExecutor _createGetSnapshotsRequestExecutor(
		OpenSearchClientResolver openSearchClientResolver) {

		GetSnapshotsRequestExecutor getSnapshotsRequestExecutor =
			new GetSnapshotsRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			getSnapshotsRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return getSnapshotsRequestExecutor;
	}

	private RestoreSnapshotRequestExecutor
		_createRestoreSnapshotRequestExecutor(
			OpenSearchClientResolver openSearchClientResolver) {

		RestoreSnapshotRequestExecutor restoreSnapshotRequestExecutor =
			new RestoreSnapshotRequestExecutorImpl();

		ReflectionTestUtil.setFieldValue(
			restoreSnapshotRequestExecutor, "_openSearchClientResolver",
			openSearchClientResolver);

		return restoreSnapshotRequestExecutor;
	}

	private SnapshotRequestExecutor _snapshotRequestExecutor;

}