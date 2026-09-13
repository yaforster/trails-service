package io.github.yaforster.trails.adapter.api.rest.testset.model;

import io.github.yaforster.trails.core.test.Browser;

import java.time.OffsetDateTime;

public record TestCaseResultModel(OffsetDateTime timestamp, Browser testedInBrowser) {

}
