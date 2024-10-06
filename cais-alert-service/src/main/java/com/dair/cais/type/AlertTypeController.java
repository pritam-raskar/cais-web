package com.dair.cais.type;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/types")
@Tag(name = "types")

public class AlertTypeController {

   @Autowired
   private AlertTypeService typeService;
   @Autowired
   private AlertTypeServiceExtended typeServiceExtended;


//   below method created to fetch all the fields associated to an alert type
//   @GetMapping("/fields")
//   @Operation(summary = "fetch alert type fields")
//   public ResponseEntity<Map<String, Object>> getAlertTypeFields(@RequestParam String alertType) {
//      AlertType alertTypeModel = typeService.getAlertTypeFields(alertType);
//      return ResponseEntity.ok(alertTypeModel.getFields());
//   }

  // below method created to fetch all the fields associated to an alert type

//    @GetMapping("/fields")
//    @Operation(summary = "fetch alert type fields")
//    public ResponseEntity<AlertTypeExtended> getAlertTypeFields(@RequestParam String alertTypeId) {
//        AlertTypeExtended alertTypeModelExtended = typeServiceExtended.getAlertTypeFields(alertTypeId);
//        return ResponseEntity.ok().body(alertTypeModelExtended);
//    }

   @GetMapping("/fields")
   @Operation(summary = "fetch alert type fields by alertTypeId")
   public ResponseEntity<Map<String, Object>> combineFields(@RequestParam String alertTypeId) {
      try {
         AlertTypeExtended alertTypeExtended = typeServiceExtended.getAlertTypeFields(alertTypeId);

         if (alertTypeExtended.getMandatory_fields() == null) {
            return ResponseEntity.notFound().build();
         }

         ObjectMapper mapper = new ObjectMapper();

         // Parse the field_schema and mandatory_fields
         List<Map<String, Object>> fieldSchema = mapper.readValue(alertTypeExtended.getField_schema(),
                 new TypeReference<List<Map<String, Object>>>(){});
         List<Map<String, Object>> mandatoryFields = mapper.readValue(alertTypeExtended.getMandatory_fields(),
                 new TypeReference<List<Map<String, Object>>>(){});

         // Combine the two lists
         List<Map<String, Object>> combinedFields = new ArrayList<>(mandatoryFields);
         combinedFields.addAll(fieldSchema);

         // Create a new map with the combined fields
         Map<String, Object> result = Map.of(
                 "fields", combinedFields
         );

         return ResponseEntity.ok(result);
      } catch (Exception e) {
         return ResponseEntity.internalServerError().body(Map.of("error", "Error processing document: " + e.getMessage()));
      }
   }

   @PostMapping
   @Operation(summary = "Create a type")
   public ResponseEntity<AlertType> createAlertType(@RequestBody AlertType type) {
      AlertType createdAlertType = typeService.createAlertType(type);
      return ResponseEntity.ok().body(createdAlertType);
   }

   @GetMapping("{typeId}")
   @Operation(summary = "Get a AlertType by its id")
   public ResponseEntity<AlertType> getAlertTypeById(@PathVariable final String typeId) {
      AlertType typeById = typeService.getAlertTypeById(typeId);
      return ResponseEntity.ok().body(typeById);
   }

   @PatchMapping("{typeId}")
   @Operation(summary = "Update a type")
   public ResponseEntity<AlertType> patchAlertType(@PathVariable final String typeId,
         @RequestParam(required = true) String typeType, @RequestBody AlertType type) {
      AlertType updatedAlertType = typeService.patchAlertType(typeId, typeType, type);
      return ResponseEntity.ok().body(updatedAlertType);
   }

   @Hidden
   @PostMapping("/bulk")
   @Operation(summary = "Create bulk types")
   public ResponseEntity<List<AlertType>> createAlertTypes(@RequestBody List<AlertType> types) {
      List<AlertType> createdAlertTypes = typeService.createAlertTypes(types);
      return ResponseEntity.ok().body(createdAlertTypes);
   }
   @GetMapping("getAllAlertTypes")
   @Operation(summary = "Get all alert types which are Active")
   public ResponseEntity<Map<String, Object>> fetchAllAlerttypes() {
      Map<String, Object> allAlertTypes = typeServiceExtended.fetchalertTypesAll();
      return ResponseEntity.ok().body(allAlertTypes);
         }



   @GetMapping("")
   @Operation(summary = "Get all types; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllAlertTypes(
         @RequestParam(required = false) String name,
         @RequestParam(required = false, name = "state") String state,
         @RequestParam(required = false, name = "accountNumber") List<String> accountNumberList,
         @RequestParam(required = false, name = "owner") List<String> owners,
         @RequestParam(required = false, name = "assignee") List<String> assignees,
         @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
         @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {

      Map<String, Object> allAlertTypes = typeService.getAllAlertTypes(name, state, accountNumberList, owners,
            assignees,
            createdDateFrom, createdDateTo, limit, offset);

      return ResponseEntity.ok().body(allAlertTypes);
   }
}