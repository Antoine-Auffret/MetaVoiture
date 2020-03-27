package voiture;

public class Voiture {
    private int vitesse = 10;
    private int position = 0;
    private int id;

    private static int _id = 0;
    public Voiture(int vitesse){
        this.vitesse = vitesse;
    }
    public void deplacement(){ position += vitesse; }
    public int getPosition(){
        return position;
    }
    public int getVitesse(){ return vitesse; }
    public int getId(){
        return id;
    }
    @Override
    public String toString(){
        return "";
    }
}
