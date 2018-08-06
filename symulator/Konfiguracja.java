package symulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Klasa przechowująca parametry dla programu.
 *
 * @author Magdalena Augustyńska
 * @version 2018.0611
 */
public class Konfiguracja {
    /**
     * Obiekt klasy Random przeprowadzający losowanie dla symulacji.
     */
    private Random maszynaLosująca;
    /**
     * Seed dla generatora liczb pseudolosowych.
     */
    private long seed;
    /**
     * Liczba agentów w populacji na początku symulacji.
     */
    private int liczbaAgentów;
    /**
     * Prawdopodobieństwo, z jakim w populacji wystąpi Agent Towarzyski.
     */
    private double prawdTowarzyski;
    /**
     * Prawdopodobieństwo, z jakim agent decyduje, czy chce się zaplanować spotkanie.
     */
    private double prawdSpotkania;
    /**
     * Prawdopodobieństwo, z jakim może dojść do zarażenia podczas spotkania agenta chorego i
     * zdrowego.
     */
    private double prawdZarażenia;
    /**
     * Prawdopodobieństwo, z jakim na początku każdego dnia każdy zarażony agent może wyzdrowieć.
     */
    private double prawdWyzdrowienia;
    /**
     * Prawdopodobieństwo, z jakim na początku każdego dnia każdy zarażony agent może umrzeć.
     */
    private double śmiertelność;
    /**
     * Liczba dni, dla których ma być przeprowadzona symulacja.
     */
    private int liczbaDni;
    /**
     * Średnia liczba znajomych agenta w populacji.
     */
    private int śrZnajomych;
    /**
     * Nazwa raportu z wynikiem symulacji, który ma powstać po zakończeniu programu.
     * Jeżeli taki plik istnieje to zostanie nadpisany.
     */
    private String plikZRaportem;
    /**
     * Ścieżki do plików zawierających parametry symulacji.
     */
    private String[] pliki;
    /**
     * Mapa przechowująca pary klucz-atrubut dla symulatora z pliku XML.
     */
    private Map<String, String> atrybutyXML;
    /**
     * Mapa przechowująca pary klucz-atrubut dla symulatora z pliku tekstowego.
     */
    private Map<String, String> atrybutyProp;
    /**
     * Lista wartości prawdopodobieństw wystąpienia poszczególnych rodzajów Agentów.
     */
    private List<Double> prawdopodobieństwa;

    /**
     * Tworzy nowy obiekt klasy Konfiguracja.
     *
     * @param pliki - tablica ścieżek do plików z parametrami.
     */
    public Konfiguracja(String[] pliki) {
        atrybutyXML = new HashMap<>();
        atrybutyProp = new HashMap<>();
        this.pliki = pliki;
    }

    /**
     * Kończy program.
     * Wypisuje na wyjście standardowe komunikat o błędzie
     * oraz kończy działanie programu zwracając błąd.
     *
     * @param komunikat - napis opisujący rodzaj błędu.
     */
    private static void zakończ(String komunikat) {
        System.out.println(komunikat);
        System.exit(1);
    }

