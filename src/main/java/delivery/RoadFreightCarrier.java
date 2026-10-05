package delivery;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class RoadFreightCarrier implements CarrierRateService {
    static final double MAX_KG = 20_000;
    public String name() { return "RoadFreight"; }
    public boolean supports(ShipmentRequest r) { return !r.international(); }
    public Rate rate(ShipmentRequest r) throws RateUnavailableException {
        if (r.international())
            throw new RateUnavailableException(RateUnavailableException.Reason.UNSUPPORTED_DESTINATION,
                    name() + " serves only domestic routes");
        if (r.weightKg() > MAX_KG)
            throw new RateUnavailableException(RateUnavailableException.Reason.PARCEL_TOO_HEAVY,
                    name() + " accepts loads up to " + (int) MAX_KG + " kg");
        BigDecimal price = BigDecimal.valueOf(25.00 + 0.06 * r.distanceKm() + 0.12 * r.weightKg())
                .setScale(2, RoundingMode.HALF_UP);
        return new Rate(price, 1 + (int) Math.ceil(r.distanceKm() / 500.0));
    }
}
