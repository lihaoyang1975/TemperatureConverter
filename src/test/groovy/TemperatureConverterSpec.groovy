import spock.lang.Specification

class TemperatureConverterSpec extends Specification {

    void "test temperature converter with invalid data"() {
        def result;
        def ex;

        // test if IllegalStateException is thrown when the converter is not ready
        when: "neither unit nor degrees is given to convert from"
        result = new TemperatureConverter().to('K').convert()

        then: "expect exception to be thrown"
        ex = thrown(IllegalStateException)
        ex.message == "Missing information on the temperature you are converting from: [<degrees not specified> <unit not specified>]."

        when: "no unit is given to convert to"
        result = new TemperatureConverter().from('C', 0).convert()

        then: "expect exception to be thrown"
        ex = thrown(IllegalStateException)
        ex.message == "You have not specified the temperature unit to convert to."

        when: "the unit to convert from is the same as the unit to convert to"
        result = new TemperatureConverter().from('K', 0).to('K').convert()

        then: "expect exception to be thrown"
        ex = thrown(IllegalStateException)
        ex.message == "The temperature unit [Kelvin] you are converting from is the same as the one [Kelvin] you are converting to."

        when: "converting from Kelvin to Fahrenheit, which is not supported"
        result = new TemperatureConverter().from('K', 0).to('F').convert()

        then: "should succeed"
        ex = thrown(IllegalStateException)
        ex.message == "Conversion is not supported from [Kelvin] to [Fahrenheit]."

        // test if IllegalArgumentException is thrown when the temperature unit is not supported
        when: "an invalid unit is given to convert from"
        result = new TemperatureConverter().from("A", 0)

        then: "expect exception to be thrown"
        ex = thrown(IllegalArgumentException)
        ex.message == "The temperature unit [A] you are converting from is not supported."

        when: "an invalid unit is given to convert to"
        result = new TemperatureConverter().from("C", 0).to("Z")

        then: "expect exception to be thrown"
        ex = thrown(IllegalArgumentException)
        ex.message == "The temperature unit [Z] you are converting to is not supported."

        // test if IllegalArgumentException is thrown when temperature is set to a value lower than the min allowed
        when: "the specified degrees in Celsius is lower than the min value allowed"
        result = new TemperatureConverter().from('C', -273.16)

        then: "expect exception to be thrown"
        ex = thrown(IllegalArgumentException)
        ex.message == "The lowest value you can set [Celsius] to is [-273.15]."

        when: "the specified degrees in Kelvin is lower than the min value allowed"
        result = new TemperatureConverter().from('K', -0.01)

        then: "expect exception to be thrown"
        ex = thrown(IllegalArgumentException)
        ex.message == "The lowest value you can set [Kelvin] to is [0.0]."

        when: "the specified degrees in Fahrenheit is lower than the min value allowed"
        result = new TemperatureConverter().from('F', -459.68)

        then: "expect exception to be thrown"
        ex = thrown(IllegalArgumentException)
        ex.message == "The lowest value you can set [Fahrenheit] to is [-459.67]."
    }

    void "test Temperature Converter with valid data"() {
        given: "various input, the result needs to match what we expect"

        expect:
        result == new TemperatureConverter().from(fromUnit, fromDegrees).to(toUnit).convert()

        where:
        fromDegrees | fromUnit | toUnit | result
        0           | 'C'      | 'F'    | "0 C = 32 F"
        0           | 'K'      | 'C'    | "0 K = -273.15 C"
        0           | 'F'      | 'K'    | "0 F = 255.37 K"
        134.56      | 'C'      | 'F'    | "134.56 C = 274.21 F"
        299.99      | 'K'      | 'C'    | "299.99 K = 26.84 C"
        98.6        | 'F'      | 'K'    | "98.6 F = 310.15 K"
        32          | 'F'      | 'K'    | "32 F = 273.15 K"
    }
}
