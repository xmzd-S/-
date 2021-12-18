public class Atom {
    private String symbol;
    private double x;
    private double y;
    private double z;

    public Atom(String symbol, double x, double y, double z) {
        this.symbol = symbol;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return  symbol +
                "   " + x +
                "   " + y +
                "  " + z ;
    }
}
