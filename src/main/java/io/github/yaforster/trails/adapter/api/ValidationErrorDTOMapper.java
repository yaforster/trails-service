package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.ValidationViolation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface ValidationErrorDTOMapper {

	ValidationErrorDTO toDTO(ValidationViolation validationViolation);

	List<ValidationErrorDTO> toDTOs(List<ValidationViolation> validationViolations);

}
