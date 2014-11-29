/**
 * Created by paul on 11/28/14.
 */
public class IntegrationThread extends Thread {
    FunctionClass function;
    double a, b, delta_x, overall_sum;
    volatile boolean are_we_done;
    public IntegrationThread(double temp_a, double temp_b, double temp_delta_x, FunctionClass func) {
        a = temp_a;
        b = temp_b;
        delta_x = temp_delta_x;
        function = func;
        are_we_done = false;
    }

    public void run() {
        double counter = Math.min(a, b);
        double first_sum = 0;
        boolean are_we_done1 = false;
        // using inscribed rectangles
        while (!are_we_done1) {
            first_sum+=Double.valueOf(function.evaluate(counter))*delta_x;
            counter+=delta_x;
            //System.out.println(counter);
            if (Double.compare(counter, Math.max(a, b))>=0) {
                are_we_done1 = true;
            }
        }
        if (Double.compare(a,b)>0) {
            first_sum = (-1)*first_sum;
        }

        counter = Math.min(a, b);
        boolean are_we_done2 = false;
        double second_sum = 0;
        // using circumscribed rectangles
        while (!are_we_done2) {
            counter+=delta_x;
            if (Double.compare(counter, Math.max(a, b))>0) {
                break;
            }

            second_sum+=Double.valueOf(function.evaluate(counter))*delta_x;

            //System.out.println(counter);

        }
        if (Double.compare(a,b)>0) {
            second_sum = (-1)*second_sum;
        }

        overall_sum = (first_sum+second_sum)/2;

        are_we_done = true;
        //System.out.println(are_we_done);
    }
}
