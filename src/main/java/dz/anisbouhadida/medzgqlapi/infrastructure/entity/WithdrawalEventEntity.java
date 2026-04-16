package dz.anisbouhadida.medzgqlapi.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "withdrawal_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalEventEntity {

  @Id
  @Column(name = "medicine_id")
  private Long medicineId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "medicine_id")
  private MedicineEntity medicine;

  @Column(name = "withdrawal_date")
  private OffsetDateTime withdrawalDate;

  @Column(name = "withdrawal_reason")
  private String withdrawalReason;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void onPrePersist() {
    updatedAt = OffsetDateTime.now();
  }

  @PreUpdate
  void onPreUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}

