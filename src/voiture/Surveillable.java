package voiture;

public interface Surveillable {
    public default int surveiller(int limite) {
        return limite;
    }
}
