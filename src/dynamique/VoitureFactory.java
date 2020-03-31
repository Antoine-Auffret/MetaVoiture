package dynamique;

import javax.tools.*;

import voiture.Voiture;
import voiture.VoitureSport;

import java.lang.*;
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

        //int x = (int) (Math.random() * 1000);
        StringBuilder sb = new StringBuilder();

        sb.append("package dynamique;\n");
        sb.append("import voiture.VoitureSport;\n");
        sb.append("public class " + nomClasse + " extends VoitureSport {\n");
        genererAttributs(sb);
        genererConstructeurs(nomClasse, 0, sb);
        genererMethodes(sb);
        sb.append("}\n");

        System.out.println("LA CLASSE");
        System.out.println(sb.toString());

        return new StringSource(nomClasse, sb.toString());
    }

    public static Voiture buildVoiture(ModeConstruction modeConstruction, boolean stop, int speed) {

        Voiture voiture = null;

        switch(modeConstruction)
        {
            case INSTANCIATION:
                voiture = new Voiture(speed);
                break;
            case META:
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
                List<JavaFileObject> source = List.of(buildSource("MetaVoitureSport"));

                // ******** ETAPE #3 : Compilation
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, collector, null,
                        null, source);
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
                try {
                    voiture = (Voiture) (Class.forName("dynamique.MetaVoitureSport", true, loader).getDeclaredConstructor(new Class[] {int.class}).newInstance(new Object[]{10}));
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
                System.out.println("CLASSE : " + voiture.getClass());
                System.out.println("VITESSE : " + voiture.getVitesse());
                System.out.println("POSITION : " + voiture.getPosition());
                System.out.println("return Meta : " + voiture.getClass().getName());

                break;

            case REFLEXION:

                Class<?> maClasse = null;

                try {

                    maClasse = Class.forName("voiture.Voiture");

                    Object o = maClasse.getDeclaredConstructor(int.class).newInstance(speed);

                    voiture = Voiture.class.cast(o);

                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
                e) {
                    e.printStackTrace();
                }

                break;
        }

        return voiture;
    }

    private static void genererConstructeurs(String nomClasse, int x, StringBuilder sb) {

        sb.append("public " + nomClasse + "(int vitesse){ super(" + x + ");}\n");
    }

    private static void genererMethodes(StringBuilder sb) {

        sb.append("public int getPosition(){return position;}\n");

    }

    private static void genererAttributs(StringBuilder sb) {

        sb.append("private int position;\n");
    }
}
