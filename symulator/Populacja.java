package symulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Klasa reprezentująca populację.
 *
 * @author Magdalena Augustyńska
 * @version 2018.0611
 */
public class Populacja {
    /**
     * Obiekt klasy Konfiguracja.
     * Zawiera parametry dla symulatora epidemii.
     */
    private Konfiguracja parametry;
    /**
     * Obiekt klasy List przechowujący agentów obecnych w populacji.
     */
    private List<Agent> agenci;

    /**
     * Tworzy nowy obiekt klasy Populacja.
     */
    public Populacja(Konfiguracja parametry) {
        this.parametry = parametry;
        agenci = new ArrayList<>();
    }

    /**
     * Liczy, ile jest aktualnie chorych osób w populacji.
     *
     * @return Liczba obiektów klasy Agent zawartych w atrybucie agenci, mających stan CHORY.
     */
    private int liczbaChorych() {
        int wynik = 0;
        for (Agent a : agenci) {
            if (a.getStan() == Stan.CHORY) wynik++;
        }
        return wynik;
    }

    /**
     * Liczy, ile jest aktualnie zdrowych osób w populacji.
     *
     * @return Liczba obiektów klasy Agent zawartych w atrybucie agenci, mających stan ZDROWY.
     */
    private int liczbaZdrowych() {
        int wynik = 0;
        for (Agent a : agenci) {
            if (a.getStan() == Stan.ZDROWY) wynik++;
        }
        return wynik;
    }

    /**
     * Symuluje początek dnia.
     * Na początku każdego dnia każdy zarażony agent może umrzeć (z prawd. śmiertelność) lub
     * wyzdrowieć (z prawd. prawdWyzdrowienia).
     * Agent, który umarł przestaje uczestniczyć w symulacji, a agent
     * który wyzdrowiał nabiera odporność i już nigdy nie zachoruje.
     */
    private void początekDnia() {
        double losowyDouble;
        boolean ktośUmarł = false;
        Iterator<Agent> iter = agenci.iterator();
        while (iter.hasNext()) {
            Agent a = iter.next();
            if (a.getStan() == Stan.CHORY) {
                losowyDouble = parametry.getMaszynaLosująca().nextDouble();
                if (losowyDouble <= parametry.getŚmiertelność()) {
                    a.śmierć();
                    iter.remove();
                    ktośUmarł = true;
                } else if (losowyDouble <= parametry.getŚmiertelność() + parametry
                        .getPrawdWyzdrowienia()) {
                    a.setStan(Stan.UODPORNIONY);
                }
            }
        }
        if (ktośUmarł) {
            for (Agent a : agenci) {
                if (a.getClass() == AgentTowarzyski.class) {
                    ((AgentTowarzyski) a).dodajZnajomychZnajomych();
                }
            }
        }
    }

    /**
     * Losuje populację.
     * W chwili obecnej rozważamy dwa rodzaje agentów: zwykłych i towarzyskich.
     * Rodzaj agenta określany jest przez losowanie przy użyciu parametru prawdTowarzyski.
     * Wszyscy poza jednym wylosowanym agentem zaczynają jako zdrowi (bez odporności), a jeden
     * zaczyna jako zarażony.
     *
     * @param konstruktory - zbiór konstruktorów, spośród których mogą być utworzeni nowi agenci.
     */
    private void losowanieAgentów(List<Constructor<? extends Agent>> konstruktory) throws
            IllegalAccessException, InvocationTargetException, InstantiationException {
        double losowyDouble;
        double suma = 0;
        for (int i = 1; i <= parametry.getLiczbaAgentów(); i++) {
            losowyDouble = parametry.getMaszynaLosująca().nextDouble();
            int opcja = -1;
            for (Double d : parametry.getPrawdopodobieństwa()) {
                suma += d;
                opcja++;
                if (losowyDouble <= suma) break;
            }
            agenci.add(konstruktory.get(opcja).newInstance(i, Stan.ZDROWY, parametry));
            suma = 0;
        }
        int agentZarażony = parametry.getMaszynaLosująca().nextInt(parametry.getLiczbaAgentów());
        agenci.get(agentZarażony).setStan(Stan.CHORY);
    }

