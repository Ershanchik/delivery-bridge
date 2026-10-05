# Delivery cost calculation: Bridge + Adapter

Java 17, Maven, JUnit 5 (no mocking framework). Individual assignment 3.

```bash
mvn test   # build + run all tests (one command)
```

- `docs/DESIGN_RATIONALE.md` design rationale
- `docs/uml-class-diagram.svg` (and `.png`) UML class diagram of the final code
- `src/main/java/delivery` bridge, carriers, adapter, dynamic selection (`ShipmentRequest` and
  `RateUnavailableException` are declared package-private in `CarrierRateService.java` to keep the
  file count minimal); `src/main/java/vendor` the adapted legacy class (unchanged)
