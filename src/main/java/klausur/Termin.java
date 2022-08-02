package klausur;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * ein Termin
 * @author Doro
 *
 */
public class Termin implements Serializable {
	/**
	 * Ein Termin, um nichts zu tun
	 */
	public static final Termin NICHTS_ZU_TUN = new Termin(LocalDateTime.now(),Kategorie.PRIVAT);

	private LocalDateTime beginn, ende;
	private Kategorie kategorie;

	/**
	 * Beginn des Termins
	 * @return
	 */
	public LocalDateTime getBeginn() {
		return beginn;
	}

	/**
	 * Ende des Termins
	 * @return
	 */
	public LocalDateTime getEnde() {
		return ende;
	}
	
	/**
	 * @return Die Kategorie des Termins
	 */
	public Kategorie getKategorie() {
		return kategorie;
	}

	/**
	 * erstellt einen Termin von beginn an mit zuf�lliger Dauer
	 * @param beginn Startzeitpunkt des Termins
	 * @param kategorie Die Kategorie des Termins
	 * @throws IllegalArgumentException, wenn beginn oder kategorie null ist 
	 */
	public Termin(LocalDateTime beginn, Kategorie kategorie)
	{
		if(beginn == null || kategorie == null)
			throw new IllegalArgumentException();
		
		Random r = new Random();
		int anzahl = r.nextInt(4) +1;
		this.ende = beginn.plusMinutes(anzahl);
		this.beginn = beginn;
		this.kategorie = kategorie;
	}
	
	/**
	 * pr�ft, ob es eine �berschneidung von this und anderer gibt
	 * @param anderer zu pr�fender Termin
	 * @return true, wenn es eine zeitliche �berschneidung von this und anderer gibt
	 */
	public boolean esGibtUeberschneidung(Termin anderer)
	{
		return this.getBeginn().isBefore(anderer.getEnde())
				&& this.getEnde().isAfter(anderer.getBeginn());
	}
	
	@Override
	public String toString()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.L H:m");
		String von = beginn.format(formatter);
		String bis = ende.format(formatter);
		return von + " - " + bis;
	}
	
	/**
	 * Methode zum Testen: liefert eine String-Darstellung aller Eigenschaften
	 * von this
	 * @return
	 */
	public String alleAusgeben()
	{
		String ausgabe = "Termin: " + System.lineSeparator();
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
	
	/**
	 * zum Testen - darf selbstverst�ndlich ver�ndert werden
	 * @param args wird nicht verwendet
	 * @throws Exception sollte nicht auftreten
	 */
	public static void main(String[] args) throws Exception
	{
/*		Kategorie k = null;
		LocalDateTime start = LocalDateTime.now();
		Termin t = new Termin(start, k);
		System.out.println(t.alleAusgeben());
*/
	}
}