    /**
     * Losuje graf, czyli połączenia między agentami w populacji.
     * Losowanie grafu społecznościowego odbywa się tak,
     * żeby średnia liczba znajomych była maksymalnie zbliżona do wartości śrZnajomych
     * (tzn. żadna inna liczba krawędzi nie dawała innego lepszej średniej).
     */
    private void losowanieGrafu() {
        int liczbaKrawędzi = parametry.getLiczbaAgentów() * parametry.getŚrZnajomych() / 2;
        int i = 0;
        Agent agent1, agent2;
        while (i < liczbaKrawędzi) {
            Collections.shuffle(agenci, parametry.getMaszynaLosująca());
            agent1 = agenci.get(1);
            agent2 = agenci.get(2);
            if (!agent1.maZnajomego(agent2)) {
                agent1.dodajZnajomego(agent2);
                agent2.dodajZnajomego(agent1);
                i++;
            }
        }
        for (Agent a : agenci) {
            if (a.getClass() == AgentTowarzyski.class) {
                ((AgentTowarzyski) a).dodajZnajomychZnajomych();
            }
        }
    }

    /**
     * Przeprowadza symulację epidemii.
     *
     * @param konstruktory - zbiór konstruktorów, spośród których mogą być utworzeni nowi agenci.
     * @return Obiekt klasy String zawierający informację o początkowej sieci społecznościowej
     * oraz stan populacji po każdym dniu.
     */
    public String symulacja(List<Constructor<? extends Agent>> konstruktory) throws
            IllegalAccessException, InstantiationException,
            InvocationTargetException {
        StringBuilder wynik = new StringBuilder();
        losowanieAgentów(konstruktory);
        losowanieGrafu();
        wynik.append(this.toString());
        wynik.append(System.lineSeparator());
        wynik.append("# liczność w kolejnych dniach");
        wynik.append(System.lineSeparator());
        for (int nrDnia = 1; nrDnia <= parametry.getLiczbaDni(); nrDnia++) {
            wynik.append(licznośćPopulacji());
            symulacjaDnia(nrDnia);
        }
        wynik.append(licznośćPopulacji());
        return wynik.toString();
    }

    /**
     * Przeprowadza symulację dnia.
     * Symulowany jest początek dnia. Każdy agent planuje spotkania.
     * Następnie dochodzi do zaplanowanych na dany dzień spotkań.
     *
     * @param nrDnia - numer dnia, dla którego przeprowadzana jest symulacja.
     */
    private void symulacjaDnia(int nrDnia) {
        początekDnia();
        for (Agent a : agenci) a.umówSpotkania(nrDnia);
        for (Agent a : agenci) a.przeprowadźSpotkania(nrDnia);
    }

    /**
     * Zwraca napis reprezentujący stan populacji na końcu dnia symulacji.
     * Napis zawiera liczbę osób kolejno zdrowych, chorych, uodpornionych oddzielonych spacją.
     *
     * @return Napis.
     */
    private String licznośćPopulacji() {
        return liczbaZdrowych() + " " + liczbaChorych() + " " + (agenci.size() - liczbaChorych()
                - liczbaZdrowych()) + System.lineSeparator();
    }

    /**
     * Zwraca napis zawierający informację o początkowej sieci społecznościowej.
     * Napis zawiera informacje o wszystkich agentach w populacji
     * oraz reprezentację grafu społecznościowego.
     *
     * @return Napis.
     */
    @Override
    public String toString() {
        Collections.sort(agenci, Comparator.comparing(Agent::getId));
        StringBuilder s = new StringBuilder();
        s.append("# agenci jako: id typ lub id* typ dla chorego");
        s.append(System.lineSeparator());
        for (Agent a : agenci) {
            s.append(a.toString());
        }
        s.append(System.lineSeparator());
        s.append("# graf");
        s.append(System.lineSeparator());
        for (Agent a : agenci) {
            s.append(a.drukujZnajomych());
        }
        return s.toString();
    }
}
