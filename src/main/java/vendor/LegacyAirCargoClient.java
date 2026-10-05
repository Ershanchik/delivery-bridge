package vendor;
import java.util.Map;
public class LegacyAirCargoClient {
    public static final int ERR_NO_ROUTE = -1;
    public static final int ERR_OVERWEIGHT = -2;
    public static final int ERR_OVERSIZE = -3;
    public static final int ERR_SERVICE_DOWN = -4;
    public static final int ERR_BAD_INPUT = -5;
    private static final Map<String, int[]> ROUTES = Map.of( // airport -> {cents/kg, flight hours}
            "BER", new int[] {450, 30}, "DXB", new int[] {380, 20}, "IST", new int[] {320, 16},
            "LHR", new int[] {510, 28}, "NRT", new int[] {690, 40});
    private final boolean online;
    public LegacyAirCargoClient() { this(true); }
    public LegacyAirCargoClient(boolean online) { this.online = online; }
    public long calcPrice(String airportCode, int lengthMm, int widthMm, int heightMm, int weightGrams) {
        if (!online) return ERR_SERVICE_DOWN;
        if (airportCode == null || lengthMm <= 0 || widthMm <= 0 || heightMm <= 0 || weightGrams <= 0) return ERR_BAD_INPUT;
        int[] route = ROUTES.get(airportCode);
        if (route == null) return ERR_NO_ROUTE;
        if (weightGrams > 150_000) return ERR_OVERWEIGHT;
        if (Math.max(lengthMm, Math.max(widthMm, heightMm)) > 3000) return ERR_OVERSIZE;
        double chargeableKg = Math.max(weightGrams / 1000.0, (double) lengthMm * widthMm * heightMm / 6_000_000.0);
        return Math.max(2500, Math.round(chargeableKg * route[0]));
    }
    public int flightHours(String airportCode) {
        if (!online) return ERR_SERVICE_DOWN;
        int[] route = airportCode == null ? null : ROUTES.get(airportCode);
        return route == null ? ERR_NO_ROUTE : route[1];
    }
}
