package logitech.rastreamento;

/** Posicao geografica valida e imutavel. */
public final class Posicao {
    private final double latitude;
    private final double longitude;

    public Posicao(double latitude, double longitude) {
        if (!valida(latitude, longitude)) {
            throw new IllegalArgumentException("posicao invalida");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static boolean valida(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posicao)) return false;
        Posicao p = (Posicao) o;
        return Double.compare(latitude, p.latitude) == 0 && Double.compare(longitude, p.longitude) == 0;
    }

    @Override public int hashCode() {
        return 31 * Double.hashCode(latitude) + Double.hashCode(longitude);
    }

    @Override public String toString() { return latitude + "," + longitude; }
}
