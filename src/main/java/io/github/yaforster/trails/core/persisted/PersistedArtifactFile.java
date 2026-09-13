package io.github.yaforster.trails.core.persisted;

public record PersistedArtifactFile(Long id, String fileName, String contentType, byte[] content) {

}
