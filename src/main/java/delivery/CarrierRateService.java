package delivery;
import java.math.BigDecimal;
import java.util.Objects;
public interface CarrierRateService{
    String name();
    boolean supports(ShipmentRequest request);
    Rate rate(ShipmentRequest request) throws RateUnavailableException;
    record Rate(BigDecimal price, int transitDays) {
        public Rate {
            if (price == null || price.signum() < 0 || transitDays < 1)
                throw new IllegalArgumentException("price must be >= 0 and transitDays >= 1");
        }
    }
}
record ShipmentRequest(
        String destinationCity, boolean international, int distanceKm,
        double weightKg, double lengthCm, double widthCm, double heightCm) {
    ShipmentRequest {
        Objects.requireNonNull(destinationCity, "destinationCity");
        if (distanceKm < 0) throw new IllegalArgumentException("distanceKm must not be negative");
        if (weightKg <= 0 || lengthCm <= 0 || widthCm <= 0 || heightCm <= 0)
            throw new IllegalArgumentException("weight and dimensions must be positive");
    }
}
class RateUnavailableException extends Exception {
    enum Reason { UNSUPPORTED_DESTINATION, PARCEL_TOO_HEAVY, PARCEL_TOO_LARGE, INVALID_REQUEST, CARRIER_UNAVAILABLE }
    private final Reason reason;
    RateUnavailableException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }
    Reason getReason() {
        return reason;
    }
}
