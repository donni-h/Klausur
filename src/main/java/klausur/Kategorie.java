package klausur;

import javafx.scene.paint.Color;

import java.io.Serializable;

/**
 * Stellt Kategorien für einen Terminkalender dar, jedem Termin ist eine Farbe zugewiesen
 */
public enum Kategorie implements Serializable {
    PRIVAT(Color.RED),
    ARBEIT(Color.BLUE),
    LEHRE(Color.PURPLE);
    private Color color;

    private Kategorie(Color c){
        this.color = c;
    }
    public Color getColor(){
        return this.color;
    }
    public Color getInverseFarbe(){
        return getColor().invert();
    }
}
