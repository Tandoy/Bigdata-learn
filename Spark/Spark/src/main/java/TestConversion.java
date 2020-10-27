import java.text.DecimalFormat;

public class TestConversion {
    public static void main(String[] args) {
        System.out.println(conversion("23.236"));
    }
    public static String conversion(String value) {
        if (value == null) {
            return  null;
        }
        Double newValue;
        DecimalFormat df = new DecimalFormat("0.00");
        try{
            newValue = Double.valueOf(value);
        }catch (Exception e){
            return value; //传入不为金额时直接返回
        }
        return df.format(newValue);
    }
}
