package dz.anisbouhadida.medzgqlapi.infrastructure.mapper;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(InstancioExtension.class)
@DisplayName("MedicineMapper")
class MedicineMapperTest {

  private final MedicineMapper mapper = Mappers.getMapper(MedicineMapper.class);

  @Test
  @DisplayName("toDomain should map medicine entities to immutable domain records")
  void toDomain_shouldMapMedicineEntityToDomainRecord() {
    MedicineEntity entity = Instancio.create(MedicineEntity.class);

    Medicine actual = mapper.toDomain(entity);

    assertAll(
        () -> assertEquals(entity.getMedicineId(), actual.id()),
        () -> assertEquals(entity.getRegistrationNumber(), actual.registrationNumber()),
        () -> assertEquals(entity.getCode(), actual.code()),
        () -> assertEquals(entity.getIcd(), actual.internationalCommonDenomination()),
        () -> assertEquals(entity.getBrandName(), actual.brandName()),
        () -> assertEquals(entity.getForm(), actual.form()),
        () -> assertEquals(entity.getDosage(), actual.dosage()),
        () -> assertEquals(entity.getPackaging(), actual.packaging()),
        () -> assertEquals(entity.getList(), actual.list()),
        () -> assertEquals(entity.getP1(), actual.p1()),
        () -> assertEquals(entity.getP2(), actual.p2()),
        () -> assertEquals(entity.getLaboratoryHolder(), actual.laboratoryHolder()),
        () -> assertEquals(entity.getLaboratoryCountry(), actual.laboratoryCountry()),
        () -> assertEquals(entity.getInitialRegistrationDate(), actual.initialRegistrationDate()),
        () -> assertEquals(entity.getType(), actual.type()),
        () -> assertEquals(entity.getOrigin(), actual.origin()));
  }

  @Test
  @DisplayName("toDomain should return null for a null nomenclature event entity")
  void toDomain_shouldReturnNull_whenNomenclatureEventEntityIsNull() {
    assertNull(mapper.toDomain((NomenclatureEventEntity) null));
  }

  @Test
  @DisplayName("toDomain should map nomenclature event entities")
  void toDomain_shouldMapNomenclatureEventEntity() {
    NomenclatureEventEntity entity = Instancio.create(NomenclatureEventEntity.class);

    NomenclatureEvent actual = mapper.toDomain(entity);

    assertAll(
        () -> assertEquals(entity.getMedicineId(), actual.medicineId()),
        () -> assertEquals(entity.getFinalRegistrationDate(), actual.finalRegistrationDate()),
        () -> assertEquals(entity.getStabilityDuration(), actual.stabilityDuration()),
        () -> assertEquals(entity.getObservations(), actual.observations()));
  }

  @Test
  @DisplayName("toDomain should map withdrawal event entities")
  void toDomain_shouldMapWithdrawalEventEntity() {
    WithdrawalEventEntity entity = Instancio.create(WithdrawalEventEntity.class);

    WithdrawalEvent actual = mapper.toDomain(entity);

    assertAll(
        () -> assertEquals(entity.getMedicineId(), actual.medicineId()),
        () -> assertEquals(entity.getWithdrawalDate(), actual.withdrawalDate()),
        () -> assertEquals(entity.getWithdrawalReason(), actual.withdrawalReason()));
  }

  @Test
  @DisplayName("toDomain should map non-renewal event entities")
  void toDomain_shouldMapNonRenewalEventEntity() {
    NonRenewalEventEntity entity = Instancio.create(NonRenewalEventEntity.class);

    NonRenewalEvent actual = mapper.toDomain(entity);

    assertAll(
        () -> assertEquals(entity.getMedicineId(), actual.medicineId()),
        () -> assertEquals(entity.getFinalRegistrationDate(), actual.finalRegistrationDate()),
        () -> assertEquals(entity.getObservations(), actual.observations()));
  }
}
