# Design Rationale — Delivery Cost Calculation (Bridge + Adapter)

## 1. Problem
A shop calculates delivery quotes. The customer picks a **delivery option** (Standard, Express, Economy) and the system prices the shipment using a **carrier**: an own zone-tariff courier (short domestic, light parcels), road freight (heavy or long domestic loads) and an external air cargo service (international). The result is a quote: price in USD, carrier name, transit days.

Two things change independently: *how the customer wants it delivered* (commercial rules: surcharge, discount, speed) and *who carries it* (tariff formulas, limits, coverage).

## 2. Structure
- **Abstraction:** `DeliveryOption`; **Refined:** `StandardDelivery`, `ExpressDelivery`, `EconomyDelivery`.
- **Implementor:** `CarrierRateService`; **Concrete:** `ZoneTariffCarrier`, `RoadFreightCarrier`, `AirCargoAdapter` (3 implementations, exactly one adapted).
- **Adaptee:** `vendor.LegacyAirCargoClient`, not modified.
- **Complexity module chosen: Dynamic implementor selection.** `DeliveryCalculator` picks the carrier (including the adapted one) from the `ShipmentRequest` via `CarrierRateService.supports(...)`. The client only passes the option factory (`ExpressDelivery::new`) and never names a carrier.

## 3. Why Bridge alone is not enough
Bridge needs every implementor to look the same to the abstraction. The air cargo service comes from a vendor SDK we cannot edit and it does not fit the `CarrierRateService` contract. Without an adapter, the abstraction (or a subclass per option) would have to know the vendor API, its units and its error codes. Bridge alone cannot fix that mismatch.

## 4. Why Adapter alone is not enough
An adapter would make air cargo usable, but the real problem is the 3 x 3 grid of options and carriers. With Adapter only we would write `ExpressZoneDelivery`, `ExpressRoadDelivery`, `ExpressAirDelivery`, `EconomyZoneDelivery` and so on: 9 classes now, and a new option or carrier adds 3 more. Bridge keeps it at 3 + 3 classes (+1 adapter) and each axis grows on its own. Here the two patterns solve different problems: Bridge structures the design, Adapter lets a foreign class take one slot in it.

## 5. Why the wrapped class is genuinely incompatible
`LegacyAirCargoClient` differs from `CarrierRateService` in more than a method name:

| Aspect | Implementor contract | Legacy client |
|---|---|---|
| Methods | one call `rate(request)` returns price and days | two calls: `calcPrice(...)` and `flightHours(...)` |
| Input | `ShipmentRequest` (city name, cm, kg as `double`) | airport code, mm and grams as `int`, different parameter order |
| Output | `Rate(BigDecimal USD, days)` | price in cents (`long`), time in hours (`int`) |
| Failure | checked `RateUnavailableException` with a `Reason` | negative error codes (`ERR_*`), no exceptions |

`AirCargoAdapter` converts the input (city to IATA code, cm/kg to mm/g, saturating on overflow), combines the two calls, converts the output, and maps **every** error code (plus any unknown negative value) to a `Reason`. Exception messages contain no legacy codes. `DeliveryOption` and its subclasses never import `vendor.*`; a test checks this by reflection.

## 6. Open/Closed on both axes
- **New abstraction variant:** a new `DeliveryOption` subclass works with all existing carriers, no edits (demonstrated by a test-local `PriorityDelivery`).
- **New implementor variant:** a new `CarrierRateService` works with all existing options, no edits (demonstrated by a test-local drone carrier). To have it selected at runtime it is added to the list in the composition root (`Main`), which is wiring, not a change of existing classes.

## 7. Tests
JUnit 5 + Mockito: delegation of three refined abstractions to a mocked implementor; failure pass-through; adapter with a mocked legacy client (unit conversion, all error codes via a parameterized test, second-call failure, no leakage, unknown city without calling the vendor); dynamic selection with mocked and real carriers.

## 8. Limitation
Carrier selection depends only on the shipment, not on the chosen delivery option, and it is "first supporting carrier in list order". So `ExpressDelivery` cannot ask for a faster carrier (for example air instead of road for a domestic urgent parcel), and overlapping `supports()` rules are resolved only by the order of the list. Fixing it would need a selection policy that receives both the request and the option, which adds a new concept to the design.
