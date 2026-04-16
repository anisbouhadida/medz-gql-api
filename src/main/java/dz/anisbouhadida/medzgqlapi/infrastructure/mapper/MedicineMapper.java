package dz.anisbouhadida.medzgqlapi.infrastructure.mapper;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// MapStruct mapper that converts JPA entities into immutable domain records.
///
/// Event-mapping methods accept a preloaded [Medicine] to avoid
/// redundant lazy-loading of the [MedicineEntity] association.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Mapper(componentModel = "spring")
public interface MedicineMapper {

  /// Maps a [MedicineEntity] to a [Medicine] domain record.
  @Mapping(source = "icd", target = "internationalCommonDenomination")
  Medicine toDomain(MedicineEntity entity);

  /// Converts [OffsetDateTime] to [LocalDateTime].
  default LocalDateTime map(OffsetDateTime value) {
    return value == null ? null : value.toLocalDateTime();
  }

  /// Maps a [NomenclatureEventEntity] to a [NomenclatureEvent],
  /// using the supplied pre-mapped [Medicine].
  default NomenclatureEvent toDomain(NomenclatureEventEntity entity, Medicine medicine) {
    if (entity == null) return null;
    return new NomenclatureEvent(
        medicine,
        map(entity.getFinalRegistrationDate()),
        entity.getStabilityDuration(),
        entity.getObservations());
  }

  /// Maps a [WithdrawalEventEntity] to a [WithdrawalEvent],
  /// using the supplied pre-mapped [Medicine].
  default WithdrawalEvent toDomain(WithdrawalEventEntity entity, Medicine medicine) {
    if (entity == null) return null;
    return new WithdrawalEvent(
        medicine,
        map(entity.getWithdrawalDate()),
        entity.getWithdrawalReason());
  }

  /// Maps a [NonRenewalEventEntity] to a [NonRenewalEvent],
  /// using the supplied pre-mapped [Medicine].
  default NonRenewalEvent toDomain(NonRenewalEventEntity entity, Medicine medicine) {
    if (entity == null) return null;
    return new NonRenewalEvent(
        medicine,
        map(entity.getFinalRegistrationDate()),
        entity.getObservations());
  }
}

