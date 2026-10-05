package delivery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
class DeliveryOptionTest {
    private final ShipmentRequest request = new ShipmentRequest("Berlin", true, 0, 1, 10, 10, 10);
    private final CarrierRateService carrier = new FixedRateCarrier();
    @Test
    void standardKeepsRateAsIs() throws Exception {
        DeliveryOption.Quote q = new StandardDelivery(carrier).quote(request);
        assertEquals(new BigDecimal("100.00"), q.price());
        assertEquals(5, q.transitDays());
    }
    @Test
    void expressAddsSurchargeAndHalvesTransitTime() throws Exception {
        DeliveryOption.Quote q = new ExpressDelivery(carrier).quote(request);
        assertEquals(new BigDecimal("165.00"), q.price());
        assertEquals(3, q.transitDays());
    }
    @Test
    void newRefinedAbstractionWorksWithoutChangingExistingClasses() throws Exception {
        class PriorityDelivery extends DeliveryOption {
            PriorityDelivery(CarrierRateService c) { super(c); }
            protected String label() { return "Priority"; }
            protected BigDecimal adjustPrice(BigDecimal base) { return base.add(BigDecimal.TEN); }
            protected int adjustDays(int days) { return 1; }
        }
        assertEquals(new BigDecimal("110.00"), new PriorityDelivery(carrier).quote(request).price());
    }
    private static class FixedRateCarrier implements CarrierRateService {
        public String name() { return "Fixed"; }
        public boolean supports(ShipmentRequest r) { return true; }
        public Rate rate(ShipmentRequest r) { return new Rate(new BigDecimal("100.00"), 5); }
    }
}
