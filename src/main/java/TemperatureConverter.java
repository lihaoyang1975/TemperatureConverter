import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A simple tool that allows you to convert
 * 1. from Celsius to Fahrenheit
 * 2. from Kelvin to Celsius
 * 3. from Fahrenheit to Kelvin
 *
 * To use this conversion tool, you can do one of the following:
 * 1. new TemperatureConverter().from(fromUnit, fromDegrees).to(toUnit).convert()
 * 2. new TemperatureConverter().to(toUnit).from(fromUnit, fromDegrees).convert()
 *
 * Returns the conversion result in this format:
 * <from degrees> <from unit> = <to degrees> <to unit>
 * without the angular brackets, e.g. 32 F = 273.15 K
 */
public class TemperatureConverter {

    private enum Unit {
        C("Celsius"),
        F("Fahrenheit"),
        K("Kelvin");

        private final String friendlyName;

        Unit(final String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public static Unit fromName(final String name) {
            return Stream.of(values()).filter(it -> it.name().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }

    private static final Double ABSOLUTE_ZERO = -273.15;

    private static class Temperature {
        private static DecimalFormat degreesformat = new DecimalFormat("#.##");

        // stores the lowest possible values for each temperature unit.
        // it would be better if we were to use Guava's ImmutableMap.
        private static Map<Unit, Double> LOWEST_DEGREES = initMap();

        private static Map<Unit, Double> initMap() {
            Map<Unit, Double> lowestDegrees = new HashMap<>();
            lowestDegrees.put(Unit.K, 0.0);
            lowestDegrees.put(Unit.C, ABSOLUTE_ZERO);
            lowestDegrees.put(Unit.F, -459.67);
            return lowestDegrees;
        }

        private Unit unit;
        private Double degrees;

        public Temperature() {
            // default constructor
        }

        public String toString() {
            degreesformat.setRoundingMode(RoundingMode.HALF_UP);
            return Optional.ofNullable(degrees).map(degreesformat::format).orElse("<degrees not specified>") + ' ' + Optional.ofNullable(unit).map(Unit::name).orElse("<unit not specified>");
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(final Unit unit) {
            this.unit = Objects.requireNonNull(unit);
        }

        public Double getDegrees() {
            return degrees;
        }

        public void setDegrees(final Double degrees) {
            Objects.requireNonNull(degrees);
            if (degrees < LOWEST_DEGREES.get(unit)) {
                throw new IllegalArgumentException(String.format("The lowest value you can set [%s] to is [%s].", unit.getFriendlyName(), LOWEST_DEGREES.get(unit)));
            }
            this.degrees = degrees;
        }
    }

    private Temperature fromTemperature = new Temperature();
    private Temperature toTemperature = new Temperature();

    private void fromKelvinToCelsius() {
        toTemperature.setDegrees(fromTemperature.getDegrees() + ABSOLUTE_ZERO);
    }

    private void fromCelsiusToFahrenheit() {
        toTemperature.setDegrees(fromTemperature.getDegrees() * 9 / 5 + 32.0);
    }

    private void fromFahrenheitToKelvin() {
        toTemperature.setDegrees((fromTemperature.getDegrees() - 32) * 5 / 9 - ABSOLUTE_ZERO);
    }

    /**
     * convert temperatures from one unit to another
     * @return String what is to be displayed as the conversion result, e.g. "0 C = 32 F"
     * @throws IllegalStateException to indicate that the converter is not in a ready state to do the conversion
     */
    public String convert() {
        // making sure the from Temperature is fully set
        if (fromTemperature.getUnit() == null || fromTemperature.getDegrees() == null) {
            throw new IllegalStateException(String.format("Missing information on the temperature you are converting from: [%s].", fromTemperature));
        }

        // making sure the to Temperature unit is set
        if (toTemperature.getUnit() == null) {
            throw new IllegalStateException(String.format("You have not specified the temperature unit to convert to."));
        }

        // making sure the from unit and the to unit are different
        if (fromTemperature.getUnit() == toTemperature.getUnit()) {
            throw new IllegalStateException(String.format("The temperature unit [%s] you are converting from is the same as the one [%s] you are converting to.", fromTemperature.getUnit().getFriendlyName(), toTemperature.getUnit().getFriendlyName()));
        }

        if (fromTemperature.getUnit() == Unit.C && toTemperature.getUnit() == Unit.F) { // from Celsius to Fahrenheit
            fromCelsiusToFahrenheit();
        } else if (fromTemperature.getUnit() == Unit.K && toTemperature.getUnit() == Unit.C) { // from Kelvin to Celsius
            fromKelvinToCelsius();
        } else if (fromTemperature.getUnit() == Unit.F && toTemperature.getUnit() == Unit.K) { // from Fahrenheit to Kelvin
            fromFahrenheitToKelvin();
        } else {
            throw new IllegalStateException(String.format("Conversion is not supported from [%s] to [%s].", fromTemperature.getUnit().getFriendlyName(), toTemperature.getUnit().getFriendlyName()));
        }

        return fromTemperature + " = " + toTemperature;
    }

    public TemperatureConverter from(final String unit, final Double degrees) {
        Objects.requireNonNull(unit);
        Unit fromUnit = Unit.fromName(unit);
        if (fromUnit == null) {
            throw new IllegalArgumentException(String.format("The temperature unit [%s] you are converting from is not supported.", unit));
        }
        fromTemperature.setUnit(fromUnit);
        fromTemperature.setDegrees(degrees);
        return this;
    }

    public TemperatureConverter to(final String unit) {
        Objects.requireNonNull(unit);
        Unit toUnit = Unit.fromName(unit);
        if (toUnit == null) {
            throw new IllegalArgumentException(String.format("The temperature unit [%s] you are converting to is not supported.", unit));
        }
        toTemperature.setUnit(toUnit);
        return this;
    }
}