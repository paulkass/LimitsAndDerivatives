import java.util.Enumeration;

/**
 * Created by paul on 11/25/14.
 */
public class Limit {

    FunctionClass function;
    enum Sign {p, n};
    double delta = 1*Math.pow(10, -10);
    public Limit(FunctionClass func) {
        function = func;
    }

    public String evalLimit(double z) {
        String return_string = "";

            String right_side = oneSidedLimit(z, Sign.p);
            String left_side = oneSidedLimit(z, Sign.n);

            if (Math.signum(Double.compare(Double.valueOf(right_side), Double.valueOf(left_side))) == 0) {
                return_string = right_side;
            } else {
                if (Double.isInfinite(Double.valueOf(function.evaluate(z))) || Double.isNaN(Double.valueOf(function.evaluate(z)))) {
                    return_string = String.valueOf((Double.valueOf(right_side)+Double.valueOf(left_side))/2.0);
                } else {
                    return_string = function.evaluate(z);
                }
            }
        return return_string;
    }

    public String oneSidedLimit(double x, Sign s) {
        String return_string = "";
        if (s == Sign.p) {
            return_string = function.evaluate(x + delta);
        } else {
            return_string = function.evaluate(x - delta);
        }
        if (function.evaluate(x).equals("Infinity") || function.evaluate(x).equals("-Infinity")) {
            if (Math.signum(Double.valueOf(return_string)) == 1) {
                return_string = new String("Infinity");
            } else if (Math.signum(Double.valueOf(return_string)) == -1) {
                return_string = new String("-Infinity");
            } else {
                return_string = function.evaluate(x);
            }
        }
        return_string = String.valueOf(Math.round(Double.valueOf(return_string) * Math.pow(10, 8)) / Math.pow(10, 8));
        return return_string;
    }



}
