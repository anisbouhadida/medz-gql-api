package dz.anisbouhadida.medzgqlapi.infrastructure.entity;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "medicine_event_history")
@IdClass(MedicineEventHistoryId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineEventHistoryEntity {

  @Id
  @Column(name = "medicine_id")
  private Long medicineId;

  @Id
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "event_type")
  private MedicineEventType eventType;

  @Column(name = "event_date", nullable = false)
  private OffsetDateTime eventDate;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
  private MedicineEntity medicine;

  @PrePersist
  void onPrePersist() {
    var now = OffsetDateTime.now();
    if (eventDate == null) {
      eventDate = now;
    }
    updatedAt = now;
  }

  @PreUpdate
  void onPreUpdate() {
    updatedAt = OffsetDateTime.now();
  }

}

