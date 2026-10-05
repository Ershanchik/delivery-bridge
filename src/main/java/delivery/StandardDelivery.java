package delivery;
import java.math.BigDecimal;
public class StandardDelivery extends DeliveryOption {
    public StandardDelivery(CarrierRateService carrier) { super(carrier); }
    protected String label() { return "Standard"; }
    protected BigDecimal adjustPrice(BigDecimal basePrice) { return basePrice; }
    protected int adjustDays(int baseDays) { return baseDays; }
}
