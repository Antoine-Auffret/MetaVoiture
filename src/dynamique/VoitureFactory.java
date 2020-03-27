package dynamique;

import javax.tools.*;

import voiture.Voiture;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class VoitureFactory {

    public enum ModeConstruction {
        INSTANCIATION,
        META,
        REFLEXION
    }

    public static JavaFileObject buildSource(String nomClasse) {

        int x = (int) (Math.random() * 1000);
        StringBuilder sb = new StringBuilder();

        sb.append("package dynamique;\n");
        sb.append("public class " + nomClasse + " extends Voiture{\n");
        genererAttributs(sb);
        genererConstructeurs(nomClasse, x, sb);
        genererMethodes(sb);
        sb.append("}\n");

        System.out.println("LA CLASSE");
        System.out.println(sb.toString());

        return new StringSource(nomClasse, sb.toString());
    }

    public static Voiture buildVoiture(ModeConstruction modeConstruction, boolean stop, int speed) {
        // ******** ETAPE #1 : Préparation pour la compilation
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<ByteArrayClass> classes = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();

        JavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // La classe qui se charge de fournir les "conteneurs" au compilateur
        fileManager = new ForwardingJavaFileManager<JavaFileManager>(fileManager){
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                       FileObject sibling) throws IOException {
                if (kind == JavaFileObject.Kind.CLASS){
                    ByteArrayClass outFile = new ByteArrayClass(className);
                    classes.add(outFile);
                    return outFile;
                }
                else
                    return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        };

        // ******** ETAPE #2 : Génération du code source
        List<JavaFileObject> sources = List.of(
                buildSource("Voiture1"),
                buildSource("Voiture2"));

        // ******** ETAPE #3 : Compilation
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, collector, null,
                null, sources);
        Boolean result = task.call();

        for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics())
            System.out.println(d);

        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!result) {
            System.out.println("ECHEC DE LA COMPILATION");
            System.exit(1);
        }

        // ******** ETAPE #4 : Instanciation
        ByteArrayClasseLoader loader = new ByteArrayClasseLoader(classes);
        List<Voiture> mesVoitures = new ArrayList<Voiture>();
        try {
            mesVoitures.add((Voiture) (Class.forName("dynamique.Voiture1", true, loader).getDeclaredConstructor().newInstance()));
            mesVoitures.add((Voiture) (Class.forName("dynamique.Voiture2", true, loader).getDeclaredConstructor().newInstance()));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // ******** ETAPE #5 : Exécution
        for (Voiture t : mesVoitures){
            System.out.println("CLASSE : " + t.getClass());
            System.out.println("VITESSE : " + t.getVitesse());
        }


        return new Voiture(speed);
    }

    private static void genererConstructeurs(String nomClasse, int x, StringBuilder sb) {

        sb.append("public " + nomClasse + "(){ y = 8; setX(" + x + ");}\n");
    }

    private static void genererMethodes(StringBuilder sb) {

        sb.append("public int getY(){return y;}\n");

    }

    private static void genererAttributs(StringBuilder sb) {

        sb.append("private int y;\n");
    }
}
