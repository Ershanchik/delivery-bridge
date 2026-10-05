package delivery;
import java.util.List;
import java.util.function.Function;
public class DeliveryCalculator {
    private final List<CarrierRateService> carriers;
    public DeliveryCalculator(List<CarrierRateService> carriers) {
        this.carriers = List.copyOf(carriers);
    }
    public CarrierRateService selectCarrier(ShipmentRequest request) throws RateUnavailableException {
        return carriers.stream()
                .filter(c -> c.supports(request))
                .findFirst()
                .orElseThrow(() -> new RateUnavailableException(
                        RateUnavailableException.Reason.UNSUPPORTED_DESTINATION, "No carrier serves this shipment"));
    }
    public DeliveryOption.Quote calculate(ShipmentRequest request,
                                          Function<CarrierRateService, ? extends DeliveryOption> option)
            throws RateUnavailableException {
        return option.apply(selectCarrier(request)).quote(request);
    }
}
