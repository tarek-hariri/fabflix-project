import java.util.ArrayList;

public class Actor {

    private String name;
    private int birthYear;


    public Actor(String name, int birthYear) {
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public String toString() {

        return  "Name: " + getName() + ", " +
                "Birth Year:" + getBirthYear();
    }
}