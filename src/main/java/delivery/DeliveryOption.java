package delivery;
import java.math.BigDecimal;
import java.util.Objects;
public abstract class DeliveryOption {
    private final CarrierRateService carrier;
    protected DeliveryOption(CarrierRateService carrier) {
        this.carrier = Objects.requireNonNull(carrier, "carrier");
    }
    public final Quote quote(ShipmentRequest request) throws RateUnavailableException {
        CarrierRateService.Rate base = carrier.rate(request);
        return new Quote(label(), carrier.name(), adjustPrice(base.price()), adjustDays(base.transitDays()));
    }
    protected abstract String label();
    protected abstract BigDecimal adjustPrice(BigDecimal basePrice);
    protected abstract int adjustDays(int baseDays);
    public record Quote(String option, String carrier, BigDecimal price, int transitDays) { }
}
