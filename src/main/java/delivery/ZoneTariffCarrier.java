package delivery;
import delivery.RateUnavailableException.Reason;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class ZoneTariffCarrier implements CarrierRateService {
    static final int MAX_KM = 300;
    static final double MAX_KG = 30;
    public String name() { return "ZoneCourier"; }
    public boolean supports(ShipmentRequest r) {
        return !r.international() && r.distanceKm() <= MAX_KM && r.weightKg() <= MAX_KG;
    }
    public Rate rate(ShipmentRequest r) throws RateUnavailableException {
        if (!supports(r)) {
            Reason reason = r.international() || r.distanceKm() > MAX_KM
                    ? Reason.UNSUPPORTED_DESTINATION
                    : Reason.PARCEL_TOO_HEAVY;
            throw new RateUnavailableException(reason, name() + " cannot handle this shipment");
        }
        BigDecimal price = BigDecimal.valueOf(4.00 + 0.015 * r.distanceKm() + 0.80 * r.weightKg())
                .setScale(2, RoundingMode.HALF_UP);
        return new Rate(price, Math.max(1, (int) Math.ceil(r.distanceKm() / 150.0)));
    }
}
