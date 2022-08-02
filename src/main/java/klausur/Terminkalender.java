package klausur;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * verwaltet eine Liste von Terminen
 * @author Doro
 *
 */
public class Terminkalender implements Serializable {
	/**
	 * Support für Observer-pattern
	 */
	private PropertyChangeSupport changeSupport;
	Lock lock;
	Condition c;
	/*
	Liste aller Termine
	 */
	private Set<Termin> terminliste; // Keine Ahnung wie man so'n kack Synchronized ding serialisiert....
	/**
	 * ObjectProperty des aktuellen Termins
	 */
	private transient ObjectProperty<Termin> aktuellerTermin = new ReadOnlyObjectWrapper<>();
	/**
	 * erstellt einen leeren Terminkalender
	 */
	public Terminkalender()
	{
		this.lock = new ReentrantLock();
		this.c = lock.newCondition();
		this.terminliste = Collections.synchronizedSet(new HashSet<>());
		this.aktuellerTermin.set(Termin.NICHTS_ZU_TUN);
		changeSupport = new PropertyChangeSupport(this);
		task();
	}

	/**
	 * liefert alle im Terminkalender verwalteten Termine
	 * @return
	 */
	public Set<Termin> getTerminliste()
	{
		return new HashSet<>(terminliste);
	}

	/**
	 * Fügt dem Terminkalender einen Listener hinzu
	 * @param l hinzuzufügender Listener
	 */
	public void subscribe(PropertyChangeListener l){
		changeSupport.addPropertyChangeListener(l);
	}

	/**
	 * Entfernt einen Listener
	 * @param l zu löschender Listener
	 */
	public void unsubscribe(PropertyChangeListener l){
		changeSupport.removePropertyChangeListener(l);
	}
	/**
	 *
	 * @return ObjectProperty<Termin> des aktuellen Termin
	 */
	public ObjectProperty<Termin> aktuellerTerminProperty(){
		return aktuellerTermin;
	}

	/**
	 * Gibt den aktuellen Termin wieder
	 * @return aktueller Termin
	 */
	public Termin getAktuellerTermin(){
		return aktuellerTermin.get();
	}

	/**
	 * Setzt den neuen, aktuellen Termin
	 * @param t zu setzender Termin
	 */
	private void setAktuellerTermin(Termin t){
		aktuellerTermin.set(t);
	}
	/**
	 * pr�ft, ob t ohne �berschneidung mit den bereits vorhandenen Terminen
	 * in den Terminkalender eingetragen werden kann.
	 * @param t der auf �berschneidungen zu �berpr�fende Termin
	 * @return true, falls es keine �berschneidung gibt
	 */
	public boolean terminErlaubt(Termin t)
	{

		return !getTerminliste().stream().anyMatch(t::esGibtUeberschneidung);
	}
	
	/**
	 * f�gt t zum Terminkalender hinzu, wenn dies �berschneidungsfrei m�glich ist
	 * @param t der neue einzutragende Termin
	 * @return true, wenn t �berschneidungsfrei hinzugef�gt werden konnte
	 */
	public boolean hinzufuegen(Termin t)
	{
		if (!terminErlaubt(t)){ return false;}
		terminliste.add(t);
		lock.lock();
		c.signalAll();
		lock.unlock();
		changeSupport.firePropertyChange("Termin", false, true);
		return true;
	}
	
	/**
	 * Methode zum Testen: liefert eine String-Darstellung aller Eigenschaften
	 * von this
	 * @return
	 */
	public String alleAusgeben()
	{
		String ausgabe = "Terminkalender: " + System.lineSeparator();
		Field felder[] = this.getClass().getDeclaredFields();
		for(Field f: felder)
		{
			try {
				ausgabe += "   " + f.getName() +": " + f.get(this) + System.lineSeparator();
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		return ausgabe;
	}
	public void speichern(String dateiname) throws Exception{
		try(FileOutputStream fos = new FileOutputStream(dateiname); ObjectOutputStream os = new ObjectOutputStream(fos)){
			os.writeObject(this);
		}
	}
	@Serial
	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		aktuellerTermin.set(Termin.NICHTS_ZU_TUN);
		task();
	}
	/**
	 * zum Testen - darf selbstverst�ndlich ver�ndert werden
	 * @param args wird nicht verwendet
	 * @throws Exception sollte nicht auftreten
	 */
	private void task(){
		Runnable task = () -> {
			if (terminliste.isEmpty()){
				lock.lock();
				try {
					c.await();
				} catch (InterruptedException ignored) {
				}
				lock.unlock();
			}
			Termin termin = getTerminliste().stream().min(Comparator.comparing(Termin::getBeginn)).get();
			if (!termin.getBeginn().isAfter(LocalDateTime.now())){
				setAktuellerTermin(termin);
				terminliste.remove(termin);
			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(task, 0, 60, TimeUnit.SECONDS);
	}
	public static void main(String[] args) throws Exception
	{
/*		Kategorie k = null;
		LocalDateTime start = LocalDateTime.now();
		Termin t1 = new Termin(start, k);
		Termin t2 = new Termin(start.plusMinutes(6), k);
		Termin t3 = new Termin(start.plusMinutes(1), k);
		
		System.out.println("-- Terminkalender neu erstellen --");
		Terminkalender kal = new Terminkalender();
		System.out.println(kal.alleAusgeben());
		
		System.out.println("-- Termin 1 hinzuf�gen --");
		System.out.println(kal.hinzufuegen(t1));
		System.out.println(kal.alleAusgeben());
		System.out.println(kal.getTerminliste());
		System.out.println("-- Termin 2 hinzuf�gen --");
		System.out.println(kal.hinzufuegen(t2));
		System.out.println(kal.alleAusgeben());
		System.out.println(kal.getTerminliste());
		
		System.out.println("-- Terminkollision --");
		System.out.println(kal.hinzufuegen(t3));
		System.out.println(kal.hinzufuegen(t3));
		System.out.println(kal.alleAusgeben());
		System.out.println(kal.getTerminliste());
		
		System.out.println("-- speichern --");
		kal.speichern("datei.txt");
		kal = null;
		
		System.out.println("-- Terminkalender wieder einlesen --");
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("datei.txt"));
		kal = (Terminkalender) in.readObject();
		in.close();
		System.out.println(kal.alleAusgeben());
*/
		
	}

}