    /**
     * Czyta parametry dla programu.
     * Sprawdza obecność plików, sprawdza obecność parametrów w plikach oraz inicjalizuje
     * poprawne parametry.
     * W przypadku błędnych danych kończy działania programu.
     */
    public void czytajParametry() {
        if (pliki.length == 0) zakończ("Brak pliku default.properties");
        if (pliki.length == 1) {
            if (pliki[0].equals("default.properties")) zakończ("Brak pliku simulation-conf.xml");
            else zakończ("Brak pliku default.properties");
        }

        final Properties prop = new Properties();
        try {
            prop.load(Channels.newReader((new FileInputStream(pliki[0])).getChannel(),
                    StandardCharsets.UTF_8.name()));
        } catch (FileNotFoundException e) {
            zakończ("Brak pliku default.properties");
        } catch (IOException e) {
            zakończ("default.properties nie jest tekstowy");
        }

        final Properties xml = new Properties();
        try {
            xml.loadFromXML(new FileInputStream(pliki[1]));
        } catch (FileNotFoundException e) {
            zakończ("Brak pliku simulation-conf.xml");
        } catch (IOException e) {
            zakończ("simulation-conf.xml nie jest XML");
        }

        Set<String> propKlucze = prop.stringPropertyNames();
        Set<String> xmlKlucze = xml.stringPropertyNames();

        final List<String> parametry = Arrays.asList("seed", "liczbaAgentów", "prawdTowarzyski",
                "prawdSpotkania", "prawdZarażenia", "prawdWyzdrowienia", "śmiertelność",
                "liczbaDni", "śrZnajomych", "plikZRaportem");

        for (String parametr : parametry) {
            if (!propKlucze.contains(parametr) && !xmlKlucze.contains(parametr))
                zakończ("Brak wartości dla klucza " + parametr);
        }

        for (String parametr : parametry) {
            if (xmlKlucze.contains(parametr)) atrybutyXML.put(parametr, xml.getProperty(parametr));
        }
        for (String parametr : parametry) {
            if (propKlucze.contains(parametr))
                atrybutyProp.put(parametr, prop.getProperty(parametr));
        }

        ustawParametry(atrybutyProp);
        ustawParametry(atrybutyXML);

        prawdopodobieństwa = new ArrayList<>(Arrays.asList(prawdTowarzyski, 1 - prawdTowarzyski));
        maszynaLosująca = new Random(seed);
    }

