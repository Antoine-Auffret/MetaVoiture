package voiture;

public class Voiture {
    private int vitesse = 10;
    private int position = 0;
    private int id;

    private static int _id = 0;
    public Voiture(int vitesse){
        this.id = _id++;
        this.vitesse = vitesse;
    }
    public void deplacement(){ position += vitesse; }
    public int getPosition(){
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getVitesse(){ return vitesse; }
    public int getId() {
        return id;
    }
    public int surveiller(int limite) {
        return vitesse-limite;
    }
    @Override
    public String toString(){
        String type = "";

        if(id%3 ==0){type="sport";}

        String method = "";

        if(id<3){
            method = "Instanciation";
        }
        else if(id<6){
            method = "Reflexion";
        }
        else{
            method = "Meta";
        }

        return "(" + method + ") Voiture " + type +" id : " + id + ", position : " + position + ", vitesse : " + vitesse;
    }
}
