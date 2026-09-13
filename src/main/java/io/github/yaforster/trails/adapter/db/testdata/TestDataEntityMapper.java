package io.github.yaforster.trails.adapter.db.testdata;

import io.github.yaforster.trails.core.data.TestDataEntry;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TestDataEntityMapper {

	public TestDataSetEntity toEntity(TestDataSetDefinition definition) {
		TestDataSetEntity entity = new TestDataSetEntity();
		updateEntity(entity, definition);
		return entity;
	}

	public void updateEntity(TestDataSetEntity entity, TestDataSetDefinition definition) {
		entity.setLabel(definition.label());
		List<TestDataEntry> values = definition.values() == null ? List.of() : definition.values();
		Map<String, TestDataEntryEntity> existingValuesByKey = entity.getValues()
			.stream()
			.collect(Collectors.toMap(TestDataEntryEntity::getKey, Function.identity()));
		Set<String> requestedKeys = values.stream().map(TestDataEntry::key).collect(Collectors.toSet());

		entity.getValues().removeIf(value -> !requestedKeys.contains(value.getKey()));
		values.forEach(value -> updateOrAddEntry(entity, existingValuesByKey, value));
	}

	public TestDataSet toDomain(TestDataSetEntity entity) {
		return new TestDataSet(entity.getId(), entity.getLabel(), entity.getCreationDate(),
				entity.getValues().stream().map(value -> new TestDataEntry(value.getKey(), value.getValue())).toList(),
				entity.isRetired());
	}

	private TestDataEntryEntity toEntryEntity(TestDataSetEntity testDataSet, TestDataEntry value) {
		return new TestDataEntryEntity().setTestDataSet(testDataSet).setKey(value.key()).setValue(value.value());
	}

	private void updateOrAddEntry(TestDataSetEntity testDataSet, Map<String, TestDataEntryEntity> existingValuesByKey,
			TestDataEntry value) {
		TestDataEntryEntity existing = existingValuesByKey.get(value.key());
		if (existing != null) {
			existing.setValue(value.value());
			return;
		}
		testDataSet.getValues().add(toEntryEntity(testDataSet, value));
	}

}