    /**
     * Inicjalizuje parametry dla programu.
     *
     * @param atrybuty - struktura przechowująca klucze i parametry.
     */
    private void ustawParametry(Map<String, String> atrybuty) {
        if (atrybuty.containsKey("seed")) {
            try {
                seed = Long.parseLong(atrybuty.get("seed"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("seed") + " dla klucza seed");
            }
        }

        if (atrybuty.containsKey("liczbaAgentów")) {
            try {
                liczbaAgentów = Integer.parseInt(atrybuty.get("liczbaAgentów"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("liczbaAgentów") + " dla klucza " +
                        "liczbaAgentów");
            }
            czyIntegerWZakresie(1, 1000000, liczbaAgentów, "liczbaAgentów", atrybuty);
        }

        if (atrybuty.containsKey("prawdTowarzyski")) {
            try {
                prawdTowarzyski = Double.parseDouble(atrybuty.get("prawdTowarzyski"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("prawdTowarzyski") + " dla klucza " +
                        "prawdTowarzyski");
            }
            czyPrawdopodobieństwoWZakresie(prawdTowarzyski, "prawdTowarzyski", atrybuty);
        }

        if (atrybuty.containsKey("prawdSpotkania")) {
            try {
                prawdSpotkania = Double.parseDouble(atrybuty.get("prawdSpotkania"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("prawdSpotkania") + " dla klucza " +
                        "prawdSpotkania");
            }
            czyPrawdopodobieństwoWZakresie(prawdSpotkania, "prawdSpotkania", atrybuty);
        }

        if (atrybuty.containsKey("prawdZarażenia")) {
            try {
                prawdZarażenia = Double.parseDouble(atrybuty.get("prawdZarażenia"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("prawdZarażenia") + " dla klucza " +
                        "prawdZarażenia");
            }
            czyPrawdopodobieństwoWZakresie(prawdZarażenia, "prawdZarażenia", atrybuty);
        }

        if (atrybuty.containsKey("prawdWyzdrowienia")) {
            try {
                prawdWyzdrowienia = Double.parseDouble(atrybuty.get("prawdWyzdrowienia"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("prawdWyzdrowienia") + " dla " +
                        "klucza prawdWyzdrowienia");
            }
            czyPrawdopodobieństwoWZakresie(prawdWyzdrowienia, "prawdWyzdrowienia", atrybuty);
        }

        if (atrybuty.containsKey("śmiertelność")) {
            try {
                śmiertelność = Double.parseDouble(atrybuty.get("śmiertelność"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("śmiertelność") + " dla klucza " +
                        "śmiertelność");
            }
            czyPrawdopodobieństwoWZakresie(śmiertelność, "śmiertelność", atrybuty);
        }

        if (atrybuty.containsKey("liczbaDni")) {
            try {
                liczbaDni = Integer.parseInt(atrybuty.get("liczbaDni"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("liczbaDni") + " dla klucza " +
                        "liczbaDni");
            }
            czyIntegerWZakresie(1, 1000, liczbaDni, "liczbaDni", atrybuty);
        }

        if (atrybuty.containsKey("śrZnajomych")) {
            try {
                śrZnajomych = Integer.parseInt(atrybuty.get("śrZnajomych"));
            } catch (NumberFormatException e) {
                zakończ("Niedozwolona wartość " + atrybuty.get("śrZnajomych") + " dla klucza " +
                        "śrZnajomych");
            }
            czyIntegerWZakresie(0, liczbaAgentów-1, śrZnajomych, "śrZnajomych", atrybuty);
        }

        if (atrybuty.containsKey("plikZRaportem")) plikZRaportem = atrybuty.get("plikZRaportem");
    }

    /**
     * Sprawdza poprawność parametru symulacji.
     * Sprawdza czy wartość zmiennej mieści się w zadanym zakresie.
     * Jeśli nie, kończy działanie programu.
     *
     * @param minimum - dolny kres parametru;
     * @param maximum - górny kres parametru;
     * @param wartość - wartość parametru;
     * @param klucz   - klucz parametru.
     */
    private void czyIntegerWZakresie(int minimum, int maximum, int wartość, String klucz,
                                     Map<String, String> atrybuty) {
        if (!(wartość <= maximum && wartość >= minimum))
            zakończ("Niedozwolona wartość " + atrybuty.get(klucz) + " dla klucza " + klucz);
    }

    /**
     * Sprawdza poprawność parametru symulacji.
     * Sprawdza czy wartość zmiennej mieści się w zakresie, w jakim mogą być wartości
     * prawdopodobieństwa.
     * Jeśli nie, kończy działanie programu.
     *
     * @param wartość - wartość parametru;
     * @param klucz   - klucz parametru.
     */
    private void czyPrawdopodobieństwoWZakresie(double wartość, String klucz, Map<String, String>
            atrybuty) {
        if (!(wartość <= 1.0 && wartość >= 0.0))
            zakończ("Niedozwolona wartość " + atrybuty.get(klucz) + " dla klucza " + klucz);
    }

    public String getPlikZRaportem() {
        return plikZRaportem;
    }

    public int getLiczbaAgentów() {
        return liczbaAgentów;
    }

    public int getLiczbaDni() {
        return liczbaDni;
    }

    public int getŚrZnajomych() {
        return śrZnajomych;
    }

    public double getPrawdSpotkania() {
        return prawdSpotkania;
    }

    public double getPrawdZarażenia() {
        return prawdZarażenia;
    }

    public double getPrawdWyzdrowienia() {
        return prawdWyzdrowienia;
    }

    public double getŚmiertelność() {
        return śmiertelność;
    }

    public Random getMaszynaLosująca() {
        return maszynaLosująca;
    }

    public List<Double> getPrawdopodobieństwa() {
        return prawdopodobieństwa;
    }

    /**
     * Zwraca tekstową reprezentację parametrów symulacji.
     *
     * @return Napis zawierający nazwy i wartości wszystkich parametrów symulacji.
     */
    @Override
    public String toString() {
        atrybutyProp.putAll(atrybutyXML);
        StringBuilder s = new StringBuilder();
        s.append("# twoje wyniki powinny zawierać te komentarze");
        s.append(System.lineSeparator());
        for (String klucz : atrybutyProp.keySet()) {
            s.append(klucz);
            s.append("=");
            s.append(atrybutyProp.get(klucz));
            s.append(System.lineSeparator());
        }
        return s.toString();
    }
}


