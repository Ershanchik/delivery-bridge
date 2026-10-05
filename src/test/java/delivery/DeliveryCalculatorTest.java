package delivery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import org.junit.jupiter.api.Test;
import vendor.LegacyAirCargoClient;
class DeliveryCalculatorTest {
    private final DeliveryCalculator calculator = new DeliveryCalculator(List.of(
            new ZoneTariffCarrier(), new RoadFreightCarrier(), new AirCargoAdapter(new LegacyAirCargoClient())));
    @Test
    void lightDomesticShipmentGoesToZoneCourier() throws Exception {
        ShipmentRequest light = new ShipmentRequest("Springfield", false, 120, 2.5, 30, 20, 10);
        assertEquals("ZoneCourier", calculator.calculate(light, StandardDelivery::new).carrier());
    }
    @Test
    void heavyDomesticShipmentGoesToRoadFreight() throws Exception {
        ShipmentRequest heavy = new ShipmentRequest("Riverton", false, 900, 400, 120, 80, 100);
        assertEquals("RoadFreight", calculator.calculate(heavy, ExpressDelivery::new).carrier());
    }
    @Test
    void internationalShipmentGoesToTheAdaptedCarrier() throws Exception {
        ShipmentRequest intl = new ShipmentRequest("Berlin", true, 0, 4.5, 50, 30, 20);
        assertEquals("AirCargo", calculator.calculate(intl, StandardDelivery::new).carrier());
    }
    @Test
    void failsWhenNoCarrierSupportsTheShipment() {
        ShipmentRequest unsupported = new ShipmentRequest("Sydney", true, 0, 4.5, 50, 30, 20);
        DeliveryCalculator onlyDomestic = new DeliveryCalculator(List.of(new RoadFreightCarrier()));
        assertThrows(RateUnavailableException.class, () -> onlyDomestic.calculate(unsupported, StandardDelivery::new));
    }
}
