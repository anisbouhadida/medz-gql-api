package dz.anisbouhadida.medzgqlapi.infrastructure.entity;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "medicine")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "medicine_id")
  private Long medicineId;

  @Column(name = "registration_number", nullable = false)
  private String registrationNumber;

  @Column(name = "code")
  private String code;

  @Column(name = "icd")
  private String icd;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "form")
  private String form;

  @Column(name = "dosage")
  private String dosage;

  @Column(name = "packaging")
  private String packaging;

  @Column(name = "list")
  private String list;

  @Column(name = "p1")
  private String p1;

  @Column(name = "p2")
  private String p2;

  @Column(name = "laboratory_holder")
  private String laboratoryHolder;

  @Column(name = "laboratory_country")
  private String laboratoryCountry;

  @Column(name = "initial_registration_date")
  private OffsetDateTime initialRegistrationDate;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type")
  private MedicineType type;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "origin")
  private MedicineOrigin origin;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;

  @Column(name = "last_updated", nullable = false)
  private OffsetDateTime lastUpdated;

  @PrePersist
  void onPrePersist() {
    lastUpdated = OffsetDateTime.now();
  }

  @PreUpdate
  void onPreUpdate() {
    lastUpdated = OffsetDateTime.now();
  }
}

