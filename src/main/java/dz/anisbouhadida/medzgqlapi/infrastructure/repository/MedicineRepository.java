package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository
    extends JpaRepository<MedicineEntity, Long>, JpaSpecificationExecutor<MedicineEntity> {

  Optional<MedicineEntity> findByRegistrationNumber(String registrationNumber);

  List<MedicineEntity> findByCode(String code);

  List<MedicineEntity> findByIcd(String icd);

  List<MedicineEntity> findByBrandName(String brandName);

  List<MedicineEntity> findByLaboratoryHolder(String laboratoryHolder);
}

