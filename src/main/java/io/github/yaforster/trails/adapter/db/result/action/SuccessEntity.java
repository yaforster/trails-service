package io.github.yaforster.trails.adapter.db.result.action;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@DiscriminatorValue("2")
public class SuccessEntity extends ResultEntity {

}
