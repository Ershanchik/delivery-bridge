package delivery;
import delivery.RateUnavailableException.Reason;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import vendor.LegacyAirCargoClient;
public class AirCargoAdapter implements CarrierRateService {
    private static final Map<String, String> AIRPORTS = Map.of(
            "berlin", "BER", "dubai", "DXB", "istanbul", "IST", "london", "LHR", "tokyo", "NRT");
    private final LegacyAirCargoClient client;
    public AirCargoAdapter(LegacyAirCargoClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }
    public String name() { return "AirCargo"; }
    public boolean supports(ShipmentRequest r) {
        return r.international() && AIRPORTS.containsKey(r.destinationCity().trim().toLowerCase(Locale.ROOT));
    }
    public Rate rate(ShipmentRequest r) throws RateUnavailableException {
        String airport = AIRPORTS.get(r.destinationCity().trim().toLowerCase(Locale.ROOT));
        if (airport == null)
            throw new RateUnavailableException(Reason.UNSUPPORTED_DESTINATION, name() + " does not serve " + r.destinationCity());
        long cents = client.calcPrice(airport, toMm(r.lengthCm()), toMm(r.widthCm()), toMm(r.heightCm()), toGrams(r.weightKg()));
        if (cents < 0) throw translate(cents);
        int hours = client.flightHours(airport);
        if (hours < 0) throw translate(hours);
        return new Rate(BigDecimal.valueOf(cents, 2), Math.max(1, (int) Math.ceil(hours / 24.0)));
    }
    private RateUnavailableException translate(long code) {
        String msg;
        Reason reason;
        if (code == LegacyAirCargoClient.ERR_NO_ROUTE) { reason = Reason.UNSUPPORTED_DESTINATION; msg = "does not serve this destination"; }
        else if (code == LegacyAirCargoClient.ERR_OVERWEIGHT) { reason = Reason.PARCEL_TOO_HEAVY; msg = "rejected the parcel: too heavy"; }
        else if (code == LegacyAirCargoClient.ERR_OVERSIZE) { reason = Reason.PARCEL_TOO_LARGE; msg = "rejected the parcel: too large"; }
        else if (code == LegacyAirCargoClient.ERR_BAD_INPUT) { reason = Reason.INVALID_REQUEST; msg = "rejected the request data"; }
        else { reason = Reason.CARRIER_UNAVAILABLE; msg = "is temporarily unavailable"; }
        return new RateUnavailableException(reason, name() + " " + msg);
    }
    private static int toMm(double cm) { return (int) Math.min(Math.round(cm * 10), Integer.MAX_VALUE); }
    private static int toGrams(double kg) { return (int) Math.min(Math.round(kg * 1000), Integer.MAX_VALUE); }
}
