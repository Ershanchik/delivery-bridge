package delivery;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import vendor.LegacyAirCargoClient;
public class Main {
    public static void main(String[] args) {
        DeliveryCalculator calculator = new DeliveryCalculator(List.of(
                new ZoneTariffCarrier(),
                new RoadFreightCarrier(),
                new AirCargoAdapter(new LegacyAirCargoClient())));
        Map<String, Function<CarrierRateService, ? extends DeliveryOption>> options = new LinkedHashMap<>();
        options.put("Standard", StandardDelivery::new);
        options.put("Express", ExpressDelivery::new);
        Map<String, ShipmentRequest> shipments = new LinkedHashMap<>();
        shipments.put("Light parcel, 120 km", new ShipmentRequest("Springfield", false, 120, 2.5, 30, 20, 10));
        shipments.put("Heavy pallet, 900 km", new ShipmentRequest("Riverton", false, 900, 400, 120, 80, 100));
        shipments.put("Parcel to Berlin", new ShipmentRequest("Berlin", true, 0, 4.5, 50, 30, 20));
        shipments.put("Parcel to Sydney (not served)", new ShipmentRequest("Sydney", true, 0, 4.5, 50, 30, 20));
        shipments.forEach((title, request) -> {
            System.out.println("== " + title);
            options.forEach((optionName, factory) -> {
                try {
                    DeliveryOption.Quote q = calculator.calculate(request, factory);
                    System.out.printf("  %-8s via %-12s $%8s  %d day(s)%n", q.option(), q.carrier(), q.price(), q.transitDays());
                } catch (RateUnavailableException e) {
                    System.out.printf("  %-8s unavailable: %s (%s)%n", optionName, e.getReason(), e.getMessage());
                }
            });
        });
    }
}