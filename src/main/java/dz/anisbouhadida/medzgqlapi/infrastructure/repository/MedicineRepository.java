package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineType;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MedicineRepository
    extends JpaRepository<MedicineEntity, Long>, JpaSpecificationExecutor<MedicineEntity> {

  List<MedicineEntity> findByRegistrationNumber(String registrationNumber);

  Optional<MedicineEntity> findFirstByRegistrationNumber(String registrationNumber);

  Optional<MedicineEntity> findFirstByCode(String code);

  List<MedicineEntity> findByLaboratoryHolder(String laboratoryHolder);

  List<MedicineEntity> findByTypeAndOrigin(MedicineType type, MedicineOrigin origin);

  Optional<MedicineEntity> findByRegistrationNumberAndCodeAndIcdAndBrandNameAndLaboratoryHolder(
      String registrationNumber,
      String code,
      String icd,
      String brandName,
      String laboratoryHolder);
}

