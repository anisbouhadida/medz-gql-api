package dz.anisbouhadida.medzgqlapi.application.controller;

import dz.anisbouhadida.medzgqlapi.domain.api.MedicineApi;
import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePage;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePageRequest;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSortInput;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// GraphQL controller that maps the `Query` type operations defined in
/// `schema.graphqls` to the [MedicineApi] domain port.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Controller
@Validated
@RequiredArgsConstructor
public class MedicineController {

  private final MedicineApi medicineApi;

  /// Retrieve a medicine by its official registration number.
  @QueryMapping
  public Medicine medicineByRegistrationNumber(@Argument @NotBlank @Size(max = 255) String registrationNumber) {
    return medicineApi.findByRegistrationNumber(registrationNumber).orElse(null);
  }

  /// Retrieve a medicine by its unique code.
  @QueryMapping
  public List<Medicine> medicinesByCode(@Argument @NotBlank @Size(max = 255) String code) {
    return medicineApi.findByCode(code);
  }

  @QueryMapping
  public List<Medicine> medicinesByIcd(@Argument @NotBlank @Size(max = 255) String icd) {
    return medicineApi.findByIcd(icd);
  }

  @QueryMapping
  public List<Medicine> medicinesByBrandName(@Argument @NotBlank @Size(max = 255) String brandName){
    return medicineApi.findByBrandName(brandName);
  }

  @QueryMapping
  public List<Medicine> medicinesByLaboratoryHolder(@Argument @NotBlank @Size(max = 255) String laboratoryHolder){
    return medicineApi.findByLaboratoryHolder(laboratoryHolder);
  }

  /// Search medicines by text and optional filters, with cursor-based pagination and sorting.
  @QueryMapping
  public MedicineConnection medicinesSearch(
      @Argument @Valid MedicineSearchFilter filter,
      @Argument @Min(1) @Max(100) Integer first,
      @Argument String after,
      @Argument @Min(1) @Max(100) Integer last,
      @Argument String before,
      @Argument List<MedicineSortInput> sort) {

    MedicinePageRequest pageRequest = new MedicinePageRequest(first, after, last, before, sort);
    MedicinePage page = medicineApi.search(filter, pageRequest);

    long startOffset = page.startOffset();
    List<MedicineEdge> edges = new ArrayList<>();
    for (int i = 0; i < page.content().size(); i++) {
      String cursor = CursorUtils.encode(startOffset + i);
      edges.add(new MedicineEdge(page.content().get(i), cursor));
    }

    String startCursor = edges.isEmpty() ? null : edges.getFirst().cursor();
    String endCursor   = edges.isEmpty() ? null : edges.getLast().cursor();

    PageInfo pageInfo = new PageInfo(
        page.hasNextPage(),
        page.hasPreviousPage(),
        startCursor,
        endCursor);

    return new MedicineConnection(edges, pageInfo, page.totalElements());
  }

  @BatchMapping
  public List<MedicineStatus> status(List<Medicine> medicines) {
    List<Long> medicineIds = medicines.stream().map(Medicine::id).toList();
    Map<Long, MedicineStatus> statusMap = medicineApi.findLatestStatusByMedicineIds(medicineIds);
    return medicines.stream()
        .map(m -> statusMap.get(m.id()))
        .toList();
  }

  @BatchMapping
  public List<List<MedicineEvent>> event(List<Medicine> medicines) {
    List<Long> medicineIds = medicines.stream().map(Medicine::id).toList();
    Map<Long, List<MedicineEvent>> eventsMap = medicineApi.findEventsByMedicineIds(medicineIds);
    return medicines.stream()
        .map(m -> eventsMap.getOrDefault(m.id(), List.of()))
        .toList();
  }
}

