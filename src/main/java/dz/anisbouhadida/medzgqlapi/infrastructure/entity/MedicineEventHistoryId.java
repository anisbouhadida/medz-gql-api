package dz.anisbouhadida.medzgqlapi.infrastructure.entity;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/// Composite primary key for [MedicineEventHistoryEntity].
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineEventHistoryId implements Serializable {

  private Long medicineId;
  private MedicineEventType eventType;

    @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MedicineEventHistoryId that)) return false;
    return Objects.equals(medicineId, that.medicineId) && eventType == that.eventType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(medicineId, eventType);
  }
}

