package symulator;

import java.util.*;

/**
 * Klasa reprezentująca agenta towarzyskiego.
 * Podklasa klasy Agent.
 *
 * @author Magdalena Augustyńska
 * @version 2018.0611
 */
public class AgentTowarzyski extends Agent {

    /**
     * Obiekt przechowujący znajomych agenta wraz z ich znajomymi.
     */
    private List<Agent> znajomiZnajomych;

    /**
     * Tworzy nowy obiekt klasy AgentTowarzyski.
     *
     * @param id        - identyfikator agenta;
     * @param stan      - stan zdrowia agenta;
     * @param parametry - obiekt zawierający parametry dla symulatora epidemii.
     */
    public AgentTowarzyski(int id, Stan stan, Konfiguracja parametry) {
        super(id, stan, parametry);
        znajomiZnajomych = new ArrayList<>();
    }

    @Override
    public Stan getStan() {
        return super.getStan();
    }

    /**
     * Zwraca napis reprezentujący agenta - jego identyfikator, stan zdrowia oraz rodzaj.
     *
     * @return Napis.
     */
    @Override
    public String toString() {
        return (stan == Stan.CHORY) ? id + "* towarzyski" + System.lineSeparator() : id + " " +
                "towarzyski" + System.lineSeparator();
    }

    @Override
    public void usuńZnajomego(Agent a) {
        super.usuńZnajomego(a);
        znajomiZnajomych.remove(a);
    }

    /**
     * Inicjalizuje zawartość atrybutu przechowującego znajomych znajomych danego agenta.
     */
    public void dodajZnajomychZnajomych() {
        znajomiZnajomych.clear();
        HashSet<Agent> pomocniczy = new LinkedHashSet<>();
        pomocniczy.addAll(znajomi);
        for (Agent znajomy : znajomi) {
            pomocniczy.addAll(znajomy.getZnajomi());
        }
        znajomiZnajomych.addAll(pomocniczy);
    }

    /**
     * Planuje spotkania agenta.
     * Jeżeli agent jest chory to dopóki nie wyzdrowieje będzie planował się spotykać tylko ze
     * swoimi bezpośrednimi znajomymi
     * (nie ma to wpływu na to czy inni będą się decydować spotykać z nim i na spotkania, które
     * już zaplanowano).
     *
     * @param nrDnia - numer dnia, w którym przeprowadzane jest umawianie spotkań.
     */
    public void umówSpotkania(int nrDnia) {
        if (stan == Stan.CHORY) super.umówSpotkania(nrDnia, znajomi, parametry.getPrawdSpotkania());
        else super.umówSpotkania(nrDnia, znajomiZnajomych, parametry.getPrawdSpotkania());
    }
}
