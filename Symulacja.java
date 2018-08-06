import symulator.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Klasa reprezentująca symulator epidemii.
 *
 * @author Magdalena Augustyńska
 * @version 2018.0611
 */
public class Symulacja {

    /**
     * Czyta dane i przeprowadza symulację.
     *
     * @param args
     */
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StringBuilder wynikSymulacji = new StringBuilder();

        Konfiguracja k = new Konfiguracja(args);
        k.czytajParametry();
        wynikSymulacji.append(k.toString());
        wynikSymulacji.append(System.lineSeparator());

        Populacja p = new Populacja(k);


        wynikSymulacji.append(p.symulacja(new ArrayList<>(Arrays.asList(AgentTowarzyski.class
                .getConstructor(int.class, Stan.class, Konfiguracja.class), AgentZwykly.class
                .getConstructor(int.class, Stan.class, Konfiguracja.class)))));

        wynikSymulacji.setLength(wynikSymulacji.length() - 1);
        PrintWriter writer;
        try {
            writer = new PrintWriter(k.getPlikZRaportem(), "UTF-8");
            writer.print(wynikSymulacji);
            writer.close();
        } catch (Exception e) {
            System.exit(1);
        }
    }
}