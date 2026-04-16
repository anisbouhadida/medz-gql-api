package dz.anisbouhadida.medzgqlapi.infrastructure.entity;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Setter
@Getter
@Entity
@Table(name = "medicine_status_history")
@IdClass(MedicineStatusHistoryId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineStatusHistoryEntity {

  @Id
  @Column(name = "medicine_id")
  private Long medicineId;

  @Id
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status")
  private MedicineStatus status;

  @Column(name = "status_timestamp", nullable = false)
  private OffsetDateTime statusTimestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
  private MedicineEntity medicine;

}

