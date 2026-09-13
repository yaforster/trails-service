package io.github.yaforster.trails.core.test;

import java.sql.Timestamp;
import java.util.List;

public record TestCaseResult(Timestamp timestamp, Browser testedInBrowser, List<TestPathResult> results) {

}
