package dz.anisbouhadida.medzgqlapi.infrastructure.mapper;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// MapStruct mapper that converts JPA entities into immutable domain records.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Mapper(componentModel = "spring")
public interface MedicineMapper {

  /// Maps a [MedicineEntity] to a [Medicine] domain record.
  @Mapping(source = "medicineId", target = "id")
  @Mapping(source = "icd", target = "internationalCommonDenomination")
  Medicine toDomain(MedicineEntity entity);

  /// Maps a [NomenclatureEventEntity] to a [NomenclatureEvent].
  default NomenclatureEvent toDomain(NomenclatureEventEntity entity) {
    if (entity == null) return null;
    return new NomenclatureEvent(
        entity.getMedicineId(),
        entity.getFinalRegistrationDate(),
        entity.getStabilityDuration(),
        entity.getObservations());
  }

  /// Maps a [WithdrawalEventEntity] to a [WithdrawalEvent].
  default WithdrawalEvent toDomain(WithdrawalEventEntity entity) {
    if (entity == null) return null;
    return new WithdrawalEvent(
        entity.getMedicineId(),
        entity.getWithdrawalDate(),
        entity.getWithdrawalReason());
  }

  /// Maps a [NonRenewalEventEntity] to a [NonRenewalEvent].
  default NonRenewalEvent toDomain(NonRenewalEventEntity entity) {
    if (entity == null) return null;
    return new NonRenewalEvent(
        entity.getMedicineId(),
        entity.getFinalRegistrationDate(),
        entity.getObservations());
  }
}

