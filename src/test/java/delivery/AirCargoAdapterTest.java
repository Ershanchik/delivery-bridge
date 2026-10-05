package delivery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import vendor.LegacyAirCargoClient;
class AirCargoAdapterTest {
    private final ShipmentRequest berlin = new ShipmentRequest("Berlin", true, 0, 4.5, 50, 30, 20);
    private final AirCargoAdapter adapter = new AirCargoAdapter(new LegacyAirCargoClient());
    @Test
    void convertsUnitsAndCurrencyOnSuccess() throws Exception {
        CarrierRateService.Rate rate = adapter.rate(berlin);
        assertEquals(new BigDecimal("25.00"), rate.price());
        assertEquals(2, rate.transitDays());
    }
    @Test
    void unknownCityMapsToUnsupportedDestination() {
        ShipmentRequest sydney = new ShipmentRequest("Sydney", true, 0, 4.5, 50, 30, 20);
        var ex = assertThrows(RateUnavailableException.class, () -> adapter.rate(sydney));
        assertEquals(RateUnavailableException.Reason.UNSUPPORTED_DESTINATION, ex.getReason());
    }
    @Test
    void overweightParcelMapsToParcelTooHeavy() {
        ShipmentRequest heavy = new ShipmentRequest("Berlin", true, 0, 200, 50, 30, 20);
        var ex = assertThrows(RateUnavailableException.class, () -> adapter.rate(heavy));
        assertEquals(RateUnavailableException.Reason.PARCEL_TOO_HEAVY, ex.getReason());
    }
    @Test
    void offlineCarrierMapsToCarrierUnavailable() {
        AirCargoAdapter offline = new AirCargoAdapter(new LegacyAirCargoClient(false));
        var ex = assertThrows(RateUnavailableException.class, () -> offline.rate(berlin));
        assertEquals(RateUnavailableException.Reason.CARRIER_UNAVAILABLE, ex.getReason());
    }
}
