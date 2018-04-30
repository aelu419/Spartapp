package hackthis.everythingthis;

/**
 * Created by HP on 2018/4/30.
 */

public class Subject{

    protected final String name;
    protected final String teacher;
    protected final String room;

    public Subject( String name, String teacher, String room){
        this.name = name;
        this.teacher = teacher;
        this.room = room;
    }

    public String name(){return name;}
    public String teacher(){return teacher;}
    public String room(){return room;}

    public boolean equals(Object obj){
        Subject temp = (Subject)obj;
        if(temp.name().equals(this.name)){
            return true;
        }
        return false;
    }

    public String toString(){return name;}
}
