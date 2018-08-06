package symulator;

/**
 * Klasa reprezentująca agenta zwykłego.
 * Podklasa klasy Agent.
 *
 * @author Magdalena Augustyńska
 * @version 2018.0611
 */
public class AgentZwykly extends Agent {

    /**
     * Tworzy nowy obiekt klasy AgentZwykly.
     *
     * @param id        - identyfikator agenta;
     * @param stan      - stan zdrowia agenta;
     * @param parametry - obiekt zawierający parametry dla symulatora epidemii.
     */
    public AgentZwykly(int id, Stan stan, Konfiguracja parametry) {
        super(id, stan, parametry);
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
        return (stan == Stan.CHORY) ? id + "* zwykły" + System.lineSeparator() : id + " zwykły" +
                System.lineSeparator();
    }

    /**
     * Planuje spotkania agenta.
     * Jeżeli agent jest zarażony, to dopóki nie wyzdrowieje będzie planował nowe spotkania z dwa
     * razy mniejszym prawdopodobieństwem.
     *
     * @param nrDnia - numer dnia, w którym przeprowadzane jest umawianie spotkań.
     */
    public void umówSpotkania(int nrDnia) {
        double pr = parametry.getPrawdSpotkania();
        if (stan == Stan.CHORY) pr /= 2;
        super.umówSpotkania(nrDnia, znajomi, pr);
    }
}
