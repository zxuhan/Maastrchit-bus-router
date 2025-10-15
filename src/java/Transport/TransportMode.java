package src.java.Transport;

// Abstract class for transportation modes
public abstract class TransportMode {
    protected double velocity;
    // In kilometers per hour

    public TransportMode(double velocity) {
        this.velocity = velocity;
    }

    // Getter for velocity
    public double getVelocity() {
        return this.velocity;
    }
}

