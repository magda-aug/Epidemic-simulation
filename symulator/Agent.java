package symulator;

import java.util.*;

/**
 * Klasa abstrakcyjna reprezentująca agenta.
 *
 * @author Magdalena Augustyńska
 * @version 2018.0611
 */
public abstract class Agent {
    /**
     * Obiekt klasy Konfiguracja.
     * Zawiera parametry dla symulatora epidemii.
     */
    protected Konfiguracja parametry;
    /**
     * Identyfikator agenta.
     * Numer oznaczający, który z kolei został wylosowany dany agent.
     */
    protected int id;
    /**
     * Aktualny stan zdrowia agenta.
     * Obiekt klasy Stan.
     */
    protected Stan stan;
    /**
     * Obiekt zawierający aktualnych znajomych agenta.
     */
    protected List<Agent> znajomi;
    /**
     * Struktura przechowująca zaplanowane spotkania agenta.
     * Obiekt klasy HashMap, której kluczami są numery dni, a wartością dla danego klucza jest
     * obiekt klasy ArrayList zawierający obiekty klasy Agent, z którymi Agent ma umówione na
     * dany dzień spotkania.
     */
    protected HashMap<Integer, List<Agent>> spotkania;

    /**
     * Tworzy nowy obiekt klasy Agent.
     *
     * @param id   - identyfikator agenta;
     * @param stan - stan zdrowia agenta.
     */
    public Agent(int id, Stan stan, Konfiguracja parametry) {
        this.parametry = parametry;
        this.id = id;
        this.stan = stan;
        znajomi = new ArrayList<>();
        spotkania = new LinkedHashMap<>();
    }

    /**
     * Usuwa znajomego z atrybutu znajomi.
     *
     * @param a - agent do usunięcia.
     */
    public void usuńZnajomego(Agent a) {
        for (List<Agent> umówieniAgenci : spotkania.values()) umówieniAgenci.remove(a);
        znajomi.remove(a);
    }

    /**
     * Usuwa agenta.
     * Wysyła komunikat do wszystkich znajomych agenta o usunięciu tego agenta ze znajomych.
     */
    public void śmierć() {
        for (Agent a : znajomi) a.usuńZnajomego(this);
    }

    /**
     * Dodaje znajomego do listy znajomych.
     *
     * @param znajomy - znajomy do dodania.
     */
    public void dodajZnajomego(Agent znajomy) {
        znajomi.add(znajomy);
    }

    /**
     * Zwraca identyfikator.
     *
     * @return - wartość atrybutu id.
     */
    public int getId() {
        return id;
    }

    /**
     * Zwraca napis reprezentujący część grafu społecznościowego -
     * identyfikator agenta oraz listę identyfikatorów jego znajomych.
     *
     * @return Napis.
     */
    public String drukujZnajomych() {
        StringBuilder s = new StringBuilder();
        s.append(id);
        s.append(" ");
        for (Agent a : znajomi) {
            s.append(a.getId());
            s.append(" ");
        }
        s.deleteCharAt(s.length() - 1);
        s.append(System.lineSeparator());
        return s.toString();
    }

    /**
     * Sprawdza, czy agent posiada danego znajomego.
     *
     * @param a - agent.
     * @return Wartość true, jeśli agent posiada znajomego podanego jako argument.
     * Wartość false, w przeciwnym przypadku.
     */
    public boolean maZnajomego(Agent a) {
        return znajomi.contains(a);
    }

    /**
     * Zwraca obiekt przechowujący znajomych agenta.
     *
     * @return Atrybut znajomi.
     */
    public List<Agent> getZnajomi() {
        return znajomi;
    }

    /**
     * Zwraca aktualny stan zdrowia agenta.
     *
     * @return Stan agenta.
     */
    public Stan getStan() {
        return stan;
    }

    /**
     * Ustawia wartość atrybutu stan.
     *
     * @param stan - nowy stan agenta.
     */
    public void setStan(Stan stan) {
        this.stan = stan;
    }

    /**
     * Symuluje spotkania.
     * Jeżeli któryś ze spotykających się agentów jest zarażony a drugi nie ma odporności,
     * to z prawd. prawdZarażenia może dojść do zarażenia, wpp. takie spotkanie nie ma żadnego
     * efektu.
     *
     * @param nrDnia - numer dnia, w którym przeprowadzane są spotkania;
     */
    public void przeprowadźSpotkania(int nrDnia) {
        if (!spotkania.containsKey(nrDnia)) return;
        double losowyDouble;
        for (Agent a : spotkania.get(nrDnia)) {
            if (a.getStan() == Stan.CHORY && this.stan == Stan.ZDROWY) {
                losowyDouble = parametry.getMaszynaLosująca().nextDouble();
                if (losowyDouble <= parametry.getPrawdZarażenia()) this.setStan(Stan.CHORY);
            } else if (a.getStan() == Stan.ZDROWY && this.stan == Stan.CHORY) {
                losowyDouble = parametry.getMaszynaLosująca().nextDouble();
                if (losowyDouble <= parametry.getPrawdZarażenia()) a.setStan(Stan.CHORY);
            }
        }
    }

    public abstract void umówSpotkania(int nrDnia);

    /**
     * Planuje spotkania agenta.
     * Funkcja pomocnicza dla funkcji abstrakcyjnej umówSpotkania. Umawia spotkania dla zadanej
     * puli znajomych danego agenta.
     * Agent z prawd. prawdSpotkania decyduje czy chce się spotkać i jeżeli tak to losuje jednego
     * ze swoich znajomych
     * (można planować spotkania i spotykać się z tym samym agentem wiele razy danego dnia).
     * Następnie agent losuje jeden z pozostałych dni symulacji kiedy do takiego spotkania dojdzie.
     * Agent powtarza planowanie spotkań dopóki nie wylosuje, że nie chce się spotykać.
     *
     * @param nrDnia         - numer dnia, w którym przeprowadzane jest umawianie spotkań;
     * @param pulaZnajomych  - zbiór znajomych, z którymi agent może się spotykać;
     * @param prawdSpotkania - prawdopodobieństwo, z jakim agent decyduje czy chce się spotkać.
     */
    public void umówSpotkania(int nrDnia, List<Agent> pulaZnajomych, double prawdSpotkania) {
        if (pulaZnajomych.isEmpty() || parametry.getLiczbaDni() == nrDnia) return;
        double losowyDouble = parametry.getMaszynaLosująca().nextDouble();
        Agent znajomy;
        int losowyZnajomy, losowyDzień;
        while (losowyDouble <= prawdSpotkania) {
            losowyZnajomy = parametry.getMaszynaLosująca().nextInt(pulaZnajomych.size());
            znajomy = pulaZnajomych.get(losowyZnajomy);
            losowyDzień = parametry.getMaszynaLosująca().nextInt(parametry.getLiczbaDni() - nrDnia);
            if (spotkania.containsKey(nrDnia + 1 + losowyDzień))
                spotkania.get(nrDnia + 1 + losowyDzień).add(znajomy);
            else spotkania.put(nrDnia + 1 + losowyDzień, new ArrayList<>(Arrays.asList(znajomy)));
            losowyDouble = parametry.getMaszynaLosująca().nextDouble();
        }
    }
}
