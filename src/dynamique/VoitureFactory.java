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

    public static JavaFileObject buildSource(String nomClasse, boolean voitureSport, int vitesse) {

        StringBuilder sb = new StringBuilder();

        sb.append("package voiture;\n");
        if (voitureSport == true) {
            sb.append("public class " + nomClasse + " extends VoitureSport implements Surveillable {\n");
        }
        else {
            sb.append("public class " + nomClasse + " extends Voiture implements Surveillable {\n");
        }
        genererConstructeurs(nomClasse, voitureSport, vitesse, sb);
        sb.append("}");

        System.out.println("LA CLASSE");
        System.out.println(sb.toString());

        return new StringSource(nomClasse, sb.toString());
    }

    public static Voiture buildVoiture(ModeConstruction modeConstruction, boolean voitureSport, int vitesse) {

        Voiture voiture = null;

        switch(modeConstruction)
        {
            case INSTANCIATION:
                if(voitureSport){
                    voiture = new VoitureSport();
                }
                else{
                    voiture = new Voiture(vitesse);
                }

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
                List<JavaFileObject> source;
                if (voitureSport == true) {
                    source = List.of(buildSource("MetaVoitureSport", voitureSport, vitesse));
                }
                else {
                    source = List.of(buildSource("MetaVoiture", voitureSport, vitesse));
                }

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
                    if (voitureSport == true) {
                        voiture = (Voiture) (Class.forName("voiture.MetaVoitureSport", true, loader).getDeclaredConstructor().newInstance());
                    }
                    else {
                        voiture = (Voiture) (Class.forName("voiture.MetaVoiture", true, loader).getDeclaredConstructor(int.class).newInstance(vitesse));
                    }
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
                System.out.println("\nCLASSE : " + voiture.getClass());
                System.out.println("VITESSE : " + voiture.getVitesse());
                System.out.println("POSITION : " + voiture.getPosition());

                break;

            case REFLEXION:

                try {
                    if(voitureSport){
                        voiture = (Voiture)  Class.forName("voiture.VoitureSport").getDeclaredConstructor().newInstance();
                    }
                    else{
                        voiture = (Voiture)  Class.forName("voiture.Voiture").getDeclaredConstructor(int.class).newInstance(vitesse);
                    }


                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
                e) {
                    e.printStackTrace();
                }

                break;
        }

        return voiture;
    }

    private static void genererConstructeurs(String nomClasse, boolean voitureSport, int vitesse, StringBuilder sb) {

        if (voitureSport == true) {
            sb.append("public " + nomClasse + "(){ super(); }\n");
        }
        else {
            sb.append("public " + nomClasse + "(int vitesse){ super(" + vitesse + "); }\n");
        }
    }
}
