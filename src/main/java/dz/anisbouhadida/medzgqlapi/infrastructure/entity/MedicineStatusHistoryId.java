package dz.anisbouhadida.medzgqlapi.infrastructure.entity;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/// Composite primary key for [MedicineStatusHistoryEntity].
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineStatusHistoryId implements Serializable {

  private Long medicineId;
  private MedicineStatus status;

    @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MedicineStatusHistoryId that)) return false;
    return Objects.equals(medicineId, that.medicineId) && status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(medicineId, status);
  }
}

