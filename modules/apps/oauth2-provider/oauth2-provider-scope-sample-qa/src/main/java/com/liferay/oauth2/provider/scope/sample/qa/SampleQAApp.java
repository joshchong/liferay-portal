/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.oauth2.provider.scope.sample.qa;

import com.liferay.oauth2.provider.scope.RequiresNoScope;
import com.liferay.oauth2.provider.scope.RequiresScope;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;

/**
 * @author Stian Sigvartsen
 */
@Component(
	property = {
		"oauth2.scopechecker.type=annotations",
		"osgi.jaxrs.application.base=/sample-qa-app",
		"osgi.jaxrs.extension.select=(liferay.extension=OAuth2)",
		"osgi.jaxrs.name=liferay-oauth2-scope-sample-qa-app"
	},
	service = Application.class
)
public class SampleQAApp extends Application {

	@GET
	@Path("/allScopesNeeded")
	@RequiresScope({"example-scope-1", "example-scope-2"})
	public String allScopesNeeded() {
		return "allScopesNeeded";
	}

	@GET
	@Path("/anyScopesNeeded")
	@RequiresScope(
		allNeeded = false, value = {"example-scope-1", "example-scope-2"}
	)
	public String anyScopesNeeded() {
		return "anyScopesNeeded";
	}

	@GET
	@Path("/noScopesNeeded")
	@RequiresNoScope
	public String noScopesNeeded() {
		return "noScopesNeeded";
	}

}