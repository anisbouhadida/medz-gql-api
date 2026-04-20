package dz.anisbouhadida.medzgqlapi.application.controller;

import dz.anisbouhadida.medzgqlapi.application.config.GraphQlConfig;
import dz.anisbouhadida.medzgqlapi.application.config.GraphQlExceptionHandler;
import dz.anisbouhadida.medzgqlapi.domain.api.MedicineApi;
import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePage;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePageRequest;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GraphQlTest(MedicineController.class)
@Import({GraphQlConfig.class, GraphQlExceptionHandler.class})
@ExtendWith(InstancioExtension.class)
@DisplayName("MedicineController GraphQL slice")
class MedicineControllerGraphQlTest {

  @Autowired
  private GraphQlTester graphQlTester;

  @MockitoBean
  private MedicineApi medicineApi;

  @Test
  @DisplayName("medicineByRegistrationNumber should serialize scalar, enum, and union-backed fields")
  void medicineByRegistrationNumber_shouldSerializeScalarEnumAndUnionBackedFields() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 1L)
        .set(field(Medicine::registrationNumber), "REG-1")
        .set(field(Medicine::initialRegistrationDate), OffsetDateTime.parse("2025-01-01T00:00:00Z"))
        .create();
    NomenclatureEvent event = Instancio.of(NomenclatureEvent.class)
        .set(field(NomenclatureEvent::medicineId), 1L)
        .set(field(NomenclatureEvent::finalRegistrationDate), OffsetDateTime.parse("2026-06-01T00:00:00Z"))
        .set(field(NomenclatureEvent::stabilityDuration), "24 MOIS")
        .set(field(NomenclatureEvent::observations), "Stable")
        .create();
    when(medicineApi.findByRegistrationNumber("REG-1")).thenReturn(Optional.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(1L))).thenReturn(Map.of(1L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(1L))).thenReturn(Map.of(1L, List.of(event)));

    graphQlTester.document("""
        query($registrationNumber: String!) {
          medicineByRegistrationNumber(registrationNumber: $registrationNumber) {
            id
            registrationNumber
            initialRegistrationDate
            status
            event {
              __typename
              ... on NomenclatureEvent {
                eventType
                status
                finalRegistrationDate
                stabilityDuration
                observations
              }
            }
          }
        }
        """)
        .variable("registrationNumber", "REG-1")
        .execute()
        .path("medicineByRegistrationNumber.id").entity(String.class).isEqualTo("1")
        .path("medicineByRegistrationNumber.registrationNumber").entity(String.class).isEqualTo("REG-1")
        .path("medicineByRegistrationNumber.initialRegistrationDate").entity(String.class).isEqualTo("2025-01-01T00:00:00.000Z")
        .path("medicineByRegistrationNumber.status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicineByRegistrationNumber.event[0].__typename").entity(String.class).isEqualTo("NomenclatureEvent")
        .path("medicineByRegistrationNumber.event[0].eventType").entity(String.class).isEqualTo("UPSERT")
        .path("medicineByRegistrationNumber.event[0].status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicineByRegistrationNumber.event[0].finalRegistrationDate").entity(String.class).isEqualTo("2026-06-01T00:00:00.000Z")
        .path("medicineByRegistrationNumber.event[0].stabilityDuration").entity(String.class).isEqualTo("24 MOIS")
        .path("medicineByRegistrationNumber.event[0].observations").entity(String.class).isEqualTo("Stable");

    verify(medicineApi).findByRegistrationNumber("REG-1");
    verify(medicineApi).findLatestStatusByMedicineIds(List.of(1L));
    verify(medicineApi).findEventsByMedicineIds(List.of(1L));
  }

  @Test
  @DisplayName("medicineByRegistrationNumber should return null when the medicine is missing")
  void medicineByRegistrationNumber_shouldReturnNull_whenMedicineIsMissing() {
    when(medicineApi.findByRegistrationNumber("REG-404")).thenReturn(Optional.empty());

    graphQlTester.document("""
        query($registrationNumber: String!) {
          medicineByRegistrationNumber(registrationNumber: $registrationNumber) {
            id
          }
        }
        """)
        .variable("registrationNumber", "REG-404")
        .execute()
        .path("medicineByRegistrationNumber").valueIsNull();

    verify(medicineApi).findByRegistrationNumber("REG-404");
  }

  @Test
  @DisplayName("medicineByRegistrationNumber should return a BAD_REQUEST error when the argument is blank")
  void medicineByRegistrationNumber_shouldReturnBadRequest_whenArgumentIsBlank() {
    GraphQlTester.Response response = graphQlTester.document("""
        query {
          medicineByRegistrationNumber(registrationNumber: "   ") {
            id
          }
        }
        """)
        .execute();

    response.errors().satisfy(errors -> {
      assertEquals(1, errors.size());
      assertEquals("BAD_REQUEST", errors.getFirst().getErrorType().toString());
    });
  }

  @Test
  @DisplayName("medicineByCode should resolve status and event fields through batched API calls")
  void medicineByCode_shouldResolveStatusAndEventFieldsThroughBatchedApiCalls() {
    Medicine first = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 1L)
        .set(field(Medicine::brandName), "Doliprane")
        .create();
    Medicine second = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 2L)
        .set(field(Medicine::brandName), "Clamoxyl")
        .create();
    when(medicineApi.findByCode("CODE-ALL")).thenReturn(List.of(first, second));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, MedicineStatus.ACTIVE, 2L, MedicineStatus.WITHDRAWN));
    when(medicineApi.findEventsByMedicineIds(List.of(1L, 2L))).thenReturn(Map.of(
        1L,
        List.of(Instancio.of(NomenclatureEvent.class).set(field(NomenclatureEvent::medicineId), 1L).create()),
        2L,
        List.of()));

    graphQlTester.document("""
        query($code: String!) {
          medicinesByCode(code: $code) {
            id
            brandName
            status
            event {
              __typename
            }
          }
        }
        """)
        .variable("code", "CODE-ALL")
        .execute()
        .path("medicinesByCode[0].id").entity(String.class).isEqualTo("1")
        .path("medicinesByCode[0].brandName").entity(String.class).isEqualTo("Doliprane")
        .path("medicinesByCode[0].status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicinesByCode[0].event[0].__typename").entity(String.class).isEqualTo("NomenclatureEvent")
        .path("medicinesByCode[1].id").entity(String.class).isEqualTo("2")
        .path("medicinesByCode[1].brandName").entity(String.class).isEqualTo("Clamoxyl")
        .path("medicinesByCode[1].status").entity(String.class).isEqualTo("WITHDRAWN")
        .path("medicinesByCode[1].event").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByCode("CODE-ALL");
    verify(medicineApi).findLatestStatusByMedicineIds(List.of(1L, 2L));
    verify(medicineApi).findEventsByMedicineIds(List.of(1L, 2L));
  }

  @Test
  @DisplayName("medicineByCode should return an empty list when no medicines match the code")
  void medicineByCode_shouldReturnEmptyList_whenNoMedicinesMatchTheCode() {
    when(medicineApi.findByCode("UNKNOWN")).thenReturn(List.of());

    graphQlTester.document("""
        query($code: String!) {
          medicinesByCode(code: $code) {
            id
          }
        }
        """)
        .variable("code", "UNKNOWN")
        .execute()
        .path("medicinesByCode").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByCode("UNKNOWN");
  }

  @Test
  @DisplayName("medicineByCode should return a BAD_REQUEST error when the argument is blank")
  void medicineByCode_shouldReturnBadRequest_whenArgumentIsBlank() {
    GraphQlTester.Response response = graphQlTester.document("""
        query {
          medicinesByCode(code: "") {
            id
          }
        }
        """)
        .execute();

    response.errors().satisfy(errors -> {
      assertEquals(1, errors.size());
      assertEquals("BAD_REQUEST", errors.getFirst().getErrorType().toString());
    });
  }

  @Test
  @DisplayName("medicineByIcd should return matching medicines with status and events")
  void medicineByIcd_shouldReturnMatchingMedicinesWithStatusAndEvents() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 5L)
        .set(field(Medicine::internationalCommonDenomination), "Paracetamol")
        .create();
    when(medicineApi.findByIcd("Paracetamol")).thenReturn(List.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(5L))).thenReturn(Map.of(5L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(5L))).thenReturn(Map.of(5L, List.of()));

    graphQlTester.document("""
        query($icd: String!) {
          medicinesByIcd(icd: $icd) {
            id
            internationalCommonDenomination
            status
            event { __typename }
          }
        }
        """)
        .variable("icd", "Paracetamol")
        .execute()
        .path("medicinesByIcd[0].id").entity(String.class).isEqualTo("5")
        .path("medicinesByIcd[0].internationalCommonDenomination").entity(String.class).isEqualTo("Paracetamol")
        .path("medicinesByIcd[0].status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicinesByIcd[0].event").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByIcd("Paracetamol");
  }

  @Test
  @DisplayName("medicineByIcd should return an empty list when no medicines match")
  void medicineByIcd_shouldReturnEmptyList_whenNoMedicinesMatch() {
    when(medicineApi.findByIcd("NonExistent")).thenReturn(List.of());

    graphQlTester.document("""
        query($icd: String!) {
          medicinesByIcd(icd: $icd) {
            id
          }
        }
        """)
        .variable("icd", "NonExistent")
        .execute()
        .path("medicinesByIcd").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByIcd("NonExistent");
  }

  @Test
  @DisplayName("medicineByIcd should return a BAD_REQUEST error when the argument is blank")
  void medicineByIcd_shouldReturnBadRequest_whenArgumentIsBlank() {
    GraphQlTester.Response response = graphQlTester.document("""
        query {
          medicinesByIcd(icd: "") {
            id
          }
        }
        """)
        .execute();

    response.errors().satisfy(errors -> {
      assertEquals(1, errors.size());
      assertEquals("BAD_REQUEST", errors.getFirst().getErrorType().toString());
    });
  }

  @Test
  @DisplayName("medicineByBrandName should return matching medicines with status and events")
  void medicineByBrandName_shouldReturnMatchingMedicinesWithStatusAndEvents() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 7L)
        .set(field(Medicine::brandName), "Doliprane")
        .create();
    when(medicineApi.findByBrandName("Doliprane")).thenReturn(List.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(7L))).thenReturn(Map.of(7L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(7L))).thenReturn(Map.of(7L, List.of()));

    graphQlTester.document("""
        query($brandName: String!) {
          medicinesByBrandName(brandName: $brandName) {
            id
            brandName
            status
            event { __typename }
          }
        }
        """)
        .variable("brandName", "Doliprane")
        .execute()
        .path("medicinesByBrandName[0].id").entity(String.class).isEqualTo("7")
        .path("medicinesByBrandName[0].brandName").entity(String.class).isEqualTo("Doliprane")
        .path("medicinesByBrandName[0].status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicinesByBrandName[0].event").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByBrandName("Doliprane");
  }

  @Test
  @DisplayName("medicineByBrandName should return an empty list when no medicines match")
  void medicineByBrandName_shouldReturnEmptyList_whenNoMedicinesMatch() {
    when(medicineApi.findByBrandName("Unknown")).thenReturn(List.of());

    graphQlTester.document("""
        query($brandName: String!) {
          medicinesByBrandName(brandName: $brandName) {
            id
          }
        }
        """)
        .variable("brandName", "Unknown")
        .execute()
        .path("medicinesByBrandName").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByBrandName("Unknown");
  }

  @Test
  @DisplayName("medicineByBrandName should return a BAD_REQUEST error when the argument is blank")
  void medicineByBrandName_shouldReturnBadRequest_whenArgumentIsBlank() {
    GraphQlTester.Response response = graphQlTester.document("""
        query {
          medicinesByBrandName(brandName: "") {
            id
          }
        }
        """)
        .execute();

    response.errors().satisfy(errors -> {
      assertEquals(1, errors.size());
      assertEquals("BAD_REQUEST", errors.getFirst().getErrorType().toString());
    });
  }

  @Test
  @DisplayName("medicineByLaboratoryHolder should return matching medicines with status and events")
  void medicineByLaboratoryHolder_shouldReturnMatchingMedicinesWithStatusAndEvents() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 9L)
        .set(field(Medicine::laboratoryHolder), "Saidal")
        .create();
    when(medicineApi.findByLaboratoryHolder("Saidal")).thenReturn(List.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(9L))).thenReturn(Map.of(9L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(9L))).thenReturn(Map.of(9L, List.of()));

    graphQlTester.document("""
        query($laboratoryHolder: String!) {
          medicinesByLaboratoryHolder(laboratoryHolder: $laboratoryHolder) {
            id
            laboratoryHolder
            status
            event { __typename }
          }
        }
        """)
        .variable("laboratoryHolder", "Saidal")
        .execute()
        .path("medicinesByLaboratoryHolder[0].id").entity(String.class).isEqualTo("9")
        .path("medicinesByLaboratoryHolder[0].laboratoryHolder").entity(String.class).isEqualTo("Saidal")
        .path("medicinesByLaboratoryHolder[0].status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicinesByLaboratoryHolder[0].event").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByLaboratoryHolder("Saidal");
  }

  @Test
  @DisplayName("medicineByLaboratoryHolder should return an empty list when no medicines match")
  void medicineByLaboratoryHolder_shouldReturnEmptyList_whenNoMedicinesMatch() {
    when(medicineApi.findByLaboratoryHolder("Unknown")).thenReturn(List.of());

    graphQlTester.document("""
        query($laboratoryHolder: String!) {
          medicinesByLaboratoryHolder(laboratoryHolder: $laboratoryHolder) {
            id
          }
        }
        """)
        .variable("laboratoryHolder", "Unknown")
        .execute()
        .path("medicinesByLaboratoryHolder").entityList(Object.class).hasSize(0);

    verify(medicineApi).findByLaboratoryHolder("Unknown");
  }

  @Test
  @DisplayName("medicineByLaboratoryHolder should return a BAD_REQUEST error when the argument is blank")
  void medicineByLaboratoryHolder_shouldReturnBadRequest_whenArgumentIsBlank() {
    GraphQlTester.Response response = graphQlTester.document("""
        query {
          medicinesByLaboratoryHolder(laboratoryHolder: "") {
            id
          }
        }
        """)
        .execute();

    response.errors().satisfy(errors -> {
      assertEquals(1, errors.size());
      assertEquals("BAD_REQUEST", errors.getFirst().getErrorType().toString());
    });
  }

  @Test
  @DisplayName("medicinesSearch should return a Connection with edges and pageInfo")
  void medicinesSearch_shouldReturnConnectionWithEdgesAndPageInfo() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 10L)
        .set(field(Medicine::brandName), "Doliprane")
        .create();
    MedicinePage page = new MedicinePage(List.of(medicine), 1L, false, false, 0L);
    MedicineSearchFilter filter = new MedicineSearchFilter("doliprane", null, null, null);
    when(medicineApi.search(eq(filter), any(MedicinePageRequest.class))).thenReturn(page);
    when(medicineApi.findLatestStatusByMedicineIds(List.of(10L))).thenReturn(Map.of(10L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(10L))).thenReturn(Map.of(10L, List.of()));

    graphQlTester.document("""
        query {
          medicinesSearch(filter: { searchText: "doliprane" }, first: 10) {
            totalCount
            pageInfo {
              hasNextPage
              hasPreviousPage
              startCursor
              endCursor
            }
            edges {
              cursor
              node {
                id
                brandName
                status
                event { __typename }
              }
            }
          }
        }
        """)
        .execute()
        .path("medicinesSearch.totalCount").entity(Integer.class).isEqualTo(1)
        .path("medicinesSearch.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(false)
        .path("medicinesSearch.pageInfo.hasPreviousPage").entity(Boolean.class).isEqualTo(false)
        .path("medicinesSearch.pageInfo.startCursor").entity(String.class).satisfies(c -> assertTrue(c != null && !c.isBlank()))
        .path("medicinesSearch.pageInfo.endCursor").entity(String.class).satisfies(c -> assertTrue(c != null && !c.isBlank()))
        .path("medicinesSearch.edges[0].node.id").entity(String.class).isEqualTo("10")
        .path("medicinesSearch.edges[0].node.brandName").entity(String.class).isEqualTo("Doliprane")
        .path("medicinesSearch.edges[0].cursor").entity(String.class).satisfies(c -> assertTrue(c != null && !c.isBlank()));
  }

  @Test
  @DisplayName("medicinesSearch should return empty edges and correct pageInfo when no results match")
  void medicinesSearch_shouldReturnEmptyEdges_whenNoResultsMatch() {
    MedicinePage page = new MedicinePage(List.of(), 0L, false, false, 0L);
    MedicineSearchFilter filter = new MedicineSearchFilter("zzzzz", null, null, null);
    when(medicineApi.search(eq(filter), any(MedicinePageRequest.class))).thenReturn(page);

    graphQlTester.document("""
        query {
          medicinesSearch(filter: { searchText: "zzzzz" }) {
            totalCount
            pageInfo { hasNextPage hasPreviousPage }
            edges { cursor node { id } }
          }
        }
        """)
        .execute()
        .path("medicinesSearch.totalCount").entity(Integer.class).isEqualTo(0)
        .path("medicinesSearch.edges").entityList(Object.class).hasSize(0)
        .path("medicinesSearch.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(false);
  }

  @Test
  @DisplayName("medicinesSearch should indicate hasNextPage=true when more results exist")
  void medicinesSearch_shouldIndicateHasNextPage_whenMoreResultsExist() {
    List<Medicine> medicines = List.of(
        Instancio.of(Medicine.class).set(field(Medicine::id), 1L).create(),
        Instancio.of(Medicine.class).set(field(Medicine::id), 2L).create());
    MedicinePage page = new MedicinePage(medicines, 10L, true, false, 0L);
    MedicineSearchFilter filter = new MedicineSearchFilter("aspirin", null, null, null);
    when(medicineApi.search(eq(filter), any(MedicinePageRequest.class))).thenReturn(page);
    when(medicineApi.findLatestStatusByMedicineIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, MedicineStatus.ACTIVE, 2L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, List.of(), 2L, List.of()));

    graphQlTester.document("""
        query {
          medicinesSearch(filter: { searchText: "aspirin" }, first: 2) {
            totalCount
            pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
            edges { cursor node { id } }
          }
        }
        """)
        .execute()
        .path("medicinesSearch.totalCount").entity(Integer.class).isEqualTo(10)
        .path("medicinesSearch.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(true)
        .path("medicinesSearch.edges").entityList(Object.class).hasSize(2);
  }

  @Test
  @DisplayName("medicinesSearch should surface validation failures as BAD_REQUEST GraphQL errors")
  void medicinesSearch_shouldSurfaceValidationFailuresAsBadRequestErrors() {
    GraphQlTester.Response response = graphQlTester.document("""
        query {
          medicinesSearch(filter: {}) {
            totalCount
            edges { node { id } }
          }
        }
        """)
        .execute();

    response.errors().satisfy(errors -> {
      assertEquals(1, errors.size());
      ResponseError error = errors.getFirst();
      assertAll(
          () -> assertTrue(error.getMessage().contains("At least one filter criterion must be provided")),
          () -> assertEquals("BAD_REQUEST", error.getErrorType().toString()));
    });
  }

  @Test
  @DisplayName("event batch mapping should serialize WithdrawalEvent fields correctly")
  void eventBatchMapping_shouldSerializeWithdrawalEventFieldsCorrectly() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 20L)
        .create();
    WithdrawalEvent event = Instancio.of(WithdrawalEvent.class)
        .set(field(WithdrawalEvent::medicineId), 20L)
        .set(field(WithdrawalEvent::withdrawalDate), OffsetDateTime.parse("2026-03-15T00:00:00Z"))
        .set(field(WithdrawalEvent::withdrawalReason), "Safety concern")
        .create();
    when(medicineApi.findByRegistrationNumber("REG-W")).thenReturn(Optional.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(20L))).thenReturn(Map.of(20L, MedicineStatus.WITHDRAWN));
    when(medicineApi.findEventsByMedicineIds(List.of(20L))).thenReturn(Map.of(20L, List.of(event)));

    graphQlTester.document("""
        query {
          medicineByRegistrationNumber(registrationNumber: "REG-W") {
            status
            event {
              __typename
              ... on WithdrawalEvent {
                eventType
                status
                withdrawalDate
                withdrawalReason
              }
            }
          }
        }
        """)
        .execute()
        .path("medicineByRegistrationNumber.status").entity(String.class).isEqualTo("WITHDRAWN")
        .path("medicineByRegistrationNumber.event[0].__typename").entity(String.class).isEqualTo("WithdrawalEvent")
        .path("medicineByRegistrationNumber.event[0].eventType").entity(String.class).isEqualTo("WITHDRAWAL")
        .path("medicineByRegistrationNumber.event[0].status").entity(String.class).isEqualTo("WITHDRAWN")
        .path("medicineByRegistrationNumber.event[0].withdrawalDate").entity(String.class).isEqualTo("2026-03-15T00:00:00.000Z")
        .path("medicineByRegistrationNumber.event[0].withdrawalReason").entity(String.class).isEqualTo("Safety concern");
  }

  @Test
  @DisplayName("event batch mapping should serialize NonRenewalEvent fields correctly")
  void eventBatchMapping_shouldSerializeNonRenewalEventFieldsCorrectly() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 30L)
        .create();
    NonRenewalEvent event = Instancio.of(NonRenewalEvent.class)
        .set(field(NonRenewalEvent::medicineId), 30L)
        .set(field(NonRenewalEvent::finalRegistrationDate), OffsetDateTime.parse("2026-07-01T00:00:00Z"))
        .set(field(NonRenewalEvent::observations), "Expired license")
        .create();
    when(medicineApi.findByRegistrationNumber("REG-NR")).thenReturn(Optional.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(30L))).thenReturn(Map.of(30L, MedicineStatus.MARKED_NOT_RENEWED));
    when(medicineApi.findEventsByMedicineIds(List.of(30L))).thenReturn(Map.of(30L, List.of(event)));

    graphQlTester.document("""
        query {
          medicineByRegistrationNumber(registrationNumber: "REG-NR") {
            status
            event {
              __typename
              ... on NonRenewalEvent {
                eventType
                status
                finalRegistrationDate
                observations
              }
            }
          }
        }
        """)
        .execute()
        .path("medicineByRegistrationNumber.status").entity(String.class).isEqualTo("MARKED_NOT_RENEWED")
        .path("medicineByRegistrationNumber.event[0].__typename").entity(String.class).isEqualTo("NonRenewalEvent")
        .path("medicineByRegistrationNumber.event[0].eventType").entity(String.class).isEqualTo("NON_RENEWAL")
        .path("medicineByRegistrationNumber.event[0].status").entity(String.class).isEqualTo("MARKED_NOT_RENEWED")
        .path("medicineByRegistrationNumber.event[0].finalRegistrationDate").entity(String.class).isEqualTo("2026-07-01T00:00:00.000Z")
        .path("medicineByRegistrationNumber.event[0].observations").entity(String.class).isEqualTo("Expired license");
  }

  @Test
  @DisplayName("event batch mapping should serialize multiple event types for the same medicine")
  void eventBatchMapping_shouldSerializeMultipleEventTypesForSameMedicine() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 40L)
        .create();
    NomenclatureEvent nomEvent = Instancio.of(NomenclatureEvent.class)
        .set(field(NomenclatureEvent::medicineId), 40L)
        .create();
    WithdrawalEvent wdEvent = Instancio.of(WithdrawalEvent.class)
        .set(field(WithdrawalEvent::medicineId), 40L)
        .create();
    when(medicineApi.findByRegistrationNumber("REG-MULTI")).thenReturn(Optional.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(40L))).thenReturn(Map.of(40L, MedicineStatus.WITHDRAWN));
    when(medicineApi.findEventsByMedicineIds(List.of(40L))).thenReturn(Map.of(40L, List.of(nomEvent, wdEvent)));

    graphQlTester.document("""
        query {
          medicineByRegistrationNumber(registrationNumber: "REG-MULTI") {
            event {
              __typename
            }
          }
        }
        """)
        .execute()
        .path("medicineByRegistrationNumber.event[0].__typename").entity(String.class).isEqualTo("NomenclatureEvent")
        .path("medicineByRegistrationNumber.event[1].__typename").entity(String.class).isEqualTo("WithdrawalEvent");
  }

  @Test
  @DisplayName("medicineByRegistrationNumber should serialize all Medicine scalar fields")
  void medicineByRegistrationNumber_shouldSerializeAllMedicineScalarFields() {
    Medicine medicine = Instancio.of(Medicine.class)
        .set(field(Medicine::id), 50L)
        .set(field(Medicine::registrationNumber), "REG-50")
        .set(field(Medicine::code), "CODE-50")
        .set(field(Medicine::internationalCommonDenomination), "Ibuprofen")
        .set(field(Medicine::brandName), "Advil")
        .set(field(Medicine::form), "Capsule")
        .set(field(Medicine::dosage), "200 mg")
        .set(field(Medicine::packaging), "Blister")
        .set(field(Medicine::list), "B")
        .set(field(Medicine::p1), "Yes")
        .set(field(Medicine::p2), "No")
        .set(field(Medicine::laboratoryHolder), "Pfizer")
        .set(field(Medicine::laboratoryCountry), "US")
        .create();
    when(medicineApi.findByRegistrationNumber("REG-50")).thenReturn(Optional.of(medicine));
    when(medicineApi.findLatestStatusByMedicineIds(List.of(50L))).thenReturn(Map.of(50L, MedicineStatus.ACTIVE));
    when(medicineApi.findEventsByMedicineIds(List.of(50L))).thenReturn(Map.of(50L, List.of()));

    graphQlTester.document("""
        query {
          medicineByRegistrationNumber(registrationNumber: "REG-50") {
            id
            registrationNumber
            code
            internationalCommonDenomination
            brandName
            form
            dosage
            packaging
            list
            p1
            p2
            laboratoryHolder
            laboratoryCountry
            type
            origin
            status
            event { __typename }
          }
        }
        """)
        .execute()
        .path("medicineByRegistrationNumber.id").entity(String.class).isEqualTo("50")
        .path("medicineByRegistrationNumber.registrationNumber").entity(String.class).isEqualTo("REG-50")
        .path("medicineByRegistrationNumber.code").entity(String.class).isEqualTo("CODE-50")
        .path("medicineByRegistrationNumber.internationalCommonDenomination").entity(String.class).isEqualTo("Ibuprofen")
        .path("medicineByRegistrationNumber.brandName").entity(String.class).isEqualTo("Advil")
        .path("medicineByRegistrationNumber.form").entity(String.class).isEqualTo("Capsule")
        .path("medicineByRegistrationNumber.dosage").entity(String.class).isEqualTo("200 mg")
        .path("medicineByRegistrationNumber.packaging").entity(String.class).isEqualTo("Blister")
        .path("medicineByRegistrationNumber.list").entity(String.class).isEqualTo("B")
        .path("medicineByRegistrationNumber.p1").entity(String.class).isEqualTo("Yes")
        .path("medicineByRegistrationNumber.p2").entity(String.class).isEqualTo("No")
        .path("medicineByRegistrationNumber.laboratoryHolder").entity(String.class).isEqualTo("Pfizer")
        .path("medicineByRegistrationNumber.laboratoryCountry").entity(String.class).isEqualTo("US")
        .path("medicineByRegistrationNumber.type").entity(String.class).isEqualTo(medicine.type().name())
        .path("medicineByRegistrationNumber.origin").entity(String.class).isEqualTo(medicine.origin().name())
        .path("medicineByRegistrationNumber.status").entity(String.class).isEqualTo("ACTIVE")
        .path("medicineByRegistrationNumber.event").entityList(Object.class).hasSize(0);
  }
}
