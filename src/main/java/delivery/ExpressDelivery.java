package delivery;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class ExpressDelivery extends DeliveryOption {
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.60");
    private static final BigDecimal RUSH_FEE = new BigDecimal("5.00");
    public ExpressDelivery(CarrierRateService carrier) { super(carrier); }
    protected String label() { return "Express"; }
    protected BigDecimal adjustPrice(BigDecimal basePrice) {
        return basePrice.multiply(MULTIPLIER).add(RUSH_FEE).setScale(2, RoundingMode.HALF_UP);
    }
    protected int adjustDays(int baseDays) { return Math.max(1, (baseDays + 1) / 2); }
}
