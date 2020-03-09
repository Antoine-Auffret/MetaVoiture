public class Voiture {
    private int vitesse;
    private int position;
    private int id;

    public Voiture(int vitesse){
        this.vitesse = vitesse;
    }

    public void deplacement(){

    }

    @Override
    public String toString(){
        return "";
    }

    public int getVitesse(){
        return this.vitesse;
    }

    public int getPosition(){
        return this.position;
    }

    public int getId(){
        return this.id;
    }
}
